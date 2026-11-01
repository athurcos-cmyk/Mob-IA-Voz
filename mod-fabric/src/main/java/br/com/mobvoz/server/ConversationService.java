package br.com.mobvoz.server;

import br.com.mobvoz.MobVozMod;
import br.com.mobvoz.ai.SidecarClient;
import br.com.mobvoz.config.Metrics;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.knowledge.KnowledgeStore;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.memory.Traits;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.net.SubtitlePayload;
import br.com.mobvoz.net.UtterancePayload;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.trade.TradeCatalog;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.trade.Trades;
import br.com.mobvoz.trade.Values;
import br.com.mobvoz.target.TargetPicker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * Coordenador de conversa (fase 4). Autoritativo: valida alvo/mira/limites, trava UMA sessao por mob e por jogador,
 * pede a resposta ao sidecar de forma assincrona (nunca no tick) e fala pelo {@link Speaker}.
 * Fase 4 ainda NAO executa acoes: capabilities e fatos vao vazios, entao nada altera o mundo.
 */
public final class ConversationService {
    /** Tolerancia do servidor sobre os 6 blocos do cliente (latencia/arredondamento). */
    static final double MAX_RANGE = 8.0;
    static final int COOLDOWN_TICKS = 20;
    static final long PIPELINE_TIMEOUT_S = 90;

    private static SidecarClient sidecar = SidecarClient.local();
    private static volatile Speaker speaker;

    private static final Map<UUID, UUID> mobToPlayer = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> playerToMob = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastTick = new ConcurrentHashMap<>();

    private ConversationService() {
    }

    public static void setSpeaker(Speaker s) {
        speaker = s;
    }

    private static final java.util.List<String> ANNOUNCED = new java.util.concurrent.CopyOnWriteArrayList<>();

    /** Ultimas falas fixas do servidor (trocas, cenas). */
    public static java.util.List<String> announced() {
        return java.util.List.copyOf(ANNOUNCED);
    }

    /** Para testes. */
    public static void setSidecar(SidecarClient c) {
        sidecar = c;
    }

    // ---- memoria CURTA da conversa (ultimas trocas por criatura+jogador): a criatura continua o assunto em vez de responder do zero ----
    private static final int HISTORY_TURNS = 6;
    private static final long HISTORY_TTL_MS = 15 * 60_000;

    private record Turn(String jogador, String voce, long at) {
    }

    private static final Map<String, java.util.concurrent.ConcurrentLinkedDeque<Turn>> HISTORY = new ConcurrentHashMap<>();

    private static List<Map<String, String>> historyFor(UUID mob, UUID player) {
        var d = HISTORY.get(mob + ":" + player);
        long now = System.currentTimeMillis();
        List<Map<String, String>> out = new ArrayList<>();
        if (d != null) {
            d.removeIf(t -> now - t.at() > HISTORY_TTL_MS);
            d.forEach(t -> out.add(Map.of("jogador", t.jogador(), "voce", t.voce())));
        }
        return out;
    }

    private static void remember(UUID mob, UUID player, String said, String reply) {
        var d = HISTORY.computeIfAbsent(mob + ":" + player, k -> new java.util.concurrent.ConcurrentLinkedDeque<>());
        d.addLast(new Turn(said, reply, System.currentTimeMillis()));
        while (d.size() > HISTORY_TURNS) {
            d.pollFirst();
        }
    }

    /** Fatos de lore sobre outras criaturas citadas na fala (para opinar sem inventar). */
    private static List<String> withMentions(List<String> facts, String type, String text) {
        List<String> extra = br.com.mobvoz.profile.Mentions.facts(type, text);
        if (extra.isEmpty()) {
            return facts;
        }
        List<String> all = new ArrayList<>(facts);
        all.addAll(extra);
        return all;
    }

    // ---- eventos: a criatura REAGE em voz alta a algo que aconteceu (ex.: o jogador pegou o ovo que ela botou); fala gerada pela IA ----
    private static final long EVENT_COOLDOWN_MS = 30_000;
    private static final Map<UUID, Long> EVENT_LAST = new ConcurrentHashMap<>();
    private static final java.util.concurrent.atomic.AtomicInteger EVENTS_IN_FLIGHT = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicInteger EVENTS_STARTED = new java.util.concurrent.atomic.AtomicInteger();

    /** Quantas falas espontaneas (eventos) comecaram nesta sessao (diagnostico e testes). */
    public static int eventsStarted() {
        return EVENTS_STARTED.get();
    }

    /** Server thread. Sem fila nem spam: uma por criatura a cada 30 s, no maximo 2 ao mesmo tempo, nunca no meio de uma conversa. */
    public static void speakEvent(Mob mob, ServerPlayer player, String event) {
        speak(mob, player, event, null, false);
    }

    /**
     * Fala sobre algo que aconteceu NA TROCA (sobra devolvida, falta valor, item sem interesse, entrega, prazo): gerada pela IA a partir do
     * fato verificado. Sem cooldown por criatura (sao respostas as acoes do jogador); {@code fallback} so se a IA/voz estiver fora.
     */
    public static void tradeLine(Mob mob, ServerPlayer player, String fact, String fallback) {
        ANNOUNCED.add(fallback); // registro das situacoes faladas (diagnostico/testes)
        while (ANNOUNCED.size() > 32) {
            ANNOUNCED.remove(0);
        }
        speak(mob, player, fact, fallback, true);
    }

    private static void speak(Mob mob, ServerPlayer player, String event, String fallback, boolean urgent) {
        UUID mid = mob.getUUID();
        long nowMs = System.currentTimeMillis();
        Long last = EVENT_LAST.get(mid);
        if (!mob.isAlive() || player.distanceTo(mob) > 24 || (!urgent && (last != null && nowMs - last < EVENT_COOLDOWN_MS))
                || EVENTS_IN_FLIGHT.get() >= (urgent ? 4 : 2) || !ServerPlayNetworking.canSend(player, SubtitlePayload.TYPE)) {
            return;
        }
        if (mobToPlayer.putIfAbsent(mid, player.getUUID()) != null) {
            return;
        }
        EVENT_LAST.put(mid, nowMs);
        EVENTS_STARTED.incrementAndGet();
        EVENTS_IN_FLIGHT.incrementAndGet();
        MinecraftServer server = player.getServer();
        UUID pid = player.getUUID();
        long tick = player.level().getGameTime();
        MobMemory mem = MemoryStore.get(mob);
        List<String> facts = new ArrayList<>(MemoryRules.factLines(mem, pid, tick));
        facts.addAll(0, br.com.mobvoz.social.SceneFacts.lines(mob, player));
        facts.add(0, "EVENTO AGORA (aconteceu agora; o jogador não falou nada; use exatamente os números e itens citados): " + event);
        var rel = mem.relationOf(pid);
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        Voices voice = Voices.forProfile(Profiles.keyOf(mob), mem.identityId());
        AtomicReference<List<SidecarClient.Proposal>> ignored = new AtomicReference<>(List.of());
        AtomicReference<String> said = new AtomicReference<>();
        sidecar.reply(type, "", List.of(), facts, List.of(), Map.of("trust", (double) rel.trust(), "affinity", (double) rel.affinity(),
                        "fear", (double) rel.fear(), "credulity", (double) Traits.credulity(Profiles.keyOf(mob), mem.identityId())), "",
                        List.of(), profileMap(mob, pid), historyFor(mid, pid))
                .thenCompose(r -> {
                    if (!r.offline()) {
                        return sidecarSpeak(r, ignored, said, voice);
                    }
                    if (fallback == null) {
                        return CompletableFuture.completedFuture((short[]) null);
                    }
                    said.set(fallback); // IA fora do ar: a reserva fixa mantem a troca compreensivel
                    return sidecar.synthesize(fallback, voice.voice(), voice.pitch(), voice.speed()).thenApply(o -> o.orElse(null));
                })
                .orTimeout(PIPELINE_TIMEOUT_S, TimeUnit.SECONDS)
                .whenComplete((pcm, err) -> server.execute(() -> {
                    EVENTS_IN_FLIGHT.decrementAndGet();
                    Runnable done = () -> mobToPlayer.remove(mid, pid);
                    if (err != null || said.get() == null || !mob.isAlive() || player.hasDisconnected()) {
                        done.run();
                        return;
                    }
                    remember(mid, pid, "(aconteceu: " + event + ")", said.get());
                    ServerPlayNetworking.send(player, new SubtitlePayload(mid, Profiles.ofMob(mob).label(), said.get()));
                    Speaker sp = speaker;
                    if (pcm == null || sp == null) {
                        done.run();
                        return;
                    }
                    sp.speak(mob, pcm, () -> server.execute(done));
                }));
    }

    public static void reset() {
        HISTORY.clear();
        EVENT_LAST.clear();
        mobToPlayer.clear();
        playerToMob.clear();
        lastTick.clear();
    }

    /** Esta criatura esta numa conversa com algum jogador agora? */
    public static boolean isBusy(UUID mob) {
        return mobToPlayer.containsKey(mob);
    }

    public static int activeSessions() {
        return mobToPlayer.size();
    }

    /** Thread do servidor. */
    public static void onUtterance(ServerPlayer player, UtterancePayload p) {
        String text = p.text().strip();
        if (text.isEmpty() || text.length() > UtterancePayload.MAX_TEXT) {
            return;
        }
        if (!p.typed() && ServerConfig.current().requireVoiceConsent && !ConsentRegistry.has(player.getUUID())) {
            send(player, p.target(), StatusPayload.IDLE); // sem opt-in neste servidor: nada e processado
            return;
        }
        Entity e = player.level().getEntity(p.target());
        if (!(e instanceof Mob mob) || !mob.isAlive() || !lookingAt(player, mob)) {
            send(player, p.target(), StatusPayload.IDLE); // alvo sumiu/saiu da mira: descarta, mas o HUD nao fica preso
            return;
        }
        UUID pid = player.getUUID();
        UUID mid = mob.getUUID();
        long now = player.level().getGameTime();
        Long last = lastTick.get(pid);
        if (last != null && now - last < COOLDOWN_TICKS) {
            return;
        }
        lastTick.put(pid, now);
        // Uma sessao por jogador e por mob: o mob ocupado nao e "acordado" por outro enunciado.
        if (playerToMob.putIfAbsent(pid, mid) != null) {
            send(player, mid, StatusPayload.BUSY);
            return;
        }
        if (mobToPlayer.putIfAbsent(mid, pid) != null) {
            playerToMob.remove(pid, mid);
            send(player, mid, StatusPayload.BUSY);
            return;
        }
        MinecraftServer server = player.getServer();
        AtomicBoolean released = new AtomicBoolean();
        Runnable release = () -> server.execute(() -> {
            if (released.compareAndSet(false, true)) {
                mobToPlayer.remove(mid, pid);
                playerToMob.remove(pid, mid);
                send(player, mid, StatusPayload.IDLE);
            }
        });

        send(player, mid, StatusPayload.THINKING);
        br.com.mobvoz.social.Parley.touch(mob, player); // enquanto conversam, nada de ataque (nem de hostis nem de quem nao e aliado)
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        String pkey = Profiles.keyOf(mob);
        Voices voice = Voices.forProfile(pkey, MemoryStore.get(mob).identityId());
        long t0 = System.nanoTime();
        MemoryStore.update(mob, m -> MemoryRules.firstMeeting(m, pid, now)); // fato verificado
        learnName(mob, pid, text);
        AtomicReference<List<SidecarClient.Proposal>> proposals = new AtomicReference<>(List.of());
        AtomicReference<String> said = new AtomicReference<>();
        List<String> analyzeCaps = new ArrayList<>(MemoryRules.CAPABILITIES);
        analyzeCaps.addAll(OrderPolicy.ACTIONS);
        analyzeCaps.add("OPEN_TRADE");
        analyzeCaps.add("SOCIAL");
        // 1) entender (sem tocar no mundo)  2) servidor decide/aplica/persiste  3) a criatura fala sobre o resultado REAL
        sidecar.analyze(text, analyzeCaps)
                .thenCompose(an -> onServerThread(server, () -> planJudge(mob, pid, an))
                        .thenCompose(job -> job == null ? CompletableFuture.completedFuture(SidecarClient.Judgment.NONE)
                                : sidecar.judge(profileMap(mob, pid), job.accept(), job.giveName()))
                        .thenCompose(j -> onServerThread(server, () -> decideAndApply(mob, player, an, j))))
                .thenCompose(ctx -> sidecar.reply(type, text, MemoryRules.CAPABILITIES, withMentions(ctx.facts(), type, text), ctx.claims(), ctx.relation(),
                        ctx.claim(), ctx.orderResults(), profileMap(mob, pid), historyFor(mid, pid)))
                .thenCompose(r -> r.offline() ? CompletableFuture.completedFuture((short[]) null)
                        : sidecarSpeak(r, proposals, said, voice))
                .orTimeout(PIPELINE_TIMEOUT_S, TimeUnit.SECONDS)
                .whenComplete((pcm, err) -> server.execute(() -> {
                    Speaker sp = speaker;
                    // Sem TTS/SVC mas com texto valido (ex.: jogador sem audio): ainda ha legenda; so a voz some.
                    if (err != null || said.get() == null || !mob.isAlive() || mob.isRemoved()) {
                        Metrics.failures.incrementAndGet();
                        send(player, mid, StatusPayload.ERROR);
                        release.run();
                        return;
                    }
                    // Salva (alegacoes/crencas validadas) ANTES de falar: a fala nunca antecede o registro.
                    MemoryStore.update(mob, m -> MemoryRules.applyProposals(m, pid, proposals.get(), player.level().getGameTime()));
                    if (!br.com.mobvoz.social.Reactions.isActive(mid)) { // se a ameaca a enfureceu, a fala dela nao a protege
                        br.com.mobvoz.social.Parley.touch(mob, player);
                    }
                    Metrics.pipelineMs.set((System.nanoTime() - t0) / 1_000_000);
                    Metrics.conversations.incrementAndGet();
                    MobVozMod.LOGGER.info("{} respondeu: {}", type, Metrics.summary(activeSessions(), br.com.mobvoz.social.Chatter.linesSpoken()));
                    remember(mid, pid, text, said.get());
                    ServerPlayNetworking.send(player, new SubtitlePayload(mid, Profiles.ofMob(mob).label(), said.get()));
                    if (pcm == null || sp == null) {
                        release.run(); // so legenda
                        return;
                    }
                    send(player, mid, StatusPayload.SPEAKING);
                    sp.speak(mob, pcm, release);
                }));
    }

    /**
     * Fala curta e fixa (sem LLM) a partir de um evento do servidor ja confirmado (ex.: troca concluida). So fala se o mob
     * nao estiver numa conversa e o jogador estiver por perto.
     */
    public static void announce(Mob mob, ServerPlayer player, String text) {
        announce(mob, player, text, () -> { });
    }

    /** {@code next} roda quando a fala termina OU e descartada (ocupado, sidecar fora, longe): encadear cenas sem travar. */
    public static void announce(Mob mob, ServerPlayer player, String text, Runnable next) {
        ANNOUNCED.add(text); // so as falas FIXAS do servidor (nunca texto de jogador); limitado, serve a testes/diagnostico
        while (ANNOUNCED.size() > 32) {
            ANNOUNCED.remove(0);
        }
        Speaker sp = speaker;
        if (sp == null || mobToPlayer.containsKey(mob.getUUID()) || player.distanceTo(mob) > 24) {
            return;
        }
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        Voices v = Voices.forProfile(Profiles.keyOf(mob), MemoryStore.get(mob).identityId());
        MinecraftServer server = player.getServer();
        sidecar.synthesize(text, v.voice(), v.pitch(), v.speed()).thenAccept(o -> server.execute(() -> {
            if (o.isPresent() && mob.isAlive() && !mobToPlayer.containsKey(mob.getUUID())) {
                sp.speak(mob, o.get(), () -> server.execute(next));
            }
        }));
    }

    /** Contexto verificado para a fala: memoria ja atualizada + resultado das ordens ja decididas. */
    record SpeechContext(List<String> facts, List<String> claims, Map<String, Double> relation, String claim,
                         List<Map<String, String>> orderResults) {
    }

    /** O jogador disse o proprio nome (ou respondeu a pergunta de nome da criatura)? So ESTA criatura aprende; cada uma tem a sua consciencia. */
    static void learnName(Mob mob, UUID pid, String text) {
        var hist = historyFor(mob.getUUID(), pid);
        String last = hist.isEmpty() ? "" : hist.get(hist.size() - 1).getOrDefault("voce", "");
        br.com.mobvoz.memory.PlayerNames.extract(text, last).ifPresent(n -> {
            if (!n.equals(MemoryStore.get(mob).nameOf(pid))) {
                MemoryStore.update(mob, m -> m.withName(pid, n));
            }
        });
    }

    /** Perfil em texto para o prompt (o sidecar nao tem catalogo proprio: a fonte e profiles.json); {@code player} = quem fala, para o nome que a criatura sabe. */
    static Map<String, String> profileMap(Mob mob, UUID player) {
        var out = profileMap(mob);
        String name = player == null ? "" : MemoryStore.get(mob).nameOf(player);
        if (!name.isEmpty()) {
            out.put("nome_jogador", name);
        }
        return out;
    }

    static Map<String, String> profileMap(Mob mob) {
        Profiles.Profile p = Profiles.ofMob(mob);
        var lore = br.com.mobvoz.profile.Lore.forMob(mob);
        java.util.function.Function<java.util.Set<String>, String> names = s -> s.stream().sorted()
                .map(i -> TradeCatalog.nameOf(i, true)).collect(java.util.stream.Collectors.joining(", "));
        var out = new java.util.HashMap<String, String>(Map.of("nome", p.label(), "estilo", p.style(), "gosta", names.apply(p.likes()), "desgosta", names.apply(p.dislikes()),
                "dieta", p.diet(), "sobre", p.about(), "trato", p.address()));
        out.putAll(Map.of(
                "origem", lore.origem(), "sentidos", lore.sentidos(), "motivacao", lore.motivacao(), "medos", lore.medos(),
                "credulidade", switch (p.credulity()) {
                    case 0 -> "baixa: desconfia de histórias e exageros";
                    case 2 -> "alta: tende a acreditar em quem é gentil";
                    default -> "média: acredita no razoável, desconfia de exageros";
                }));
        return out;
    }

    private static <T> CompletableFuture<T> onServerThread(MinecraftServer server, java.util.function.Supplier<T> job) {
        CompletableFuture<T> f = new CompletableFuture<>();
        server.execute(() -> {
            try {
                f.complete(job.get());
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });
        return f;
    }

    /** Thread do servidor: registra a alegacao (crenca inicial deterministica), decide e aplica as ordens, salva. */
    static SpeechContext decideAndApply(Mob mob, ServerPlayer player, SidecarClient.Analysis an) {
        return decideAndApply(mob, player, an, SidecarClient.Judgment.NONE);
    }

    /** O que o jogador PEDE (item real) e se, na verdade, ele esta OFERECENDO algo que ela aceita ("pega 3 fenos que eu te dou..."). */
    record Ask(String item, boolean isPayment) {
    }

    static Ask resolveAsk(Mob mob, SidecarClient.Analysis an) {
        String asked = Values.lookup(an.ask().name()).orElse("");
        if (Values.isGenericFlower(an.ask().name())) { // "uma flor": a que ELA da (cada pasto tem a sua), se der alguma
            asked = TradeCatalog.recipesFor(Profiles.keyOf(mob)).stream().map(TradeCatalog.Recipe::outputItem).filter(Values::isFlower)
                    .findFirst().orElse(asked);
        }
        boolean payment = !asked.isEmpty() && Profiles.ofMob(mob).wants().contains(asked);
        return new Ask(payment ? "" : asked, payment);
    }

    /**
     * E negociacao de verdade? Sim se ele quer trocar (OPEN_TRADE), pede algo que a criatura PODE dar, argumenta/pede desconto, ou ja ha oferta
     * aberta com ele. Apenas citar ou oferecer um item, ou pedir algo que a criatura APRECIA (ela seria quem paga), NAO abre proposta:
     * ela so reage ao que ouviu (ver {@link #mentionOnly}).
     */
    static boolean isTradeTalk(Mob mob, UUID pid, SidecarClient.Analysis an) {
        if (an.orders().contains("OPEN_TRADE") || !an.persuadeItem().isBlank() || an.haggle() > 0) {
            return true;
        }
        var open = Trades.get(mob);
        boolean openWithPlayer = open.hasOffer() && open.offerPlayer().equals(pid.toString());
        if (openWithPlayer && (!an.offers().isEmpty() || !an.ask().name().isBlank())) {
            return true;
        }
        return !an.ask().name().isBlank() && !resolveAsk(mob, an).isPayment();
    }

    /** Falou de um item sem pedir nem propor troca (ex.: "eu te dou um bloco de ouro", "voce me daria ouro?" a quem gosta de ouro). */
    static boolean mentionOnly(Mob mob, UUID pid, SidecarClient.Analysis an) {
        return !isTradeTalk(mob, pid, an) && (!an.offers().isEmpty() || !an.ask().name().isBlank());
    }

    /** O que a IA precisa julgar: itens oferecidos fora do gosto fixo (aceitaria?) e o item pedido fora do catalogo (teria?). */
    record JudgeJob(List<String> accept, String giveName) {
    }

    /** Server thread. Null = nada a julgar (tudo coberto pelo catalogo e pelos gostos fixos). */
    static JudgeJob planJudge(Mob mob, UUID pid, SidecarClient.Analysis an) {
        if (!isTradeTalk(mob, pid, an) || !mob.isAlive()) {
            return null;
        }
        var p = Profiles.ofMob(mob);
        Ask ask = resolveAsk(mob, an);
        java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<>();
        java.util.stream.Stream.concat(an.offers().stream().map(SidecarClient.Item::name),
                java.util.stream.Stream.of(ask.isPayment() ? an.ask().name() : "", an.persuadeItem()))
                .map(n -> Values.lookup(n).orElse(""))
                .filter(id -> !id.isEmpty() && Values.value(id) > 0 && !p.wants().contains(id) && !p.dislikes().contains(id))
                .limit(6).forEach(id -> names.add(Values.name(id, false)));
        String give = !ask.item().isEmpty() && TradeCatalog.forItem(Profiles.keyOf(mob), ask.item()).isEmpty() ? Values.name(ask.item(), false) : "";
        return names.isEmpty() && give.isEmpty() ? null : new JudgeJob(List.copyOf(names), give);
    }

    static SpeechContext decideAndApply(Mob mob, ServerPlayer player, SidecarClient.Analysis an, SidecarClient.Judgment judged) {
        UUID pid = player.getUUID();
        long tick = player.level().getGameTime();
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        if (!mob.isAlive() || mob.isRemoved()) {
            throw new IllegalStateException("mob morto");
        }
        MobMemory mem = MemoryStore.get(mob);
        if (!an.claim().isBlank()) {
            MobMemory.Relationship r0 = mem.relationOf(pid);
            String stance = switch (Traits.credulity(Profiles.keyOf(mob), mem.identityId()) + (r0.trust() >= 0.5f ? 1 : 0) + (r0.trust() <= -0.3f ? -1 : 0)) {
                case 0, -1, -2 -> "DOUBTS";
                case 1 -> "SUSPECTS";
                default -> "BELIEVES";
            };
            mem = MemoryStore.update(mob, m -> MemoryRules.applyProposals(m, pid, List.of(new SidecarClient.Proposal("REGISTER_CLAIM",
                    Map.of("text", an.claim(), "stance", stance))), tick));
        }
        if (an.social().equals("NONE") || an.social().equals("COMFORT")) { // papo amistoso quebra o gelo (so um pouco; confianca de verdade vem de presentes e atos)
            mem = MemoryStore.update(mob, m -> MemoryRules.chatted(m, pid, tick));
        }
        List<Map<String, String>> results = new ArrayList<>();
        List<String> orderReqs = new ArrayList<>(an.orders());
        boolean tradeTalk = isTradeTalk(mob, pid, an);
        orderReqs.remove("OPEN_TRADE");
        if (tradeTalk) { // negociacao: oferta aberta SO pelo servidor, so se a receita existe; itens falados sao resolvidos no catalogo
            Ask ak = resolveAsk(mob, an);
            String asked = ak.item();
            boolean askedIsPayment = ak.isPayment();
            List<SidecarClient.Item> spokenOffers = new ArrayList<>(an.offers());
            if (askedIsPayment) {
                spokenOffers.add(new SidecarClient.Item(an.ask().name(), Math.max(1, an.ask().count())));
            }
            Map<String, String> acceptJudged = new java.util.HashMap<>(); // nome falado -> id real (so itens que o servidor conhece)
            judged.accept().forEach((n, v) -> Values.lookup(n).ifPresent(id -> acceptJudged.put(id, v)));
            if (!an.ask().name().isBlank() && asked.isEmpty() && !askedIsPayment) {
                results.add(Map.of("pedido", "OPEN_TRADE", "resultado", "IMPOSSIVEL",
                        "motivo", "ela não conhece \"" + an.ask().name() + "\" como algo que possa dar"));
            } else {
                Trades.OpenResult tr = Trades.open(mob, player, OrderPolicy.warmth(mem.relationOf(pid), false), asked, askedIsPayment ? 0 : an.ask().count(), judged.give());
                results.add(Map.of("pedido", "OPEN_TRADE", "resultado", tr.verdict(), "motivo", tr.reason()));
                if (tr.verdict().equals("ACEITO") || tr.verdict().equals("JA_ATIVO")) {
                    Trades.applyJudgments(mob, acceptJudged); // a IA decide o que ELA aceitaria alem do gosto fixo
                    if (an.haggle() > 0) { // pediu desconto com um argumento: o servidor limita o efeito (credulidade, confianca, piso do valor)
                        MobMemory.Relationship hr = mem.relationOf(pid);
                        Trades.OpenResult hg = Trades.haggle(mob, an.haggle(), Traits.credulity(Profiles.keyOf(mob), mem.identityId()), hr.trust());
                        results.add(Map.of("pedido", "BARGANHA", "resultado", hg.verdict(), "motivo", hg.reason()));
                    }
                    var pit = Values.lookup(an.persuadeItem());
                    if (pit.isPresent()) {
                        MobMemory.Relationship rr = mem.relationOf(pid);
                        Trades.OpenResult pr = Trades.persuade(mob, pit.get(), an.persuadeStrength(),
                                Traits.credulity(Profiles.keyOf(mob), mem.identityId()), rr.trust());
                        results.add(Map.of("pedido", "CONVENCER", "resultado", pr.verdict(), "motivo", pr.reason()));
                    }
                    Map<String, Integer> offered = new java.util.LinkedHashMap<>();
                    List<String> unknown = new ArrayList<>();
                    for (SidecarClient.Item it : spokenOffers) {
                        Values.lookup(it.name()).ifPresentOrElse(id -> offered.merge(id, it.count(), Integer::sum), () -> unknown.add(it.name()));
                    }
                    String ev = Trades.evaluate(mob, offered);
                    if (ev != null || !unknown.isEmpty()) {
                        results.add(Map.of("pedido", "AVALIAR_OFERTA", "resultado", "RESUMO", "motivo",
                                (ev == null ? "" : ev) + (unknown.isEmpty() ? "" : "; ela não reconhece como valor: " + String.join(", ", unknown))));
                    }
                }
            }
        }
        if (mentionOnly(mob, pid, an)) { // citou/ofereceu um item sem pedir nada: nada de proposta; ela so reage com naturalidade
            String falou = !an.offers().isEmpty() ? an.offers().get(0).name() : an.ask().name();
            results.add(Map.of("pedido", "OFERTA_ITEM", "resultado", "SEM_PEDIDO", "motivo",
                    "o jogador falou de \"" + falou + "\" mas não pediu nada seu nem propôs uma troca; NÃO proponha preço, troca nem item seu: "
                            + "reaja com naturalidade ao que ele disse (pode mostrar interesse ou curiosidade) e, se quiser, pergunte o que ele procura"));
        }
        if (!tradeTalk) { // sem pedido de troca nesta fala, mas ha oferta aberta com este jogador: a criatura ainda sabe os termos
            var open = Trades.get(mob);
            var recipe = open.hasOffer() && open.offerPlayer().equals(pid.toString()) ? TradeCatalog.byId(open.offerRecipe()) : java.util.Optional.<TradeCatalog.Recipe>empty();
            if (recipe.isPresent()) {
                results.add(Map.of("pedido", "OFERTA_ABERTA", "resultado", "JA_ATIVO", "motivo", "a troca combinada continua de pé: "
                        + recipe.get().conditions(open.offerUnits())));
            }
        }
        if (!orderReqs.isEmpty()) {
            var rel1 = mem.relationOf(pid);
            double bonus = OrderPolicy.appealBonus(an.appeal(), Traits.credulity(Profiles.keyOf(mob), mem.identityId()), rel1.fear());
            for (OrderPolicy.Decision d : Orders.handle(mob, player, orderReqs, Orders.kinshipBelieved(mem, pid), bonus)) {
                results.add(Map.of("pedido", d.action(), "resultado", switch (d.verdict()) {
                    case ACCEPTED -> "ACEITO";
                    case REFUSED -> "RECUSADO";
                    case UNSUPPORTED -> "IMPOSSIVEL";
                    case ALREADY -> "JA_ATIVO";
                    case NOT_APPLICABLE -> "NAO_SE_APLICA";
                }, "motivo", d.reason()));
            }
        }
        String pkey = Profiles.keyOf(mob);
        String faction = KnowledgeStore.factionOf(pkey);
        var know = KnowledgeStore.get(player.getServer());
        if (!an.social().equals("NONE")) { // ameaca/consolo por palavras: a criatura REAGE segundo a sua natureza
            var rx = br.com.mobvoz.social.Reactions.react(mob, player, an.social());
            if (rx != null) {
                results.add(rx);
                mem = MemoryStore.get(mob);
            }
        }
        MobMemory.Relationship rel = mem.relationOf(pid);
        List<String> facts = new ArrayList<>(MemoryRules.factLines(mem, pid, tick));
        facts.addAll(0, br.com.mobvoz.social.SceneFacts.lines(mob, player)); // hora, clima, vida, o que o jogador segura, quem esta perto
        if (!faction.isEmpty()) { // memoria coletiva: a faccao desconfia de quem atacou um dos seus
            facts.addAll(know.factionLines(faction, pid, tick));
            float pen = know.trustPenalty(faction, pid, tick);
            rel = new MobMemory.Relationship(rel.trust() - pen, rel.affinity(), rel.fear());
        }
        if (pkey.equals("minecraft:ender_dragon")) { // o Dragao so "narra" o que esta no registro verificado
            facts.addAll(know.historyLines(pid, tick));
        }
        player.displayClientMessage(TrustBar.text(mob, pid), true); // a barrinha de confianca, a cada fala
        return new SpeechContext(facts, MemoryRules.claimLines(mem, pid),
                Map.of("trust", (double) rel.trust(), "affinity", (double) rel.affinity(), "fear", (double) rel.fear(),
                        "credulity", (double) Traits.credulity(Profiles.keyOf(mob), mem.identityId())),
                an.claim(), results);
    }

    private static CompletableFuture<short[]> sidecarSpeak(SidecarClient.Reply r, AtomicReference<List<SidecarClient.Proposal>> out,
                                                          AtomicReference<String> said, Voices voice) {
        out.set(r.proposals());
        said.set(r.speechText());
        Metrics.llmMs.set(r.llmMs());
        long t = System.nanoTime();
        return sidecar.synthesize(r.speechText(), voice.voice(), voice.pitch(), voice.speed()).thenApply(o -> {
            Metrics.ttsMs.set((System.nanoTime() - t) / 1_000_000);
            return o.orElse(null);
        });
    }

    /** Mesma geometria do cliente (TargetPicker), com tolerancia. Sem texto/audio em logs. */
    static boolean lookingAt(ServerPlayer player, Mob mob) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getViewVector(1.0F).scale(MAX_RANGE));
        return TargetPicker.nearest(from, to, List.of(mob.getBoundingBox().inflate(mob.getPickRadius() + 0.5))) >= 0;
    }

    private static void send(ServerPlayer player, UUID mob, int state) {
        if (!player.hasDisconnected() && ServerPlayNetworking.canSend(player, StatusPayload.TYPE)) {
            ServerPlayNetworking.send(player, new StatusPayload(mob, state));
        }
    }
}

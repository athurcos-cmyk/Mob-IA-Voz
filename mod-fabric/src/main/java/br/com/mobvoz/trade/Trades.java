package br.com.mobvoz.trade;

import br.com.mobvoz.MobVozMod;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.trade.Negotiation.Stack;
import br.com.mobvoz.trade.TradeCatalog.Recipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Troca FISICA com negociacao (fases 7+). A criatura abre uma oferta (receita do seu oficio) e fala em cima do que ACONTECE:
 * so aceita itens do interesse dela (ou em que foi convencida), toma o MINIMO para fechar, devolve a sobra com um comentario,
 * recusa item errado, e desfaz o negocio se o jogador nao cumprir. Reserva e conclusao sao idempotentes e persistentes.
 */
public final class Trades {
    private static final int TICK_EVERY = 10;
    private static final double SCAN_RADIUS = 6.0;
    private static final double PICKUP_REACH = 1.8;
    private static final long DWELL_TICKS = 20;
    private static final int SETTLE_TICKS = 60;

    private static final Set<UUID> tracked = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Set<Integer>> noticed = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> shortNotified = new ConcurrentHashMap<>();
    private static final Map<UUID, String[]> excessNote = new ConcurrentHashMap<>(); // {fato para a IA, fala reserva}
    private static final Map<UUID, Long> protectedUntil = new ConcurrentHashMap<>();
    /** Criaturas que recolhem itens do chao (piglin etc.) ficam sem recolher por um tempo depois de entregar/devolver, senao pegam de volta o que acabaram de dar. */
    private static final Map<UUID, Long> lootShield = new ConcurrentHashMap<>();
    private static final long SHIELD_TICKS = 160;

    /** Ultima entrega de cada criatura (so memoria): serve para reconhecer quando o jogador DEVOLVE o que ela deu, sem ser pedido. */
    private record Delivery(String item, UUID player, long tick) {
    }

    private static final Map<UUID, Delivery> deliveries = new ConcurrentHashMap<>();
    private static final long HONEST_WINDOW = 12000;

    /**
     * Um item acabou de ser lancado por um jogador. Se for do tipo que uma criatura PERTO acabou de entregar a ele e ela nao pediu nada
     * (sem oferta aberta), e uma devolucao honesta: vira fato na memoria dela, no grupo dela e no registro do mundo (que o Dragao conhece).
     * Sem limite diario: cada devolucao consome a entrega que a originou, entao so se repete negociando de novo.
     */
    public static void onPlayerThrew(ItemEntity it, ServerLevel level) {
        if (deliveries.isEmpty() || !(it.getOwner() instanceof ServerPlayer p)) {
            return;
        }
        String id = idOf(it.getItem());
        long now = level.getGameTime();
        for (var e : List.copyOf(deliveries.entrySet())) {
            Delivery d = e.getValue();
            if (now - d.tick() > HONEST_WINDOW) {
                deliveries.remove(e.getKey());
                continue;
            }
            if (!d.item().equals(id) || !d.player().equals(p.getUUID()) || !(level.getEntity(e.getKey()) instanceof Mob m) || !m.isAlive()
                    || m.distanceTo(it) > 6 || get(m).hasOffer()) {
                continue;
            }
            deliveries.remove(e.getKey()); // cada devolucao exige uma entrega nova (nao ha como repetir sem negociar de novo)
            int n = it.getItem().getCount();
            String detail = n + " " + Values.name(id, n != 1);
            MemoryStore.update(m, mem -> MemoryRules.honestReturn(mem, p.getUUID(), detail, now));
            String label = br.com.mobvoz.profile.Mentions.labelOf(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).toString());
            String faction = br.com.mobvoz.knowledge.KnowledgeStore.factionOf(Profiles.keyOf(m));
            UUID pid = p.getUUID();
            br.com.mobvoz.knowledge.KnowledgeStore.update(level.getServer(), k -> {
                var k2 = faction.isEmpty() ? k : k.withFaction(faction, new br.com.mobvoz.knowledge.Knowledge.Event("HONEST_RETURN", pid, detail, now));
                return k2.withHistory(new br.com.mobvoz.knowledge.Knowledge.Event("HONEST_RETURN", pid, label == null ? "uma criatura" : "um(a) " + label, now));
            });
            MobVozMod.LOGGER.info("devolucao honesta registrada ({} -> {})", id, label);
            if (br.com.mobvoz.config.ServerConfig.current().naturalEvents) {
                ConversationService.speakEvent(m, p, "o jogador acabou de te devolver " + detail + " que você tinha entregado a ele, sem você pedir. "
                        + "A maioria ficaria com o item; reaja a essa honestidade e diga que vai se lembrar disso");
            }
            return;
        }
    }

    private Trades() {
    }

    public static MobTrades get(Mob mob) {
        MobTrades t = mob.getAttached(MobTrades.TYPE);
        return t == null ? MobTrades.EMPTY : t;
    }

    static void set(Mob mob, MobTrades t) {
        mob.setAttached(MobTrades.TYPE, t);
        if (t.active()) {
            tracked.add(mob.getUUID());
            mob.setPersistenceRequired();
        } else {
            tracked.remove(mob.getUUID());
        }
    }

    public static int trackedCount() {
        return tracked.size();
    }

    public record OpenResult(String verdict, String reason) {
    }

    /** Ha negociacao (oferta aberta e valida, ou reserva) deste mob com este jogador? Durante ela, nao ha ataque mutuo. */
    public static boolean negotiatingWith(Mob mob, ServerPlayer player, long now) {
        MobTrades t = get(mob);
        String id = player.getUUID().toString();
        return (t.hasOffer() && t.offerPlayer().equals(id) && now < t.offerExpires()) || (t.hasTx() && t.txPlayer().equals(id));
    }

    /** O jogador feriu quem negociava: a oferta acaba na hora (e a reserva, se houver, e devolvida). */
    public static void onAttacked(Mob mob, ServerPlayer player) {
        MobTrades t = get(mob);
        if (!t.active() || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        String id = player.getUUID().toString();
        if (t.hasTx() && t.txPlayer().equals(id)) {
            refund(mob, level, level.getGameTime());
            t = get(mob);
        }
        if (t.hasOffer() && t.offerPlayer().equals(id)) {
            set(mob, t.closeOffer());
        }
    }

    // ---- conversa: abrir oferta, convencer, avaliar ofertas faladas ----

    public static OpenResult open(Mob mob, ServerPlayer player, double warmth, String askedItem, int askedCount) {
        return open(mob, player, warmth, askedItem, askedCount, "");
    }

    /** Chance (por criatura, item e dia de jogo) de ela ter por sorte algo que nao combina com ela. */
    static final double LUCK_MAYBE = 0.35;
    static final double LUCK_NO = 0.15;
    static final double LUCK_MAX_VALUE = 20.0;

    /** Determinista por (criatura, item, dia): insistir no mesmo pedido no mesmo dia nao muda a sorte; outra criatura/dia, sim. */
    static boolean luck(java.util.UUID mob, String item, long day, String verdict) {
        double chance = verdict.equals("MAYBE") ? LUCK_MAYBE : LUCK_NO;
        long seed = mob.getMostSignificantBits() * 31 + mob.getLeastSignificantBits() * 17 + item.hashCode() * 131L + day * 7919L;
        return new java.util.Random(seed).nextDouble() < chance;
    }

    /**
     * Server thread. {@code askedItem}: item que o jogador pediu ("" = o que a criatura normalmente da).
     * {@code giveVerdict}: julgamento da IA ("YES" plausivel, "MAYBE" improvavel, "NO" impossivel, "" sem IA) para item fora do catalogo.
     */
    public static OpenResult open(Mob mob, ServerPlayer player, double warmth, String askedItem, int askedCount, String giveVerdict) {
        String key = Profiles.keyOf(mob);
        var recipes = TradeCatalog.recipesFor(key);
        if (recipes.isEmpty() && askedItem.isEmpty()) {
            return new OpenResult("IMPOSSIVEL", "essa criatura não tem nada para trocar; nada do que ela é ou faz rende um item para dar");
        }
        String note = "";
        Recipe r = askedItem.isEmpty() ? recipes.get(0) : TradeCatalog.forItem(key, askedItem).orElse(null);
        if (r == null) { // fora do catalogo: a IA julga se e plausivel; senao, so por sorte (e nunca coisa cara)
            long day = player.level().getGameTime() / 24000;
            boolean lucky = false;
            if (giveVerdict.equals("YES")) {
                r = TradeCatalog.dynamic(key, askedItem, 1.5, 3).orElse(null);
            } else if (Values.value(askedItem) <= LUCK_MAX_VALUE && luck(mob.getUUID(), askedItem, day, giveVerdict)) {
                r = TradeCatalog.dynamic(key, askedItem, 2.5, 1).orElse(null);
                lucky = r != null;
            }
            if (r == null) {
                return new OpenResult("IMPOSSIVEL", "ela não tem " + Values.name(askedItem, false) + " nem teria como conseguir; "
                        + "isso não combina com o que ela é nem com a vida dela. " + (recipes.isEmpty() ? "" : whatItCanGive(recipes)));
            }
            if (lucky) {
                note = "Normalmente ela não teria isso, mas por sorte achou " + Values.name(askedItem, false)
                        + " largado por aí; conte do seu jeito onde achou, e cobra mais caro por ser um achado raro. ";
            }
        }
        MobTrades cur = get(mob);
        long now = player.level().getGameTime();
        if (cur.hasTx()) {
            return new OpenResult("JA_ATIVO", "já está no meio de uma troca");
        }
        if (warmth < -0.3) {
            return new OpenResult("RECUSADO", "não confia em quem pede");
        }
        if (cur.hasOffer() && !cur.offerPlayer().equals(player.getUUID().toString()) && now < cur.offerExpires()) {
            return new OpenResult("RECUSADO", "está negociando com outro jogador");
        }
        String pid = player.getUUID().toString();
        boolean wasOpen = cur.hasOffer() && cur.offerRecipe().equals(r.id()) && cur.offerPlayer().equals(pid);
        int lock = 0; // preco por unidade travado nesta oferta (decimos); 0 = o da receita
        if (!wasOpen && br.com.mobvoz.config.ServerConfig.current().dynamicPrices) {
            var q = Pricing.quote(r, warmth, mob.getUUID(), player.level().getGameTime() / 24000);
            lock = (int) Math.round(q.unitPrice() * 10);
            if (!q.why().isEmpty()) {
                note += "Sobre o preço: " + q.why() + ". ";
            }
        }
        int wanted = askedCount > 0 ? askedCount : (wasOpen ? cur.offerUnits() : 1);
        int units = Math.max(1, Math.min(wanted, r.maxUnits()));
        set(mob, cur.open(r.id(), pid, now + r.offerTicks(), units, lock));
        double unitPrice = get(mob).unitPrice(r.price());
        String cut = wanted > units ? "O jogador quis " + wanted + ", mas ela só dá até " + units + " por negócio. " : "";
        String others = recipes.size() > 1 && askedItem.isEmpty() ? ". " + whatItCanGive(recipes) : "";
        return new OpenResult(wasOpen && askedCount <= 0 ? "JA_ATIVO" : "ACEITO", note + cut + r.conditions(units, unitPrice) + others);
    }

    /** O que a criatura realmente teria para dar (so o que o servidor verificou). */
    private static String whatItCanGive(List<Recipe> recipes) {
        return "O que ela poderia dar: " + recipes.stream().map(x -> Values.name(x.outputItem(), x.outputCount() != 1)).distinct()
                .collect(Collectors.joining(", "));
    }

    /** Interesse inicial julgado pela IA para itens que ela nao tem no gosto fixo: YES serve, MAYBE talvez, NO nao (mas pode ser convencida). */
    public static void applyJudgments(Mob mob, Map<String, String> verdictByItem) {
        MobTrades t = get(mob);
        if (!t.hasOffer()) {
            return;
        }
        var p = Profiles.of(Profiles.keyOf(mob));
        for (var e : verdictByItem.entrySet()) {
            if (p.wants().contains(e.getKey()) || p.dislikes().contains(e.getKey())) {
                continue; // gosto fixo vale mais que o julgamento
            }
            int milli = switch (e.getValue()) {
                case "YES" -> 700;
                case "MAYBE" -> 350;
                default -> 0;
            };
            if (milli > 0) {
                t = t.withJudged(e.getKey(), milli);
            }
        }
        set(mob, t);
    }

    /** O jogador argumenta por um preco menor. O analisador so da a forca (0..2); o efeito e limitado por {@link Pricing#haggle}. */
    public static OpenResult haggle(Mob mob, int strength, int credulity, double trust) {
        MobTrades t = get(mob);
        var recipe = TradeCatalog.byId(t.offerRecipe());
        if (!t.hasOffer() || recipe.isEmpty()) {
            return new OpenResult("NAO_SE_APLICA", "não há negociação aberta para pedir desconto");
        }
        Recipe r = recipe.get();
        double now = t.unitPrice(r.price());
        if (t.persuasions() >= Negotiation.MAX_PERSUASIONS) {
            return new OpenResult("RECUSADO", "já ouviu argumentos demais; o preço fica como está (" + r.conditions(t.offerUnits(), now) + ")");
        }
        if (strength <= 0) {
            return new OpenResult("RECUSADO", "o jogador pediu desconto sem dar motivo; ela pode perguntar por que deveria baixar o preço");
        }
        int cur = (int) Math.round(now * 10);
        int next = Pricing.haggle(r, cur, strength, credulity, trust);
        if (next >= cur) {
            return new OpenResult("RECUSADO", "esse já é o menor preço que ela faz por isso, nem com bons argumentos (" + r.conditions(t.offerUnits(), now) + ")");
        }
        set(mob, t.withHaggledPrice(next));
        return new OpenResult("CONVENCIDO", "o argumento funcionou e ela baixou o preço: " + r.conditions(t.offerUnits(), next / 10.0));
    }

    /** O jogador deu um motivo para a criatura querer {@code item}. O efeito e limitado por Negotiation. */
    public static OpenResult persuade(Mob mob, String item, int strength, int credulity, double trust) {
        MobTrades t = get(mob);
        if (!t.hasOffer()) {
            return new OpenResult("NAO_SE_APLICA", "não há negociação aberta para convencer");
        }
        var p = Profiles.of(Profiles.keyOf(mob));
        String n = Values.name(item, false);
        var res = Negotiation.persuade(p, item, credulity, trust, strength, t.persuadedMap().getOrDefault(item, 0), t.persuasions());
        if (res.verdict() == Negotiation.Verdict.CONVINCED || res.verdict() == Negotiation.Verdict.PARTIAL) {
            set(mob, t.withPersuasion(item, res.newMilli()));
        }
        return switch (res.verdict()) {
            case CONVINCED -> new OpenResult("CONVENCIDO", "foi convencida e agora tem interesse em " + n + " como pagamento");
            case PARTIAL -> new OpenResult("PARCIAL", "ficou um pouco mais interessada em " + n + ", mas ainda quer um motivo melhor");
            case NO_ARGUMENT -> new OpenResult("RECUSADO", "o jogador não deu motivo nenhum; ela pode perguntar para que ela iria querer " + n);
            case AVERSION -> new OpenResult("RECUSADO", "tem aversão a " + n + " e nada a convence disso");
            case FATIGUE -> new OpenResult("RECUSADO", "já ouviu argumentos demais; não quer mais ser convencida");
            case ALREADY_INTERESTED -> new OpenResult("JA_ATIVO", "já se interessa por " + n);
        };
    }

    /** Avalia, item a item, o que o jogador DISSE que ofereceria (nada e entregue ainda). Null se nao ha oferta aberta. */
    public static String evaluate(Mob mob, Map<String, Integer> offered) {
        MobTrades t = get(mob);
        var recipe = TradeCatalog.byId(t.offerRecipe());
        if (!t.hasOffer() || recipe.isEmpty() || offered.isEmpty()) {
            return null;
        }
        var p = Profiles.of(Profiles.keyOf(mob));
        Map<String, Integer> pm = t.persuadedMap();
        double total = t.unitPrice(recipe.get().price()) * t.offerUnits();
        List<String> parts = new ArrayList<>();
        for (var e : offered.entrySet()) {
            double in = Negotiation.interest(p, e.getKey(), pm);
            String name = e.getValue() + " " + Values.name(e.getKey(), e.getValue() != 1);
            String how;
            if (p.dislikes().contains(e.getKey())) {
                how = "ela detesta isso";
            } else if (in <= 0) {
                how = "sem interesse nenhum (poderia ser convencida com um bom motivo)";
            } else {
                how = "interessa (paga uns " + Math.min(100, Math.round(100 * Values.value(e.getKey()) * in * e.getValue() / total)) + "% do preço)";
            }
            parts.add(name + ": " + how);
        }
        double accepted = Negotiation.acceptedValue(p, pm, offered);
        long pct = Math.min(100, Math.round(100 * accepted / total));
        return String.join("; ", parts) + "; no total o que interessa paga " + pct + "% do preço"
                + (accepted + 1e-6 >= total ? " (basta; depois é só jogar no chão)" : "; ainda faltaria " + missingText(p, total - accepted));
    }

    /** Quanto falta, em ITENS ("o equivalente a 7 barras de ouro"), pelo item aceito mais comum que caiba em ate 16 unidades. */
    static String missingText(Profiles.Profile p, double missing) {
        List<String> ids = p.wants().stream().filter(i -> Values.value(i) > 0)
                .sorted((a, b) -> Double.compare(Values.value(a), Values.value(b))).toList();
        for (String i : ids) {
            int n = (int) Math.ceil(missing / Values.value(i) - 1e-9);
            if (n >= 1 && n <= 16) {
                return "o equivalente a " + n + " " + Values.name(i, n != 1);
            }
        }
        return ids.isEmpty() ? "mais valor" : "o equivalente a mais " + Values.name(ids.get(ids.size() - 1), true);
    }

    // ---- ciclo no mundo ----

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ItemEntity it && it.tickCount < 5 && it.getOwner() instanceof ServerPlayer) {
                onPlayerThrew(it, level);
            }
            if (entity instanceof Mob m && get(m).active()) {
                tracked.add(m.getUUID()); // retoma: RESERVADO conclui/reembolsa exatamente uma vez
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (lootShield.remove(entity.getUUID()) != null && entity instanceof Mob m) {
                m.setCanPickUpLoot(true); // nunca deixa a criatura sem recolher para sempre
            }
            tracked.remove(entity.getUUID());
            noticed.remove(entity.getUUID());
            shortNotified.remove(entity.getUUID());
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof Mob m && get(m).hasTx() && m.level() instanceof ServerLevel level) {
                refund(m, level, level.getGameTime()); // morreu entre reserva e entrega: devolve ao chao
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % TICK_EVERY != 0) {
                return;
            }
            if (!protectedUntil.isEmpty() || !lootShield.isEmpty()) {
                releaseProtections(server);
            }
            for (UUID id : List.copyOf(tracked)) {
                for (ServerLevel level : server.getAllLevels()) {
                    if (level.getEntity(id) instanceof Mob m && m.isAlive()) {
                        tickMob(server, level, m);
                        break;
                    }
                }
            }
        });
    }

    /** Protege o que {@code mob} acabou de entregar/devolver contra o proprio mob (so quem recolhia itens antes). */
    private static void shield(Mob mob, long now) {
        if (mob.canPickUpLoot()) {
            mob.setCanPickUpLoot(false);
            lootShield.put(mob.getUUID(), now + SHIELD_TICKS);
        } else if (lootShield.containsKey(mob.getUUID())) {
            lootShield.put(mob.getUUID(), now + SHIELD_TICKS);
        }
    }

    private static void releaseProtections(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        lootShield.entrySet().removeIf(e -> {
            if (now < e.getValue()) {
                return false;
            }
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getEntity(e.getKey()) instanceof Mob m) {
                    m.setCanPickUpLoot(true);
                    break;
                }
            }
            return true;
        });
        protectedUntil.entrySet().removeIf(e -> {
            if (now < e.getValue()) {
                return false;
            }
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getEntity(e.getKey()) instanceof ItemEntity it) {
                    it.setTarget(null);
                    break;
                }
            }
            return true;
        });
    }

    static void tickMob(MinecraftServer server, ServerLevel level, Mob mob) {
        long now = level.getGameTime();
        MobTrades t = get(mob);
        // enquanto negocia, o mob nao mira em quem negocia (hostis incluidos: aranha, esqueleto Wither, blaze, enderman...)
        if (mob.getTarget() instanceof ServerPlayer tp && negotiatingWith(mob, tp, now)) {
            mob.setTarget(null);
            if (mob instanceof net.minecraft.world.entity.monster.Creeper c) {
                c.setSwellDir(-1);
            }
        }
        if (t.hasTx()) {
            if (t.isDone(t.txId())) { // reprocessamento da mesma transacao: nada a fazer, so limpa
                set(mob, t.completed());
            } else if (now - t.txTick() >= DWELL_TICKS) {
                finish(mob, level, t, now);
            }
            return;
        }
        if (!t.hasOffer()) {
            return;
        }
        var prof = Profiles.of(Profiles.keyOf(mob));
        Recipe r = TradeCatalog.byId(t.offerRecipe()).orElse(null);
        if (r == null) {
            set(mob, t.closeOffer());
            return;
        }
        if (now >= t.offerExpires()) { // prazo: o negocio nao fechou porque o combinado nao foi cumprido
            set(mob, t.closeOffer());
            announceTo(mob, level, t.offerPlayer(),
                    "o prazo da troca acabou e o jogador não entregou o combinado, então você desfez o negócio (nada foi tomado)",
                    prof.address() + ", você não cumpriu o combinado, então o negócio está desfeito.");
            return;
        }
        UUID player = UUID.fromString(t.offerPlayer());
        Map<String, Integer> pm = t.persuadedMap();
        List<ItemEntity> mine = level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(SCAN_RADIUS),
                e -> e.isAlive() && e.getOwner() != null && e.getOwner().getUUID().equals(player));
        List<Stack> stacks = new ArrayList<>();
        for (ItemEntity e : mine) {
            String id = idOf(e.getItem());
            if (Negotiation.interest(prof, id, pm) > 0 && Values.value(id) > 0) {
                stacks.add(new Stack(e.getId(), id, e.getItem().getCount(), e.distanceTo(mob)));
            } else if (e.tickCount > 20 && noticed.computeIfAbsent(mob.getUUID(), k -> new HashSet<>()).add(e.getId())) {
                notifyUninterested(mob, level, prof, id, e.getItem().getCount(), t);
            }
        }
        if (stacks.isEmpty()) {
            attend(mob, level, player);
            return;
        }
        double totalPrice = t.unitPrice(r.price()) * t.offerUnits();
        var sel = Negotiation.select(prof, pm, stacks, totalPrice);
        if (!sel.enough()) { // joga-se algo do interesse, mas ainda nao paga o combinado
            int sig = (int) Math.round(sel.available() * 100);
            boolean settled = mine.stream().allMatch(e -> e.tickCount > SETTLE_TICKS);
            if (settled && !Integer.valueOf(sig).equals(shortNotified.get(mob.getUUID()))) {
                shortNotified.put(mob.getUUID(), sig);
                long pct = Math.round(100 * sel.available() / totalPrice);
                String missing = missingText(prof, totalPrice - sel.available());
                announceTo(mob, level, t.offerPlayer(), "o jogador largou itens que pagam só " + pct + "% do que combinaram; para fechar falta "
                        + missing + "; sem isso o negócio não está fechado e você não pegou nada",
                        prof.address() + ", isso paga só uns " + pct + "% do que combinamos; falta " + missing + ". Sem isso o negócio não está fechado.");
            }
            attend(mob, level, player);
            return;
        }
        ItemEntity nearest = null;
        for (Stack s : stacks) {
            if (level.getEntity(s.ref()) instanceof ItemEntity e && (nearest == null || e.distanceTo(mob) < nearest.distanceTo(mob))) {
                nearest = e;
            }
        }
        boolean stationary = mob.isNoAi() || !Orders.canWalkFollow(mob);
        boolean reached = nearest != null && (nearest.distanceTo(mob) <= PICKUP_REACH || stationary && nearest.distanceTo(mob) <= 3.5);
        if (!reached) {
            if (nearest != null && !stationary) {
                mob.getNavigation().moveTo(nearest.getX(), nearest.getY(), nearest.getZ(), 1.1);
            }
            return;
        }
        // RESERVA: tira exatamente o MINIMO necessario, anota no ledger no mesmo tick; devolve o excedente (so interessantes).
        Map<String, Integer> taken = new HashMap<>();
        for (var tk : sel.takes()) {
            if (level.getEntity(tk.ref()) instanceof ItemEntity e) {
                if (tk.count() >= e.getItem().getCount()) {
                    e.discard();
                } else {
                    e.getItem().shrink(tk.count());
                }
                taken.merge(tk.item(), tk.count(), Integer::sum);
            }
        }
        int returned = 0;
        ServerPlayer sp = server.getPlayerList().getPlayer(player);
        for (var lf : sel.leftovers()) {
            if (level.getEntity(lf.ref()) instanceof ItemEntity e && sp != null) {
                Vec3 dir = sp.position().subtract(e.position()).normalize().scale(0.28);
                e.setDeltaMovement(dir.x, 0.22, dir.z); // "toma de volta o que sobrou"
                e.hurtMarked = true;
                returned += lf.count();
            }
        }
        mob.getNavigation().stop();
        if (returned > 0) {
            shield(mob, now); // o que ela devolveu nao e para ela pegar de volta
            String needed = taken.entrySet().stream().map(e -> e.getValue() + " " + Values.name(e.getKey(), e.getValue() != 1))
                    .collect(Collectors.joining(" e "));
            excessNote.put(mob.getUUID(), new String[] {"você só pegou " + needed + " do chão (era o que bastava) e devolveu o resto ao jogador, que deu mais que o combinado",
                    prof.address() + ", eu só pedi " + needed + ". Peguei o que precisava e devolvi o que sobrou."});
        }
        set(mob, t.reserve(UUID.randomUUID().toString(), r.id(), t.offerPlayer(), now, taken, t.offerUnits()));
    }

    private static void notifyUninterested(Mob mob, ServerLevel level, Profiles.Profile prof, String item, int count, MobTrades t) {
        String name = Values.name(item, count != 1);
        String fact = "o jogador largou " + count + " " + name + " no chão perto de você; "
                + (prof.dislikes().contains(item) ? "você detesta isso" : "isso não te interessa como pagamento")
                + (!prof.dislikes().contains(item) && t.persuasions() < Negotiation.MAX_PERSUASIONS
                ? ", mas você poderia mudar de ideia se ele der um bom motivo" : "") + "; você não pegou";
        String line;
        if (prof.dislikes().contains(item)) {
            line = prof.address() + ", " + name + (count != 1 ? " eu detesto." : " eu detesto.");
        } else {
            line = prof.address() + ", " + name + (count != 1 ? " não me interessam." : " não me interessa.");
            if (t.persuasions() < Negotiation.MAX_PERSUASIONS) {
                line += " A não ser que você me dê um bom motivo.";
            }
        }
        announceTo(mob, level, t.offerPlayer(), fact, line);
    }

    /** Mantem a criatura perto de quem negocia (senao ela vagueia para fora do alcance de coleta). */
    private static void attend(Mob mob, ServerLevel level, UUID player) {
        ServerPlayer p = level.getServer().getPlayerList().getPlayer(player);
        if (p == null || p.level() != level || mob.isNoAi() || !Orders.canWalkFollow(mob)) {
            return;
        }
        if (mob.distanceTo(p) > 5.0) {
            mob.getNavigation().moveTo(p, 1.0);
        } else {
            mob.getNavigation().stop();
        }
    }

    private static void finish(Mob mob, ServerLevel level, MobTrades t, long now) {
        Recipe r = TradeCatalog.byId(t.txRecipe()).orElse(null);
        if (r == null) { // receita removida (save antigo): devolve o que foi reservado
            refund(mob, level, now);
            return;
        }
        Vec3 pos = mob.position().add(0, 0.6, 0);
        UUID p = UUID.fromString(t.txPlayer());
        spawn(level, r.outputItem(), r.outputCount() * t.txUnits(), pos, mob, p);
        deliveries.put(mob.getUUID(), new Delivery(r.outputItem(), p, now));
        set(mob, t.completed());
        MemoryStore.update(mob, m -> MemoryRules.trade(m, p, r.id(), now));
        MobVozMod.LOGGER.info("troca {} concluida ({} -> {})", t.txId(), r.id(), r.outputItem());
        String[] note = excessNote.remove(mob.getUUID());
        int total = r.outputCount() * t.txUnits();
        String delivered = "FATO PRINCIPAL (diga isso): você acabou de entregar ao jogador " + total + " " + Values.name(r.outputItem(), total != 1) + ", como combinado";
        String fact = delivered + (note != null ? ". Detalhe: " + note[0] : "")
                + (r.flavor().isEmpty() ? "" : "; se quiser, diga de onde veio (dica, com suas palavras: " + r.flavor() + ")");
        String line = (note != null ? note[1] + " " : "Combinado! ") + "Toma o que eu prometi." + (r.flavor().isEmpty() ? "" : " " + r.flavor());
        announceTo(mob, level, t.txPlayer(), fact, line);
    }

    static void refund(Mob mob, ServerLevel level, long now) {
        MobTrades t = get(mob);
        if (!t.hasTx() || t.isDone(t.txId())) {
            return;
        }
        for (var e : t.reservedMap().entrySet()) { // devolve EXATAMENTE o que foi reservado
            spawn(level, e.getKey(), e.getValue(), mob.position().add(0, 0.6, 0), mob, UUID.fromString(t.txPlayer()));
        }
        set(mob, t.refunded());
    }

    private static void spawn(ServerLevel level, String itemId, int count, Vec3 pos, Mob from, UUID target) {
        Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(itemId));
        ItemEntity drop = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(item, count));
        drop.setThrower(from);
        drop.setTarget(target); // protecao anti-furto por alguns segundos
        drop.setPickUpDelay(10);
        level.addFreshEntity(drop);
        protectedUntil.put(drop.getUUID(), level.getGameTime() + 200);
        shield(from, level.getGameTime());
    }

    /** A criatura comenta o que ACONTECEU na troca: fala gerada pela IA a partir do fato verificado; {@code fallback} so se a IA estiver fora. */
    private static void announceTo(Mob mob, ServerLevel level, String player, String fact, String fallback) {
        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(UUID.fromString(player));
        if (sp != null) {
            ConversationService.tradeLine(mob, sp, fact, fallback);
        }
    }

    private static String idOf(ItemStack s) {
        return BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
    }
}

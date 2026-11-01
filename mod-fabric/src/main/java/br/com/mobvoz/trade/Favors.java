package br.com.mobvoz.trade;

import br.com.mobvoz.MobVozMod;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.trade.Trades.OpenResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Pagamento por um favor. Quando uma criatura recusa um pedido (seguir, ficar, lutar, domar, tregua) so por falta de confianca, o servidor
 * abre um PEDAGIO: um preco em itens que ela de fato aceita (o que gosta ou quer, nunca o que odeia), pagavel aos poucos. Ela pega do chao,
 * so o necessario, anota o que ja recebeu e, quando o total chega ao preco, o pedido e cumprido. Prazo vencido = o pago volta ao jogador.
 * A IA so repete o preco que o servidor informou: nunca inventa o que a criatura quer.
 */
public final class Favors {
    static final long OFFER_TICKS = 12000; // 10 min
    private static final int TICK_EVERY = 10;
    private static final double PRICE_PER_TRUST = 24.0; // pontos por 1,0 de confianca que falta
    private static final double MIN_PRICE = 3.0;
    private static final int MAX_UNITS = 16; // o preco nunca passa de 16 do item mais valioso que ela aceita
    private static final double EPS = 1e-6;
    private static final double BOSS_FACTOR = 6.0;
    /** Para quem nao tem gosto nenhum no perfil: moedas que qualquer um aceita. */
    private static final List<String> CURRENCY = List.of("minecraft:diamond", "minecraft:emerald", "minecraft:gold_ingot", "minecraft:iron_ingot");

    private static final Set<UUID> tracked = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> notified = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<Integer>> noticed = new ConcurrentHashMap<>();

    private Favors() {
    }

    public static MobFavor get(Mob mob) {
        MobFavor f = mob.getAttached(MobFavor.TYPE);
        return f == null ? MobFavor.EMPTY : f;
    }

    static void set(Mob mob, MobFavor f) {
        mob.setAttached(MobFavor.TYPE, f);
        if (f.active()) {
            tracked.add(mob.getUUID());
            mob.setPersistenceRequired();
        } else {
            tracked.remove(mob.getUUID());
            notified.remove(mob.getUUID());
        }
    }

    public static int trackedCount() {
        return tracked.size();
    }

    /** A recusa foi so falta de confianca (o unico motivo que dinheiro resolve)? */
    public static boolean trustRefusal(OrderPolicy.Decision d) {
        return d.verdict() == OrderPolicy.Verdict.REFUSED && d.reason().contains("confia");
    }

    // ---- preco e itens ----

    /** O que paga: o que ela gosta e o que ela quer, menos o que odeia; sem gosto nenhum, moedas. */
    static List<String> payItems(Profiles.Profile p) {
        Set<String> s = new LinkedHashSet<>();
        s.addAll(p.wants());
        s.addAll(p.likes());
        s.removeAll(p.dislikes());
        s.removeIf(i -> Values.value(i) <= 0 || i.endsWith("_pickaxe") || i.endsWith("_sword"));
        if (s.isEmpty()) {
            return CURRENCY;
        }
        return s.stream().sorted((a, b) -> {
            int c = Double.compare(Values.value(b), Values.value(a));
            return c != 0 ? c : a.compareTo(b);
        }).toList();
    }

    /** Preco em pontos: cresce com a confianca que falta, no minimo {@link #MIN_PRICE} e no maximo 16 do item mais valioso aceito. */
    static double priceFor(Profiles.Profile p, double gap) {
        double top = Values.value(payItems(p).get(0));
        return Math.min(Math.max(MIN_PRICE, gap * PRICE_PER_TRUST), MAX_UNITS * top);
    }

    /** Ate 3 jeitos de pagar o valor {@code points}, em itens ("16 ossos", "27 flechas"); so quantidades razoaveis. */
    static String examples(Profiles.Profile p, double points) {
        List<String> out = new ArrayList<>();
        for (String i : payItems(p)) {
            int n = (int) Math.ceil(points / Values.value(i) - EPS);
            if (n >= 1 && n <= 40 && out.size() < 3) {
                out.add(n + " " + Values.name(i, n != 1));
            }
        }
        if (out.isEmpty()) {
            String top = payItems(p).get(0);
            int n = (int) Math.ceil(points / Values.value(top) - EPS);
            out.add(n + " " + Values.name(top, n != 1));
        }
        return String.join(", ou ", out);
    }

    static String describe(String action) {
        return switch (action) {
            case "FOLLOW_PLAYER" -> "seguir o jogador";
            case "STAY_AT" -> "ficar de guarda onde o jogador mandar";
            case "DEFEND_PLAYER" -> "lutar pelo jogador";
            case "TAME_ME" -> "ser do jogador (aceitar ser domada)";
            case "CEASE_HOSTILITY" -> "baixar a guarda e fazer trégua";
            default -> "fazer o pedido";
        };
    }

    // ---- abrir / consultar ----

    /**
     * Server thread. A criatura recusou {@code action} por falta de confianca: abre (ou continua) o pedagio. Retorna null se nao se aplica
     * (desligado, ou ela nao confia nem um pouco: ai nem por dinheiro).
     */
    public static OpenResult open(Mob mob, ServerPlayer player, String action, double warmth) {
        if (!ServerConfig.current().favorPayments || warmth < -0.3) {
            return null;
        }
        var subject = Orders.subjectOf(mob);
        Profiles.Profile prof = Profiles.of(Profiles.keyOf(mob));
        String pid = player.getUUID().toString();
        long now = player.level().getGameTime();
        MobFavor cur = get(mob);
        if (cur.active() && cur.action().equals(action) && cur.player().equals(pid) && now < cur.expires()) {
            double left = Math.max(0, cur.price() - cur.paid());
            return new OpenResult("JA_ATIVO", status(prof, action, cur, left));
        }
        if (cur.active() && cur.paid() > 0 && mob.level() instanceof ServerLevel lv) {
            refund(mob, lv); // trocou de pedido ou de jogador: devolve o que tinha sido pago
        }
        double gap = Math.max(0.05, OrderPolicy.trustNeeded(action, subject) - warmth);
        double price = priceFor(prof, gap) * (subject.boss() ? BOSS_FACTOR : 1); // chefe cobra caro: Warden uns 6 diamantes, Wither um bloco de diamante
        MobFavor f = new MobFavor(action, pid, now + OFFER_TICKS, (int) Math.round(price * 10), 0, List.of());
        set(mob, f);
        return new OpenResult("CONDICIONAL", "ainda não confia o bastante para " + describe(action) + ", MAS aceita ser paga para mudar de ideia: o preço é o equivalente a "
                + examples(prof, price) + "; é só jogar no chão perto dela; pode pagar aos poucos, ela guarda o que já recebeu por 10 minutos e só pega o necessário");
    }

    private static String status(Profiles.Profile prof, String action, MobFavor f, double left) {
        long pct = Math.min(100, Math.round(100 * f.paid() / f.price()));
        return "para " + describe(action) + " ela já recebeu " + pct + "% do pagamento combinado; falta o equivalente a " + examples(prof, Math.max(left, 0.1))
                + " (jogado no chão perto dela; ela só pega o necessário)";
    }

    /** O que o jogador DISSE que pagaria (nada e entregue ainda). Null se nao ha pedagio aberto com ele. */
    public static String evaluate(Mob mob, UUID player, Map<String, Integer> offered) {
        MobFavor f = get(mob);
        if (!f.active() || !f.player().equals(player.toString()) || offered.isEmpty()) {
            return null;
        }
        Profiles.Profile p = Profiles.of(Profiles.keyOf(mob));
        List<String> pay = payItems(p);
        double left = Math.max(0, f.price() - f.paid());
        double total = 0;
        List<String> parts = new ArrayList<>();
        for (var e : offered.entrySet()) {
            String name = e.getValue() + " " + Values.name(e.getKey(), e.getValue() != 1);
            if (pay.contains(e.getKey())) {
                total += Values.value(e.getKey()) * e.getValue();
                parts.add(name + ": serve como pagamento");
            } else if (p.dislikes().contains(e.getKey())) {
                parts.add(name + ": ela detesta isso");
            } else {
                parts.add(name + ": não interessa como pagamento");
            }
        }
        String end = total <= 0 ? "nada disso paga; ela aceitaria " + examples(p, Math.max(left, 0.1))
                : total + EPS >= left ? "o que serve já cobre o que falta; ela só pega o necessário (" + examples(p, Math.max(left, 0.1)) + ") e o resto fica com o jogador"
                : "o que serve cobre uns " + Math.round(100 * total / Math.max(left, 0.1)) + "% do que falta; falta o equivalente a " + examples(p, left - total);
        return String.join("; ", parts) + "; " + end + "; nada foi entregue ainda: é só jogar no chão perto dela";
    }

    // ---- ciclo no mundo ----

    public static void register() {
        MobFavor.TYPE.identifier(); // registra o anexo persistente antes de carregar qualquer criatura salva
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Mob m && get(m).active()) {
                tracked.add(m.getUUID());
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            tracked.remove(entity.getUUID());
            noticed.remove(entity.getUUID());
            notified.remove(entity.getUUID());
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof Mob m && get(m).active() && m.level() instanceof ServerLevel level) {
                refund(m, level); // morreu com o pagamento na mao: o que recebeu cai no chao
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % TICK_EVERY != 0 || tracked.isEmpty()) {
                return;
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

    /** O jogador feriu quem cobrava: o pedagio acaba na hora e o que foi pago volta. */
    public static void onAttacked(Mob mob, ServerPlayer player) {
        MobFavor f = get(mob);
        if (f.active() && f.player().equals(player.getUUID().toString()) && mob.level() instanceof ServerLevel level) {
            refund(mob, level);
        }
    }

    /** Cancela o pedagio (por exemplo, o pedido passou a ser aceito de graca) devolvendo o que ja foi pago. */
    public static void cancel(Mob mob) {
        if (get(mob).active() && mob.level() instanceof ServerLevel level) {
            refund(mob, level);
        }
    }

    static void tickMob(MinecraftServer server, ServerLevel level, Mob mob) {
        MobFavor f = get(mob);
        if (!f.active()) {
            tracked.remove(mob.getUUID());
            return;
        }
        long now = level.getGameTime();
        Profiles.Profile prof = Profiles.of(Profiles.keyOf(mob));
        if (now >= f.expires()) {
            boolean paid = f.paid() > 0;
            String who = f.player();
            String action = f.action();
            refund(mob, level);
            Trades.announceTo(mob, level, who, "o prazo do pagamento para " + describe(action) + " acabou" + (paid ? " e você devolveu ao jogador tudo o que ele tinha pago" : "")
                    + "; você não vai mais fazer isso a não ser que ele peça de novo e pague", "O prazo acabou, então esquece" + (paid ? ". Tá aí de volta o que você deu." : "."));
            return;
        }
        UUID player = UUID.fromString(f.player());
        ServerPlayer sp = server.getPlayerList().getPlayer(player);
        if (sp == null || sp.level() != level) {
            return;
        }
        List<ItemEntity> mine = level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(Trades.SCAN_RADIUS),
                e -> e.isAlive() && e.getOwner() != null && e.getOwner().getUUID().equals(player));
        List<String> pay = payItems(prof);
        List<ItemEntity> good = new ArrayList<>();
        for (ItemEntity e : mine) {
            String id = Trades.idOf(e.getItem());
            if (pay.contains(id)) {
                good.add(e);
            } else if (e.tickCount > 20 && noticed.computeIfAbsent(mob.getUUID(), k -> new HashSet<>()).add(e.getId())) {
                notifyUninterested(mob, level, prof, id, e.getItem().getCount(), f);
            }
        }
        if (good.isEmpty()) {
            Trades.attend(mob, level, player);
            return;
        }
        good.sort((a, b) -> Float.compare(a.distanceTo(mob), b.distanceTo(mob)));
        ItemEntity nearest = good.get(0);
        boolean stationary = mob.isNoAi() || !Orders.canWalkFollow(mob);
        boolean reached = nearest.distanceTo(mob) <= Trades.PICKUP_REACH || stationary && nearest.distanceTo(mob) <= 3.5;
        if (!reached) {
            if (!stationary) {
                mob.getNavigation().moveTo(nearest.getX(), nearest.getY(), nearest.getZ(), 1.1);
            }
            return;
        }
        mob.getNavigation().stop();
        double remaining = f.price() - f.paid();
        Map<String, Integer> took = new HashMap<>();
        double gained = 0;
        for (ItemEntity e : good) {
            if (remaining - gained <= EPS) {
                break;
            }
            String id = Trades.idOf(e.getItem());
            double unit = Values.value(id);
            int n = Math.min(e.getItem().getCount(), (int) Math.ceil((remaining - gained - EPS) / unit));
            if (n <= 0) {
                continue;
            }
            if (n >= e.getItem().getCount()) {
                e.discard();
            } else {
                e.getItem().shrink(n);
            }
            took.merge(id, n, Integer::sum);
            gained += n * unit;
        }
        if (took.isEmpty()) {
            return;
        }
        MobFavor nf = f.withPayment((int) Math.round(gained * 10), took);
        set(mob, nf);
        String tookText = took.entrySet().stream().map(e -> e.getValue() + " " + Values.name(e.getKey(), e.getValue() != 1)).collect(Collectors.joining(" e "));
        if (nf.paid() + EPS >= nf.price()) {
            complete(mob, level, sp, nf, tookText, prof);
            return;
        }
        long pct = Math.round(100 * nf.paid() / nf.price());
        int sig = nf.paidTenths();
        if (!Integer.valueOf(sig).equals(notified.get(mob.getUUID()))) {
            notified.put(mob.getUUID(), sig);
            String missing = examples(prof, nf.price() - nf.paid());
            Trades.announceTo(mob, level, f.player(), "o jogador acabou de te pagar " + tookText + ", para você " + describe(f.action()) + "; já são " + pct
                    + "% do combinado e ainda falta o equivalente a " + missing + "; você guarda o que recebeu e ainda NÃO aceitou o pedido",
                    "Recebi " + tookText + ". Ainda falta " + missing + ".");
        }
    }

    /** Pagamento completo: aplica o pedido (a confianca extra e o pagamento) e conta o que aconteceu. */
    private static void complete(Mob mob, ServerLevel level, ServerPlayer sp, MobFavor f, String lastTook, Profiles.Profile prof) {
        long now = level.getGameTime();
        var mem = MemoryStore.get(mob);
        var decisions = Orders.handle(mob, sp, List.of(f.action()), Orders.kinshipBelieved(mem, sp.getUUID()), 0, true); // o pagamento cobre a confianca que faltava
        var d = decisions.isEmpty() ? null : decisions.get(0);
        String who = f.player();
        String paidText = f.takenMap().entrySet().stream().map(e -> e.getValue() + " " + Values.name(e.getKey(), e.getValue() != 1)).collect(Collectors.joining(" e "));
        if (d != null && (d.accepted() || d.verdict() == OrderPolicy.Verdict.ALREADY)) {
            set(mob, MobFavor.EMPTY);
            MemoryStore.update(mob, m -> MemoryRules.trade(m, sp.getUUID(), "favor:" + f.action(), now));
            MobVozMod.LOGGER.info("favor {} pago ({})", f.action(), mob.getType().getDescriptionId());
            Trades.announceTo(mob, level, who, "FATO PRINCIPAL: o jogador terminou de pagar (" + paidText + ") e você ACEITOU: a partir de agora você vai " + describe(f.action())
                    + "; diga isso com as suas palavras, sem repetir a conta", "Pagou direitinho. Tá fechado.");
        } else {
            String why = d == null ? "não deu para fazer" : d.reason();
            MobVozMod.LOGGER.info("favor {} pago, mas nao cumprido: {}", f.action(), d == null ? "sem decisao" : d.verdict() + " " + d.reason());
            refund(mob, level);
            Trades.announceTo(mob, level, who, "o jogador pagou, mas você não pode " + describe(f.action()) + " (" + why + "); por isso devolveu tudo o que ele tinha pago",
                    "Não dá, mesmo assim. Toma de volta o que você pagou.");
        }
    }

    /** Devolve ao chao exatamente o que foi recebido e encerra o pedagio. */
    static void refund(Mob mob, ServerLevel level) {
        MobFavor f = get(mob);
        if (!f.active()) {
            return;
        }
        UUID p = UUID.fromString(f.player());
        for (var e : f.takenMap().entrySet()) {
            Trades.spawn(level, e.getKey(), e.getValue(), mob.position().add(0, 0.6, 0), mob, p);
        }
        set(mob, MobFavor.EMPTY);
    }

    private static void notifyUninterested(Mob mob, ServerLevel level, Profiles.Profile prof, String item, int count, MobFavor f) {
        String name = Values.name(item, count != 1);
        boolean hates = prof.dislikes().contains(item);
        String missing = examples(prof, Math.max(0.1, f.price() - f.paid()));
        Trades.announceTo(mob, level, f.player(), "o jogador largou " + count + " " + name + " no chão perto de você, mas " + (hates ? "você detesta isso" : "isso não serve como pagamento para você")
                + "; você não pegou; o que você aceitaria é o equivalente a " + missing,
                prof.address() + ", " + name + (hates ? " eu detesto." : " não me servem.") + " Prefiro " + missing + ".");
    }
}

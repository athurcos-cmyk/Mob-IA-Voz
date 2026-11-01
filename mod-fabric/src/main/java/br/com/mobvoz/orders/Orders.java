package br.com.mobvoz.orders;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.orders.OrderPolicy.Decision;
import br.com.mobvoz.orders.OrderPolicy.Subject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import br.com.mobvoz.profile.Profiles;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Execucao REAL das ordens no mundo (thread do servidor): le/grava o anexo {@link MobOrders}, conduz a navegacao
 * (seguir/ficar), segura portais (via mixin) e mantem tregua. Adaptador de movimento: so mobs a pe com IA por "goals";
 * os demais recebem recusa narrativa honesta (UNSUPPORTED) ate terem adaptador proprio (fases 10/12).
 */
public final class Orders {
    private static final int TICK_EVERY = 10;
    private static final double FOLLOW_STOP = 3.0;
    private static final double FOLLOW_TELEPORT = 20.0;
    private static final double STAY_RADIUS = 2.5;

    /** Mobs carregados com alguma ordem ativa (evita varrer o mundo). */
    /** Alvo de defesa reafirmado pelo ticker (a IA de alvo do mob o limparia em poucos ticks). */
    private record Rally(UUID target, long until) {
    }

    private static final long RALLY_TICKS = 400;
    private static final java.util.Map<UUID, Rally> rallied = new ConcurrentHashMap<>();
    private static final Set<UUID> tracked = ConcurrentHashMap.newKeySet();

    private Orders() {
    }

    public static MobOrders get(Mob mob) {
        MobOrders o = mob.getAttached(MobOrders.TYPE);
        return o == null ? MobOrders.NONE : o;
    }

    public static void set(Mob mob, MobOrders o) {
        mob.setAttached(MobOrders.TYPE, o);
        if (o.active()) {
            tracked.add(mob.getUUID());
            mob.setPersistenceRequired(); // quem tem dono nao some por despawn
        } else {
            tracked.remove(mob.getUUID());
        }
    }

    public static boolean portalHeld(Mob mob) {
        MobOrders o = mob.getAttached(MobOrders.TYPE);
        return o != null && o.portalHold();
    }

    public static int trackedCount() {
        return tracked.size();
    }

    // ---- adaptador de movimento ----

    public static boolean canWalkFollow(Mob mob) {
        // piglins, hoglins, sapo, cabra, camelo e sniffer (IA por brain) agora seguem pelo adaptador do brain (BossAdapter.usesBrain); ficam de fora quem nada
        // (WaterAnimal, axolote), quem voa (Allay, FlyingAnimal; o Ghast nem e PathfinderMob), os aldeoes (rotina da vila) e os chefes (BossAdapter)
        return mob instanceof PathfinderMob && !(mob instanceof AbstractVillager || mob instanceof Warden || mob instanceof Allay
                || mob instanceof Axolotl || mob instanceof WaterAnimal || mob instanceof FlyingAnimal || mob instanceof WitherBoss);
    }

    public static Subject subjectOf(Mob mob) {
        return new Subject(Profiles.keyOf(mob), canWalkFollow(mob) || BossAdapter.canFollow(mob), mob instanceof Enemy,
                mob.getAttribute(Attributes.ATTACK_DAMAGE) != null || mob instanceof EnderDragon, BossAdapter.isBoss(mob), tameable(mob));
    }

    /** Cavalos, burros, mulas, lhamas (nao o camelo) e os domesticaveis (lobo, gato, papagaio). */
    static boolean tameable(Mob mob) {
        return mob instanceof net.minecraft.world.entity.animal.horse.AbstractHorse && !(mob instanceof Camel)
                || mob instanceof net.minecraft.world.entity.TamableAnimal;
    }

    // ---- aplicacao de pedidos ----

    /** Decide e aplica cada pedido (server thread). Persiste ANTES de qualquer fala sobre o resultado. */
    public static List<Decision> handle(Mob mob, ServerPlayer player, List<String> requested, boolean kinshipBelieved) {
        return handle(mob, player, requested, kinshipBelieved, 0);
    }

    /** {@code appealBonus}: calor extra vindo de um bom argumento do jogador (ver {@link OrderPolicy#appealBonus}). */
    public static List<Decision> handle(Mob mob, ServerPlayer player, List<String> requested, boolean kinshipBelieved, double appealBonus) {
        long tick = player.level().getGameTime();
        MobMemory mem = MemoryStore.get(mob);
        Subject subject = subjectOf(mob);
        String pid = player.getUUID().toString();
        List<Decision> out = new ArrayList<>();
        for (String action : OrderPolicy.filter(requested)) {
            MobOrders cur = get(mob);
            boolean otherOnline = !cur.owner().isEmpty() && !cur.owner().equals(pid) && isOnline(player.getServer(), cur.owner());
            var cfg = ServerConfig.current();
            if (action.equals("TAME_ME") && alreadyTamed(mob) != null) { // ja tem dono (vanilla): nao ha o que decidir
                out.add(alreadyTamed(mob).equals(player.getUUID()) ? new Decision(action, OrderPolicy.Verdict.ALREADY, "ja e sua")
                        : new Decision(action, OrderPolicy.Verdict.REFUSED, "ja tem dono"));
                continue;
            }
            var rel0 = mem.relationOf(player.getUUID());
            var rel = appealBonus > 0 ? new br.com.mobvoz.memory.MobMemory.Relationship(rel0.trust() + (float) (2 * appealBonus), rel0.affinity(), rel0.fear()) : rel0;
            Decision d = OrderPolicy.decide(action, subject, rel, kinshipBelieved, cur, pid, otherOnline, tick,
                    new OrderPolicy.Rules(cfg.bossContractsEnabled, cfg.maxActiveFollowersPerPlayer, followersOf(player)));
            if (d.accepted() && action.equals("TAME_ME")) {
                tame(mob, player);
                MemoryStore.update(mob, m -> MemoryRules.tamed(m, player.getUUID(), tick));
            }
            if (d.accepted()) {
                String dim = mob.level().dimension().location().toString();
                set(mob, OrderPolicy.apply(d, cur, pid, dim, mob.getX(), mob.getY(), mob.getZ(), tick));
                if (action.equals("DISMISS_ALLY")) {
                    mob.getNavigation().stop();
                }
            }
            out.add(d);
        }
        return out;
    }

    /** Aliados carregados deste jogador (seguindo ou defendendo). */
    public static List<Mob> alliesOf(ServerPlayer p) {
        String id = p.getUUID().toString();
        List<Mob> out = new ArrayList<>();
        for (UUID u : tracked) {
            for (ServerLevel l : p.getServer().getAllLevels()) {
                if (l.getEntity(u) instanceof Mob m && m.isAlive()) {
                    MobOrders o = get(m);
                    if (o.owner().equals(id) && (o.mode() == MobOrders.Mode.FOLLOW || o.defend())) {
                        out.add(m);
                    }
                    break;
                }
            }
        }
        return out;
    }

    /** Dono atual (vanilla) se a criatura ja e domada; null se nao. */
    static UUID alreadyTamed(Mob mob) {
        if (mob instanceof net.minecraft.world.entity.animal.horse.AbstractHorse h && h.isTamed()) {
            return h.getOwnerReference() != null ? h.getOwnerReference().getUUID() : new UUID(0, 0);
        }
        if (mob instanceof net.minecraft.world.entity.TamableAnimal t && t.isTame()) {
            return t.getOwnerReference() != null ? t.getOwnerReference().getUUID() : new UUID(0, 0);
        }
        return null;
    }

    /** Domestica de verdade (mecanica do jogo): o jogador vira dono; cavalos passam a poder ser montados sem maca. */
    static void tame(Mob mob, ServerPlayer player) {
        if (mob instanceof net.minecraft.world.entity.animal.horse.AbstractHorse h) {
            h.tameWithName(player);
        } else if (mob instanceof net.minecraft.world.entity.TamableAnimal t) {
            t.tame(player);
        }
    }

    /** Aliados ativos (seguindo/defendendo) deste jogador, entre as criaturas carregadas e rastreadas. */
    static int followersOf(ServerPlayer p) {
        String id = p.getUUID().toString();
        int n = 0;
        for (UUID u : tracked) {
            for (ServerLevel l : p.getServer().getAllLevels()) {
                if (l.getEntity(u) instanceof Mob m) {
                    MobOrders o = get(m);
                    if (o.owner().equals(id) && (o.mode() == MobOrders.Mode.FOLLOW || o.defend())) {
                        n++;
                    }
                    break;
                }
            }
        }
        return n;
    }

    private static boolean isOnline(MinecraftServer server, String uuid) {
        try {
            return server.getPlayerList().getPlayer(UUID.fromString(uuid)) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Alguma alegacao de parentesco ACREDITADA (BELIEVES) por este jogador? Soma ao "calor" do pedido. */
    public static boolean kinshipBelieved(MobMemory mem, UUID player) {
        return mem.claims().stream().anyMatch(c -> c.player().equals(player) && c.stance() == MobMemory.Stance.BELIEVES
                && MemoryRules.isKinship(c.text()));
    }

    // ---- ticker ----

    /** O dono (ou aliado dele) NAO e alvo de dano de uma criatura que o serve (evita dano amigo, ex.: creeper aliado). */
    static boolean isFriendlyFire(Entity attacker, Entity victim) {
        if (!(attacker instanceof Mob a)) {
            return false;
        }
        if (victim instanceof ServerPlayer p) { // conversando ou negociando: ninguem ataca ninguem, aliado ou nao
            long now = a.level().getGameTime();
            if (br.com.mobvoz.social.Parley.protects(a, p, now) || br.com.mobvoz.trade.Trades.negotiatingWith(a, p, now)) {
                return true;
            }
        }
        MobOrders ao = get(a);
        if (ao.owner().isEmpty() && ao.truceFor().isEmpty()) {
            return false;
        }
        if (victim instanceof ServerPlayer p) {
            String id = p.getUUID().toString();
            long now = a.level().getGameTime();
            return ao.owner().equals(id) || (ao.truceFor().equals(id) && now < ao.truceUntil());
        }
        return victim instanceof Mob v && !ao.owner().isEmpty() && ao.owner().equals(get(v).owner());
    }

    /** Chefe sob contrato (aliado ou em tregua) nao destroi blocos, salvo se o admin ligou {@code bossDestructionOnAlliance}. */
    public static boolean restrictsGriefing(Mob m) {
        if (ServerConfig.current().bossDestructionOnAlliance) {
            return false;
        }
        MobOrders o = m.getAttached(MobOrders.TYPE);
        return o != null && (o.mode() != MobOrders.Mode.NONE || o.defend() || o.truceUntil() > m.level().getGameTime());
    }

    private static ServerPlayer playerOrNull(MinecraftServer server, String uuid) {
        try {
            return server.getPlayerList().getPlayer(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isAlly(Mob m, ServerPlayer owner) {
        MobOrders o = get(m);
        return o.owner().equals(owner.getUUID().toString()) && (o.mode() != MobOrders.Mode.NONE || o.defend());
    }

    public static void register() {
        // dano amigo bloqueado; ferir quem tem tregua com voce rompe o pacto
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !isFriendlyFire(source.getEntity(), entity));
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
            if (taken <= 0) {
                return;
            }
            // 1) jogador atacado por uma criatura: defensores do jogador atacam o agressor
            if (entity instanceof ServerPlayer p && source.getEntity() instanceof LivingEntity attacker && !(attacker instanceof ServerPlayer)) {
                rally(p, attacker);
            }
            // 2) quebra de pacto: o jogador fere quem tem tregua com ele
            if (entity instanceof Mob m && source.getEntity() instanceof ServerPlayer p) {
                br.com.mobvoz.trade.Trades.onAttacked(m, p);
                br.com.mobvoz.social.Parley.end(m); // ferir quem conversa acaba com a conversa
                MobOrders o = get(m);
                if (o.truceUntil() > m.level().getGameTime() && o.truceFor().equals(p.getUUID().toString())) {
                    long now = m.level().getGameTime();
                    set(m, o.truceBroken(now + 6000));
                    MemoryStore.update(m, mem -> MemoryRules.pactBroken(mem, p.getUUID(), now));
                }
                // traicao por gatilho: ferir o chefe que o serve rompe o contrato e ele se volta contra quem o feriu
                MobOrders cur = get(m);
                if (BossAdapter.isBoss(m) && cur.owner().equals(p.getUUID().toString()) && (cur.mode() != MobOrders.Mode.NONE || cur.defend())) {
                    long now = m.level().getGameTime();
                    set(m, cur.dismissed());
                    rallied.remove(m.getUUID());
                    MemoryStore.update(m, mem -> MemoryRules.pactBroken(mem, p.getUUID(), now));
                    BossAdapter.attack(m, p);
                }
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Mob m && get(m).active()) {
                tracked.add(m.getUUID());
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> tracked.remove(entity.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % TICK_EVERY == 0 && !tracked.isEmpty()) {
                tickAll(server);
            }
        });
    }

    /** Defensores do jogador (ordem DEFEND_PLAYER, dono = jogador) no raio miram o agressor, nunca outro aliado. */
    static void rally(ServerPlayer owner, LivingEntity attacker) {
        if (attacker instanceof Mob am && isAlly(am, owner)) {
            return;
        }
        java.util.Set<Mob> defenders = new java.util.LinkedHashSet<>(owner.level().getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(24),
                x -> x != attacker && x.isAlive()));
        for (Mob a : alliesOf(owner)) { // o Dragao voa longe do dono: vale onde estiver, na mesma dimensao
            if (a instanceof EnderDragon && a.level() == owner.level() && a != attacker) {
                defenders.add(a);
            }
        }
        for (Mob m : defenders) {
            MobOrders o = get(m);
            if (o.defend() && o.owner().equals(owner.getUUID().toString()) && !m.isNoAi()) {
                BossAdapter.attack(m, attacker);
                rallied.put(m.getUUID(), new Rally(attacker.getUUID(), m.level().getGameTime() + RALLY_TICKS));
            }
        }
    }

    private static void tickAll(MinecraftServer server) {
        for (UUID id : List.copyOf(tracked)) {
            Mob mob = null;
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getEntity(id) instanceof Mob m) {
                    mob = m;
                    break;
                }
            }
            if (mob == null || !mob.isAlive()) {
                continue; // descarregado/morto: ENTITY_UNLOAD limpa; ao recarregar volta pelo ENTITY_LOAD
            }
            tickMob(server, mob);
        }
    }

    static void tickMob(MinecraftServer server, Mob mob) {
        long now = mob.level().getGameTime();
        MobOrders o = get(mob);
        Rally rally = rallied.get(mob.getUUID());
        if (rally != null) {
            Entity t = ((ServerLevel) mob.level()).getEntity(rally.target());
            if (now >= rally.until() || !(t instanceof LivingEntity le) || !le.isAlive() || !o.defend()) {
                rallied.remove(mob.getUUID());
            } else if (mob.getTarget() != le || mob instanceof EnderDragon) {
                BossAdapter.attack(mob, le);
            }
        }
        // tregua
        if (o.truceUntil() > 0) {
            if (now >= o.truceUntil()) {
                set(mob, o.withTruce("", 0, o.truceCooldownUntil()));
                o = get(mob);
            } else if (BossAdapter.isBoss(mob)) {
                ServerPlayer tp = playerOrNull(server, o.truceFor());
                if (tp != null && !rallied.containsKey(mob.getUUID())) {
                    BossAdapter.pacify(mob, tp);
                }
            } else if (mob.getTarget() instanceof ServerPlayer tp && tp.getUUID().toString().equals(o.truceFor())) {
                mob.setTarget(null);
                if (mob instanceof Creeper c) {
                    c.setSwellDir(-1);
                }
            }
        }
        if (o.mode() == MobOrders.Mode.NONE) {
            return;
        }
        ServerPlayer owner = null;
        try {
            owner = server.getPlayerList().getPlayer(UUID.fromString(o.owner()));
        } catch (IllegalArgumentException ignored) {
            // dono invalido: tratado como ausente
        }
        if (owner != null) {
            if (now - o.ownerLastSeen() > 100) {
                set(mob, o = o.withOwnerSeen(now));
            }
        } else if (now - o.ownerLastSeen() > OrderPolicy.OWNER_ABSENT_TIMEOUT) {
            set(mob, o.dismissed()); // dono sumiu por muito tempo: ordem expira (nao fica parado para sempre)
            return;
        }
        if (mob.isNoAi()) {
            return;
        }
        if (owner != null && (BossAdapter.isBoss(mob) || BossAdapter.usesBrain(mob)) && !rallied.containsKey(mob.getUUID())) {
            BossAdapter.pacify(mob, owner); // o aliado nunca mira quem o comanda
        }
        if (o.mode() == MobOrders.Mode.FOLLOW && owner != null && owner.level() == mob.level()) {
            double d = mob.distanceTo(owner);
            if (d > FOLLOW_TELEPORT) {
                safeTeleportNear(owner, mob);
                if (mob.distanceTo(owner) > FOLLOW_TELEPORT) {
                    BossAdapter.moveToEntity(mob, owner); // sem chao seguro (voador): continua indo ate o dono
                }
            } else if (d > FOLLOW_STOP) {
                BossAdapter.moveToEntity(mob, owner);
            } else {
                BossAdapter.stopMoving(mob);
            }
        } else if (o.mode() == MobOrders.Mode.STAY && mob.level().dimension().location().toString().equals(o.anchorDim())) {
            if (mob.position().distanceTo(new Vec3(o.ax(), o.ay(), o.az())) > STAY_RADIUS) {
                BossAdapter.moveToPos(mob, o.ax(), o.ay(), o.az());
            } else {
                BossAdapter.stopMoving(mob);
            }
        } else {
            BossAdapter.stopMoving(mob); // dono em outra dimensao / sem dono: mantem a posicao, sem teleporte involuntario
        }
    }

    /** Teleporte seguro (mesma dimensao): chao solido, sem liquido/lava, sem colisao. Falhando, nao teleporta. */
    static void safeTeleportNear(ServerPlayer owner, Mob mob) {
        ServerLevel level = (ServerLevel) mob.level();
        BlockPos base = owner.blockPosition();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 1; dy >= -2; dy--) {
                    BlockPos p = base.offset(dx, dy, dz);
                    if (Math.abs(dx) + Math.abs(dz) < 2) {
                        continue; // nao em cima do jogador
                    }
                    if (level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)
                            && level.getFluidState(p).isEmpty() && level.getFluidState(p.below()).isEmpty()
                            && level.noCollision(mob, mob.getBoundingBox().move(p.getX() + 0.5 - mob.getX(), p.getY() - mob.getY(),
                            p.getZ() + 0.5 - mob.getZ()))
                            && mob.getPathfindingMalus(PathType.DANGER_FIRE) >= 0
                            && level.getBlockState(p).getFluidState().isEmpty()) {
                        mob.getNavigation().stop();
                        mob.teleportTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
                        return;
                    }
                }
            }
        }
    }
}

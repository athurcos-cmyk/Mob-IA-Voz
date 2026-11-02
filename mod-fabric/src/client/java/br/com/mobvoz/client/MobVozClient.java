package br.com.mobvoz.client;

import br.com.mobvoz.MobVozMod;
import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.client.voice.VoiceUi;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.voice.MobVozVoicechatPlugin;
import br.com.mobvoz.target.TargetPicker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Fase 1: marca com uma pequena estrela UM mob sob a mira (ate 6 blocos, com linha de visao).
 * Qualquer {@link Mob} e candidato (requisito universal v2.1); sem chat, IA ou voz ainda.
 */
public final class MobVozClient implements ClientModInitializer {
    private static final ResourceLocation STAR =
            ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "textures/misc/star.png");

    /** Alvo atual; escrito no tick do cliente, lido no render (mesma thread). */
    private static Mob target;
    private static final br.com.mobvoz.target.StickyTarget sticky = new br.com.mobvoz.target.StickyTarget();
    private static java.util.UUID lastSentTarget;
    private static int lastSentTick;

    /** Alvo atual da mira, ou null. */
    public static Mob target() {
        return target;
    }

    private static volatile List<br.com.mobvoz.target.NameCall.Candidate> named = List.of();

    /** Criaturas com apelido ao alcance da voz (para chama-las pelo nome sem mirar). Atualizado a cada 10 ticks. */
    public static List<br.com.mobvoz.target.NameCall.Candidate> namedNearby() {
        return named;
    }

    private static void scanNamed(Minecraft mc) {
        if (mc.level == null || mc.player == null || mc.player.tickCount % 10 != 0) {
            return;
        }
        double r = br.com.mobvoz.target.NameCall.RANGE;
        named = mc.level.getEntitiesOfClass(Mob.class, mc.player.getBoundingBox().inflate(r), m -> m.isAlive() && m.hasCustomName()).stream()
                .map(m -> new br.com.mobvoz.target.NameCall.Candidate(m.getUUID(), m.getCustomName().getString(), m.distanceTo(mc.player)))
                .toList();
    }

    @Override
    public void onInitializeClient() {
        MobVozVoicechatPlugin.micFrameHook = VoiceInput::onFrame;
        MobVozVoicechatPlugin.entityAudioHook = VoiceInput::onEntityAudio;
        ClientPlayNetworking.registerGlobalReceiver(StatusPayload.TYPE,
                (payload, ctx) -> VoiceInput.setStatus(payload.state()));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((h, s, mc) -> {
            br.com.mobvoz.client.voice.VoiceConsent.sendToServer();
            if (!ClientPrefs.helpHintShown()) { // so na primeira vez
                ClientPrefs.setHelpHintShown();
                mc.execute(() -> mc.gui.getChat().addMessage(net.minecraft.network.chat.Component.literal(
                        "§6MobVoz IA:§r digite §e/mobvoz ajuda§r para ver como falar com as criaturas.")));
            }
        });
        br.com.mobvoz.client.ChatMode.register();
        VoiceUi.register();
        ClientTickEvents.END_CLIENT_TICK.register(MobVozClient::pickTarget);
        ClientTickEvents.END_CLIENT_TICK.register(MobVozClient::scanNamed);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> VoiceInput.tick());
        WorldRenderEvents.AFTER_ENTITIES.register(MobVozClient::renderStar);
    }

    private static void pickTarget(Minecraft mc) {
        target = null;
        VoiceInput.setTarget(null);
        Entity viewer = mc.getCameraEntity();
        if (mc.level == null || viewer == null || mc.screen != null || mc.player == null || mc.player.isSpectator()) {
            return;
        }
        Vec3 from = viewer.getEyePosition();
        Vec3 look = viewer.getViewVector(1.0F).scale(TargetPicker.RANGE);
        Vec3 to = from.add(look);
        // Linha de visao: blocos solidos encurtam o raio.
        var blockHit = mc.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer));
        if (blockHit.getType() != HitResult.Type.MISS) {
            to = blockHit.getLocation();
        }
        List<Mob> mobs = mc.level.getEntitiesOfClass(Mob.class,
                viewer.getBoundingBox().expandTowards(look).inflate(1.0),
                m -> m.isAlive() && !m.isSpectator() && !m.isInvisible());
        List<AABB> boxes = mobs.stream().map(m -> m.getBoundingBox().inflate(m.getPickRadius())).toList();
        int idx = TargetPicker.nearest(from, to, boxes);
        if (idx < 0) { // nenhum no raio exato: um cone pequeno ao redor da mira ajuda com bicho pequeno (galinha, morcego, peixe)
            idx = TargetPicker.nearestInCone(from, look, mobs.stream().map(m -> m.getBoundingBox().getCenter()).toList(), TargetPicker.CONE_DEGREES);
        }
        Mob aimed = idx >= 0 ? mobs.get(idx) : null;
        // folga: se a mira escapar (o bicho anda, o mouse mexe), o alvo anterior fica uns segundos, desde que perto e visivel
        Mob previous = sticky.current() == null ? null : mc.level.getEntitiesOfClass(Mob.class, mc.player.getBoundingBox().inflate(TargetPicker.STICKY_RANGE),
                m -> m.getUUID().equals(sticky.current()) && m.isAlive()).stream().findFirst().orElse(null);
        boolean valid = previous != null && mc.player.hasLineOfSight(previous);
        java.util.UUID chosen = sticky.choose(aimed == null ? null : aimed.getUUID(), mc.level.getGameTime(), valid);
        target = aimed != null ? aimed : (chosen != null ? previous : null);
        VoiceInput.setTarget(target == null ? null : target.getUUID());
        // avisa o servidor (1x por segundo) para ele segurar a criatura parada e perto: so para quem usa voz ou chat
        // (na hora em que a mira muda, sem esperar o proximo segundo: a fala pode vir logo em seguida e o servidor so aceita quem ele sabe que foi mirado)
        if (target != null && (ClientPrefs.voiceConsent() || ClientPrefs.chatMode()) && ClientPlayNetworking.canSend(br.com.mobvoz.net.AttentionPayload.TYPE)
                && (!target.getUUID().equals(lastSentTarget) || mc.player.tickCount - lastSentTick >= 20)) {
            lastSentTarget = target.getUUID();
            lastSentTick = mc.player.tickCount;
            ClientPlayNetworking.send(new br.com.mobvoz.net.AttentionPayload(target.getUUID()));
        }
    }

    private static void renderStar(WorldRenderContext ctx) {
        Mob mob = target;
        PoseStack pose = ctx.matrixStack();
        if (mob == null || pose == null || ctx.consumers() == null) {
            return;
        }
        float pt = ctx.tickCounter().getGameTimeDeltaPartialTick(false);
        Vec3 cam = ctx.camera().getPosition();
        double x = Mth.lerp(pt, mob.xo, mob.getX()) - cam.x;
        double y = Mth.lerp(pt, mob.yo, mob.getY()) - cam.y + mob.getBbHeight() + 0.55
                + 0.06 * Math.sin((mob.tickCount + pt) * 0.2);
        double z = Mth.lerp(pt, mob.zo, mob.getZ()) - cam.z;

        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(ctx.camera().rotation());
        var m = pose.last().pose();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderType.text(STAR));
        int light = LightTexture.FULL_BRIGHT;
        float h = 0.2F;
        vc.addVertex(m, -h, -h, 0).setColor(255, 255, 255, 255).setUv(0, 1).setLight(light);
        vc.addVertex(m, h, -h, 0).setColor(255, 255, 255, 255).setUv(1, 1).setLight(light);
        vc.addVertex(m, h, h, 0).setColor(255, 255, 255, 255).setUv(1, 0).setLight(light);
        vc.addVertex(m, -h, h, 0).setColor(255, 255, 255, 255).setUv(0, 0).setLight(light);
        pose.popPose();
    }
}

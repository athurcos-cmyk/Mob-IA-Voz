package br.com.mobvoz.server;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.entity.Mob;

/** Fala pelo {@code EntityAudioChannel} do Simple Voice Chat: o som sai da posicao real do mob. */
public final class SvcSpeaker implements Speaker {
    public static final String CATEGORY = "mobvoz";
    private static final int FRAME = 960; // 20 ms a 48 kHz
    private static final float HEARING_DISTANCE = 24f;

    private final VoicechatServerApi api;
    private final Map<UUID, AudioPlayer> active = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicBoolean> cancelled = new ConcurrentHashMap<>();

    public SvcSpeaker(VoicechatServerApi api) {
        this.api = api;
    }

    @Override
    public void speak(Mob mob, short[] pcm, Runnable onDone) {
        UUID id = mob.getUUID();
        cancel(mob);
        EntityAudioChannel channel = api.createEntityAudioChannel(UUID.randomUUID(), api.fromEntity(mob));
        if (channel == null) {
            onDone.run();
            return;
        }
        channel.setCategory(CATEGORY);
        channel.setDistance(HEARING_DISTANCE);
        OpusEncoder encoder = api.createEncoder();
        AtomicBoolean stop = new AtomicBoolean();
        cancelled.put(id, stop);
        int[] pos = {0};
        AudioPlayer player = api.createAudioPlayer(channel, encoder, () -> {
            if (stop.get() || mob.isRemoved() || !mob.isAlive() || pos[0] >= pcm.length) {
                return null; // fim da fala
            }
            short[] frame = Arrays.copyOfRange(pcm, pos[0], pos[0] + FRAME); // completa com silencio no ultimo
            pos[0] += FRAME;
            return frame;
        });
        player.setOnStopped(() -> {
            active.remove(id, player);
            cancelled.remove(id, stop);
            encoder.close();
            onDone.run();
        });
        active.put(id, player);
        player.startPlaying();
    }

    @Override
    public void cancel(Mob mob) {
        AtomicBoolean stop = cancelled.get(mob.getUUID());
        if (stop != null) {
            stop.set(true); // o supplier devolve null no proximo frame -> onStopped
        }
    }
}

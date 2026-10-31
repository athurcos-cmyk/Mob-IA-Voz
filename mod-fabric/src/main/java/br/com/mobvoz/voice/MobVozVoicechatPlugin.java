package br.com.mobvoz.voice;

import br.com.mobvoz.MobVozMod;
import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.server.SvcSpeaker;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Plugin do Simple Voice Chat (entrypoint "voicechat"), carregado pelo SVC em cliente e servidor.
 * Cliente: entrega frames do microfone e observa audio de entidade recebido (ganchos definidos pelo cliente).
 * Servidor: cria o {@link SvcSpeaker} quando o servidor de voz sobe. Nao altera nem cancela o audio dos jogadores.
 */
public final class MobVozVoicechatPlugin implements VoicechatPlugin {
    private static final String VOLUME_CATEGORY = SvcSpeaker.CATEGORY;

    /** Definido pelo codigo de cliente; no-op em servidor dedicado. */
    public static volatile Consumer<short[]> micFrameHook = f -> { };
    public static volatile BiConsumer<UUID, short[]> entityAudioHook = (id, f) -> { };

    @Override
    public String getPluginId() {
        return "mobvoz";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatClientApi client) {
            try {
                client.registerClientVolumeCategory(client.volumeCategoryBuilder()
                        .setId(VOLUME_CATEGORY).setName("MobVoz Mobs")
                        .setDescription("Fala dos mobs").build());
            } catch (RuntimeException e) {
                MobVozMod.LOGGER.warn("Categoria de volume do cliente nao registrada: {}", e.toString());
            }
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientSoundEvent.class, e -> micFrameHook.accept(e.getRawAudio()));
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class,
                e -> entityAudioHook.accept(e.getEntityId(), e.getRawAudio()));
        registration.registerEvent(VoicechatServerStartedEvent.class, e -> {
            var api = e.getVoicechat();
            api.registerVolumeCategory(api.volumeCategoryBuilder()
                    .setId(VOLUME_CATEGORY).setName("MobVoz Mobs")
                    .setDescription("Fala dos mobs").build());
            ConversationService.setSpeaker(new SvcSpeaker(api));
        });
        registration.registerEvent(VoicechatServerStoppedEvent.class, e -> ConversationService.setSpeaker(null));
    }
}

package br.com.mobvoz;

import br.com.mobvoz.memory.MemoryEvents;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.net.MobVozNet;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.knowledge.KnowledgeStore;
import br.com.mobvoz.social.Chatter;
import br.com.mobvoz.trade.Trades;
import br.com.mobvoz.server.ConversationService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MobVozMod implements ModInitializer {
    public static final String MOD_ID = "mobvoz";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerConfig.load();
        MobVozNet.register();
        MemoryStore.init();
        MemoryEvents.register();
        Orders.register();
        Trades.register();
        Chatter.register();
        br.com.mobvoz.social.Ambient.register();
        KnowledgeStore.register();
        br.com.mobvoz.social.Reactions.register();
        br.com.mobvoz.social.Parley.register();
        br.com.mobvoz.social.Events.register();
        br.com.mobvoz.social.WorldReactions.register();
        br.com.mobvoz.social.Initiative.register(); // a criatura puxa assunto de vez em quando
        br.com.mobvoz.server.MobVozCommands.register();
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ConversationService.reset();
            br.com.mobvoz.server.ConsentRegistry.clear();
        });
        LOGGER.info("MobVoz IA: carregado (fases 0-11: alvo, voz, IA local, fala espacial, memoria, ordens, troca fisica, perfis, conversa mob-mob, aliancas, memoria especial).");
    }
}


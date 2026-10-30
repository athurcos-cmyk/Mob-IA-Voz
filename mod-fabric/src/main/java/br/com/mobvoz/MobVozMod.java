package br.com.mobvoz;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MobVozMod implements ModInitializer {
    public static final String MOD_ID = "mobvoz";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("MobVoz IA: base Fabric carregada (fase 0).");
    }
}


package io.github.lgatodu47.testmod;

import io.github.lgatodu47.catconfig.CatConfig;
import io.github.lgatodu47.catconfigmc.MinecraftConfigSides;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestMod implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("Test Mod");
    public static CatConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = new MyCatConfig(MinecraftConfigSides.COMMON);
    }
}

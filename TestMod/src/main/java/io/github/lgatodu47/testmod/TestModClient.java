package io.github.lgatodu47.testmod;

import io.github.lgatodu47.catconfig.CatConfig;
import io.github.lgatodu47.catconfigmc.MinecraftConfigSides;
import net.fabricmc.api.ClientModInitializer;

public class TestModClient implements ClientModInitializer {
    // This is just an example, never store fields like that in your main mod class
    public static CatConfig CONFIG;

    @Override
    public void onInitializeClient() {
        CONFIG = new MyCatConfig(MinecraftConfigSides.CLIENT);
    }
}

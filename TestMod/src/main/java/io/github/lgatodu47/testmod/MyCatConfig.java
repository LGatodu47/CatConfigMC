package io.github.lgatodu47.testmod;

import io.github.lgatodu47.catconfig.CatConfig;
import io.github.lgatodu47.catconfig.CatConfigLogger;
import io.github.lgatodu47.catconfig.ConfigOptionAccess;
import io.github.lgatodu47.catconfig.ConfigSide;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

class MyCatConfig extends CatConfig {
    private static final Logger LOGGER = LogManager.getLogger("TestMod Config");

    public MyCatConfig(ConfigSide side) {
        super(side, "testmod", CatConfigLogger.delegate(LOGGER));
    }

    @Override
    protected @NotNull ConfigOptionAccess getConfigOptions() {
        return MyConfigOptions.OPTIONS;
    }

    @Override
    protected @NotNull Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    protected @Nullable ConfigWatcher makeAndStartConfigWatcherThread() {
        return new ConfigWatcher("TestMod Config Watcher");
    }
}

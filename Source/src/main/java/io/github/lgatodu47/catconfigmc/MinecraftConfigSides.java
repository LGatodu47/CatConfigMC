package io.github.lgatodu47.catconfigmc;

import io.github.lgatodu47.catconfig.ConfigSide;

import java.util.Locale;

/**
 * Implementation of {@link ConfigSide} for Minecraft sides.
 */
public enum MinecraftConfigSides implements ConfigSide {
    CLIENT,
    SERVER,
    COMMON;

    @Override
    public String sideName() {
        return name().toLowerCase(Locale.ROOT);
    }
}

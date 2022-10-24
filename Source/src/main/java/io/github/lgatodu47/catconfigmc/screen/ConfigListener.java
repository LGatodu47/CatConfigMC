package io.github.lgatodu47.catconfigmc.screen;

@FunctionalInterface
public interface ConfigListener {
    /**
     * Method called when changes are written to the config in the ModConfigScreen.
     */
    void configUpdated();

    /**
     * Combines multiple ConfigListeners into one.
     * @param listeners The listeners to be combined.
     * @return A ConfigListener that notifies all the given listeners.
     */
    static ConfigListener combine(ConfigListener... listeners) {
        return () -> {
            for (ConfigListener listener : listeners) {
                listener.configUpdated();
            }
        };
    }
}

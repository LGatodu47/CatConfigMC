package io.github.lgatodu47.catconfigmc;

import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * This interface holds a ConfigOption alongside other elements that
 * may be used in order to render that option in a configuration menu.<br>
 * Note that in contrast with {@link ConfigOption ConfigOptions}, <strong>rendered config options don't
 * need to be defined in a static final field</strong> as most of the time you don't need
 * to get a specific one. Rendered config options just hold information that is
 * useful to render them in a gui.
 * @param <V> The type of value that the ConfigOption holds.
 */
public interface RenderedConfigOption<V> {
    /**
     * @return The ConfigOption that represents this RenderedOption.
     */
    ConfigOption<V> option();

    /**
     * @return A Minecraft Text Component that holds the display name of the ConfigOption.
     */
    Text displayName();

    /**
     * @return A Minecraft Text Component that holds the description of the ConfigOption.
     */
    Text description();

    /**
     * A method that handles the creation for a new widget which will represent the value of the ConfigOption.
     * This widget should represent the value of this ConfigOption stored in the config and allow its modification.
     * <strong>NOTE</strong>: the widget's x and y values will be assigned when they will be rendered.
     *
     * @param config Access to the config.
     * @return A widget that can interact with the config, {@code null} for no representation of the value.
     * @see BuiltinWidgets BuiltinWidgets and its use in RenderedConfigOptionBuilder.
     */
    @Nullable
    ClickableWidget createWidget(ConfigAccess config);
}

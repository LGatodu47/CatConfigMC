package io.github.lgatodu47.catconfigmc;

import com.google.common.collect.ImmutableList;
import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.lgatodu47.catconfigmc.BuiltinWidgets.*;

/**
 * A class that helps with creating rendered config options.
 */
public class RenderedConfigOptionBuilder {
    private final List<RenderedConfigOption<?>> options;

    public RenderedConfigOptionBuilder() {
        this(new ArrayList<>());
    }

    public RenderedConfigOptionBuilder(@NotNull List<RenderedConfigOption<?>> list) {
        list.clear();
        this.options = list;
    }

    /**
     * Initiates the creation of a new rendered config option.
     *
     * @param option The option associated with the rendered config option to create.
     * @return A {@link BuildingRenderedConfigOption} instance.
     * @param <V> The type of object that the given option accepts.
     */
    public <V> BuildingRenderedConfigOption<V> option(ConfigOption<V> option) {
        return new BuildingRenderedConfigOption<>(option, options::add);
    }

    /**
     * Initiates the creation of a new Boolean rendered config option.
     *
     * @param option The option associated with the rendered config option to create.
     * @return A {@link BuildingRenderedConfigOption} instance.
     */
    public BuildingRenderedConfigOption<Boolean> ofBoolean(ConfigOption<Boolean> option) {
        return option(option).setWidgetFactory(config -> createBoolWidget(config, option));
    }

    /**
     * Initiates the creation of a new Integer rendered config option.
     *
     * @param option The option associated with the rendered config option to create.
     * @return A {@link BuildingRenderedConfigOption} instance.
     */
    public BuildingRenderedConfigOption<Integer> ofInt(ConfigOption<Integer> option) {
        return option(option).setWidgetFactory(config -> createIntWidget(config, option));
    }

    /**
     * Initiates the creation of a new Long rendered config option.
     *
     * @param option The option associated with the rendered config option to create.
     * @return A {@link BuildingRenderedConfigOption} instance.
     */
    public BuildingRenderedConfigOption<Long> ofLong(ConfigOption<Long> option) {
        return option(option).setWidgetFactory(config -> createLongWidget(config, option));
    }

    /**
     * Initiates the creation of a new Double rendered config option.
     *
     * @param option The option associated with the rendered config option to create.
     * @return A {@link BuildingRenderedConfigOption} instance.
     */
    public BuildingRenderedConfigOption<Double> ofDouble(ConfigOption<Double> option) {
        return option(option).setWidgetFactory(config -> createDoubleWidget(config, option));
    }

    /**
     * Initiates the creation of a new String rendered config option.
     *
     * @param option The option associated with the rendered config option to create.
     * @return A {@link BuildingRenderedConfigOption} instance.
     */
    public BuildingRenderedConfigOption<String> ofString(ConfigOption<String> option, boolean extendedLength) {
        return option(option).setWidgetFactory(config -> createStringWidget(config, option, extendedLength));
    }

    /**
     * Initiates the creation of a new Enum rendered config option.
     *
     * @param option The option associated with the rendered config option to create.
     * @param enumClass The class that holds all the enum values.
     * @return A {@link BuildingRenderedConfigOption} instance.
     * @param <E> The type of the enum.
     */
    public <E extends Enum<E>> BuildingRenderedConfigOption<E> ofEnum(ConfigOption<E> option, Class<E> enumClass) {
        return option(option).setWidgetFactory(config -> createEnumWidget(config, option, enumClass));
    }

    /**
     * @return An immutable list holding the rendered options of this builder.
     */
    public List<RenderedConfigOption<?>> optionsToRender() {
        return ImmutableList.copyOf(options);
    }

    /**
     * Represents a building rendered config option.
     * @param <V> The type of the config option.
     */
    public static final class BuildingRenderedConfigOption<V> {
        private final ConfigOption<V> option;
        private final Consumer<RenderedConfigOption<?>> appender;
        private Text name, description;
        private Function<ConfigAccess, ClickableWidget> widgetFactory;

        private BuildingRenderedConfigOption(ConfigOption<V> option, Consumer<RenderedConfigOption<?>> appender) {
            this.option = option;
            this.appender = appender;
        }

        /**
         * Sets the same translation key for the name and the description (description is suffixed of '.desc').
         *
         * @param translationKey The translation key to set.
         * @return this
         */
        public BuildingRenderedConfigOption<V> setCommonTranslationKey(@NotNull String translationKey) {
            return setName(new TranslatableText(translationKey)).setDescription(new TranslatableText(translationKey.concat(".desc")));
        }

        public BuildingRenderedConfigOption<V> setName(Text name) {
            this.name = name;
            return this;
        }

        public BuildingRenderedConfigOption<V> setDescription(Text description) {
            this.description = description;
            return this;
        }

        public BuildingRenderedConfigOption<V> setWidgetFactory(Function<ConfigAccess, ClickableWidget> widgetFactory) {
            this.widgetFactory = widgetFactory;
            return this;
        }

        /**
         * @return A new build Rendered Config Option instance.
         */
        public RenderedConfigOption<V> build() {
            RenderedConfigOption<V> opt = new RenderedConfigOptionImpl<>(this.option,
                    name == null ? new LiteralText(option.name()) : name,
                    description == null ? LiteralText.EMPTY : description,
                    widgetFactory == null ? w -> null : widgetFactory);
            appender.accept(opt);
            return opt;
        }

        private record RenderedConfigOptionImpl<V>(ConfigOption<V> option, Text displayName, Text description, Function<ConfigAccess, ClickableWidget> widgetMaker) implements RenderedConfigOption<V> {
            @Override
            public @Nullable ClickableWidget createWidget(ConfigAccess config) {
                return widgetMaker().apply(config);
            }
        }
    }
}

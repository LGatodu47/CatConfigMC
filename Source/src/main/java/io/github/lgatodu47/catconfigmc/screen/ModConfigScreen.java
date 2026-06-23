package io.github.lgatodu47.catconfigmc.screen;

import io.github.lgatodu47.catconfig.CatConfig;
import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import io.github.lgatodu47.catconfigmc.RenderedConfigOptionAccess;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ModConfigScreen extends Screen {
    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    protected final Screen parent;
    // A config for one side only.
    protected final CatConfig config;
    protected final UnsavedConfig unsavedConfig;
    protected final RenderedConfigOptionAccess renderedOptions;
    // The list widget. The interface defined below is just to avoid issues with nullability.
    @NotNull
    protected IConfigOptionListWidget list = IConfigOptionListWidget.NONE;
    protected ConfigListener listeners = () -> {};
    @Nullable
    protected Identifier backgroundTexture;

    public ModConfigScreen(Component title, Screen parent, CatConfig config, RenderedConfigOptionAccess renderedOptions) {
        super(title);
        this.parent = parent;
        this.config = config;
        this.unsavedConfig = new UnsavedConfig(config);
        this.renderedOptions = renderedOptions;
    }

    // NOTE: this method removes all previous listeners.
    public ModConfigScreen withListeners(ConfigListener... listeners) {
        this.listeners = ConfigListener.combine(listeners);
        return this;
    }

    public ModConfigScreen withBackgroundTexture(Identifier texture) {
        this.backgroundTexture = texture;
        return this;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        LinearLayout bottom = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        bottom.addChild(Button.builder(Component.translatable("button.catconfigmc.config.discard_changes").withStyle(ChatFormatting.RED), button -> onClose()).build());
        bottom.addChild(Button.builder(Component.translatable("button.catconfigmc.config.save_changes").withStyle(ChatFormatting.GREEN), button -> saveAndClose()).build());

        ConfigOptionListWidget<?> listWidget = new ConfigOptionListWidget<>(this.minecraft, this.width, layout.getContentHeight(), layout.getHeaderHeight());
        listWidget.addAll(this.unsavedConfig, this.renderedOptions, this.unsavedConfig::changed);
        this.layout.addToContents(listWidget);
        this.list = listWidget;

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        list.updateWidgetSize(this.width, this.layout);
    }

    @Override
    public void tick() {
        super.tick();
        list.tick();
    }

    protected void saveAndClose() {
        this.unsavedConfig.saveChanges();
        onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
        if(this.parent instanceof ConfigListener screen) {
            screen.configUpdated();
        }
    }

    @Override
    public void removed() {
        this.config.writeToFile();
        listeners.configUpdated();
    }

    /**
     * Interface just used to avoid having a null ConfigOptionListWidget.
     * Defines all the methods that this parent class uses.
     */
    public interface IConfigOptionListWidget extends GuiEventListener, Renderable {
        /**
         * Implementation where there is simply no list.
         */
        IConfigOptionListWidget NONE = new IConfigOptionListWidget() {
            @Override
            public void tick() {
            }

            @Override
            public void updateWidgetSize(int width, HeaderAndFooterLayout layout) {
            }

            @Override
            public void setFocused(boolean focused) {
            }

            @Override
            public boolean isFocused() {
                return false;
            }

            @Override
            public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
            }
        };

        /**
         * Tick method that is called from the parent screen tick method.
         */
        void tick();

        void updateWidgetSize(int width, HeaderAndFooterLayout layout);
    }

    protected static class UnsavedConfig implements ConfigAccess {
        protected final ConfigAccess delegateConfig;
        protected final Map<ConfigOption<?>, @Nullable Object> changes;

        protected UnsavedConfig(ConfigAccess delegateConfig) {
            this.delegateConfig = delegateConfig;
            this.changes = new HashMap<>();
        }

        @Override
        public <V> void put(ConfigOption<V> option, @Nullable V value) {
            this.changes.put(option, value);
        }

        @Override
        public <V> Optional<V> get(ConfigOption<V> option) {
            return changes.containsKey(option) ? Optional.ofNullable(option.type().cast(changes.get(option))) : delegateConfig.get(option);
        }

        protected boolean changed(ConfigOption<?> option) {
            return changes.containsKey(option) && !Objects.equals(changes.get(option), delegateConfig.get(option).orElse(null));
        }

        @SuppressWarnings("unchecked")
        protected void saveChanges() {
            changes.forEach((option, value) -> delegateConfig.put((ConfigOption<Object>) option, value));
        }
    }
}

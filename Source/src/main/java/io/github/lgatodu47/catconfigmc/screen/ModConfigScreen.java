package io.github.lgatodu47.catconfigmc.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lgatodu47.catconfig.CatConfig;
import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import io.github.lgatodu47.catconfigmc.RenderedConfigOptionAccess;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModConfigScreen extends Screen {
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

    public ModConfigScreen(Text title, Screen parent, CatConfig config, RenderedConfigOptionAccess renderedOptions) {
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
    public void tick() {
        list.tick();
    }

    @Override
    protected void init() {
        final int spacing = 8;
        final int btnHeight = 20;
        final int btnWidth = 150;

        ConfigOptionListWidget<?> listWidget = new ConfigOptionListWidget<>(this.client, this.width, this.height - spacing * 5 - btnHeight, spacing * 3, this.height - spacing * 2 - btnHeight);
        listWidget.addAll(this.unsavedConfig, this.renderedOptions, this.unsavedConfig::changed);
        this.list = listWidget;
        // We manually render the list because it needs to be rendered before the other children.
        this.addSelectableChild(listWidget);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.catconfigmc.config.discard_changes").formatted(Formatting.RED), button -> close())
                .dimensions((this.width - spacing) / 2 - btnWidth, this.height - btnHeight - spacing, btnWidth, btnHeight)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.catconfigmc.config.save_changes").formatted(Formatting.GREEN), button -> saveAndClose())
                .dimensions((this.width + spacing) / 2, this.height - btnHeight - spacing, btnWidth, btnHeight)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context, mouseX, mouseY, delta);
        list.render(context, mouseX, mouseY, delta);
        list.bottom().ifPresent(this::renderAboveList);
        context.drawCenteredTextWithShadow(textRenderer, title, this.width / 2, 8, 0xFFFFFF);
        context.getMatrices().translate(0, 0, 2);
        children().stream().filter(Drawable.class::isInstance).map(Drawable.class::cast).forEachOrdered(drawable -> drawable.render(context, mouseX, mouseY, delta));
        context.getMatrices().translate(0, 0, -2);
        list.getHoveredButtonDescription(mouseX, mouseY).ifPresent(desc -> context.drawTooltip(textRenderer, desc, mouseX, mouseY));
    }

    /**
     * Covers the screen from the list bottom position all the way up to the bottom of the screen.
     * By doing this we hide the other list entries that are out of the render area.
     * @param listBottom The bottom y position of the list.
     */
    protected void renderAboveList(int listBottom) {
        Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(0.0f, this.height, 1).texture(0.0f, (float) (listBottom - this.height) / 32.0f).color(64, 64, 64, 255);
        builder.vertex(this.width, this.height, 1).texture((float) this.width / 32.0f, (float) (listBottom - this.height) / 32.0f).color(64, 64, 64, 255);
        builder.vertex(this.width, listBottom, 1).texture((float) this.width / 32.0f, 0).color(64, 64, 64, 255);
        builder.vertex(0.0f, listBottom, 1).texture(0.0f, 0).color(64, 64, 64, 255);
    }

    protected void saveAndClose() {
        this.unsavedConfig.saveChanges();
        close();
    }

    public void renderBackgroundTexture(DrawContext context, int mouseX, int mouseY, float delta) {
        if(backgroundTexture == null) {
            renderBackground(context, mouseX, mouseY, delta);
            return;
        }
        context.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
        context.drawTexture(this.backgroundTexture, 0, 0, 0, 0.0f, 0.0f, this.width, this.height, 32, 32);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
        if(this.parent instanceof ConfigListener screen) {
            screen.configUpdated();
        }
    }

    @Override
    public void removed() {
        this.config.writeToFile();
        listeners.configUpdated();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return children().stream().filter(element -> element.mouseClicked(mouseX, mouseY, button)).findFirst().filter(element -> {
            setFocused(element);
            if (button == 0) {
                setDragging(true);
            }
            return true;
        }).isPresent();
    }

    /**
     * Interface just used to avoid having a null ConfigOptionListWidget.
     * Defines all the methods that this parent class uses.
     */
    public interface IConfigOptionListWidget extends Element, Drawable {
        /**
         * Implementation where there is simply no list.
         */
        IConfigOptionListWidget NONE = new IConfigOptionListWidget() {
            @Override
            public void tick() {
            }

            @Override
            public Optional<Text> getHoveredButtonDescription(double mouseX, double mouseY) {
                return Optional.empty();
            }

            @Override
            public OptionalInt bottom() {
                return OptionalInt.empty();
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            }

            @Override
            public void setFocused(boolean focused) {
            }

            @Override
            public boolean isFocused() {
                return false;
            }
        };

        /**
         * Tick method that is called from the parent screen tick method.
         */
        void tick();

        /**
         * Gets the button at the specified mouse position and obtains its description.
         *
         * @param mouseX The x position of the mouse in the viewport.
         * @param mouseY The y position of the mouse in the viewport.
         * @return An Optional holding a Text representation of the button's description. This Optional is empty if no button was found.
         */
        Optional<Text> getHoveredButtonDescription(double mouseX, double mouseY);

        /**
         * @return An Optional holding the int value for the 'bottom' field in the list. This should only be empty
         * for the {@link IConfigOptionListWidget#NONE} implementation.
         */
        OptionalInt bottom();
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

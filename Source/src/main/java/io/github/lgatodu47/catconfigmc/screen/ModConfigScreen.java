package io.github.lgatodu47.catconfigmc.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lgatodu47.catconfig.CatConfig;
import io.github.lgatodu47.catconfigmc.RenderedConfigOption;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

public class ModConfigScreen extends Screen {
    protected final Screen parent;
    // A config for one side only.
    protected final CatConfig config;
    // A supplier for all the rendered config options
    protected final Supplier<Iterable<RenderedConfigOption<?>>> renderedOptionsSupplier;
    // The list widget. The interface defined below is just to avoid issues with nullability.
    @NotNull
    protected IConfigOptionListWidget list = IConfigOptionListWidget.NONE;
    protected ConfigListener listeners = () -> {};

    public ModConfigScreen(Text title, Screen parent, CatConfig config, Supplier<Iterable<RenderedConfigOption<?>>> renderedOptionsSupplier) {
        super(title);
        this.parent = parent;
        this.config = config;
        this.renderedOptionsSupplier = renderedOptionsSupplier;
    }

    // NOTE: this method removes all previous listeners.
    public ModConfigScreen withListeners(ConfigListener... listeners) {
        this.listeners = ConfigListener.combine(listeners);
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
        final int btnWidth = 200;

        ConfigOptionListWidget<?> listWidget = new ConfigOptionListWidget<>(this.client, this.width, this.height - spacing * 5 - btnHeight, spacing * 3, this.height - spacing * 2 - btnHeight);
        listWidget.addAll(this.config, this.renderedOptionsSupplier);
        this.list = listWidget;
        // We manually render the list because it needs to be rendered before the other children.
        this.addSelectableChild(listWidget);
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                .dimensions((this.width - btnWidth) / 2, this.height - btnHeight - spacing, btnWidth, btnHeight)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);
        list.renderImpl(context, mouseX, mouseY, delta);
        list.bottom().ifPresent(this::renderAboveList);
        context.drawCenteredTextWithShadow(textRenderer, title, this.width / 2, 8, 0xFFFFFF);
        context.getMatrices().translate(0, 0, 2);
        super.render(context, mouseX, mouseY, delta);
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
        BufferBuilder builder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(0.0, this.height, 1).texture(0.0f, (float) (listBottom - this.height) / 32.0f).color(64, 64, 64, 255).next();
        builder.vertex(this.width, this.height, 1).texture((float) this.width / 32.0f, (float) (listBottom - this.height) / 32.0f).color(64, 64, 64, 255).next();
        builder.vertex(this.width, listBottom, 1).texture((float) this.width / 32.0f, 0).color(64, 64, 64, 255).next();
        builder.vertex(0.0, listBottom, 1).texture(0.0f, 0).color(64, 64, 64, 255).next();

        tessellator.draw();
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
        list.updateChildElementsClickedState(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Interface just used to avoid having a null ConfigOptionListWidget.
     * Defines all the methods that this parent class uses.
     */
    public interface IConfigOptionListWidget {
        /**
         * Implementation where there is simply no list.
         */
        IConfigOptionListWidget NONE = new IConfigOptionListWidget() {
            @Override
            public void tick() {
            }

            @Override
            public void renderImpl(DrawContext context, int mouseX, int mouseY, float delta) {
            }

            @Override
            public void updateChildElementsClickedState(double mouseX, double mouseY, int button) {
            }

            @Override
            public Optional<Text> getHoveredButtonDescription(double mouseX, double mouseY) {
                return Optional.empty();
            }

            @Override
            public OptionalInt bottom() {
                return OptionalInt.empty();
            }
        };

        /**
         * Tick method that is called from the parent screen tick method.
         */
        void tick();

        /**
         * Render method of the widget.
         */
        void renderImpl(DrawContext context, int mouseX, int mouseY, float delta);

        /**
         * Updates the child elements of the list by calling 'onMouseClicked'.
         */
        void updateChildElementsClickedState(double mouseX, double mouseY, int button);

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
}

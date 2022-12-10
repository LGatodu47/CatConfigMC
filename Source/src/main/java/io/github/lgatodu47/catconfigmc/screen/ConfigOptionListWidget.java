package io.github.lgatodu47.catconfigmc.screen;

import com.google.common.collect.Lists;
import io.github.lgatodu47.catconfig.CatConfig;
import io.github.lgatodu47.catconfigmc.RenderedConfigOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

public class ConfigOptionListWidget<E extends ConfigOptionListWidget.ConfigOptionEntry<E>> extends ElementListWidget<E> implements ModConfigScreen.IConfigOptionListWidget {
    public ConfigOptionListWidget(MinecraftClient client, int width, int height, int top, int bottom) {
        super(client, width, height, top, bottom, 36);
    }

    @Override
    public void tick() {
        this.children().forEach(ConfigOptionEntry::tick);
    }

    @Override
    public int getRowLeft() {
        return 0;
    }

    @Override
    public int getRowWidth() {
        return this.width - 6;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.width - 6;
    }

    // We can't just leave the same method names as it wouldn't work in obfuscated environment.
    @Override
    public void renderImpl(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void updateChildElementsClickedState(double mouseX, double mouseY, int button) {
        children().stream().map(child -> child.widget).filter(TextFieldWidget.class::isInstance).forEach(field -> field.mouseClicked(mouseX, mouseY, button));
    }

    public void addAll(CatConfig config, Supplier<Iterable<RenderedConfigOption<?>>> renderedOptionsSupplier) {
        renderedOptionsSupplier.get().forEach(option -> {
            ClickableWidget widget = option.createWidget(config);
            if (widget != null) {
                addEntry(makeEntry(option, widget));
            }
        });
    }

    @Override
    public Optional<Text> getHoveredButtonDescription(double mouseX, double mouseY) {
        if(mouseX > this.left && mouseX < this.left + this.width && mouseY > this.top && mouseY < this.top + height) {
            for (ConfigOptionEntry<?> entry : this.children()) {
                if (entry.widget.isMouseOver(mouseX, mouseY)) {
                    return Optional.of(entry.option.description());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public OptionalInt bottom() {
        return OptionalInt.of(this.bottom);
    }

    /**
     * Creates a new entry of type E for this list widget.
     *
     * @param option The rendered config passed down to the entry constructor.
     * @param widget The widget passed down to the entry constructor.
     * @return A new entry of the type required by this list.
     */
    @SuppressWarnings("unchecked")
    protected E makeEntry(RenderedConfigOption<?> option, ClickableWidget widget) {
        return (E) new ConfigOptionEntry<>(this.client, option, widget);
    }

    public static class ConfigOptionEntry<E extends ConfigOptionEntry<E>> extends Entry<E> {
        protected final MinecraftClient client;
        protected final RenderedConfigOption<?> option;
        protected final ClickableWidget widget;

        public ConfigOptionEntry(MinecraftClient client, RenderedConfigOption<?> option, ClickableWidget widget) {
            this.client = client;
            this.option = option;
            this.widget = widget;
        }

        /**
         * Ticks the widget if it is tick-able.
         */
        protected void tick() {
            if(widget instanceof TextFieldWidget textField) {
                textField.tick();
            }
        }

        // Time for which the entry has been hovered. Between 0 and 1.
        protected float hoveredTime;

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if(hovered) {
                if(hoveredTime < 1) {
                    hoveredTime = Math.min(1, hoveredTime + 0.1F);
                }
            }
            else {
                if(hoveredTime > 0) {
                    hoveredTime = Math.max(0, hoveredTime - 0.1F);
                }
            }
            DrawableHelper.fill(matrices, x, y, x + entryWidth, y + entryHeight, ColorHelper.Argb.getArgb((int) (hoveredTime * 0.2 * 255), 65, 65, 65));
            final int spacing = 8;
            drawTextWithShadow(matrices, client.textRenderer, option.displayName(), spacing, y + (entryHeight - client.textRenderer.fontHeight) / 2, 0xFFFFFF);
            widget.setX(x + entryWidth - spacing - widget.getWidth());
            widget.setY(y + (entryHeight - widget.getHeight()) / 2);
            widget.render(matrices, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Lists.newArrayList(widget);
        }

        @Override
        public List<? extends Element> children() {
            return Lists.newArrayList(widget);
        }
    }

}

package io.github.lgatodu47.catconfigmc.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import io.github.lgatodu47.catconfigmc.RenderedConfigOption;
import io.github.lgatodu47.catconfigmc.RenderedConfigOptionAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfigOptionListWidget<E extends ConfigOptionListWidget.AbstractEntry<E>> extends ElementListWidget<E> implements ModConfigScreen.IConfigOptionListWidget {
    public ConfigOptionListWidget(MinecraftClient client, int width, int height, int top, int bottom) {
        super(client, width, height, top, bottom, 36);
    }

    @Override
    public void tick() {
        this.children().forEach(AbstractEntry::tick);
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

    /*@Override
    protected E getEntryAtPosition(double x, double y) {
        int halfRowWidth = this.getRowWidth() / 2;
        int listCenter = this.left + this.width / 2;
        int rowStartX = listCenter - halfRowWidth;
        int rowEndX = listCenter + halfRowWidth;

        int rowYOffset = MathHelper.floor(y - (double) this.top) - this.headerHeight + (int) this.getScrollAmount() - 4;
        int rowIndex = -1;
        for (int i = 0; i < children().size(); i++) {
            if(rowYOffset - getRowYOffset(i + 1) <= 0) {
                rowIndex = i;
                break;
            }
        }

        if(x < (double) getScrollbarPositionX() && x >= (double) rowStartX && x <= (double) rowEndX && rowIndex >= 0 && rowYOffset >= 0 && rowIndex < getEntryCount()) {
            return children().get(rowIndex);
        }
        return null;
    }*/

    @Override
    protected int getMaxPosition() {
        return getRowYOffset(getEntryCount()) + this.headerHeight;
    }

    @Override
    protected void centerScrollOn(E entry) {
        setScrollAmount(getRowYOffset(children().indexOf(entry)) + entry.entryHeight() / 2. - (this.bottom - this.top) / 2.);
    }

    @Override
    protected void ensureVisible(E entry) {
        int rowTop = getRowTop(children().indexOf(entry));
        int topOffset = rowTop - this.top - 4 - entry.entryHeight();
        if (topOffset < 0) {
            setScrollAmount(getScrollAmount() + topOffset);
        }

        int bottomOffset = this.bottom - rowTop - 2 * entry.entryHeight();
        if (bottomOffset < 0) {
            setScrollAmount(getScrollAmount() - bottomOffset);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int scrollbarPositionX = this.getScrollbarPositionX();
        int scrollBarWidth = scrollbarPositionX + 6;
        int rowLeft = this.getRowLeft();
        int hearderHeight = this.top + 4 - (int)this.getScrollAmount();

        renderBackground(context);
        enableScissor(context);
        renderHeader(context, rowLeft, hearderHeight);
        renderList(context, mouseX, mouseY, delta);
        context.disableScissor();
        renderHorizontalShadows(context);

        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int scrollbarHeight = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getMaxPosition());
            scrollbarHeight = MathHelper.clamp(scrollbarHeight, 32, this.bottom - this.top - 8);
            int scrollY = (int)this.getScrollAmount() * (this.bottom - this.top - scrollbarHeight) / maxScroll + this.top;
            if (scrollY < this.top) {
                scrollY = this.top;
            }

            context.fill(scrollbarPositionX, this.top, scrollBarWidth, this.bottom, -16777216);
            context.fill(scrollbarPositionX, scrollY, scrollBarWidth, scrollY + scrollbarHeight, -8355712);
            context.fill(scrollbarPositionX, scrollY, scrollBarWidth - 1, scrollY + scrollbarHeight - 1, -4144960);
        }

        renderDecorations(context, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        int rowLeft = getRowLeft();
        int rowWidth = getRowWidth();
        int entryCount = getEntryCount();

        for(int i = 0; i < entryCount; ++i) {
            int rowTop = getRowTop(i);
            int rowBottom = getRowBottom(i);

            if (rowBottom >= this.top && rowTop <= this.bottom) {
                renderEntry(context, mouseX, mouseY, delta, i, rowLeft, rowTop, rowWidth, getRowHeight(i));
            }
        }
    }

    public void renderHorizontalShadows(DrawContext context) {
//        context.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
//        context.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, this.left, 0, 0.0F, 0.0F, this.width, this.top, 32, 32);
//        context.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, this.left, this.bottom, 0.0F, (float)this.bottom, this.width, this.height - this.bottom, 32, 32);
//        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        context.fillGradient(RenderLayer.getGuiOverlay(), this.left, this.top, this.right, this.top + 4, -16777216, 0, 0);
        context.fillGradient(RenderLayer.getGuiOverlay(), this.left, this.bottom - 4, this.right, this.bottom, 0, -16777216, 0);
    }

    @Override
    protected int getRowTop(int index) {
        return this.top + 4 - (int) getScrollAmount() + getRowYOffset(index) + this.headerHeight;
    }

    @Override
    protected int getRowBottom(int index) {
        return getRowTop(index) + getRowHeight(index);
    }

    protected int getRowHeight(int index) {
        return children().get(index).entryHeight();
    }

    protected int getRowYOffset(int index) {
        return children().subList(0, index).stream().mapToInt(AbstractEntry::entryHeight).sum();
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

    @SuppressWarnings("unchecked")
    public void addAll(ConfigAccess config, RenderedConfigOptionAccess renderedOptions, Predicate<ConfigOption<?>> optionChanged) {
        Map<String, ConfigCategoryEntry<?>> categoryEntries = renderedOptions.optionsToRender()
                .stream()
                .map(option -> option.option().optionPath())
                .map(path -> path.substring(0, path.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR)))
                .filter(path -> !path.isEmpty())
                .distinct()
                .collect(Collectors.toMap(Function.identity(), path -> new ConfigCategoryEntry<>(this.client, renderedOptions.getNameForCategory(path, () -> Text.literal(path.substring(path.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR) + 1))), renderedOptions.getDescriptionForCategory(path))));

        List<AbstractEntry<?>> finalEntries = new ArrayList<>();
        for (RenderedConfigOption<?> option : renderedOptions.optionsToRender()) {
            ClickableWidget widget = option.createWidget(config);
            if(widget == null) {
                continue;
            }

            String optionPath = option.option().optionPath();
            String categoryPath = optionPath.substring(0, optionPath.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR));
            ConfigOptionEntry<?> optionEntry = new ConfigOptionEntry<>(this.client, option, widget, () -> optionChanged.test(option.option()));
            if(categoryPath.isEmpty() || !categoryEntries.containsKey(categoryPath)) {
                finalEntries.add(optionEntry);
                continue;
            }
            categoryEntries.get(categoryPath).addEntry(optionEntry);
        }

        List<String> categories = categoryEntries.keySet().stream().sorted(ConfigOption.categoryPathComparator()).toList();
        categories_loop: for (String categoryPath : categories) {
            while(categoryPath.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR) > 0) {
                ConfigCategoryEntry<?> categoryEntry = categoryEntries.get(categoryPath);
                categoryPath = categoryPath.substring(0, categoryPath.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR));
                if(categoryEntries.containsKey(categoryPath)) {
                    categoryEntries.get(categoryPath).addEntry(categoryEntry);
                    continue categories_loop;
                }
                final String finalCategoryPath = categoryPath;
                categoryEntries.put(categoryPath, Util.make(new ConfigCategoryEntry<>(this.client, renderedOptions.getNameForCategory(categoryPath, () -> Text.literal(finalCategoryPath.substring(finalCategoryPath.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR) + 1))), renderedOptions.getDescriptionForCategory(categoryPath)), entry -> entry.addEntry(categoryEntry)));
            }
            final String finalCategoryPath1 = categoryPath;
            finalEntries.add(categoryEntries.getOrDefault(categoryPath, new ConfigCategoryEntry<>(this.client, renderedOptions.getNameForCategory(categoryPath, () -> Text.literal(finalCategoryPath1.substring(1))), renderedOptions.getDescriptionForCategory(categoryPath))));
        }

        finalEntries.stream().filter(Objects::nonNull).forEach(abstractEntry -> this.addEntry((E) abstractEntry));
    }

    @Override
    public Optional<Text> getHoveredButtonDescription(double mouseX, double mouseY) {
        if(mouseX > this.left && mouseX < this.left + this.width && mouseY > this.top && mouseY < this.top + height) {
            for (AbstractEntry<?> entry : this.children()) {
                Optional<Text> desc = entry.getHoveringDescription(mouseX, mouseY);
                if(desc.isPresent()) {
                    return desc;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public OptionalInt bottom() {
        return OptionalInt.of(this.bottom);
    }

    public static abstract class AbstractEntry<E extends AbstractEntry<E>> extends Entry<E> {
        protected final MinecraftClient client;
        protected boolean hovered;

        protected AbstractEntry(MinecraftClient client) {
            this.client = client;
        }

        protected abstract int entryHeight();

        protected void tick() {
        }

        protected abstract Optional<Text> getHoveringDescription(double mouseX, double mouseY);

        @Override
        public boolean isFocused() {
            return false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return hovered;
        }

        @Deprecated
        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }
    }

    public static class ConfigOptionEntry<E extends ConfigOptionEntry<E>> extends AbstractEntry<E> {
        protected final RenderedConfigOption<?> option;
        protected final ClickableWidget widget;
        protected final BooleanSupplier changed;

        public ConfigOptionEntry(MinecraftClient client, RenderedConfigOption<?> option, ClickableWidget widget, BooleanSupplier changed) {
            super(client);
            this.option = option;
            this.widget = widget;
            this.changed = changed;
        }

        @Override
        protected int entryHeight() {
            return 36;
        }

        /**
         * Ticks the widget if it is tick-able.
         */
        @Override
        protected void tick() {
            if(widget instanceof TextFieldWidget textField) {
                textField.tick();
            }
        }

        @Override
        protected Optional<Text> getHoveringDescription(double mouseX, double mouseY) {
            if(widget.isMouseOver(mouseX, mouseY)) {
                return Optional.of(option.description());
            }
            return Optional.empty();
        }

        // Time for which the entry has been hovered. Between 0 and 1.
        protected float hoveredTime;

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean unused, float tickDelta) {
            hovered = mouseX > x && mouseX <= x + entryWidth && mouseY > y && mouseY <= y + 36;
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
            context.fill(x, y, x + entryWidth, y + entryHeight, ColorHelper.Argb.getArgb((int) (hoveredTime * 0.2 * 255), 65, 65, 65));
            final int spacing = 8;
            context.drawTextWithShadow(client.textRenderer, option.displayName().copy().styled(style -> style.withItalic(changed.getAsBoolean())), x + spacing, y + (entryHeight - client.textRenderer.fontHeight) / 2, 0xFFFFFF);
            widget.setX(x + entryWidth - spacing - widget.getWidth());
            widget.setY(y + (entryHeight - widget.getHeight()) / 2);
            widget.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(widget.mouseClicked(mouseX, mouseY, button)) {
                setFocused(widget);
                if(button == 0) {
                    setDragging(true);
                }
                return true;
            }
            setFocused(null);
            return false;
        }

        @SuppressWarnings("deprecation")
        @Override
        public List<? extends Selectable> selectableChildren() {
            return Lists.newArrayList(widget);
        }

        @Override
        public List<? extends Element> children() {
            return Lists.newArrayList(widget);
        }
    }

    public static class ConfigCategoryEntry<E extends ConfigCategoryEntry<E>> extends AbstractEntry<E> {
        protected final Text categoryName;
        @Nullable
        protected final Text categoryDesc;
        private final List<AbstractEntry<?>> entries;
        private boolean showing;

        public ConfigCategoryEntry(MinecraftClient client, Text categoryName, @Nullable Text categoryDesc) {
            super(client);
            this.categoryName = categoryName;
            this.categoryDesc = categoryDesc;
            this.entries = new ArrayList<>();
        }

        protected void addEntry(AbstractEntry<?> entry) {
            entries.add(entry);
        }

        @Override
        protected int entryHeight() {
            return showing ? this.entries.stream().mapToInt(AbstractEntry::entryHeight).sum() + 36 : 36;
        }

        @Override
        protected void tick() {
            entries.forEach(AbstractEntry::tick);
        }

        @Override
        protected Optional<Text> getHoveringDescription(double mouseX, double mouseY) {
            if(hovered && categoryDesc != null) {
                return Optional.of(categoryDesc);
            }
            if(!showing) {
                return Optional.empty();
            }
            for (AbstractEntry<?> entry : entries) {
                Optional<Text> desc = entry.getHoveringDescription(mouseX, mouseY);
                if(desc.isPresent()) {
                    return desc;
                }
            }
            return Optional.empty();
        }

        // Time for which the entry has been hovered. Between 0 and 1.
        protected float hoveredTime;

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean unused, float tickDelta) {
            hovered = mouseX > x && mouseX <= x + entryWidth && mouseY > y && mouseY <= y + 36;
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
            context.fill(x, y, x + entryWidth, y + 36, ColorHelper.Argb.getArgb((int) (hoveredTime * 0.2 * 255), 65, 65, 65));
            final int spacing = 8;
            context.drawTextWithShadow(client.textRenderer, categoryName.copy().formatted(Formatting.YELLOW), x + spacing, y + (36 - client.textRenderer.fontHeight) / 2, 0xFFFFFF);

            if(showing) {
                final int entryXOffset = 10;
                for (int i = 0; i < entries.size(); i++) {
                    AbstractEntry<?> entry = entries.get(i);
                    entry.render(context, i, y + 36 + entries.subList(0, i).stream().mapToInt(AbstractEntry::entryHeight).sum(), x + entryXOffset, entryWidth - entryXOffset, entry.entryHeight(), mouseX, mouseY, false, tickDelta);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean clickResult = showing && children().stream().filter(element -> element.mouseClicked(mouseX, mouseY, button)).findFirst().filter(element -> {
                setFocused(element);
                if (button == 0) {
                    setDragging(true);
                }
                return true;
            }).isPresent();
            if(hovered) {
                showing = !showing;
                return true;
            }
            return clickResult;
        }

        @Override
        public List<? extends Element> children() {
            return List.copyOf(this.entries);
        }
    }
}

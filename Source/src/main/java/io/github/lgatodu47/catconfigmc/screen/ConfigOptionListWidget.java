package io.github.lgatodu47.catconfigmc.screen;

import com.google.common.collect.Lists;
import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import io.github.lgatodu47.catconfigmc.RenderedConfigOption;
import io.github.lgatodu47.catconfigmc.RenderedConfigOptionAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfigOptionListWidget<E extends ConfigOptionListWidget.AbstractEntry<E>> extends ContainerObjectSelectionList<E> implements ModConfigScreen.IConfigOptionListWidget {
    public ConfigOptionListWidget(Minecraft mc, int width, int height, int top) {
        super(mc, width, height, top, 36);
    }

    @Override
    public void tick() {
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
    protected int scrollBarX() {
        return this.width - 6;
    }

    protected int getYOfFirstEntry() {
        return this.getY() + 2;
    }

    protected void recalculateAllChildrenPositions() {
        int entryY = this.getYOfFirstEntry() - (int) this.scrollAmount();

        for (E entry : this.children()) {
            entry.setY(entryY);
            entryY += entry.getHeight();
            entry.setX(this.getRowLeft());
            entry.setWidth(this.getRowWidth());
            entry.onReposition();
        }
    }

    @Override
    protected void sort(Comparator<E> comparator) {
        super.sort(comparator);
        recalculateAllChildrenPositions();
    }

    @Override
    protected void swap(int pos1, int pos2) {
        super.swap(pos1, pos2);
        recalculateAllChildrenPositions();
        scrollToEntry(children().get(pos2));
    }

    @Override
    public void setScrollAmount(double scrollY) {
        super.setScrollAmount(scrollY);
        recalculateAllChildrenPositions();
    }

    @Override
    public void updateSizeAndPosition(int width, int height, int x, int y) {
        this.setSize(width, height);
        this.setPosition(x, y);
        recalculateAllChildrenPositions();
        if (this.getSelected() != null) {
            this.scrollToEntry(this.getSelected());
        }

        this.refreshScrollAmount();
    }

    @Override
    protected void removeEntry(E entry) {
        super.removeEntry(entry);
        recalculateAllChildrenPositions();
    }

    @SuppressWarnings("unchecked")
    public void addAll(ConfigAccess config, RenderedConfigOptionAccess renderedOptions, Predicate<ConfigOption<?>> optionChanged) {
        Map<String, ConfigCategoryEntry<?>> categoryEntries = renderedOptions.optionsToRender()
                .stream()
                .map(option -> option.option().optionPath())
                .map(path -> path.substring(0, path.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR)))
                .filter(path -> !path.isEmpty())
                .distinct()
                .collect(Collectors.toMap(Function.identity(), path -> new ConfigCategoryEntry<>(this.minecraft, renderedOptions.getNameForCategory(path, () -> Component.literal(path.substring(path.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR) + 1))), renderedOptions.getDescriptionForCategory(path), this::recalculateAllChildrenPositions)));

        List<AbstractEntry<?>> finalEntries = new ArrayList<>();
        for (RenderedConfigOption<?> option : renderedOptions.optionsToRender()) {
            AbstractWidget widget = option.createWidget(config);
            if(widget == null) {
                continue;
            }

            String optionPath = option.option().optionPath();
            String categoryPath = optionPath.substring(0, optionPath.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR));
            ConfigOptionEntry<?> optionEntry = new ConfigOptionEntry<>(this.minecraft, option, widget, () -> optionChanged.test(option.option()));
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
                categoryEntries.put(categoryPath, Util.make(new ConfigCategoryEntry<>(this.minecraft, renderedOptions.getNameForCategory(categoryPath, () -> Component.literal(finalCategoryPath.substring(finalCategoryPath.lastIndexOf(ConfigOption.CATEGORY_SEPARATOR) + 1))), renderedOptions.getDescriptionForCategory(categoryPath), this::recalculateAllChildrenPositions), entry -> entry.addEntry(categoryEntry)));
            }
            final String finalCategoryPath1 = categoryPath;
            finalEntries.add(categoryEntries.getOrDefault(categoryPath, new ConfigCategoryEntry<>(this.minecraft, renderedOptions.getNameForCategory(categoryPath, () -> Component.literal(finalCategoryPath1.substring(1))), renderedOptions.getDescriptionForCategory(categoryPath), this::recalculateAllChildrenPositions)));
        }

        finalEntries.stream().filter(Objects::nonNull).forEach(abstractEntry -> this.addEntry((E) abstractEntry));
    }

    public static abstract class AbstractEntry<E extends AbstractEntry<E>> extends Entry<E> {
        protected final Minecraft client;

        protected AbstractEntry(Minecraft client) {
            this.client = client;
        }

        protected void onReposition() {
        }

        protected void tick() {
        }
    }

    public static class ConfigOptionEntry<E extends ConfigOptionEntry<E>> extends AbstractEntry<E> {
        protected final RenderedConfigOption<?> option;
        protected final AbstractWidget widget;
        protected final BooleanSupplier changed;

        public ConfigOptionEntry(Minecraft client, RenderedConfigOption<?> option, AbstractWidget widget, BooleanSupplier changed) {
            super(client);
            this.option = option;
            this.widget = widget;
            this.changed = changed;
        }

        @Override
        public int getHeight() {
            return 36;
        }

        // Time for which the entry has been hovered. Between 0 and 1.
        protected float hoveredTime;

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            boolean nameHovered = hovered && mouseY >= getY() && mouseY <= getY() + 36;
            if(nameHovered) {
                if(option.description() != null && !option.description().getString().isBlank()) {
                    context.setTooltipForNextFrame(client.font, option.description(), mouseX, mouseY);
                }
                if(hoveredTime < 1) {
                    hoveredTime = Math.min(1, hoveredTime + 0.1F);
                }
            }
            else {
                if(hoveredTime > 0) {
                    hoveredTime = Math.max(0, hoveredTime - 0.1F);
                }
            }
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), ARGB.color((int) (hoveredTime * 0.2 * 255), 65, 65, 65));
            final int spacing = 8;
            context.drawString(client.font, option.displayName().copy().withStyle(style -> style.withItalic(changed.getAsBoolean())), getX() + spacing, getY() + (getHeight() - client.font.lineHeight) / 2, 0xFFFFFFFF);
            widget.setX(getX() + getWidth() - spacing - widget.getWidth());
            widget.setY(getY() + (getHeight() - widget.getHeight()) / 2);
            widget.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Lists.newArrayList(widget);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Lists.newArrayList(widget);
        }
    }

    public static class ConfigCategoryEntry<E extends ConfigCategoryEntry<E>> extends AbstractEntry<E> {
        protected final Component categoryName;
        @Nullable
        protected final Component categoryDesc;
        private final List<AbstractEntry<?>> entries;
        private final Runnable positionUpdater;
        private boolean showing;

        public ConfigCategoryEntry(Minecraft client, Component categoryName, @Nullable Component categoryDesc, Runnable positionUpdater) {
            super(client);
            this.categoryName = categoryName;
            this.categoryDesc = categoryDesc;
            this.positionUpdater = positionUpdater;
            this.entries = new ArrayList<>();
        }

        @Override
        public int getHeight() {
            return showing ? this.entries.stream().mapToInt(AbstractEntry::getHeight).sum() + 36 : 36;
        }

        protected void addEntry(AbstractEntry<?> entry) {
            entries.add(entry);
        }

        // Time for which the entry has been hovered. Between 0 and 1.
        protected float hoveredTime;

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            boolean nameHovered = hovered && mouseY >= getY() && mouseY <= getY() + 36;
            if(nameHovered) {
                if(categoryDesc != null) {
                    context.setTooltipForNextFrame(client.font, categoryDesc, mouseX, mouseY);
                }
                if(hoveredTime < 1) {
                    hoveredTime = Math.min(1, hoveredTime + 0.1F);
                }
            }
            else {
                if(hoveredTime > 0) {
                    hoveredTime = Math.max(0, hoveredTime - 0.1F);
                }
            }
            context.fill(getX(), getY(), getX() + getWidth(), getY() + 36, ARGB.color((int) (hoveredTime * 0.2 * 255), 65, 65, 65));
            final int spacing = 8;
            context.drawString(client.font, categoryName.copy().withStyle(ChatFormatting.YELLOW), getX() + spacing, getY() + (36 - client.font.lineHeight) / 2, 0xFFFFFFFF);

            if(showing) {
                for (AbstractEntry<?> entry : this.entries) {
                    entry.renderContent(context, mouseX, mouseY, hovered, deltaTicks);
                }
            }
        }

        @Override
        protected void onReposition() {
            if(!showing) {
                return;
            }
            final int entryXOffset = 10;
            for (int i = 0; i < entries.size(); i++) {
                AbstractEntry<?> entry = entries.get(i);
                entry.setY(getY() + 36 + entries.subList(0, i).stream().mapToInt(AbstractEntry::getHeight).sum());
                entry.setX(getX() + entryXOffset);
                entry.setWidth(getWidth() - entryXOffset);
                entry.onReposition();
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if(click.y() <= getY() + 36) {
                showing = !showing;
                positionUpdater.run();
            }
            return super.mouseClicked(click, doubled);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.copyOf(this.entries);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }
}

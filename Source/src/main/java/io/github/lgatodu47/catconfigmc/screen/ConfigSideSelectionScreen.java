package io.github.lgatodu47.catconfigmc.screen;

import io.github.lgatodu47.catconfig.ConfigSide;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A screen that allows selection between multiple config screens depending on the side.
 */
public class ConfigSideSelectionScreen extends Screen implements ConfigListener {
    protected final Screen previous;
    protected final Map<ConfigSide, @NotNull ConfigScreenFactory> screenFactories;
    protected int entriesPerRow = 3;
    protected int maxRowAmount = 3;

    protected ConfigSideSelectionScreen(Text title, Screen previous, Map<ConfigSide, @NotNull ConfigScreenFactory> screenFactories) {
        super(title);
        this.previous = previous;
        this.screenFactories = screenFactories;
    }

    public void addConfigScreen(ConfigSide side, @NotNull ConfigScreenFactory screenFactory) {
        screenFactories.put(side, screenFactory);
    }

    @Override
    protected void init() {
        if(screenFactories.isEmpty()) {
            LogManager.getLogger().warn("A mod is initializing 'ConfigSideSelectionScreen' without defining first the ConfigScreenFactories! If you can determine the mod that causes this message to be logged please report it to the mod author!");
            close();
        }

        // pixels between buttons and screen borders
        final int spacing = 8;

        final int entryCount = screenFactories.size();
        // number of entries per row
        final int entriesPerRow = Math.min(this.entriesPerRow, entryCount);
        // number of rows
        final int rowAmount = Math.min(this.maxRowAmount, MathHelper.ceil((float) entryCount / entriesPerRow));

        final int maxBtnWidth = 150;
        // total free space for buttons (without taking into account the spacing)
        final int btnHorizontalSpace = this.width - spacing * (entriesPerRow + 1);
        final int btnWidth = Math.min(maxBtnWidth, btnHorizontalSpace / entriesPerRow);
        // start by offsetting from the center if there is enough space to fit the buttons without shrinking them, otherwise start from the left
        final int startX = btnHorizontalSpace > maxBtnWidth * entriesPerRow ? (this.width - btnWidth * entriesPerRow) / 2 : spacing;
        final int btnHeight = 20;
        // start y pos so that all the buttons are centered vertically
        final int startY = (this.height - (btnHeight * rowAmount + spacing * (rowAmount - 1))) / 2;

        int rowIndex = 0;
        int columnIndex = 0;
        for(Map.Entry<ConfigSide, ConfigScreenFactory> entry : screenFactories.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().sideName())).toList()) {
            // reached maximum amount of rows, break from loop
            if(columnIndex == rowAmount) {
                break;
            }

            int x = startX + rowIndex * (btnWidth + spacing);
            int y = startY + columnIndex * (btnHeight + spacing);
            addDrawableChild(ButtonWidget.builder(getNameForSide(entry.getKey()), button -> this.client.setScreen(entry.getValue().create(isParentScreen() ? this : this.previous)))
                    .dimensions(x, y, btnWidth, btnHeight)
                    .build());

            if(++rowIndex == entriesPerRow) {
                rowIndex = 0;
                columnIndex++;
            }
        }

        addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> close())
                .position((width - 200) / 2, this.height - spacing - 20)
                .width(200)
                .build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredTextWithShadow(matrices, textRenderer, title, this.width / 2, 8, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.previous);
    }

    /**
     * @param side The config side.
     * @return A Text object of the config side's name.
     */
    protected Text getNameForSide(ConfigSide side) {
        return Text.of(StringUtils.capitalize(side.sideName()));
    }

    /**
     * @return {@code true} if this screen should be considered as a 'parent' screen, meaning that every child screen
     * created by this screen will have it as parent. Look at the usage in 'init' for better understanding.
     */
    protected boolean isParentScreen() {
        return false;
    }

    /**
     * Builder with no dynamic language support.
     * @return a Builder instance with a preset title.
     */
    public static ConfigSideSelectionScreen.Builder create() {
        return create(Text.literal("Select Configuration Side..."));
    }

    /**
     * Builder with a specified title. Allows language support.
     * @param title The title of the screen that will be built.
     * @return A new Builder.
     */
    public static ConfigSideSelectionScreen.Builder create(Text title) {
        return new Builder(title);
    }

    @Override
    public void configUpdated() {
        if(isParentScreen()) {
            if (previous instanceof ConfigListener listener) {
                listener.configUpdated();
            }
        }
    }

    public static class Builder {
        private final Text title;
        protected final Map<ConfigSide, @NotNull ConfigScreenFactory> screenFactories = new HashMap<>();

        Builder(Text title) {
            this.title = title;
        }

        /**
         * Assigns the given ConfigScreenFactory to the specified ConfigSide.
         * @param side The ConfigSide object.
         * @param screenFactory The factory to associate to this side.
         * @return this
         */
        public Builder with(ConfigSide side, @NotNull ConfigScreenFactory screenFactory) {
            screenFactories.put(side, screenFactory);
            return this;
        }

        /**
         * Creates the screen using the builder info.
         * @param previous The previous screen that opened this one.
         * @return a new instance of ConfigSideSelectionScreen.
         */
        public ConfigSideSelectionScreen build(Screen previous) {
            return new ConfigSideSelectionScreen(title, previous, screenFactories);
        }
    }

    @FunctionalInterface
    public interface ConfigScreenFactory {
        ModConfigScreen create(Screen parent);
    }
}

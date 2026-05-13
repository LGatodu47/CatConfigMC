package io.github.lgatodu47.testmod;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ParentClickableWidget extends AbstractWidget {
    private final List<AbstractWidget> children;

    public ParentClickableWidget(int x, int y, int width, int height, Component message, AbstractWidget... children) {
        super(x, y, width, height, message);
        this.children = ImmutableList.copyOf(children);
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }
}

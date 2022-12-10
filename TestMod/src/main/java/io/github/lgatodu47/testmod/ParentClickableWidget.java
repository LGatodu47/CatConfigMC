package io.github.lgatodu47.testmod;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ParentClickableWidget extends ClickableWidget {
    private final List<ClickableWidget> children;

    public ParentClickableWidget(int x, int y, int width, int height, Text message, ClickableWidget... children) {
        super(x, y, width, height, message);
        this.children = ImmutableList.copyOf(children);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}

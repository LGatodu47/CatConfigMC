package io.github.lgatodu47.testmod;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.lgatodu47.catconfigmc.MinecraftConfigSides;
import io.github.lgatodu47.catconfigmc.screen.ConfigSideSelectionScreen;
import io.github.lgatodu47.catconfigmc.screen.ModConfigScreen;
import net.minecraft.text.Text;

public class TestModModMenuApi implements ModMenuApi {
    private static final ConfigSideSelectionScreen.Builder BUILDER = ConfigSideSelectionScreen.create()
            .with(MinecraftConfigSides.CLIENT, parent -> new ModConfigScreen(Text.literal("TestMod Client Config"), parent, TestModClient.CONFIG, MyRenderedOptions.Client.access()))
            .with(MinecraftConfigSides.COMMON, parent -> new ModConfigScreen(Text.literal("TestMod Common Config"), parent, TestMod.CONFIG, MyRenderedOptions.Common.access()));

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // If you want to skip the selection screen and open directly the mod config screen, you can replace it with this line:
        // return parent -> new ModConfigScreen(Text.literal("TestMod Client Config"), parent, TestModClient.CONFIG, MyRenderedOptions.Client::options);
        return BUILDER::build;
    }
}

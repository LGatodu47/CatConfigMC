package io.github.lgatodu47.catconfigmc;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public interface RenderedConfigOptionAccess {
    /**
     * @return An immutable list holding the rendered options of this builder.
     */
    List<RenderedConfigOption<?>> optionsToRender();

    /**
     * Obtains the display Text for a given category.
     * @param categoryPath The string which denotes the full category path.
     * @param fallback A fallback value in case no name was directly assigned to the category.
     * @return A Text component representing the display name of the category.
     */
    Text getNameForCategory(String categoryPath, Supplier<Text> fallback);

    /**
     * Obtains the display description for a given category.
     * @param categoryPath The full path of the category.
     * @return A Text component representing the description of the category.
     * {@code null} if no description was assigned to the given category
     */
    @Nullable
    Text getDescriptionForCategory(String categoryPath);
}

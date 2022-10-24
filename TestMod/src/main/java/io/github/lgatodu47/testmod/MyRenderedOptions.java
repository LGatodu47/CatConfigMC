package io.github.lgatodu47.testmod;

import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfigmc.RenderedConfigOption;
import io.github.lgatodu47.catconfigmc.RenderedConfigOptionBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.DyeColor;

import java.util.List;

public class MyRenderedOptions {
    public static final class Client {
        private static final RenderedConfigOptionBuilder BUILDER = new RenderedConfigOptionBuilder();

        static {
            BUILDER.ofBoolean(MyConfigOptions.BOOLEAN_OPTION).setCommonTranslationKey("bool_option").build();
            BUILDER.ofInt(MyConfigOptions.INT_OPTION).setCommonTranslationKey("int_option").build();
            BUILDER.ofInt(MyConfigOptions.LIMITED_INT_OPTION).setCommonTranslationKey("limited_int_option").build();
            BUILDER.ofLong(MyConfigOptions.LONG_OPTION).setCommonTranslationKey("long_option").build();
            BUILDER.ofDouble(MyConfigOptions.DOUBLE_OPTION).setCommonTranslationKey("double_option").build();
            BUILDER.ofString(MyConfigOptions.STRING_OPTION, false).setCommonTranslationKey("string_option").build();
            BUILDER.ofEnum(MyConfigOptions.COLOR_OPTION, DyeColor.class).setCommonTranslationKey("enum_option").build();
        }

        public static List<RenderedConfigOption<?>> options() {
            return BUILDER.optionsToRender();
        }
    }

    public static final class Common {
        private static final RenderedConfigOptionBuilder BUILDER = new RenderedConfigOptionBuilder();

        static {
//            BUILDER.option(MyConfigOptions.USER_INFO_OPTION).setCommonTranslationKey("user_info").setWidgetFactory(Common::makeUserInfoWidget).build();
            BUILDER.ofBoolean(MyConfigOptions.COMMON_BOOL).setCommonTranslationKey("common_bool").build();
        }

        private static ClickableWidget makeUserInfoWidget(ConfigAccess config) {
            return null;
        }

        public static List<RenderedConfigOption<?>> options() {
            return BUILDER.optionsToRender();
        }
    }
}

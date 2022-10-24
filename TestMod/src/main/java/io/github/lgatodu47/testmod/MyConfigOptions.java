package io.github.lgatodu47.testmod;

import io.github.lgatodu47.catconfig.ConfigOption;
import io.github.lgatodu47.catconfig.ConfigOptionAccess;
import io.github.lgatodu47.catconfig.ConfigOptionBuilder;
import io.github.lgatodu47.catconfigmc.MinecraftConfigSides;
import net.minecraft.util.DyeColor;

import java.util.UUID;

class MyConfigOptions {
    private static final ConfigOptionBuilder BUILDER = ConfigOptionBuilder.create();
    public static final ConfigOptionAccess OPTIONS = BUILDER;

    static {
        BUILDER.onSides(MinecraftConfigSides.CLIENT);
    }
    public static final ConfigOption<Boolean> BOOLEAN_OPTION = BUILDER.createBool("bool_option", false);
    public static final ConfigOption<Integer> INT_OPTION = BUILDER.createInt("int_option", 0);
    public static final ConfigOption<Integer> LIMITED_INT_OPTION = BUILDER.createInt("limited_int_option", 3, -48, 256);
    public static final ConfigOption<Long> LONG_OPTION = BUILDER.createLong("long_option", 973231L, -64961320216986L, 6496849874612234854L);
    public static final ConfigOption<Double> DOUBLE_OPTION = BUILDER.createDouble("double_option", 44.8D);
    public static final ConfigOption<String> STRING_OPTION = BUILDER.createString("string_option", null);
    public static final ConfigOption<DyeColor> COLOR_OPTION = BUILDER.createEnum("color_option", DyeColor.class, DyeColor.WHITE);

    static {
        BUILDER.onSides(MinecraftConfigSides.COMMON);
    }
    public static final ConfigOption<Boolean> COMMON_BOOL = BUILDER.createBool("common_bool", true);
    public static final ConfigOption<UserInfoTest> USER_INFO_OPTION = BUILDER.put(new UserInfoTest.Option("user_info_test", new UserInfoTest("MyName", UUID.fromString("ba47dd18-1869-4fa3-850a-df0ddf371202"), 34)));
}

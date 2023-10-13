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
    public static final ConfigOption<Integer> INT_OPTION = BUILDER.createInt("int_option").category("numbers/ints").defaultValue(0).make();
    public static final ConfigOption<Integer> LIMITED_INT_OPTION = BUILDER.createInt("limited_int_option").category("numbers/ints").defaultValue(3).bounds(-48, 256).make();
    public static final ConfigOption<Long> LONG_OPTION = BUILDER.createLong("long_option").category("numbers").defaultValue(973231L).bounds(-64961320216986L, 6496849874612234854L).make();
    public static final ConfigOption<Double> DOUBLE_OPTION = BUILDER.createDouble("double_option").category("numbers/doubles").defaultValue(44.8D).make();
    public static final ConfigOption<Double> LIMITED_DOUBLE_OPTION = BUILDER.createDouble("limited_double_option").category("numbers/doubles").defaultValue(12D).bounds(1D, 90D).make();
    public static final ConfigOption<Boolean> BOOLEAN_OPTION = BUILDER.createBool("bool_option").defaultValue(false).make();
    public static final ConfigOption<String> STRING_OPTION = BUILDER.createString("string_option").make();
    public static final ConfigOption<DyeColor> COLOR_OPTION = BUILDER.createEnum("color_option", DyeColor.class).category("custom").defaultValue(DyeColor.WHITE).make();

    static {
        BUILDER.onSides(MinecraftConfigSides.COMMON);
    }
    public static final ConfigOption<Boolean> COMMON_BOOL = BUILDER.createBool("common_bool").defaultValue(true).make();
    public static final ConfigOption<Integer> SMOL_INT = BUILDER.createInt("smol_int").defaultValue(4).bounds(2, 8).make();
    public static final ConfigOption<UserInfoTest> USER_INFO_OPTION = BUILDER.put(new UserInfoTest.Option("user_info_test", new UserInfoTest("MyName", UUID.fromString("ba47dd18-1869-4fa3-850a-df0ddf371202"), 34)));
}

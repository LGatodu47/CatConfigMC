package io.github.lgatodu47.catconfigmc;

import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.apache.commons.lang3.function.FailableFunction;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;

// Package-private class that creates all the builtin widgets for specific option types.
final class BuiltinWidgets {
    // The maximum number of digits an int value can have.
    private static final int INT_MAX_DIGITS = numDigits(Integer.MAX_VALUE);
    // The maximum number of digits a long value can have.
    private static final int LONG_MAX_DIGITS = Long.toString(Long.MAX_VALUE).length();

    static ClickableWidget createBoolWidget(ConfigAccess config, ConfigOption<Boolean> option) {
        return new ButtonWidget(0, 0, 100, 20, Text.empty(), button -> config.put(option, !config.getOrDefault(option, false))) {
            @Override
            public Text getMessage() {
                return Text.literal(Boolean.toString(config.getOrDefault(option, false)));
            }
        };
    }

    static ClickableWidget createIntWidget(ConfigAccess config, ConfigOption<Integer> option) {
        int space = getSpaceForIntOption(option);
        TextFieldWidget widget = createNumberWidget(config, option, Math.min(space * 10, 100), i -> Integer.toString(i), Integer::parseInt, Math::min, Math::max, false);
        widget.setMaxLength(space);
        return widget;
    }

    static ClickableWidget createLongWidget(ConfigAccess config, ConfigOption<Long> option) {
        TextFieldWidget widget = createNumberWidget(config, option, 100, l -> Long.toString(l), Long::parseLong, Math::min, Math::max, false);
        widget.setMaxLength(LONG_MAX_DIGITS + 1);
        return widget;
    }

    private static final DecimalFormat FORMAT = Util.make(new DecimalFormat("#"), format -> format.setMaximumFractionDigits(8));

    static ClickableWidget createDoubleWidget(ConfigAccess config, ConfigOption<Double> option) {
        TextFieldWidget widget = createNumberWidget(config, option, 100, FORMAT::format, Double::parseDouble, Math::min, Math::max, true);
        widget.setMaxLength(64);
        return widget;
    }

    static ClickableWidget createStringWidget(ConfigAccess config, ConfigOption<String> option, boolean extendedLength) {
        TextFieldWidget widget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 100, 20, Text.empty());
        widget.setText(config.getOrDefault(option, ""));
        widget.setMaxLength(extendedLength ? 256 : 64);
        widget.setChangedListener(s -> config.put(option, s.isEmpty() ? null : s));
        return widget;
    }

    static <E extends Enum<E>> ClickableWidget createEnumWidget(ConfigAccess config, ConfigOption<E> option, Class<E> enumClass) {
        CyclingButtonWidget.Builder<E> builder = CyclingButtonWidget.builder(e -> Text.literal(e.toString().toUpperCase()));
        builder.values(enumClass.getEnumConstants());
        E val = config.getOrDefault(option);
        if(val != null) {
            builder.initially(val);
        }
        builder.omitKeyText();
        return builder.build(0, 0, 100, 20, Text.empty(), (button, value) -> config.put(option, value));
    }

    /**
     * Creates a TextFieldWidget for number options.
     * @param config The config.
     * @param option The config option represented by the returned widget.
     * @param widgetWidth The width of the widget.
     * @param toString A function that parses the number to a String.
     * @param parser A function that parses a String to a number of this type.
     * @param minFunc A function that returns the smallest number from two numbers of type N.
     * @param maxFunc A function that returns the largest number from two numbers of type N.
     * @param acceptFloatingPoint A boolean that indicates if the TextField's predicate and listener functions should accept floating-point notation (used for Double options)
     * @return A TextFieldWidget that represents the given option.
     * @param <N> The type of Number of the config option.
     */
    private static <N extends Number> TextFieldWidget createNumberWidget(ConfigAccess config, ConfigOption<N> option, int widgetWidth, Function<N, String> toString, FailableFunction<String, N, NumberFormatException> parser, BinaryOperator<N> minFunc, BinaryOperator<N> maxFunc, boolean acceptFloatingPoint) {
        TextFieldWidget widget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, widgetWidth, 20, Text.empty());
        widget.setText(Optional.ofNullable(config.getOrDefault(option)).map(toString).orElse(""));
        widget.setTextPredicate(s -> {
            if(s.isEmpty() || s.equals("-") || (acceptFloatingPoint && s.equals("."))) {
                return true;
            }
            try {
                parser.apply(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        widget.setChangedListener(s -> {
            if(s.isEmpty() || s.equals("-") || (acceptFloatingPoint && s.equals("."))) {
                config.put(option, null);
                return;
            }
            try {
                final N parsed = parser.apply(s);
                N clamped = clamped(parsed, option, minFunc, maxFunc);
                if(!Objects.equals(parsed, clamped)) {
                    widget.setText(toString.apply(clamped));
                }
                config.put(option, clamped);
            } catch (NumberFormatException ignored) {
            }
        });
        return widget;
    }

    /**
     * Calculates the number of digits required for the given option.
     * If the option is a NumberOption, it will compare the number of digits of
     * the minimum value and of the maximum value. Otherwise, it will return the
     * maximum number of digits an int can have + 1 for the sign.
     *
     * @param option The integer config option.
     * @return The number of digits required for this option.
     */
    private static int getSpaceForIntOption(ConfigOption<Integer> option) {
        int res = INT_MAX_DIGITS + 1;
        if(option instanceof ConfigOption.NumberOption<Integer> numberOption) {
            Integer min = numberOption.min();
            Integer max = numberOption.max();

            if(min != null && max != null) {
                // Include the negative sign
                int added = min < 0 ? 1 : 0;
                res = Math.max(numDigits(min) + added, numDigits(max));
            }
        }
        return res;
    }

    // Fast method to calculate number of digits of a number
    private static int numDigits(int val) {
        val = Math.abs(val);
        int n = 1;
        if (val >= 100000000) {
            n += 8;
            val /= 100000000;
        }
        if (val >= 10000) {
            n += 4;
            val /= 10000;
        }
        if (val >= 100) {
            n += 2;
            val /= 100;
        }
        if (val >= 10) {
            n += 1;
        }
        return n;
    }

    /**
     * Clamps a number using the given min and max function.
     *
     * @param num The number to clamp.
     * @param option The ConfigOption holding the minimum and maximum value the given value can have.
     * @param minFunc The min function that can apply to the given number.
     * @param maxFunc The max function that can apply to the given number.
     * @return A number that is between the option's minimum value and maximum value.
     * @param <N> The type of Number.
     */
    private static <N extends Number> N clamped(N num, ConfigOption<N> option, BinaryOperator<N> minFunc, BinaryOperator<N> maxFunc) {
        N res = num;
        if(option instanceof ConfigOption.NumberOption<N> numberOption) {
            N min = numberOption.min();
            N max = numberOption.max();

            if(min != null) {
                res = maxFunc.apply(res, min);
            }
            if(max != null) {
                res = minFunc.apply(res, max);
            }
        }
        return res;
    }
}

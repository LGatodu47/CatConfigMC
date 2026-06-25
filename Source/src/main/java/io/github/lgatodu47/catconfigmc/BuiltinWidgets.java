package io.github.lgatodu47.catconfigmc;

import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import io.github.lgatodu47.catconfig.NumberOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.function.FailableFunction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

// Package-private class that creates all the builtin widgets for specific option types.
final class BuiltinWidgets {
    // The maximum number of digits an int value can have.
    private static final int INT_MAX_DIGITS = numDigits(Integer.MAX_VALUE);
    // The maximum number of digits a long value can have.
    private static final int LONG_MAX_DIGITS = Long.toString(Long.MAX_VALUE).length();

    static AbstractWidget createBoolWidget(ConfigAccess config, ConfigOption<Boolean> option) {
        return new Button.Plain(0, 0, 100, 20, Component.empty(), button -> config.put(option, config.get(option).map(b -> !b).orElse(false)), Supplier::get) {
            @Override
            public net.minecraft.network.chat.Component getMessage() {
                return config.get(option).map(Object::toString).map(net.minecraft.network.chat.Component::nullToEmpty).orElseGet(super::getMessage);
            }
        };
    }

    static AbstractWidget createIntWidget(ConfigAccess config, ConfigOption<Integer> option) {
        int space = getSpaceForIntOption(option);
        OldEditBox widget = createNumberWidget(config, option, Mth.clamp(space * 10, 20, 100), String::valueOf, Integer::parseInt, Math::min, Math::max, false);
        widget.setMaxLength(space);
        return widget;
    }

    static AbstractWidget createLongWidget(ConfigAccess config, ConfigOption<Long> option) {
        OldEditBox widget = createNumberWidget(config, option, 100, String::valueOf, Long::parseLong, Math::min, Math::max, false);
        widget.setMaxLength(LONG_MAX_DIGITS + 1);
        return widget;
    }

    private static final DecimalFormat FORMAT = Util.make(new DecimalFormat("#"), format -> {
        format.setMaximumFractionDigits(8);
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(symbols);
    });

    static AbstractWidget createDoubleWidget(ConfigAccess config, ConfigOption<Double> option) {
        OldEditBox widget = createNumberWidget(config, option, 100, FORMAT::format, Double::parseDouble, Math::min, Math::max, true);
        widget.setMaxLength(64);
        return widget;
    }

    static AbstractWidget createStringWidget(ConfigAccess config, ConfigOption<String> option, boolean extendedLength) {
        EditBox widget = new EditBox(Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        widget.setValue(config.getOrFallback(option, ""));
        widget.setMaxLength(extendedLength ? 256 : 64);
        widget.setResponder(s -> config.put(option, s));
        return widget;
    }

    static <E extends Enum<E>> AbstractWidget createEnumWidget(ConfigAccess config, ConfigOption<E> option, Class<E> enumClass) {
        if(enumClass.getEnumConstants().length == 0) {
            throw new IllegalArgumentException("Can't create widget of an empty enum!");
        }
        E init = config.getOrFallback(option, enumClass.getEnumConstants()[0]);
        CycleButton.Builder<E> builder = CycleButton.builder(e -> Component.literal(e.toString().toUpperCase()), init);
        builder.withValues(enumClass.getEnumConstants());
        builder.displayOnlyValue();
        return builder.create(0, 0, 100, 20, Component.empty(), (button, value) -> config.put(option, value));
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
    private static <N extends Number> OldEditBox createNumberWidget(ConfigAccess config, ConfigOption<N> option, int widgetWidth, Function<N, String> toString, FailableFunction<String, N, NumberFormatException> parser, BinaryOperator<N> minFunc, BinaryOperator<N> maxFunc, boolean acceptFloatingPoint) {
        OldEditBox widget = new OldEditBox(Minecraft.getInstance().font, 0, 0, widgetWidth, 20, Component.empty());
        widget.setValue(config.get(option).map(toString).orElse(""));
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
        widget.setResponder(s -> {
            if(s.isEmpty() || s.equals("-") || (acceptFloatingPoint && s.equals("."))) {
                config.put(option, null);
                return;
            }
            try {
                final N parsed = parser.apply(s);
                N clamped = clamped(parsed, option, minFunc, maxFunc);
                if(!Objects.equals(parsed, clamped)) {
                    widget.setValue(toString.apply(clamped));
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
        if(option instanceof NumberOption<Integer> numberOption) {
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
        if(option instanceof NumberOption<N> numberOption) {
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

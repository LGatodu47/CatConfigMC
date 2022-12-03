package io.github.lgatodu47.catconfigmc;

import com.google.common.collect.ImmutableList;
import io.github.lgatodu47.catconfig.ConfigAccess;
import io.github.lgatodu47.catconfig.ConfigOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

// Package-private class that creates all the builtin widgets for specific option types.
final class BuiltinWidgets {
    // The maximum number of digits an int value can have.
    private static final int INT_MAX_DIGITS = numDigits(Integer.MAX_VALUE);
    // The maximum number of digits a long value can have.
    private static final int LONG_MAX_DIGITS = Long.toString(Long.MAX_VALUE).length();

    static ClickableWidget createBoolWidget(ConfigAccess config, ConfigOption<Boolean> option) {
        return new ButtonWidget(0, 0, 100, 20, LiteralText.EMPTY, button -> config.put(option, config.get(option).map(b -> !b).orElse(false))) {
            @Override
            public Text getMessage() {
                return config.get(option).map(Object::toString).map(Text::of).orElseGet(super::getMessage);
            }
        };
    }

    static ClickableWidget createIntWidget(ConfigAccess config, ConfigOption<Integer> option) {
        int space = getSpaceForIntOption(option);
        TextFieldWidget widget = createNumberWidget(config, option, MathHelper.clamp(space * 10, 20, 100), String::valueOf, Integer::parseInt, Math::min, Math::max, false);
        widget.setMaxLength(space);
        return widget;
    }

    static ClickableWidget createLongWidget(ConfigAccess config, ConfigOption<Long> option) {
        TextFieldWidget widget = createNumberWidget(config, option, 100, String::valueOf, Long::parseLong, Math::min, Math::max, false);
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
        TextFieldWidget widget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 100, 20, LiteralText.EMPTY);
        widget.setText(config.getOrFallback(option, ""));
        widget.setMaxLength(extendedLength ? 256 : 64);
        widget.setChangedListener(s -> config.put(option, s));
        return widget;
    }

    static <E extends Enum<E>> ClickableWidget createEnumWidget(ConfigAccess config, ConfigOption<E> option, Class<E> enumClass) {
        return CyclingButtonWidget.create(0, 0, 100, 20,
                config.get(option).orElse(null),
                e -> new LiteralText(e.toString().toUpperCase()),
                value -> config.put(option, value),
                enumClass.getEnumConstants());
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
    private static <N extends Number> TextFieldWidget createNumberWidget(ConfigAccess config, ConfigOption<N> option, int widgetWidth, Function<N, String> toString, ThrowableFunction<String, N, NumberFormatException> parser, BinaryOperator<N> minFunc, BinaryOperator<N> maxFunc, boolean acceptFloatingPoint) {
        TextFieldWidget widget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, widgetWidth, 20, LiteralText.EMPTY);
        widget.setText(config.get(option).map(toString).orElse(""));
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
        if(option instanceof ConfigOption.NumberOption) {
            ConfigOption.NumberOption<Integer> numberOption = (ConfigOption.NumberOption<Integer>) option;
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
        if(option instanceof ConfigOption.NumberOption) {
            ConfigOption.NumberOption<N> numberOption = (ConfigOption.NumberOption<N>) option;
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

    @FunctionalInterface
    interface ThrowableFunction<T, R, X extends Throwable> {
        R apply(T t) throws X;
    }

    private static final class CyclingButtonWidget<T> extends PressableWidget {
        private int index;
        private final ImmutableList<T> values;
        private final Function<T, Text> valueToText;
        private final Consumer<T> callback;

        CyclingButtonWidget(int x, int y, int width, int height, Text message, int index, ImmutableList<T> values, Function<T, Text> valueToText, Consumer<T> callback) {
            super(x, y, width, height, message);
            this.index = index;
            this.values = values;
            this.valueToText = valueToText;
            this.callback = callback;
        }

        @Override
        public void onPress() {
            if (Screen.hasShiftDown()) {
                this.cycle(-1);
            } else {
                this.cycle(1);
            }
        }

        private void cycle(int amount) {
            List<T> list = this.values;
            this.index = MathHelper.floorMod(this.index + amount, list.size());
            T val = list.get(this.index);
            this.internalSetValue(val);
            this.callback.accept(val);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            if (amount > 0.0) {
                this.cycle(-1);
            } else if (amount < 0.0) {
                this.cycle(1);
            }

            return true;
        }

        private void internalSetValue(T value) {
            Text text = this.valueToText.apply(value);
            this.setMessage(text);
        }

        @SafeVarargs
        static <T> CyclingButtonWidget<T> create(int x, int y, int width, int height, @Nullable T initialValue, Function<T, Text> valueToText, Consumer<T> callback, T... values) {
            ImmutableList<T> list = ImmutableList.copyOf(values);
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                int index = Math.max(0, list.indexOf(initialValue));
                T value = initialValue != null ? initialValue : list.get(index);
                Text message = valueToText.apply(value);
                return new CyclingButtonWidget<>(x, y, width, height, message, index, list, valueToText, callback);
            }
        }
    }
}

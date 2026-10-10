package com.mrcrayfish.guns.client.util;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 *
 * All easing functions ported from 1.19.X. Each constant maps a normalised
 * time (0.0 - 1.0) to an eased value.
 */
public enum Easings
{
    LINEAR("linear", t -> t),
    EASE_IN_QUAD("ease_in_quad", t -> t * t),
    EASE_IN_CUBIC("ease_in_cubic", t -> t * t * t),
    EASE_IN_CIRC("ease_in_circ", t -> 1.0F - (float) Math.sqrt(1.0F - t * t)),
    EASE_OUT_QUAD("ease_out_quad", t -> 1.0F - (1.0F - t) * (1.0F - t)),
    EASE_OUT_CUBIC("ease_out_cubic", t -> 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t)),
    EASE_OUT_CIRC("ease_out_circ", t -> (float) Math.sqrt(1.0F - (t - 1.0F) * (t - 1.0F))),
    EASE_IN_OUT_QUAD("ease_in_out_quad", t -> t < 0.5F ? 2.0F * t * t : 1.0F - ((-2.0F * t + 2.0F) * (-2.0F * t + 2.0F)) / 2.0F),
    EASE_IN_OUT_CUBIC("ease_in_out_cubic", t -> t < 0.5F ? 4.0F * t * t * t : 1.0F - ((-2.0F * t + 2.0F) * (-2.0F * t + 2.0F) * (-2.0F * t + 2.0F)) / 2.0F),
    EASE_IN_OUT_CIRC("ease_in_out_circ", t -> t < 0.5F ? (1.0F - (float) Math.sqrt(1.0F - (2.0F * t) * (2.0F * t))) / 2.0F : ((float) Math.sqrt(1.0F - (-2.0F * t + 2.0F) * (-2.0F * t + 2.0F)) + 1.0F) / 2.0F),
    HALF_EASE_IN("half_ease_in", t -> t > 0.5F ? EASE_IN_QUAD.apply((t - 0.5F) / 0.5F) : 0.0F),
    ZERO("zero", t -> 0F);

    private static final Map<String, Easings> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((easing) -> easing.name, (easing) -> easing));

    private final String name;
    private final Function<Float, Float> function;

    Easings(String name, Function<Float, Float> function)
    {
        this.name = name;
        this.function = function;
    }

    public float apply(float time)
    {
        return this.function.apply(time);
    }

    public double apply(double time)
    {
        return this.function.apply(((Double) time).floatValue());
    }

    public String getName()
    {
        return this.name;
    }

    public static Easings byName(String name)
    {
        return BY_NAME.getOrDefault(name, Easings.LINEAR);
    }
}
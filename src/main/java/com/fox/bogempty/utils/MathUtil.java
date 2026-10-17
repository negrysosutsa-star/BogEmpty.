package com.fox.bogempty.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {
    private static final Random random = new Random();

    // Линейная интерполяция (плавное движение)
    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    // Ограничение значения (Clamp)
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // Случайное число в диапазоне
    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    // GCD Fix (для обхода античита, привязка к сенсе мыши)
    public static float applyGcd(float delta, float sensitivity) {
        if (delta == 0f) return 0f;
        float f = sensitivity * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;
        return Math.round(delta / gcd) * gcd;
    }
}
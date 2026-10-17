package com.fox.bogempty.utils;

import net.minecraft.entity.Entity;
import java.util.Random;

public class FreeAim extends AimMode {
    private final Random random = new Random();
    private float jitterX = 0;
    private float jitterY = 0;

    public FreeAim() {
        super("FreeAim");
    }

    @Override
    public double[] getAimPoint(Entity target) {
        // Целимся в центр + случайный разброс (чтобы не палиться)
        double x = 0;
        double y = target.getHeight() * 0.6; // Грудь
        double z = 0;

        // Добавляем рандом, чтобы не бить в одну точку идеально
        x += (random.nextDouble() - 0.5) * 0.1;
        y += (random.nextDouble() - 0.5) * 0.1;
        z += (random.nextDouble() - 0.5) * 0.1;

        return new double[]{x, y, z};
    }

    @Override
    public void update() {
        // Генерируем новый шум каждый тик
        jitterX = (random.nextFloat() - 0.5f) * 0.2f;
        jitterY = (random.nextFloat() - 0.5f) * 0.2f;
    }
}
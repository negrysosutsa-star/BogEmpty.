package com.fox.bogempty.utils;

import net.minecraft.entity.Entity;

public abstract class AimMode {
    private final String name;

    public AimMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Точка наводки (голова, грудь, ноги)
    public abstract double[] getAimPoint(Entity target);

    // Обновление (сброс паттернов)
    public void update() {}
}
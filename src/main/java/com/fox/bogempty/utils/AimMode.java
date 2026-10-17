package com.fox.bogempty.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public abstract class AimMode {
    private final String name;

    public AimMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Здесь мы возвращаем точку на хитбоксе цели (голова, грудь, ноги или динамически)
    public abstract double[] getAimPoint(Entity target);

    // Этот метод обновляется каждый тик, чтобы сбрасывать паттерны наводки
    public void update() {}
}
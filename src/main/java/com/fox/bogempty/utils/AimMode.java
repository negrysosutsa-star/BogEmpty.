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

    // Возвращает точку наводки (смещение по X, Y, Z от центра хитбокса)
    public abstract double[] getAimPoint(Entity target);

    // Вызывается каждый тик для обновления рандома/паттернов
    public abstract void update();

    // Главный метод расчета дельты (вызывается из AimAssist)
    public abstract double[] calculateDelta(double yawDelta, double pitchDelta, double dist, long timeDelta);
}
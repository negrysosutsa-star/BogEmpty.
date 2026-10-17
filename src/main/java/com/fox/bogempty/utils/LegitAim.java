package com.fox.bogempty.utils;

import net.minecraft.entity.Entity;

public class LegitAim extends AimMode {
    public LegitAim() {
        super("LegitAim");
    }

    @Override
    public double[] getAimPoint(Entity target) {
        // Целимся строго в голову (для легитности)
        return new double[]{0, target.getHeight() * 0.85, 0};
    }
}
package com.fox.bogempty.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

public class FreeAim extends AimMode {
    private double value2, value3, value4, value5, value6, value7, value8, value9, value10;
    private boolean flag;
    private double value11, value12, value13, value14;
    
    // Настройки (пока захардкожены для простоты, можно вынести в Setting)
    private double speed = 15.0;
    private double strengthKorrekcii = 0.5;
    private double svobodaVniz = 0.0;
    private double porogZamedl = 6.0;

    public FreeAim() {
        super("Свободная");
    }

    @Override
    public void update() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        this.value2 = rnd.nextDouble(Math.PI * 2);
        this.value3 = rnd.nextDouble(Math.PI * 2);
        this.value4 = rnd.nextDouble(0.06, 0.18);
        this.value5 = rnd.nextDouble(0.04, 0.14);
        this.value6 = rnd.nextDouble(-0.08, 0.08);
        this.value7 = 0.0;
        this.value8 = rnd.nextDouble(0.85, 1.15);
        this.value9 = 0.0;
        this.value10 = 0.0;
        this.flag = false;
        this.value14 = 0.0;
    }

    // Главный метод расчета (вызывается из AimAssist каждый кадр)
    public double[] calculateDelta(double yawDelta, double pitchDelta, double angleDist, double dist, long timeDeltaMs) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        PlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return new double[]{yawDelta * 0.1, pitchDelta * 0.1};

        // Проверка на хитбокс (мертвая зона)
        boolean onTarget = isOnTarget(p, angleDist);
        double targetWeight = onTarget ? 0.0 : 1.0;

        // Динамическая агрессия
        if (!onTarget) {
            this.value10 = Math.min(this.value10 + 0.06 * dist, 3.0);
        } else {
            this.value10 = this.value10 * Math.max(0.0, 1.0 - 0.15 * dist);
        }

        double d1 = onTarget ? 0.08 : 0.08 + this.value10 * 0.04;
        this.value9 = this.value9 + (targetWeight - this.value9) * d1 * dist;
        this.value9 = MathHelper.clamp(this.value9, 0.0, 1.0);

        double d2 = strengthKorrekcii * this.value7 * this.value8;
        double d21 = MathHelper.clamp(timeDeltaMs / (450.0 + rnd.nextDouble(150.0)), 0.0, 1.0);
        double d5 = d21 * d21 * (3.0 - 2.0 * d21); // Smoothstep
        double d6 = dist * d2 * d5;

        if (angleDist < porogZamedl) {
            double d8 = Math.max(0.2, angleDist / porogZamedl);
            d6 *= d8;
        }

        d6 *= getSpeedMultiplier(angleDist);
        d6 *= 1.0 + this.value10 * 0.5;

        if (angleDist > 80.0 && !this.flag) {
            this.flag = true;
            this.value11 = rnd.nextDouble(2.0, 3.5);
            this.value12 = rnd.nextDouble(5.0, 12.0);
            this.value13 = rnd.nextDouble(0.15, 0.4);
            this.value14 = rnd.nextDouble(Math.PI * 2);
        }

        if (angleDist < 6.0 && this.flag) this.flag = false;

        if (this.flag) {
            double d23 = Math.sin(this.value14) * this.value13;
            d6 *= this.value11 * (1.0 + d23);
        }

        double d24 = dist / 15.0;
        double d9 = rnd.nextDouble(-0.002, 0.002);
        double d10 = MathHelper.clamp(d6 * 0.03 * d24 + d9, 0.003, 0.85);
        double d11 = yawDelta * d10;
        double d12 = pitchDelta * d10 * (0.85 + rnd.nextDouble(0.3));

        // Шум при наводке
        double d13 = 0.05 + rnd.nextDouble(0.07);
        d11 += Math.sin(this.value2) * d13 * (0.5 + rnd.nextDouble(0.6));
        d12 += Math.cos(this.value3) * d13 * 0.45 * (0.4 + rnd.nextDouble(0.5));

        if (this.value10 > 0.3 && !onTarget) {
            double d25 = MathHelper.clamp(this.value10 * 0.07, 0.02, 0.2);
            double d14 = yawDelta * d25;
            double d15 = pitchDelta * d25 * 0.85;
            if (Math.abs(d11) < Math.abs(d14)) d11 = d14;
            if (Math.abs(d12) < Math.abs(d15)) d12 = d15;
        }

        double d16 = (1.8 + rnd.nextDouble(1.2)) * dist * (1.0 + Math.min(angleDist / 30.0, 4.0) * d24);
        double d17 = (1.3 + rnd.nextDouble(0.8)) * dist * (1.0 + Math.min(angleDist / 30.0, 4.0) * d24);

        return new double[]{d11, d12};
    }

    private double getSpeedMultiplier(double angleDist) {
        if (angleDist < 1.5) return 0.12 + angleDist * 0.12;
        if (angleDist < 8.0) return 0.3 + ((angleDist - 1.5) / 6.5) * 0.4;
        if (angleDist < 40.0) {
            double d2 = (angleDist - 8.0) / 32.0;
            return 0.7 + (d2 * d2 * (3.0 - 2.0 * d2)) * 1.8;
        }
        return 2.5 + Math.min((angleDist - 40.0) / 140.0, 1.0) * 2.5;
    }

    private boolean isOnTarget(PlayerEntity p, double angleDist) {
        if (angleDist > 45.0) return false;
        // Упрощенная проверка (в оригинале там рейкаст по всем энтити)
        return angleDist < 2.0;
    }

    @Override
    public double[] getAimPoint(Entity target) {
        return new double[]{0.0, target.getHeight() * 0.6, 0.0};
    }
}
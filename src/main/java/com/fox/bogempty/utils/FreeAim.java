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

    public FreeAim() {
        super("FreeAim");
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

    @Override
    public double[] calculateDelta(double yawDelta, double pitchDelta, double dist, long timeDelta) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        PlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return new double[]{yawDelta * 0.1, pitchDelta * 0.1};

        double angleDist = Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
        boolean onTarget = angleDist < 2.0;
        double targetWeight = onTarget ? 0.0 : 1.0;

        if (!onTarget) {
            this.value10 = Math.min(this.value10 + 0.06 * dist, 3.0);
        } else {
            this.value10 = this.value10 * Math.max(0.0, 1.0 - 0.15 * dist);
        }

        double d1 = onTarget ? 0.08 : 0.08 + this.value10 * 0.04;
        this.value9 = this.value9 + (targetWeight - this.value9) * d1 * dist;
        this.value9 = MathHelper.clamp(this.value9, 0.0, 1.0);

        double d2 = 0.5 * this.value7 * this.value8;
        double d21 = MathHelper.clamp(timeDelta / (450.0 + rnd.nextDouble(150.0)), 0.0, 1.0);
        double d5 = d21 * d21 * (3.0 - 2.0 * d21);
        double d6 = dist * d2 * d5;

        if (angleDist < 6.0) {
            double d8 = Math.max(0.2, angleDist / 6.0);
            d6 *= d8;
        }

        double d24 = dist / 15.0;
        double d9 = rnd.nextDouble(-0.002, 0.002);
        double d10 = MathHelper.clamp(d6 * 0.03 * d24 + d9, 0.003, 0.85);
        double d11 = yawDelta * d10;
        double d12 = pitchDelta * d10 * (0.85 + rnd.nextDouble(0.3));

        double d13 = 0.05 + rnd.nextDouble(0.07);
        d11 += Math.sin(this.value2) * d13 * (0.5 + rnd.nextDouble(0.6));
        d12 += Math.cos(this.value3) * d13 * 0.45 * (0.4 + rnd.nextDouble(0.5));

        return new double[]{d11, d12};
    }

    @Override
    public double[] getAimPoint(Entity target) {
        // Динамическая точка (грудь/голова)
        return new double[]{0.0, target.getHeight() * 0.6, 0.0};
    }
}
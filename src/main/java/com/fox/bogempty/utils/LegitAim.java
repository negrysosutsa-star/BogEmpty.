package com.fox.bogempty.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import java.security.SecureRandom;

public class LegitAim extends AimMode {
    private final SecureRandom secureRandom = new SecureRandom();
    private int value = 0;
    private double value2 = 0.0;
    private long time = System.currentTimeMillis();
    private final double[] doubleArray = new double[15];
    private final float[] floatArray = new float[5];
    private int value3 = 0;
    private float value4 = 0f;
    private long time3 = System.currentTimeMillis();

    public LegitAim() {
        super("LegitAim");
    }

    @Override
    public void update() {
        this.value = 0;
        this.value2 = 0.0;
        this.time = System.currentTimeMillis();
        this.value4 = 0.0F;
        this.time3 = System.currentTimeMillis();
        this.value3 = 0;
        java.util.Arrays.fill(this.floatArray, 0.0F);
    }

    private float getFloatByFloat2(float angleDist) {
        long i = System.currentTimeMillis() - this.time3;
        float f1 = switch (this.value) {
            case 0 -> 0.6F + secureRandom.nextFloat() * 0.4F;
            case 1 -> 0.8F + secureRandom.nextFloat() * 0.3F;
            case 2 -> 0.5F + secureRandom.nextFloat() * 0.3F;
            case 3 -> 0.7F + secureRandom.nextFloat() * 0.5F;
            default -> 0.7F + secureRandom.nextFloat() * 0.4F;
        } * (0.9F + secureRandom.nextFloat() * 0.3F);

        if (i < 45L) f1 *= 0.7F + secureRandom.nextFloat() * 0.4F;
        else if (i > 300L) f1 *= 1.1F + secureRandom.nextFloat() * 0.4F;

        if (angleDist > 120.0F) f1 *= 1.3F;
        else if (angleDist < 15.0F) f1 *= 0.5F + secureRandom.nextFloat() * 0.4F;

        if (this.value4 > 0.0F) {
            float f = f1 - this.value4;
            if (Math.abs(f) > 0.35F) {
                f1 = this.value4 + (f > 0.0F ? 0.35F : -0.35F);
            }
        }

        float f2 = MathHelper.clamp(f1, 0.15F, 1.8F);
        this.value4 = f2;
        this.time3 = System.currentTimeMillis();
        return f2;
    }

    private float getFloat() {
        float f = 0.0F;
        int i = 0;
        for (float f1 : this.floatArray) {
            if (f1 > 0.0F) { f += f1; i++; }
        }
        return i > 0 ? f / i : (this.value4 > 0.0F ? this.value4 : 1.0F);
    }

    @Override
    public double[] calculateDelta(double yawDelta, double pitchDelta, double dist, long timeDelta) {
        float f = (float) Math.abs(yawDelta);
        float f1 = this.getFloatByFloat2(f);
        this.floatArray[this.value3] = f1;
        this.value3 = (this.value3 + 1) % this.floatArray.length;
        float f2 = this.getFloat();
        
        double d0 = 1.0; // smooth
        double d1 = dist / 3.0 * (Math.abs(yawDelta) + Math.abs(pitchDelta)) / d0;
        
        double d4 = MathHelper.clamp(d1 * f2, 0.05, 1.5);
        
        double stepYaw = yawDelta * d4;
        double stepPitch = pitchDelta * d4;
        
        return new double[]{stepYaw, stepPitch};
    }

    @Override
    public double[] getAimPoint(Entity target) {
        // Целимся в голову
        return new double[]{0.0, target.getHeight() * 0.85, 0.0};
    }
}
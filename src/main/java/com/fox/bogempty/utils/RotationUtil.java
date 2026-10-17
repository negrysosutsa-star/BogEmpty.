package com.fox.bogempty.utils;

import com.fox.bogempty.config.Config;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class RotationUtil {

    public static void applyRotation(ClientPlayerEntity p, float targetYaw, float targetPitch, float speed) {
        float yawDelta = MathHelper.wrapDegrees(targetYaw - p.getYaw());
        float pitchDelta = targetPitch - p.getPitch();

        float stepYaw = yawDelta * (speed / 100f);
        float stepPitch = pitchDelta * (speed / 100f);

        // Ограничиваем максимальный шаг за тик
        float maxStep = 30f;
        stepYaw = MathHelper.clamp(stepYaw, -maxStep, maxStep);
        stepPitch = MathHelper.clamp(stepPitch, -maxStep, maxStep);

        // Ставим итоговые углы
        p.setYaw(p.getYaw() + stepYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + stepPitch, -90f, 90f));
    }
}
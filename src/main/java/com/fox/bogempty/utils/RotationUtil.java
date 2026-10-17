package com.fox.bogempty.utils;

import com.fox.bogempty.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class RotationUtil {
    
    // Правильный поворот с учетом сенсы мыши (GCD Fix)
    public static void applyRotation(ClientPlayerEntity p, float targetYaw, float targetPitch, float speed) {
        float yawDelta = MathHelper.wrapDegrees(targetYaw - p.getYaw());
        float pitchDelta = targetPitch - p.getPitch();

        // Скорость доводки (чем больше, тем быстрее)
        float stepYaw = yawDelta * (speed / 100f);
        float stepPitch = pitchDelta * (speed / 100f);

        // Ограничиваем максимальный шаг за тик
        float maxStep = 30f;
        stepYaw = MathHelper.clamp(stepYaw, -maxStep, maxStep);
        stepPitch = MathHelper.clamp(stepPitch, -maxStep, maxStep);

        // Шейкинг (тряска) для обхода античита
        if (Config.aimShake > 0) {
            stepYaw += (float) ((Math.random() - 0.5) * Config.aimShake);
            stepPitch += (float) ((Math.random() - 0.5) * Config.aimShake);
        }

        // Применяем GCD (синхронизация с мышкой)
        if (Config.useMouseSensitivity) {
            float sens = MinecraftClient.getInstance().options.getMouseSensitivity().getValue().floatValue();
            float f = sens * 0.6f + 0.2f;
            float gcd = f * f * f * 1.2f;
            stepYaw = Math.round(stepYaw / gcd) * gcd;
            stepPitch = Math.round(stepPitch / gcd) * gcd;
        }

        // Ставим итоговые углы
        p.setYaw(p.getYaw() + stepYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + stepPitch, -90f, 90f));
    }
}
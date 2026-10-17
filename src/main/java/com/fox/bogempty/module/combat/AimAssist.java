package com.fox.bogempty.module.combat;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.utils.RotationUtil;
import com.fox.bogempty.utils.TargetUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class AimAssist {
    private static final Random RNG = new Random();
    private Entity lockedTarget = null;

    public void onFrame(MinecraftClient mc) {
        if (!Config.aimEnabled) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;
        
        // Ищем цель или берем залоченную
        Entity target = null;
        if (lockedTarget != null && TargetUtil.isValidTarget(lockedTarget) && p.squaredDistanceTo(lockedTarget) <= Config.aimRange * Config.aimRange) {
            target = lockedTarget;
        } else {
            lockedTarget = getTarget(mc, p);
            target = lockedTarget;
        }

        if (target == null) return;

        // Точка прицеливания (в голову или грудь)
        Vec3d eye = p.getEyePos();
        Vec3d targetPos = target.getBoundingBox().getCenter();
        
        double diffX = targetPos.x - eye.x;
        double diffY = targetPos.y - eye.y;
        double diffZ = targetPos.z - eye.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        // Вычисляем нужные углы
        float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        // Логика режимов
        float speed = Config.aimSpeed;
        if (Config.aimMode == Config.AimMode.ASSISTANT) {
            // Медленная помощка (как легит)
            speed *= 0.5f;
        } else if (Config.aimMode == Config.AimMode.SMOOTH) {
            // Плавная доводка
            speed *= 0.8f;
        }

        // Применяем поворот
        RotationUtil.applyRotation(p, targetYaw, targetPitch, speed);
    }

    private static Entity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        Box search = p.getBoundingBox().stretch(look.multiply(Config.aimRange)).expand(1.0);

        Entity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!TargetUtil.isValidTarget(e)) continue;
            
            // Проверка FOV (чтобы не дергался на тех, кто за спиной)
            Vec3d center = e.getBoundingBox().getCenter();
            Vec3d dir = center.subtract(eye).normalize();
            double dot = dir.dotProduct(look);
            if (dot < 0.5) continue; // ~60 градусов в стороны

            double d = p.squaredDistanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }
}
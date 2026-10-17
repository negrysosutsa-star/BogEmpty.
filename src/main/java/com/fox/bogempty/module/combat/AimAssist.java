package com.fox.bogempty.module.combat;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.utils.RotationUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist {
    private Entity lockedTarget = null;

    public void onFrame(MinecraftClient mc) {
        if (!Config.aimEnabled || mc.player == null || mc.world == null || mc.currentScreen != null) return;
        ClientPlayerEntity p = mc.player;

        // Берем цель
        Entity target = null;
        if (lockedTarget != null && isValid(lockedTarget) && p.squaredDistanceTo(lockedTarget) <= Config.aimRange * Config.aimRange) {
            target = lockedTarget;
        } else {
            lockedTarget = getTarget(mc, p);
            target = lockedTarget;
        }

        if (target == null) return;

        Vec3d eye = p.getEyePos();
        Vec3d targetPos = target.getBoundingBox().getCenter();
        
        double diffX = targetPos.x - eye.x;
        double diffY = targetPos.y - eye.y;
        double diffZ = targetPos.z - eye.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        // Вычисляем нужные углы
        float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        // Берем скорость из нашего нового конфига
        float speed = (float) ((Config.aimYawSpeed + Config.aimPitchSpeed) / 2.0);

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
            if (!isValid(e)) continue;
            
            // Проверка FOV (чтобы не дергался на тех, кто за спиной)
            Vec3d center = e.getBoundingBox().getCenter();
            Vec3d dir = center.subtract(eye).normalize();
            double dot = dir.dotProduct(look);
            
            // Config.aimFov (по умолчанию 51 градус)
            if (dot < Math.cos(Math.toRadians(Config.aimFov / 2.0))) continue;

            double d = p.squaredDistanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    private static boolean isValid(Entity e) {
        if (!(e instanceof LivingEntity) || !e.isAlive()) return false;
        if (e instanceof net.minecraft.entity.player.PlayerEntity && Config.targetPlayers) return true;
        if (e instanceof net.minecraft.entity.mob.MobEntity && !(e instanceof net.minecraft.entity.passive.AnimalEntity) && Config.targetMobs) return true;
        if (e instanceof net.minecraft.entity.passive.AnimalEntity && Config.targetAnimals) return true;
        return false;
    }
}
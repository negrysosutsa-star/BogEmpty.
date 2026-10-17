package com.fox.bogempty.module.combat;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {
    private final Setting range = new Setting("Range", 4.5f, 2.0f, 8.0f);
    private final Setting fov = new Setting("FOV", 90.0f, 10.0f, 360.0f);
    private final Setting speed = new Setting("Speed", 5.0f, 1.0f, 20.0f);
    private final Setting targets = new Setting("Targets", "Players", new String[]{"Players", "Mobs", "All"});
    private final Setting gcdFix = new Setting("GCD Fix", true);

    private Entity lockedTarget = null;

    public AimAssist() {
        super("AimAssist", Category.COMBAT);
        addSetting(range);
        addSetting(fov);
        addSetting(speed);
        addSetting(targets);
        addSetting(gcdFix);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;

        // Если цель умерла или далеко — сбрасываем
        if (lockedTarget != null && (!lockedTarget.isAlive() || p.squaredDistanceTo(lockedTarget) > range.getFloat() * range.getFloat())) {
            lockedTarget = null;
        }
        
        // Ищем цель
        if (lockedTarget == null) {
            lockedTarget = getTarget(mc, p);
        }

        if (lockedTarget == null) return;

        // === СЛОЖНАЯ МАТЕМАТИКА ПОВОРОТА ===
        Vec3d eye = p.getEyePos();
        Vec3d targetPos = getAimPoint(lockedTarget);
        
        double diffX = targetPos.x - eye.x;
        double diffY = targetPos.y - eye.y;
        double diffZ = targetPos.z - eye.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-diffX, diffZ));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        float yawDelta = MathHelper.wrapDegrees(targetYaw - p.getYaw());
        float pitchDelta = targetPitch - p.getPitch();

        // Формула инерции (плавно в начале, быстро в конце)
        double smoothFactor = 1.0 - Math.pow(0.72, speed.getFloat() * 0.1);
        float stepYaw = (float) (yawDelta * smoothFactor);
        float stepPitch = (float) (pitchDelta * smoothFactor);

        // GCD-фикс для обхода античита (камера движется как у реальной мыши)
        if (gcdFix.getBool()) {
            float sens = mc.options.getMouseSensitivity().getValue().floatValue();
            float f = sens * 0.6f + 0.2f;
            float gcd = f * f * f * 1.2f;
            stepYaw = Math.round(stepYaw / gcd) * gcd;
            stepPitch = Math.round(stepPitch / gcd) * gcd;
        }

        p.setYaw(p.getYaw() + stepYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + stepPitch, -90f, 90f));
    }

    private Vec3d getAimPoint(Entity e) {
        // Берем центр хитбокса + немного выше (как в системе)
        return e.getBoundingBox().getCenter().add(0, e.getHeight() * 0.15, 0);
    }

    private Entity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVector();
        Box search = p.getBoundingBox().stretch(look.multiply(range.getFloat())).expand(1.0);

        Entity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;
            
            String t = targets.getEnum();
            if (t.equals("Players") && !(e instanceof PlayerEntity)) continue;
            if (t.equals("Mobs") && !(e instanceof MobEntity)) continue;

            // Проверка FOV
            Vec3d center = le.getBoundingBox().getCenter();
            Vec3d dir = center.subtract(eye).normalize();
            double dot = dir.dotProduct(look);
            if (dot < Math.cos(Math.toRadians(fov.getFloat() / 2.0))) continue;

            double d = p.squaredDistanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }
}
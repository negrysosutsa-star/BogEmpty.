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
    // Настройки
    private final Setting range = new Setting("Range", 4.5f, 2.0f, 8.0f);
    private final Setting fov = new Setting("FOV", 90.0f, 10.0f, 360.0f);
    private final Setting speed = new Setting("Speed", 5.0f, 1.0f, 20.0f);
    private final Setting priority = new Setting("Priority", "Angle", new String[]{"Distance", "Health", "Angle"});
    private final Setting targets = new Setting("Targets", "Players", new String[]{"Players", "Mobs", "All"});
    private final Setting checkWalls = new Setting("Check Walls", false);

    private Entity lockedTarget = null;
    private long lastAttackTime = 0;

    public AimAssist() {
        super("AimAssist", Category.COMBAT);
        addSetting(range);
        addSetting(fov);
        addSetting(speed);
        addSetting(priority);
        addSetting(targets);
        addSetting(checkWalls);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;

        // Берем цель
        if (lockedTarget != null && (!lockedTarget.isAlive() || p.squaredDistanceTo(lockedTarget) > range.getFloat() * range.getFloat())) {
            lockedTarget = null;
        }
        if (lockedTarget == null) {
            lockedTarget = getTarget(mc, p);
        }

        if (lockedTarget == null) return;

        // Математика из SystemDLC (Инерция камеры)
        Vec3d eye = p.getEyePos();
        Vec3d targetPos = getAimPoint(lockedTarget, p);
        
        double diffX = targetPos.x - eye.x;
        double diffY = targetPos.y - eye.y;
        double diffZ = targetPos.z - eye.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-diffX, diffZ));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        float yawDelta = MathHelper.wrapDegrees(targetYaw - p.getYaw());
        float pitchDelta = targetPitch - p.getPitch();

        // Формула плавности (как в CS:GO / SystemDLC)
        double smoothFactor = 1.0 - Math.pow(0.72, speed.getFloat() * 0.1);

        float stepYaw = (float) (yawDelta * smoothFactor);
        float stepPitch = (float) (pitchDelta * smoothFactor);

        p.setYaw(p.getYaw() + stepYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + stepPitch, -90f, 90f));
    }

    private Vec3d getAimPoint(Entity e, ClientPlayerEntity p) {
        // Мультипоинт из SystemDLC
        Vec3d base = e.getPos();
        double dist = p.distanceTo(e);
        
        double heightRatio = 0.78; // Грудь
        if (dist <= 4.0) heightRatio = 0.55;
        else if (dist <= 12.0) heightRatio = 0.55 + ((dist - 4.0) / 8.0) * 0.3;
        else heightRatio = 0.85 + MathHelper.clamp((dist - 12.0) / 13.0, 0.0, 1.0) * 0.2;

        return base.add(0, e.getHeight() * heightRatio, 0);
    }

    private Entity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVector();
        Box search = p.getBoundingBox().stretch(look.multiply(range.getFloat())).expand(1.0);

        Entity best = null;
        double bestValue = Double.MAX_VALUE;

        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (!isValid(e)) continue;

            // Проверка FOV
            Vec3d center = le.getBoundingBox().getCenter();
            Vec3d dir = center.subtract(eye).normalize();
            double dot = dir.dotProduct(look);
            if (dot < Math.cos(Math.toRadians(fov.getFloat() / 2.0))) continue;

            // Проверка стен
            if (checkWalls.getBool() && !p.canSee(le)) continue;

            // Приоритет
            double value = 0;
            String prio = priority.getEnum();
            if (prio.equals("Distance")) value = p.squaredDistanceTo(le);
            else if (prio.equals("Health")) value = le.getHealth();
            else if (prio.equals("Angle")) {
                // Угловое расстояние до цели
                value = Math.acos(dot);
            }

            if (value < bestValue) {
                bestValue = value;
                best = e;
            }
        }
        return best;
    }

    private boolean isValid(Entity e) {
        String t = targets.getEnum();
        if (t.equals("Players") && !(e instanceof PlayerEntity)) return false;
        if (t.equals("Mobs") && !(e instanceof MobEntity)) return false;
        return true;
    }
}
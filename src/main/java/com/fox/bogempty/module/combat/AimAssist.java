package com.fox.bogempty.module.combat;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import com.fox.bogempty.utils.AimMode;
import com.fox.bogempty.utils.FreeAim;
import com.fox.bogempty.utils.LegitAim;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AimAssist extends Module {
    private final Setting range = new Setting("Range", 4.5f, 2.0f, 8.0f);
    private final Setting fov = new Setting("FOV", 90.0f, 10.0f, 360.0f);
    private final Setting speed = new Setting("Speed", 1.0f, 0.1f, 3.0f);
    private final Setting modeSetting = new Setting("Mode", "LegitAim", new String[]{"LegitAim", "FreeAim"});
    private final Setting targets = new Setting("Targets", "Players", new String[]{"Players", "Mobs", "All"});
    private final Setting holdTime = new Setting("Hold Time", 3.0f, 0.0f, 10.0f); // Удержание после удара (сек)
    
    private final List<AimMode> aimModes = new ArrayList<>();
    private Entity lockedTarget = null;
    private Entity lastHitTarget = null;
    private long lastHitTime = 0;

    public AimAssist() {
        super("AimAssist", Category.COMBAT);
        addSetting(range);
        addSetting(fov);
        addSetting(speed);
        addSetting(modeSetting);
        addSetting(targets);
        addSetting(holdTime);

        aimModes.add(new LegitAim());
        aimModes.add(new FreeAim());
    }

    public void onRender() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null || mc.world == null || mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;
        AimMode currentMode = getMode();
        if (currentMode != null) currentMode.update();

        // === ЛОГИКА УДЕРЖАНИЯ ПОСЛЕ УДАРА ===
        long now = System.currentTimeMillis();
        if (lastHitTarget != null && now - lastHitTime > holdTime.getFloat() * 1000) {
            lastHitTarget = null; // Время вышло
        }

        if (lastHitTarget != null && lastHitTarget.isAlive() && p.squaredDistanceTo(lastHitTarget) <= range.getFloat() * range.getFloat()) {
            lockedTarget = lastHitTarget;
        } else {
            // Обычный поиск цели
            if (lockedTarget != null && (!lockedTarget.isAlive() || p.squaredDistanceTo(lockedTarget) > range.getFloat() * range.getFloat())) {
                lockedTarget = null;
            }
            if (lockedTarget == null) {
                lockedTarget = getTarget(mc, p);
            }
        }

        if (lockedTarget == null) return;

        Vec3d eye = p.getEyePos();
        Vec3d targetPos = getAimPoint(lockedTarget, p); // Динамическая точка

        double diffX = targetPos.x - eye.x;
        double diffY = targetPos.y - eye.y;
        double diffZ = targetPos.z - eye.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-diffX, diffZ));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        float yawDelta = MathHelper.wrapDegrees(targetYaw - p.getYaw());
        float pitchDelta = targetPitch - p.getPitch();

        float stepYaw = 0;
        float stepPitch = 0;

        if (currentMode instanceof LegitAim legit) {
            double[] delta = legit.calculateDelta(yawDelta, pitchDelta, (float) dist);
            stepYaw = (float) delta[0];
            stepPitch = (float) delta[1];
        } else if (currentMode instanceof FreeAim free) {
            double angleDist = Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
            double[] delta = free.calculateDelta(yawDelta, pitchDelta, angleDist, dist, 50.0);
            stepYaw = (float) delta[0];
            stepPitch = (float) delta[1];
        }

        stepYaw *= speed.getFloat();
        stepPitch *= speed.getFloat();

        // GCD Fix
        float sens = mc.options.getMouseSensitivity().getValue().floatValue();
        float f = sens * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;
        stepYaw = Math.round(stepYaw / gcd) * gcd;
        stepPitch = Math.round(stepPitch / gcd) * gcd;

        p.setYaw(p.getYaw() + stepYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + stepPitch, -90f, 90f));
    }

    // Вызывается из TriggerBot при ударе
    public void onAttack(Entity target) {
        this.lastHitTarget = target;
        this.lastHitTime = System.currentTimeMillis();
    }

    // Динамическая точка наводки (как в SystemDLC)
    private Vec3d getAimPoint(Entity target, ClientPlayerEntity p) {
        Vec3d base = target.getPos();
        double dist = p.distanceTo(target);
        
        double heightRatio = 0.78; // По умолчанию грудь
        if (dist <= 4.0) {
            heightRatio = 0.55; // Близко — целимся ниже
        } else if (dist <= 12.0) {
            heightRatio = 0.55 + ((dist - 4.0) / 8.0) * 0.3;
        } else {
            heightRatio = 0.85 + MathHelper.clamp((dist - 12.0) / 13.0, 0.0, 1.0) * 0.2; // Далеко — выше
        }

        return base.add(0, target.getHeight() * heightRatio, 0);
    }

    private AimMode getMode() {
        String mode = modeSetting.getEnum();
        for (AimMode m : aimModes) {
            if (m.getName().equals(mode)) return m;
        }
        return aimModes.isEmpty() ? null : aimModes.get(0);
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
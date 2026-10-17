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
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AimAssist extends Module {
    private final Setting rotation = new Setting("Rotation", "LegitAim", new String[]{"LegitAim", "FreeAim"});
    private final Setting priority = new Setting("Priority", "Angle", new String[]{"Distance", "Health", "Angle"});
    private final Setting range = new Setting("Range", 4.5f, 2.0f, 8.0f);
    private final Setting fov = new Setting("FOV", 90.0f, 10.0f, 360.0f);
    private final Setting holdAfterHit = new Setting("Hold", 5.0f, 0.0f, 30.0f);
    private final Setting targetInvisible = new Setting("Invisible", false);

    private final List<AimMode> aimModes = new ArrayList<>();
    private Entity lockedTarget = null;
    private Entity lastHitTarget = null;
    private long lastHitTime = 0;

    public AimAssist() {
        super("AimAssist", Category.COMBAT);
        addSetting(rotation);
        addSetting(priority);
        addSetting(range);
        addSetting(fov);
        addSetting(holdAfterHit);
        addSetting(targetInvisible);

        aimModes.add(new LegitAim());
        aimModes.add(new FreeAim());
    }

    public void onRender() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null || mc.world == null || mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;
        AimMode currentMode = getMode();
        if (currentMode != null) currentMode.update();

        // Удержание после удара
        long now = System.currentTimeMillis();
        if (lastHitTarget != null && now - lastHitTime > holdAfterHit.getFloat() * 1000) {
            lastHitTarget = null; 
        }

        if (lastHitTarget != null && lastHitTarget.isAlive() && p.squaredDistanceTo(lastHitTarget) <= range.getFloat() * range.getFloat()) {
            lockedTarget = lastHitTarget;
        } else {
            if (lockedTarget != null && (!lockedTarget.isAlive() || p.squaredDistanceTo(lockedTarget) > range.getFloat() * range.getFloat())) {
                lockedTarget = null;
            }
            if (lockedTarget == null) {
                lockedTarget = getTarget(mc, p);
            }
        }

        if (lockedTarget == null) return;

        Vec3d eye = p.getEyePos();
        double[] offsets = currentMode != null ? currentMode.getAimPoint(lockedTarget) : new double[]{0, 0, 0};
        Vec3d targetPos = lockedTarget.getPos().add(offsets[0], offsets[1], offsets[2]);

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

        if (currentMode != null) {
            // Используем метод calculateDelta из AimMode
            double[] delta = currentMode.calculateDelta(yawDelta, pitchDelta, dist, 50);
            stepYaw = (float) delta[0];
            stepPitch = (float) delta[1];
        }

        // GCD Фикс
        float sens = mc.options.getMouseSensitivity().getValue().floatValue();
        float f = sens * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;
        stepYaw = Math.round(stepYaw / gcd) * gcd;
        stepPitch = Math.round(stepPitch / gcd) * gcd;

        p.setYaw(p.getYaw() + stepYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + stepPitch, -90f, 90f));
    }

    public void onAttack(Entity target) {
        this.lastHitTarget = target;
        this.lastHitTime = System.currentTimeMillis();
    }

    private AimMode getMode() {
        String mode = rotation.getEnum();
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
        double bestVal = Double.MAX_VALUE;

        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (e == p || e instanceof ArmorStandEntity) continue;
            if (le.isInvisible() && !targetInvisible.getBool()) continue;

            Vec3d center = le.getBoundingBox().getCenter();
            Vec3d dir = center.subtract(eye).normalize();
            double dot = dir.dotProduct(look);
            if (dot < Math.cos(Math.toRadians(fov.getFloat() / 2.0))) continue;

            String prio = priority.getEnum();
            double val;
            if (prio.equals("Distance")) val = p.squaredDistanceTo(e);
            else if (prio.equals("Health")) val = le.getHealth();
            else val = Math.acos(dot);

            if (val < bestVal) {
                bestVal = val;
                best = e;
            }
        }
        return best;
    }
}
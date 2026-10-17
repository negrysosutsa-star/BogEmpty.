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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AimAssist extends Module {
    private final Setting range = new Setting("Range", 4.5f, 2.0f, 8.0f);
    private final Setting fov = new Setting("FOV", 90.0f, 10.0f, 360.0f);
    private final Setting modeSetting = new Setting("Mode", "LegitAim", new String[]{"LegitAim", "FreeAim"});
    private final Setting targets = new Setting("Targets", "Players", new String[]{"Players", "Mobs", "All"});
    private final Setting gcdFix = new Setting("GCD Fix", true);

    private final List<AimMode> aimModes = new ArrayList<>();
    private Entity lockedTarget = null;
    private long lastFrameTime = System.currentTimeMillis();

    public AimAssist() {
        super("AimAssist", Category.COMBAT);
        addSetting(range);
        addSetting(fov);
        addSetting(modeSetting);
        addSetting(targets);
        addSetting(gcdFix);

        aimModes.add(new LegitAim());
        aimModes.add(new FreeAim());
    }

    // Вызывается каждый кадр из BogEmpty (для плавности)
    public void onRender() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null || mc.world == null || mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;
        AimMode currentMode = getMode();
        if (currentMode != null) currentMode.update();

        if (lockedTarget != null && (!lockedTarget.isAlive() || p.squaredDistanceTo(lockedTarget) > range.getFloat() * range.getFloat())) {
            lockedTarget = null;
        }
        if (lockedTarget == null) {
            lockedTarget = getTarget(mc, p);
        }

        if (lockedTarget == null) return;

        Vec3d eye = p.getEyePos();
        double[] offsets = currentMode != null ? currentMode.getAimPoint(lockedTarget) : new double[]{0, 0, 0};
        Vec3d targetPos = lockedTarget.getPos().add(offsets[0], lockedTarget.getHeight() * 0.6, offsets[2]);

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

        // Считаем время с прошлого кадра
        long now = System.currentTimeMillis();
        long timeDelta = now - lastFrameTime;
        lastFrameTime = now;

        if (currentMode instanceof LegitAim legit) {
            double[] delta = legit.calculateDelta(yawDelta, pitchDelta, (float) dist);
            stepYaw = (float) delta[0];
            stepPitch = (float) delta[1];
        } 
        else if (currentMode instanceof FreeAim free) {
            // Вычисляем общий угол отклонения
            double angleDist = Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
            double[] delta = free.calculateDelta(yawDelta, pitchDelta, angleDist, dist, timeDelta);
            stepYaw = (float) delta[0];
            stepPitch = (float) delta[1];
        }

        // GCD Фикс (привязка к сенсе мыши, чтобы не палиться)
        if (gcdFix.getBool()) {
            float sens = mc.options.getMouseSensitivity().getValue().floatValue();
            float f = sens * 0.6f + 0.2f;
            float gcd = f * f * f * 1.2f;
            stepYaw = Math.round(stepYaw / gcd) * gcd;
            stepPitch = Math.round(stepPitch / gcd) * gcd;
        }

        // Ограничение максимального поворота за кадр
        float maxTurn = 25f;
        stepYaw = MathHelper.clamp(stepYaw, -maxTurn, maxTurn);
        stepPitch = MathHelper.clamp(stepPitch, -maxTurn, maxTurn);

        p.setYaw(p.getYaw() + stepYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + stepPitch, -90f, 90f));
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
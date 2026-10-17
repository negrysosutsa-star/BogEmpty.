package com.fox.bogempty.module.combat;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.utils.TargetUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class TriggerBot {
    private static final Random RNG = new Random();
    private int attackCooldown = 0;
    private int jumpCooldown = 0;
    private boolean waitingForFall = false;

    public void onTick(MinecraftClient mc) {
        if (!Config.triggerEnabled) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;
        if (p.isUsingItem()) return;

        if (attackCooldown > 0) { attackCooldown--; }
        if (jumpCooldown > 0) { jumpCooldown--; }

        // Ищем цель
        Entity target = getTarget(mc, p);
        if (target == null || !TargetUtil.isValidTarget(target)) {
            waitingForFall = false;
            return;
        }

        // Проверка дистанции
        double dist = p.squaredDistanceTo(target);
        if (dist > Config.triggerRange * Config.triggerRange) return;

        // === ЛОГИКА КРИТОВ ===
        if (Config.triggerMode == Config.TriggerMode.NORMAL) {
            if (attackCooldown > 0) return;
            if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;
            attack(mc, p, target);
            return;
        }

        // УМНЫЕ КРИТЫ / ТОЛЬКО КРИТЫ
        Vec3d vel = p.getVelocity();

        // Если стоим на земле — прыгаем (если включено в настройках)
        if (p.isOnGround() && jumpCooldown == 0) {
            p.jump();
            jumpCooldown = 12 + RNG.nextInt(4); // рандомный прыжок, чтобы не палиться
            waitingForFall = true;
            return;
        }

        // Ждем фазу падения (velocity.y < 0) для 100% крита
        boolean falling = !p.isOnGround() && vel.y < 0 && p.fallDistance > 0.08f;
        if (!falling) {
            waitingForFall = true;
            return;
        }

        // Проверки на крит
        if (p.isTouchingWater() || p.isInLava()) return;
        if (p.hasStatusEffect(StatusEffects.BLINDNESS)) return;
        if (p.isClimbing()) return;

        if (attackCooldown > 0) return;
        if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;

        // Сброс спринта перед ударом
        if (p.isSprinting()) {
            p.setSprinting(false);
            attackCooldown = 1; // пропускаем тик, чтобы сервер увидел сброс
            return;
        }

        attack(mc, p, target);
        waitingForFall = false;
    }

    private void attack(MinecraftClient mc, ClientPlayerEntity p, Entity target) {
        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);

        // Высчитываем задержку (CPS)
        // CPS 12 = 1000ms / 12 = ~83ms. В 1 тике 50ms.
        // Значит бьем примерно каждые 2 тика с рандомом.
        int baseTicks = 20 / Math.max(1, Config.triggerCps);
        attackCooldown = baseTicks + RNG.nextInt(2); // рандом чтобы не палиться на античите
    }

    private static Entity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof EntityHitResult ehr) {
            return ehr.getEntity();
        }

        // Если не смотрим прямо — ищем рядом
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d end = eye.add(look.multiply(Config.triggerRange));
        Box search = p.getBoundingBox().stretch(look.multiply(Config.triggerRange)).expand(1.0);

        Entity best = null;
        double bestD = Double.MAX_VALUE;
        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!TargetUtil.isValidTarget(e)) continue;
            var opt = e.getBoundingBox().expand(0.1).raycast(eye, end);
            if (opt.isPresent()) {
                double d = eye.squaredDistanceTo(opt.get());
                if (d < bestD) { bestD = d; best = e; }
            }
        }
        return best;
    }
}
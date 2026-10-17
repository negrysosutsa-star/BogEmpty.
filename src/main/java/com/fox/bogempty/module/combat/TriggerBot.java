package com.fox.bogempty.module.combat;

import com.fox.bogempty.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;
import java.util.Random;

public class TriggerBot {
    private static final Random RNG = new Random();
    private int cooldown = 0;

    public void onTick(MinecraftClient mc) {
        if (!Config.triggerEnabled || mc.player == null || mc.world == null || mc.currentScreen != null) return;
        ClientPlayerEntity p = mc.player;

        if (p.isUsingItem()) return;
        if (cooldown > 0) { cooldown--; return; }

        Entity target = getTarget(mc, p);
        if (target == null) return;

        double dist = p.squaredDistanceTo(target);
        if (dist > Config.triggerRange * Config.triggerRange) return;

        // === ЛОГИКА КРИТОВ (БЕЗ ПРЫЖКОВ) ===
        if (Config.triggerOnlyCrits || Config.triggerSmartCrits) {
            Vec3d vel = p.getVelocity();
            // Удар только если мы падаем (Y < 0) и пролетели чуть больше 0.05 блока
            boolean isFalling = !p.isOnGround() && vel.y < 0.0 && p.fallDistance > 0.05f;

            if (!isFalling) return; // Стоим на земле или летим вверх — не бьем

            // Проверка готовности удара
            if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;

            // Сброс спринта
            if (Config.triggerSprintReset && p.isSprinting()) {
                p.setSprinting(false);
                cooldown = 1; // Пропускаем тик, чтобы сервер увидел сброс
                return;
            }

            attack(mc, p, target);
        } else {
            // Обычный триггер (без критов)
            if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;
            attack(mc, p, target);
        }
    }

    private void attack(MinecraftClient mc, ClientPlayerEntity p, Entity target) {
        // Быстрый рейкаст перед ударом (чтобы не бить сквозь стены)
        if (!canHit(p, target)) return;

        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        
        // Рандомизация КД (чтобы античит не видел идеальные тайминги)
        cooldown = 1 + RNG.nextInt(2); 
    }

    private static boolean canHit(ClientPlayerEntity p, Entity target) {
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d end = eye.add(look.multiply(Config.triggerRange));
        
        // Если цель перекрыта блоком (кроме травы) — не бьем
        BlockHitResult blockHit = p.getWorld().raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, p));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState state = p.getWorld().getBlockState(blockHit.getBlockPos());
            if (!(state.getBlock() instanceof PlantBlock)) {
                return false; // Блок мешает удару
            }
        }
        return true;
    }

    private static Entity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (isValid(e)) return e;
        }

        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d end = eye.add(look.multiply(Config.triggerRange));
        Box search = p.getBoundingBox().stretch(look.multiply(Config.triggerRange)).expand(1.0);

        Entity best = null;
        double bestD = Double.MAX_VALUE;
        
        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!isValid(e)) continue;
            var opt = e.getBoundingBox().expand(0.1).raycast(eye, end);
            if (opt.isPresent()) {
                double d = eye.squaredDistanceTo(opt.get());
                if (d < bestD) { bestD = d; best = e; }
            }
        }
        return best;
    }

    private static boolean isValid(Entity e) {
        if (!(e instanceof LivingEntity) || !e.isAlive()) return false;
        if (e instanceof PlayerEntity && Config.targetPlayers) return true;
        if (e instanceof MobEntity && !(e instanceof AnimalEntity) && Config.targetMobs) return true;
        if (e instanceof AnimalEntity && Config.targetAnimals) return true;
        return false;
    }
}
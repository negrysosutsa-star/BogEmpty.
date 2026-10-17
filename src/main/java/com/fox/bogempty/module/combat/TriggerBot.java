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
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Random;

public class TriggerBot {
    private static final Random RNG = new Random();
    private int cooldown = 0;
    private int sprintReset = 0;

    public void onTick(MinecraftClient mc) {
        if (!Config.triggerEnabled || mc.player == null || mc.world == null || mc.currentScreen != null) return;
        ClientPlayerEntity p = mc.player;

        if (p.isUsingItem()) return;
        if (mc.interactionManager.isBreakingBlock()) return;

        if (sprintReset > 0) {
            sprintReset--;
            if (sprintReset == 0 && cooldown == 0) {
                LivingEntity target = getTarget(mc, p);
                if (target != null) performAttack(mc, p, target);
            }
            return;
        }

        if (cooldown > 0) { cooldown--; return; }

        LivingEntity target = getTarget(mc, p);
        if (target == null) return;

        double dist = p.squaredDistanceTo(target);
        if (dist > Config.triggerRange * Config.triggerRange) return;

        // Кулдаун
        float cdProgress = p.getAttackCooldownProgress(1.0f);
        if (Config.trigCustomCooldown) {
            if (cdProgress < (Config.trigCustomCooldownValue / 100f)) return;
        } else {
            if (cdProgress < 1.0f) return;
        }

        // Логика критов
        if (Config.triggerMode.equals("Smart") || Config.triggerMode.equals("OnlyCrits")) {
            Vec3d vel = p.getVelocity();
            boolean isFalling = !p.isOnGround() && vel.y < 0.0 && p.fallDistance > 0.05f;

            if (!isFalling) return; // Ждем падения

            if (Config.trigNoSweaps && p.fallDistance < 0.15f) return; // Не бить свипами

            if (p.isSprinting()) {
                p.setSprinting(false);
                sprintReset = 1;
                return;
            }

            performAttack(mc, p, target);
        } else {
            performAttack(mc, p, target);
        }
    }

    private void performAttack(MinecraftClient mc, ClientPlayerEntity p, Entity target) {
        if (target == null) return;
        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        cooldown = 1 + RNG.nextInt(2);
    }

    private static LivingEntity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (e instanceof LivingEntity le && isValid(le, p)) return le;
        }

        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d end = eye.add(look.multiply(Config.triggerRange));
        Box search = p.getBoundingBox().stretch(look.multiply(Config.triggerRange)).expand(1.0);

        BlockHitResult blockHit = mc.world.raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, p));
        boolean isPlant = false;
        double blockDist = Double.MAX_VALUE;
        
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState state = mc.world.getBlockState(blockHit.getBlockPos());
            isPlant = state.getBlock() instanceof PlantBlock;
            blockDist = eye.squaredDistanceTo(blockHit.getPos());
        }

        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;

        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!(e instanceof LivingEntity le) || !isValid(le, p)) continue;

            var opt = le.getBoundingBox().expand(0.1).raycast(eye, end);
            if (opt.isPresent()) {
                double d = eye.squaredDistanceTo(opt.get());
                // Если мешает блок (и это не трава), скипаем
                if (!isPlant && d > blockDist && !Config.trigThroughEntities) continue;
                if (d < bestD) { bestD = d; best = le; }
            }
        }
        return best;
    }

    private static boolean isValid(LivingEntity e, ClientPlayerEntity p) {
        if (e == p || !e.isAlive() || e.isSpectator() || !e.canHit()) return false;
        
        if (Config.triggerTargets.equals("Players") && !(e instanceof PlayerEntity)) return false;
        if (Config.triggerTargets.equals("Mobs") && !(e instanceof MobEntity)) return false;
        
        if (e.isInvisible() && !Config.espInvisible) return false;

        return true;
    }
}
package com.fox.bogempty.utils;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class CritChecks {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isBadEnvironment() {
        return mc.player == null || mc.player.isTouchingWater() || mc.player.isSwimming()
                || mc.player.isGliding() || mc.player.isClimbing() || mc.player.isInsideWall()
                || mc.player.isInLava() || mc.player.hasVehicle();
    }

    public static boolean isHeavyWeapon() {
        if (mc.player == null) return false;
        Item item = mc.player.getMainHandStack().getItem();
        return item instanceof AxeItem || item instanceof TridentItem || item instanceof MaceItem;
    }

    // Главная проверка на крит (падение)
    public static boolean isFallingCrit(float threshold) {
        if (mc.player == null || mc.player.isOnGround() || mc.player.isInLava() 
                || mc.player.isTouchingWater() || mc.player.hasVehicle()) {
            return false;
        }
        
        double yVel = mc.player.getVelocity().y;
        
        // Проверка на потолок (если над нами блок, сервер не засчитает)
        BlockPos up = mc.player.getBlockPos().up(2);
        BlockState state = mc.world.getBlockState(up);
        if (!state.isAir() && state.isFullCube(mc.world, up) && yVel < -0.01) {
            double distToGround = mc.player.getY() - (mc.player.getBlockPos().down().getY() + 1.0);
            if (distToGround > 0.4) return true;
        }

        return yVel < threshold;
    }

    public static boolean isHoldingWeapon() {
        if (mc.player == null) return false;
        return isWeaponItem(mc.player.getMainHandStack()) || isWeaponItem(mc.player.getOffHandStack());
    }

    public static boolean isWeaponItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem || item instanceof MaceItem;
    }

    public static void attack(Entity target) {
        if (mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.resetLastAttackedTicks();
        }
    }

    // Динамический кулдаун (для пропена)
    public static float getSmartCooldown() {
        if (mc.player == null) return 0.8f;
        boolean heavy = isHeavyWeapon();
        if (!mc.player.isOnGround() && !isBadEnvironment()) {
            return heavy ? 0.85f : 0.75f;
        } else {
            return heavy ? 0.95f : 0.8f;
        }
    }

    // Проверка на готовность удара
    public static boolean canAttack(String mode, float customCd) {
        if (mc.player == null) return false;

        // Если выключен кастомный кулдаун — бьем только на 100%
        float needed = customCd > 0 ? customCd : getSmartCooldown();
        float current = mc.player.getAttackCooldownProgress(0.0f);
        
        if (current < needed) return false;

        boolean falling = isFallingCrit(-0.01f);
        
        if ("OnlyCrits".equals(mode)) {
            return isBadEnvironment() || falling;
        } else if ("Smart".equals(mode)) {
            if (!isBadEnvironment() && mc.player.getVelocity().y > 0.0) return false; // Летим вверх - не бьем
            return mc.player.isOnGround() ? current >= needed : (isBadEnvironment() || falling);
        }
        
        return current >= 1.0f; // Normal mode
    }
}
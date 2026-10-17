package com.fox.bogempty.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;

public class PlayerUtil {
    
    public static ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    // Проверка, что мы в "плохом" состоянии (вода, лава, лестница) — криты не работают
    public static boolean isBadEnvironment() {
        ClientPlayerEntity p = getPlayer();
        if (p == null) return true;
        return p.isTouchingWater() || p.isSwimming() || p.isGliding() 
                || p.isClimbing() || p.isInsideWall() || p.isInLava() || p.hasVehicle();
    }

    // Проверка, что у нас в руке оружие
    public static boolean isHoldingWeapon() {
        ClientPlayerEntity p = getPlayer();
        if (p == null) return false;
        ItemStack stack = p.getMainHandStack();
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    // Проверка на слепоту (криты не работают)
    public static boolean hasBlindness() {
        ClientPlayerEntity p = getPlayer();
        return p != null && p.hasStatusEffect(StatusEffects.BLINDNESS);
    }
}
package com.fox.bogempty.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;

public class CritChecks {
    public static boolean isCrit(ClientPlayerEntity p) {
        if (p.fallDistance <= 0.0f) return false;
        if (p.isOnGround()) return false;
        if (p.isClimbing()) return false;
        if (p.isTouchingWater() || p.isInLava()) return false;
        if (p.hasStatusEffect(StatusEffects.BLINDNESS)) return false;
        if (p.hasVehicle()) return false;
        if (p.getAbilities().flying) return false;
        if (p.isGliding()) return false;
        return true;
    }

    public static boolean isAboutToLand(ClientPlayerEntity p) {
        // Если мы падаем и до земли меньше 0.5 блока
        if (p.getVelocity().y < 0.0 && !p.isOnGround()) {
            double distToGround = p.getWorld().getBlockState(p.getBlockPos().down()).isAir() ? 10.0 : 0.1;
            return p.fallDistance + distToGround < 0.5;
        }
        return false;
    }
}
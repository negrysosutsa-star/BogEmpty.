package com.fox.bogempty.utils;

import com.fox.bogempty.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetUtil {
    public static boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity le)) return false;
        if (!le.isAlive()) return false;
        
        if (Config.targetType == Config.TargetType.PLAYERS && !(entity instanceof PlayerEntity)) return false;
        if (Config.TargetType.MOBS != null && Config.targetType == Config.TargetType.MOBS && !(entity instanceof MobEntity)) return false;

        return true;
    }
}
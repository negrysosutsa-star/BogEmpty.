package com.fox.bogempty.utils;

import com.fox.bogempty.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetUtil {
    public static boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity le)) return false;
        if (!le.isAlive()) return false;
        
        if (entity instanceof PlayerEntity) return Config.targetPlayers;
        if (entity instanceof AnimalEntity) return Config.targetAnimals;
        if (entity instanceof MobEntity) return Config.targetMobs;

        return false;
    }
}
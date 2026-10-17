package com.fox.bogempty.utils;

import com.fox.bogempty.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetUtil {
    
    // Проверяем, можно ли атаковать эту сущность
    public static boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity le)) return false;
        if (!le.isAlive()) return false;
        
        // Если это игрок
        if (entity instanceof PlayerEntity) return Config.targetPlayers;
        
        // Если это животное
        if (entity instanceof AnimalEntity) return Config.targetAnimals;
        
        // Если это моб (зомби, скелет и т.д.)
        if (entity instanceof MobEntity) return Config.targetMobs;

        return false;
    }
}
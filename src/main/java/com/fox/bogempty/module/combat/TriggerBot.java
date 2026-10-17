package com.fox.bogempty.module.combat;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class TriggerBot extends Module {
    private int cooldown = 0;

    private final Setting range = new Setting("Range", 3.0f, 2.0f, 6.0f);

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        addSetting(range);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        ClientPlayerEntity p = mc.player;

        if (cooldown > 0) { cooldown--; return; }

        // Просто ищем цель прямо под прицелом
        Entity target = getTarget(mc, p);
        if (target == null) return;

        // Проверяем дистанцию
        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;

        // Ждем готовности удара
        if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;

        // Бьем!
        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        
        // Задержка 2 тика
        cooldown = 2;
    }

    private Entity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        // Берем только то, на что смотрит прицел (crosshairTarget)
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (e instanceof LivingEntity le && le.isAlive() && e != p) {
                return le;
            }
        }
        return null;
    }
}
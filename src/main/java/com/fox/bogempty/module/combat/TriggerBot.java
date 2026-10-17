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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class TriggerBot extends Module {
    private static final Random RNG = new Random();
    private int cooldown = 0;

    // Настройки
    private final Setting mode = new Setting("Mode", "OnlyCrits", new String[]{"Smart", "OnlyCrits", "Normal"});
    private final Setting range = new Setting("Range", 3.0f, 2.0f, 6.0f);
    private final Setting targetPlayers = new Setting("Players", true);
    private final Setting targetMobs = new Setting("Mobs", false);
    private final Setting targetInvisible = new Setting("Invisible", false);
    private final Setting targetOnlyArmor = new Setting("Only Armor", false);
    private final Setting targetOnlyNaked = new Setting("Only Naked", false);

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        addSetting(mode);
        addSetting(range);
        addSetting(targetPlayers);
        addSetting(targetMobs);
        addSetting(targetInvisible);
        addSetting(targetOnlyArmor);
        addSetting(targetOnlyNaked);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        ClientPlayerEntity p = mc.player;

        if (p.isUsingItem() || mc.interactionManager.isBreakingBlock()) return;
        if (cooldown > 0) { cooldown--; return; }

        LivingEntity target = getTarget(mc, p);
        if (target == null) return;

        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;
        if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;

        String modeStr = mode.getEnum();

        if (modeStr.equals("OnlyCrits")) {
            // БЬЕМ ТОЛЬКО КРИТАМИ. 
            // Если мы на земле — пропускаем (ждем, пока игрок сам прыгнет).
            // Если падаем — бьем.
            if (p.isOnGround()) return;
            if (p.fallDistance <= 0.0f) return;
            if (p.getVelocity().y >= 0) return; // Летим вверх — крита не будет

            // Сброс спринта (обязательно для крита)
            if (p.isSprinting()) {
                p.setSprinting(false);
                cooldown = 1;
                return;
            }
        } else if (modeStr.equals("Smart")) {
            // Если мы в падении — бьем критом. Если на земле — бьем сплешом.
            if (!p.isOnGround() && p.getVelocity().y < 0) {
                if (p.isSprinting()) {
                    p.setSprinting(false);
                    cooldown = 1;
                    return;
                }
            }
        }

        // Финальная проверка дистанции перед ударом
        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;

        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        
        // Рандомизация для обхода античита
        cooldown = 1 + RNG.nextInt(2);
    }

    private LivingEntity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (e instanceof LivingEntity le && isValid(le, p)) return le;
        }

        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVector();
        Vec3d end = eye.add(look.multiply(range.getFloat()));
        Box search = p.getBoundingBox().stretch(look.multiply(range.getFloat())).expand(1.0);

        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;

        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!(e instanceof LivingEntity le) || !isValid(le, p)) continue;

            var opt = le.getBoundingBox().expand(0.1).raycast(eye, end);
            if (opt.isPresent()) {
                double d = eye.squaredDistanceTo(opt.get());
                if (d < bestD) { bestD = d; best = le; }
            }
        }
        return best;
    }

    private boolean isValid(LivingEntity e, ClientPlayerEntity p) {
        if (e == p || !e.isAlive() || e.isSpectator() || !e.canHit()) return false;
        
        boolean isPlayer = e instanceof PlayerEntity;
        boolean isMob = e instanceof MobEntity;
        
        if (isPlayer && !targetPlayers.getBool()) return false;
        if (isMob && !targetMobs.getBool()) return false;
        if (!isPlayer && !isMob) return false;

        if (e.isInvisible() && !targetInvisible.getBool()) return false;
        
        if (isPlayer) {
            boolean hasArmor = false;
            for (ItemStack stack : e.getArmorItems()) {
                if (!stack.isEmpty()) { hasArmor = true; break; }
            }
            if (targetOnlyArmor.getBool() && !hasArmor) return false;
            if (targetOnlyNaked.getBool() && hasArmor) return false;
        }
        return true;
    }
}
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

import java.util.Random;

public class TriggerBot extends Module {
    private static final Random RNG = new Random();
    private int cooldown = 0;

    // Настройки
    private final Setting mode = new Setting("Mode", "Smart", new String[]{"Smart", "OnlyCrits", "Normal"});
    private final Setting range = new Setting("Range", 3.0f, 2.0f, 6.0f);
    private final Setting targetPlayers = new Setting("Players", true);
    private final Setting targetMobs = new Setting("Mobs", true);
    private final Setting targetInvisible = new Setting("Invisible", false);
    private final Setting targetOnlyArmor = new Setting("Only Armor", false);

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        addSetting(mode);
        addSetting(range);
        addSetting(targetPlayers);
        addSetting(targetMobs);
        addSetting(targetInvisible);
        addSetting(targetOnlyArmor);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null || mc.world == null || mc.currentScreen != null) return;
        ClientPlayerEntity p = mc.player;

        if (p.isUsingItem() || mc.interactionManager.isBreakingBlock()) return;
        if (cooldown > 0) { cooldown--; return; }

        // 1. Ищем цель
        LivingEntity target = getTarget(mc, p);
        if (target == null) return;

        // 2. Проверяем дистанцию
        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;

        // 3. Проверяем кулдаун удара (готовность оружия)
        if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;

        // 4. Логика критов
        String modeStr = mode.getEnum();
        if (modeStr.equals("OnlyCrits")) {
            // Бьем ТОЛЬКО если падаем (крит)
            if (!p.isOnGround() && p.getVelocity().y < 0 && p.fallDistance > 0.1f) {
                // Сброс спринта
                if (p.isSprinting()) { p.setSprinting(false); cooldown = 1; return; }
                attack(mc, p, target);
            }
        } else if (modeStr.equals("Smart")) {
            // Если в падении — крит. Если на земле — обычный удар (сплеш).
            if (!p.isOnGround() && p.getVelocity().y < 0) {
                if (p.isSprinting()) { p.setSprinting(false); cooldown = 1; return; }
                attack(mc, p, target);
            } else if (p.isOnGround()) {
                attack(mc, p, target);
            }
        } else {
            // Normal (просто бьет)
            attack(mc, p, target);
        }
    }

    private void attack(MinecraftClient mc, ClientPlayerEntity p, Entity target) {
        // Сброс спринта перед ударом (чтобы не было отскока)
        if (p.isSprinting()) p.setSprinting(false);
        
        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        
        // Небольшая задержка (2-3 тика), чтобы не спамить
        cooldown = 2 + RNG.nextInt(2);
    }

    private LivingEntity getTarget(MinecraftClient mc, ClientPlayerEntity p) {
        // Сначала проверяем, на что смотрит прицел
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (e instanceof LivingEntity le && isValid(le, p)) return le;
        }

        // Если не смотрим — ищем ближайшего в радиусе
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
        
        if (isPlayer && targetOnlyArmor.getBool()) {
            boolean hasArmor = false;
            for (var stack : e.getArmorItems()) { if (!stack.isEmpty()) { hasArmor = true; break; } }
            if (!hasArmor) return false;
        }
        
        return true;
    }
}
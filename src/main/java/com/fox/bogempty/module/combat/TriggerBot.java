package com.fox.bogempty.module.combat;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import com.fox.bogempty.utils.CritChecks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class TriggerBot extends Module {
    private static final Random RNG = new Random();
    private int cooldown = 0;

    private final Setting mode = new Setting("Mode", "Smart", new String[]{"Smart", "OnlyCrits", "Normal"});
    private final Setting range = new Setting("Range", 3.0f, 2.0f, 6.0f);
    private final Setting customCd = new Setting("Custom CD", false);
    private final Setting cdValue = new Setting("CD Value", 0.8f, 0.1f, 1.0f);
    
    private final Setting targetPlayers = new Setting("Players", true);
    private final Setting targetMobs = new Setting("Mobs", false);
    private final Setting targetInvisible = new Setting("Invisible", false);
    private final Setting targetOnlyArmor = new Setting("Only Armor", false);

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        addSetting(mode);
        addSetting(range);
        addSetting(customCd);
        addSetting(cdValue);
        addSetting(targetPlayers);
        addSetting(targetMobs);
        addSetting(targetInvisible);
        addSetting(targetOnlyArmor);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        ClientPlayerEntity p = mc.player;

        if (cooldown > 0) { cooldown--; return; }

        LivingEntity target = getTarget(mc, p);
        if (target == null) return;

        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;

        // Используем мощный крит-чекер из SystemDLC
        float customVal = customCd.getBool() ? cdValue.getFloat() : -1f;
        if (!CritChecks.canAttack(mode.getEnum(), customVal)) return;

        // Сброс спринта перед ударом (для крита)
        if (p.isSprinting()) {
            p.setSprinting(false);
            cooldown = 1;
            return;
        }

        // Бьем
        CritChecks.attack(target);
        
        // Рандомизация задержки
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
        }
        return true;
    }
}
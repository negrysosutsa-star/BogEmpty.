package com.fox.bogempty.module.combat;

import com.fox.bogempty.BogEmpty;
import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Random;

public class TriggerBot extends Module {
    private static final Random RNG = new Random();
    private int cooldown = 0;

    private final Setting mode = new Setting("Mode", "Smart", new String[]{"Smart", "OnlyCrits", "Normal"});
    private final Setting range = new Setting("Range", 3.0f, 2.0f, 6.0f);
    private final Setting customCd = new Setting("Custom CD", false);
    private final Setting cdValue = new Setting("CD Value", 80f, 1f, 100f);
    private final Setting tpsSync = new Setting("TPS Sync", false);
    private final Setting noSweaps = new Setting("No Sweaps", false);
    private final Setting throughEntities = new Setting("Through Entities", false);
    
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
        addSetting(tpsSync);
        addSetting(noSweaps);
        addSetting(throughEntities);
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

        LivingEntity target = getTarget(mc, p);
        if (target == null) return;

        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;

        // 1. Кулдаун (свой или ванильный)
        float cdProgress = p.getAttackCooldownProgress(1.0f);
        if (customCd.getBool()) {
            if (cdProgress < (cdValue.getFloat() / 100f)) return;
        } else {
            if (cdProgress < 1.0f) return;
        }

        // 2. Логика критов
        String modeStr = mode.getEnum();
        if (modeStr.equals("OnlyCrits")) {
            if (p.isOnGround() || p.fallDistance <= 0.05f) return;
            if (noSweaps.getBool() && p.fallDistance < 0.15f) return; // Свип-защита
            if (p.isSprinting()) { p.setSprinting(false); cooldown = 1; return; }
            attack(mc, p, target);
        } else if (modeStr.equals("Smart")) {
            if (!p.isOnGround() && p.getVelocity().y < 0) {
                if (noSweaps.getBool() && p.fallDistance < 0.15f) return;
                if (p.isSprinting()) { p.setSprinting(false); cooldown = 1; return; }
                attack(mc, p, target);
            } else if (p.isOnGround()) {
                attack(mc, p, target);
            }
        } else {
            attack(mc, p, target);
        }
    }

    private void attack(MinecraftClient mc, ClientPlayerEntity p, Entity target) {
        if (p.isSprinting()) p.setSprinting(false);
        
        // Связь с Аимом (удержание цели)
        AimAssist aim = BogEmpty.getModule(AimAssist.class);
        if (aim != null) aim.onAttack(target);

        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        
        // Рандомизация задержки (1-3 тика)
        cooldown = 1 + RNG.nextInt(3);
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

        BlockHitResult blockHit = mc.world.raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, p));
        boolean isPlant = false;
        double blockDist = Double.MAX_VALUE;
        
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState state = mc.world.getBlockState(blockHit.getBlockPos());
            isPlant = state.getBlock() instanceof PlantBlock;
            blockDist = eye.squaredDistanceTo(blockHit.getPos());
        }

        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;

        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!(e instanceof LivingEntity le) || !isValid(le, p)) continue;
            var opt = le.getBoundingBox().expand(0.1).raycast(eye, end);
            if (opt.isPresent()) {
                double d = eye.squaredDistanceTo(opt.get());
                if (!isPlant && d > blockDist && !throughEntities.getBool()) continue;
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
            for (ItemStack stack : e.getArmorItems()) { if (!stack.isEmpty()) { hasArmor = true; break; } }
            if (!hasArmor) return false;
        }
        return true;
    }
}
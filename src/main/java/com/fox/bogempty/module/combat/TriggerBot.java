package com.fox.bogempty.module.combat;

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
    private final Setting throughGrass = new Setting("Through Grass", true);
    private final Setting throughEntities = new Setting("Through Entities", false);
    private final Setting targetPlayers = new Setting("Players", true);
    private final Setting targetMobs = new Setting("Mobs", false);
    private final Setting targetInvisible = new Setting("Invisible", false);
    private final Setting targetOnlyArmor = new Setting("Only Armor", false);
    private final Setting targetOnlyNaked = new Setting("Only Naked", false);

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        addSetting(mode);
        addSetting(range);
        addSetting(throughGrass);
        addSetting(throughEntities);
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

        // Проверка дистанции
        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;

        // Проверка кулдауна
        if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;

        // Логика критов
        String modeStr = mode.getEnum();
        if (modeStr.equals("Smart") || modeStr.equals("OnlyCrits")) {
            boolean isFalling = !p.isOnGround() && p.getVelocity().y < 0 && p.fallDistance > 0.1f;
            if (!isFalling) return;

            // Smart: не бьем в самом конце падения (свип)
            if (modeStr.equals("Smart") && p.fallDistance < 0.2f) return;

            // Сброс спринта
            if (p.isSprinting()) {
                p.setSprinting(false);
                cooldown = 1;
                return;
            }
        }

        // === ФИНАЛЬНАЯ ПРОВЕРКА: ЦЕЛЬ НЕ ЗА БЛОКОМ ===
        if (!canSeeTarget(p, target)) return;

        // ФИНАЛЬНАЯ ПРОВЕРКА ДИСТАНЦИИ
        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;

        // Бьем
        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        
        // Рандомизация (1-3 тика)
        cooldown = 1 + RNG.nextInt(3);
    }

    // Проверка, что между нами и целью нет блоков
    private boolean canSeeTarget(ClientPlayerEntity p, LivingEntity target) {
        Vec3d eye = p.getEyePos();
        Vec3d targetCenter = target.getBoundingBox().getCenter();
        Vec3d diff = targetCenter.subtract(eye);
        double dist = diff.length();
        
        if (dist > range.getFloat()) return false;
        
        Vec3d end = eye.add(diff.normalize().multiply(dist));
        BlockHitResult blockHit = p.getWorld().raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, p));
        
        // Если блок перекрывает цель и это не трава (и мы не бьем через траву)
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState state = p.getWorld().getBlockState(blockHit.getBlockPos());
            boolean isPlant = state.getBlock() instanceof PlantBlock;
            
            if (!isPlant && !throughEntities.getBool()) {
                // Блок перекрывает - не бьем
                return false;
            }
            if (isPlant && !throughGrass.getBool()) {
                return false;
            }
        }
        return true;
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
                if (isPlant && !throughGrass.getBool()) continue;
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
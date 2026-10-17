package com.fox.bogempty.module.combat;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import com.fox.bogempty.utils.CritChecks;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
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

    // === НАСТРОЙКИ ИЗ SYSTEM DLC ===
    private final Setting mode = new Setting("Mode", "Smart", new String[]{"Smart", "OnlyCrits", "Normal"});
    private final Setting targets = new Setting("Targets", "Players", new String[]{"Players", "Mobs", "All"});
    private final Setting bit = new Setting("Bit", "ThroughGrass", new String[]{"ThroughGrass", "Invisible", "OnlyArmor", "OnlyNaked"});
    private final Setting range = new Setting("Range", 3.0f, 2.0f, 6.0f);
    private final Setting throughEntities = new Setting("Through Entities", false);
    private final Setting customCooldown = new Setting("Custom Cooldown", false);
    private final Setting cooldownValue = new Setting("CD %", 80f, 1f, 100f);
    private final Setting tpsSync = new Setting("TPS Sync", false);
    private final Setting noSweaps = new Setting("No Sweaps", true);

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        // Добавляем настройки в меню
        addSetting(mode);
        addSetting(targets);
        addSetting(bit);
        addSetting(range);
        addSetting(throughEntities);
        addSetting(customCooldown);
        addSetting(cooldownValue);
        addSetting(tpsSync);
        addSetting(noSweaps);
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

        double dist = p.squaredDistanceTo(target);
        if (dist > range.getFloat() * range.getFloat()) return;

        // Кулдаун
        float cdProgress = p.getAttackCooldownProgress(1.0f);
        if (customCooldown.getBool()) {
            if (cdProgress < (cooldownValue.getFloat() / 100f)) return;
        } else {
            if (cdProgress < 1.0f) return;
        }

        // Логика критов
        String modeStr = mode.getEnum();
        if (modeStr.equals("Smart") || modeStr.equals("OnlyCrits")) {
            // Проверка, что мы падаем (крит)
            if (!CritChecks.isCrit(p)) return;

            // "Не бить свипами" (пропускаем удар, если мы почти на земле)
            if (noSweaps.getBool() && CritChecks.isAboutToLand(p)) return;

            // Сброс спринта
            if (p.isSprinting()) {
                p.setSprinting(false);
                cooldown = 1;
                return;
            }
        }

        // Бьем!
        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        
        // Рандомизация (обход античита)
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

        // Через траву
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
        String bitStr = bit.getEnum(); // Настройка "Бить"
        boolean canThroughGrass = bitStr.equals("ThroughGrass");

        for (Entity e : mc.world.getOtherEntities(p, search)) {
            if (!(e instanceof LivingEntity le) || !isValid(le, p)) continue;

            var opt = le.getBoundingBox().expand(0.1).raycast(eye, end);
            if (opt.isPresent()) {
                double d = eye.squaredDistanceTo(opt.get());
                
                // Если мешает блок (и это не трава, или мы не хотим через траву)
                if (!isPlant && d > blockDist && !throughEntities.getBool()) continue;
                if (isPlant && !canThroughGrass) continue;

                if (d < bestD) { bestD = d; best = le; }
            }
        }
        return best;
    }

    private boolean isValid(LivingEntity e, ClientPlayerEntity p) {
        if (e == p || !e.isAlive() || e.isSpectator() || !e.canHit()) return false;
        
        String targetStr = targets.getEnum();
        if (targetStr.equals("Players") && !(e instanceof PlayerEntity)) return false;
        if (targetStr.equals("Mobs") && !(e instanceof MobEntity)) return false;
        
        String bitStr = bit.getEnum();
        if (bitStr.equals("Invisible") && !e.isInvisible()) return false;
        // Проверка на броню (упрощенно)
        if (bitStr.equals("OnlyArmor") && e.getArmorItems().iterator().next().isEmpty()) return false;
        if (bitStr.equals("OnlyNaked") && !e.getArmorItems().iterator().next().isEmpty()) return false;

        return true;
    }
}
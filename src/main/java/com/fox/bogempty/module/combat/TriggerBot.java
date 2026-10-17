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
    private final Setting targets = new Setting("Targets", "Players", new String[]{"Players", "Mobs", "All"});
    private final Setting range = new Setting("Range", 3.0f, 2.0f, 6.0f);
    private final Setting noSweaps = new Setting("No Sweaps", true);

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        addSetting(mode);
        addSetting(targets);
        addSetting(range);
        addSetting(noSweaps);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        ClientPlayerEntity p = mc.player;

        if (p.isUsingItem()) return;
        if (cooldown > 0) { cooldown--; return; }

        LivingEntity target = getTarget(mc, p);
        if (target == null) return;

        if (p.squaredDistanceTo(target) > range.getFloat() * range.getFloat()) return;

        // Ждем полной готовности удара
        if (p.getAttackCooldownProgress(1.0f) < 1.0f) return;

        String m = mode.getEnum();

        if (m.equals("OnlyCrits")) {
            if (!isCrit(p)) return;
        } else if (m.equals("Smart")) {
            // Умная логика: если мы на земле, бьем сплешом (обычный удар).
            // Если падаем — проверяем, не свип ли это, и бьем критом.
            if (!p.isOnGround()) {
                // Если падаем и близко к земле (свип) - не бьем, ждем
                if (noSweaps.getBool() && p.getVelocity().y < 0 && p.fallDistance < 0.2f) return;
            }
        }

        // Сброс спринта для крита
        if (!p.isOnGround() && p.isSprinting()) {
            p.setSprinting(false);
            cooldown = 1;
            return;
        }

        // Удар
        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        cooldown = 1 + RNG.nextInt(2);
    }

    private boolean isCrit(ClientPlayerEntity p) {
        return !p.isOnGround() && p.fallDistance > 0.0f && p.getVelocity().y < 0;
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

        // Проверка на траву
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
                if (!isPlant && d > blockDist) continue; // Блок мешает
                if (d < bestD) { bestD = d; best = le; }
            }
        }
        return best;
    }

    private boolean isValid(LivingEntity e, ClientPlayerEntity p) {
        if (e == p || !e.isAlive() || e.isSpectator() || !e.canHit()) return false;
        String t = targets.getEnum();
        if (t.equals("Players") && !(e instanceof PlayerEntity)) return false;
        if (t.equals("Mobs") && !(e instanceof MobEntity)) return false;
        return true;
    }
}
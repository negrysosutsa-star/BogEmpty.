package com.fox.bogempty.module.player;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", Category.PLAYER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (mc.options.useKey.isPressed()) {
            ItemStack stack = mc.player.getMainHandStack();
            
            if (stack.getItem() instanceof BlockItem) {
                // Проверяем, что мы смотрим на блок
                if (mc.crosshairTarget instanceof BlockHitResult bhr && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                    // Симулируем установку блока
                    mc.interactionManager.interactBlock(mc.player, net.minecraft.util.Hand.MAIN_HAND, bhr);
                }
            }
        }
    }
}
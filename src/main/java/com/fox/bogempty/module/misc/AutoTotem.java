package com.fox.bogempty.module.misc;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AutoTotem extends Module {
    private final Setting healthThreshold = new Setting("Health", 10f, 1f, 20f);
    private final Setting delay = new Setting("Delay", 1f, 0f, 5f);
    private int cooldown = 0;

    public AutoTotem() {
        super("AutoTotem", Category.MISC);
        addSetting(healthThreshold);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        ClientPlayerEntity p = mc.player;

        if (cooldown > 0) { cooldown--; return; }

        // Если хп выше порога — не свапаем
        if (p.getHealth() > healthThreshold.getFloat()) return;

        // Если в оффхенде уже тотем — ничего не делаем
        if (p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        // Ищем тотем в хотбаре
        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getStack(i);
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                int prevSlot = p.getInventory().selectedSlot;
                p.getInventory().selectedSlot = i;
                // Свап в оффхенд
                mc.interactionManager.interactItem(p, net.minecraft.util.Hand.MAIN_HAND);
                p.getInventory().selectedSlot = prevSlot;
                cooldown = (int) delay.getFloat();
                break;
            }
        }
    }
}
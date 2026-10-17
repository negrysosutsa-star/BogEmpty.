package com.fox.bogempty.module.misc;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import com.fox.bogempty.utils.InventoryUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;

public class AutoTotem extends Module {
    private final Setting healthThreshold = new Setting("Health", 10f, 1f, 20f);
    private int cooldown = 0;

    public AutoTotem() {
        super("AutoTotem", Category.MISC);
        addSetting(healthThreshold);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        ClientPlayerEntity p = mc.player;

        if (cooldown > 0) { cooldown--; return; }
        if (p.getHealth() > healthThreshold.getFloat()) return;
        if (InventoryUtil.hasTotemInOffhand()) return;

        int slot = InventoryUtil.findItemInHotbar(Items.TOTEM_OF_UNDYING);
        if (slot != -1) {
            int prevSlot = p.getInventory().selectedSlot;
            InventoryUtil.switchToSlot(slot);
            mc.interactionManager.interactItem(p, net.minecraft.util.Hand.MAIN_HAND);
            InventoryUtil.switchToSlot(prevSlot);
            cooldown = 5;
        }
    }
}
package com.fox.bogempty.module.player;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoSwap extends Module {
    private final Setting mode = new Setting("Mode", "Totem", new String[]{"Totem", "Orb", "Cycle"});
    private final Setting delay = new Setting("Delay", 2f, 1f, 10f);
    
    private int cooldown = 0;
    private int cycleIndex = 0;

    public AutoSwap() {
        super("AutoSwap", Category.PLAYER);
        addSetting(mode);
        addSetting(delay);
    }

    public void onTrigger() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (cooldown > 0) return;

        ClientPlayerEntity p = mc.player;
        ItemStack targetItem = getTargetItem();
        if (targetItem == null) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getStack(i);
            if (stack.getItem() == targetItem.getItem()) {
                p.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(p, Hand.MAIN_HAND);
                cooldown = (int) delay.getFloat();
                return;
            }
        }
    }

    private ItemStack getTargetItem() {
        String m = mode.getEnum();
        if (m.equals("Totem")) return new ItemStack(Items.TOTEM_OF_UNDYING);
        if (m.equals("Orb")) return new ItemStack(Items.ENDER_PEARL);
        
        ItemStack[] cycle = {new ItemStack(Items.TOTEM_OF_UNDYING), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_EYE)};
        ItemStack res = cycle[cycleIndex];
        cycleIndex = (cycleIndex + 1) % cycle.length;
        return res;
    }

    @Override
    public void onTick() {
        if (cooldown > 0) cooldown--;
    }
}
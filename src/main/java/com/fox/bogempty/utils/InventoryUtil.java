package com.fox.bogempty.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class InventoryUtil {
    
    // Ищет слот с нужным предметом в хотбаре (0-8)
    public static int findItemInHotbar(Item item) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return -1;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getStack(i);
            if (stack.isOf(item)) return i;
        }
        return -1;
    }

    // Меняет слот в руке
    public static void switchToSlot(int slot) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p != null && slot >= 0 && slot < 9) {
            p.getInventory().selectedSlot = slot;
        }
    }

    // Проверяет, есть ли тотем в оффхенде
    public static boolean hasTotemInOffhand() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        return p != null && p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
    }
}
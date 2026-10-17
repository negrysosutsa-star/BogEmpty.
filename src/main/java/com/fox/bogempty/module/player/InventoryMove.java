package com.fox.bogempty.module.player;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import net.minecraft.client.MinecraftClient;

public class InventoryMove extends Module {
    public InventoryMove() {
        super("InventoryMove", Category.PLAYER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        // Разрешает движение при открытом экране (инвентарь/сундук)
        if (mc.currentScreen != null && !(mc.currentScreen instanceof com.fox.bogempty.gui.ClickGUI)) {
            // По сути, мы просто даем игре обрабатывать ввод
        }
    }
}
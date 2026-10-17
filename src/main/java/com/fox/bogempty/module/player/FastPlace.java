package com.fox.bogempty.module.player;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import net.minecraft.client.MinecraftClient;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", Category.PLAYER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        // Ставит задержку использования предмета на 0
        mc.itemUseCooldown = 0;
    }
}
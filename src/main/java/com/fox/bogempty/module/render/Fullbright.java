package com.fox.bogempty.module.render;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import net.minecraft.client.MinecraftClient;

public class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", Category.RENDER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            // Ставим гамму на максимум (как будто эффект ночного видения)
            if (mc.options.getGamma().getValue() < 10.0) {
                mc.options.getGamma().setValue(10.0);
            }
        }
    }

    @Override
    public void setEnabled(boolean state) {
        super.setEnabled(state);
        if (!state) {
            // Возвращаем стандартную гамму
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options != null) {
                mc.options.getGamma().setValue(1.0);
            }
        }
    }
}
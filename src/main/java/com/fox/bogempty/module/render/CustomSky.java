package com.fox.bogempty.module.render;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import net.minecraft.client.MinecraftClient;

public class CustomSky extends Module {
    private final Setting color = new Setting("Color", "Purple", new String[]{"Purple", "Blue", "Red", "Green", "Black"});

    public CustomSky() {
        super("CustomSky", Category.RENDER);
        addSetting(color);
    }

    // Мы будем использовать это в миксине для изменения цвета неба
    public int getSkyColor() {
        switch (color.getEnum()) {
            case "Purple": return 0x4A148C; // Фиолетовый
            case "Blue": return 0x0D47A1;   // Синий
            case "Red": return 0xB71C1C;    // Красный
            case "Green": return 0x1B5E20;  // Зеленый
            case "Black": return 0x000000;  // Черный
            default: return 0x4A148C;
        }
    }
}
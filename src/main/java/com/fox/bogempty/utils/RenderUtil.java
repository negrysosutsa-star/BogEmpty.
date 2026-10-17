package com.fox.bogempty.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class RenderUtil {

    // Рисует текст с обводкой (просто рисуем 4 раза черным со сдвигом)
    public static void drawTextWithOutline(DrawContext context, String text, int x, int y, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer font = mc.textRenderer;

        // Черная обводка
        context.drawText(font, text, x - 1, y, 0xFF000000, false);
        context.drawText(font, text, x + 1, y, 0xFF000000, false);
        context.drawText(font, text, x, y - 1, 0xFF000000, false);
        context.drawText(font, text, x, y + 1, 0xFF000000, false);

        // Сам текст (с тенью)
        context.drawText(font, text, x, y, color, true);
    }

    // Рисует фон под текст (просто прямоугольник)
    public static void drawTextBackground(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x - 2, y - 2, x + width + 2, y + height + 2, color);
    }
}
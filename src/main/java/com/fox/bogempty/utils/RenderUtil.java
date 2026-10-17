package com.fox.bogempty.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class RenderUtil {

    // Рисует текст с обводкой (чтобы читалось на любом фоне)
    public static void drawTextWithOutline(DrawContext context, String text, int x, int y, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer font = mc.textRenderer;
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Черная обводка (сдвигаем текст на 1 пиксель во все стороны)
        font.draw(text, x - 1, y, 0xFF000000, false, matrix, context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        font.draw(text, x + 1, y, 0xFF000000, false, matrix, context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        font.draw(text, x, y - 1, 0xFF000000, false, matrix, context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        font.draw(text, x, y + 1, 0xFF000000, false, matrix, context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

        // Сам текст
        font.draw(text, x, y, color, true, matrix, context.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
    }

    // Рисует фон для текста с отступами (padding)
    public static void drawTextBackground(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x - 2, y - 2, x + width + 2, y + height + 2, color);
    }

    // Линейная интерполяция (для плавной анимации)
    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    // Скругление углов (упрощенное для 2D)
    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        // Рисуем основной прямоугольник
        context.fill(x + 2, y, x + width - 2, y + height, color);
        context.fill(x, y + 2, x + 2, y + height - 2, color);
        context.fill(x + width - 2, y + 2, x + width, y + height - 2, color);
        
        // Закрашиваем углы (имитация скругления)
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }
}
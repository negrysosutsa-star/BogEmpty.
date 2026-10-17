package com.fox.bogempty.module.render;

import com.fox.bogempty.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ESPModule {

    public void onRender(MatrixStack matrices, Camera camera, float tickDelta) {
        if (!Config.espEnabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d camPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player || !le.isAlive()) continue;

            // Фильтр целей
            boolean isPlayer = le instanceof PlayerEntity;
            boolean isMob = le instanceof MobEntity && !(le instanceof AnimalEntity);
            
            if (isPlayer && !Config.espPlayers) continue;
            if (isMob && !Config.espMobs) continue;

            // Фильтр по броне
            if (isPlayer && Config.espFilterArmor && !hasArmor(le)) continue;
            if (isPlayer && Config.espFilterNaked && hasArmor(le)) continue;

            double dist = mc.player.squaredDistanceTo(le);
            if (dist > 128 * 128) continue; // Дальше 128 блоков не рисуем

            // Интерполяция позиции (чтобы не дёргался)
            double x = MathHelper.lerp(tickDelta, le.prevX, le.getX()) - camPos.x;
            double y = MathHelper.lerp(tickDelta, le.prevY, le.getY()) - camPos.y;
            double z = MathHelper.lerp(tickDelta, le.prevZ, le.getZ()) - camPos.z;

            Box box = le.getBoundingBox();
            
            // Масштабирование линий от дистанции
            float thickness = Config.espScaleByDistance ? Math.max(1.0f, (float)(Config.espLineThickness * (1.0f - dist / (128*128)))) : Config.espLineThickness;
            RenderSystem.lineWidth(thickness);

            matrices.push();
            matrices.translate(x, y, z);
            Matrix4f m = matrices.peek().getPositionMatrix();

            float w = (float)(box.maxX - box.minX) / 2f;
            float h = (float)(box.maxY - box.minY);

            // Рисуем Бокс
            drawBox(matrices, m, w, h, Config.espLineColor, Config.espFill ? Config.espFillColor : 0);

            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void drawBox(MatrixStack matrices, Matrix4f m, float w, float h, int lineColor, int fillColor) {
        Tessellator tess = Tessellator.getInstance();
        
        // Линии (Контур)
        BufferBuilder bufLines = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        float x = w, z = w;
        
        // Низ
        bufLines.vertex(m, -x, 0, -z).color(lineColor); bufLines.vertex(m, x, 0, -z).color(lineColor);
        bufLines.vertex(m, x, 0, -z).color(lineColor); bufLines.vertex(m, x, 0, z).color(lineColor);
        bufLines.vertex(m, x, 0, z).color(lineColor); bufLines.vertex(m, -x, 0, z).color(lineColor);
        bufLines.vertex(m, -x, 0, z).color(lineColor); bufLines.vertex(m, -x, 0, -z).color(lineColor);
        // Верх
        bufLines.vertex(m, -x, h, -z).color(lineColor); bufLines.vertex(m, x, h, -z).color(lineColor);
        bufLines.vertex(m, x, h, -z).color(lineColor); bufLines.vertex(m, x, h, z).color(lineColor);
        bufLines.vertex(m, x, h, z).color(lineColor); bufLines.vertex(m, -x, h, z).color(lineColor);
        bufLines.vertex(m, -x, h, z).color(lineColor); bufLines.vertex(m, -x, h, -z).color(lineColor);
        // Вертикали
        bufLines.vertex(m, -x, 0, -z).color(lineColor); bufLines.vertex(m, -x, h, -z).color(lineColor);
        bufLines.vertex(m, x, 0, -z).color(lineColor); bufLines.vertex(m, x, h, -z).color(lineColor);
        bufLines.vertex(m, x, 0, z).color(lineColor); bufLines.vertex(m, x, h, z).color(lineColor);
        bufLines.vertex(m, -x, 0, z).color(lineColor); bufLines.vertex(m, -x, h, z).color(lineColor);
        
        BufferRenderer.drawWithGlobalProgram(bufLines.end());

        // Заливка (Квады)
        if (fillColor != 0) {
            BufferBuilder bufFill = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            // Задняя сторона
            bufFill.vertex(m, -x, 0, -z).color(fillColor); bufFill.vertex(m, x, 0, -z).color(fillColor); bufFill.vertex(m, x, h, -z).color(fillColor); bufFill.vertex(m, -x, h, -z).color(fillColor);
            // Передняя
            bufFill.vertex(m, -x, 0, z).color(fillColor); bufFill.vertex(m, -x, h, z).color(fillColor); bufFill.vertex(m, x, h, z).color(fillColor); bufFill.vertex(m, x, 0, z).color(fillColor);
            // Левая
            bufFill.vertex(m, -x, 0, -z).color(fillColor); bufFill.vertex(m, -x, h, -z).color(fillColor); bufFill.vertex(m, -x, h, z).color(fillColor); bufFill.vertex(m, -x, 0, z).color(fillColor);
            // Правая
            bufFill.vertex(m, x, 0, -z).color(fillColor); bufFill.vertex(m, x, 0, z).color(fillColor); bufFill.vertex(m, x, h, z).color(fillColor); bufFill.vertex(m, x, h, -z).color(fillColor);
            
            BufferRenderer.drawWithGlobalProgram(bufFill.end());
        }
    }

    private boolean hasArmor(LivingEntity e) {
        for (ItemStack stack : e.getArmorItems()) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }
}
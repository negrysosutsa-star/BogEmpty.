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
        RenderSystem.disableDepthTest(); // Рисуем сквозь стены
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player || !le.isAlive()) continue;
            
            // --- ЧЕТКИЙ ФИЛЬТР ЦЕЛЕЙ ---
            boolean isPlayer = le instanceof PlayerEntity;
            boolean isMob = le instanceof MobEntity && !(le instanceof AnimalEntity);
            
            if (isPlayer && !Config.espPlayers) continue; // Если это игрок, а игроки выключены — скип
            if (isMob && !Config.espMobs) continue;       // Если это моб, а мобы выключены — скип
            if (!isPlayer && !isMob) continue;            // Животных игнорим

            // Проверка на невидимку
            if (le.isInvisible() && !Config.espInvisible) continue;

            // Фильтры по броне (только для игроков)
            if (isPlayer) {
                boolean hasArmor = false;
                for (ItemStack stack : le.getArmorItems()) {
                    if (!stack.isEmpty()) { hasArmor = true; break; }
                }
                if (Config.espFilterArmor && !hasArmor) continue;
                if (Config.espFilterNaked && hasArmor) continue;
            }

            double dist = mc.player.squaredDistanceTo(le);
            if (dist > 128 * 128) continue; // Дальность 128 блоков

            // Интерполяция (плавность)
            double x = MathHelper.lerp(tickDelta, le.prevX, le.getX()) - camPos.x;
            double y = MathHelper.lerp(tickDelta, le.prevY, le.getY()) - camPos.y;
            double z = MathHelper.lerp(tickDelta, le.prevZ, le.getZ()) - camPos.z;

            Box box = le.getBoundingBox();
            float thickness = Config.espScaleByDistance ? Math.max(1.0f, Config.espLineThickness * (float)(1.0 - Math.min(1.0, Math.sqrt(dist) / 128.0))) : Config.espLineThickness;
            
            RenderSystem.lineWidth(thickness);

            matrices.push();
            matrices.translate(x, y, z);
            Matrix4f m = matrices.peek().getPositionMatrix();

            float w = (float)(box.maxX - box.minX) / 2f;
            float h = (float)(box.maxY - box.minY);

            // Если нужна обводка (рисуем черный бокс чуть больше)
            if (Config.espOutline) {
                drawBox(m, w + 0.02f, h + 0.02f, 0xFF000000, 0);
            }

            // Основной бокс
            drawBox(m, w, h, Config.espLineColor, Config.espFill ? Config.espFillColor : 0);

            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void drawBox(Matrix4f m, float w, float h, int lineColor, int fillColor) {
        Tessellator tess = Tessellator.getInstance();
        
        // Заливка
        if (fillColor != 0) {
            BufferBuilder bufFill = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            // Стороны куба
            bufFill.vertex(m, -w, 0, -w).color(fillColor); bufFill.vertex(m, w, 0, -w).color(fillColor); bufFill.vertex(m, w, h, -w).color(fillColor); bufFill.vertex(m, -w, h, -w).color(fillColor);
            bufFill.vertex(m, -w, 0, w).color(fillColor); bufFill.vertex(m, -w, h, w).color(fillColor); bufFill.vertex(m, w, h, w).color(fillColor); bufFill.vertex(m, w, 0, w).color(fillColor);
            bufFill.vertex(m, -w, 0, -w).color(fillColor); bufFill.vertex(m, -w, h, -w).color(fillColor); bufFill.vertex(m, -w, h, w).color(fillColor); bufFill.vertex(m, -w, 0, w).color(fillColor);
            bufFill.vertex(m, w, 0, -w).color(fillColor); bufFill.vertex(m, w, 0, w).color(fillColor); bufFill.vertex(m, w, h, w).color(fillColor); bufFill.vertex(m, w, h, -w).color(fillColor);
            BufferRenderer.drawWithGlobalProgram(bufFill.end());
        }

        // Линии
        BufferBuilder bufLines = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        // Низ
        bufLines.vertex(m, -w, 0, -w).color(lineColor); bufLines.vertex(m, w, 0, -w).color(lineColor);
        bufLines.vertex(m, w, 0, -w).color(lineColor); bufLines.vertex(m, w, 0, w).color(lineColor);
        bufLines.vertex(m, w, 0, w).color(lineColor); bufLines.vertex(m, -w, 0, w).color(lineColor);
        bufLines.vertex(m, -w, 0, w).color(lineColor); bufLines.vertex(m, -w, 0, -w).color(lineColor);
        // Верх
        bufLines.vertex(m, -w, h, -w).color(lineColor); bufLines.vertex(m, w, h, -w).color(lineColor);
        bufLines.vertex(m, w, h, -w).color(lineColor); bufLines.vertex(m, w, h, w).color(lineColor);
        bufLines.vertex(m, w, h, w).color(lineColor); bufLines.vertex(m, -w, h, w).color(lineColor);
        bufLines.vertex(m, -w, h, w).color(lineColor); bufLines.vertex(m, -w, h, -w).color(lineColor);
        // Вертикали
        bufLines.vertex(m, -w, 0, -w).color(lineColor); bufLines.vertex(m, -w, h, -w).color(lineColor);
        bufLines.vertex(m, w, 0, -w).color(lineColor); bufLines.vertex(m, w, h, -w).color(lineColor);
        bufLines.vertex(m, w, 0, w).color(lineColor); bufLines.vertex(m, w, h, w).color(lineColor);
        bufLines.vertex(m, -w, 0, w).color(lineColor); bufLines.vertex(m, -w, h, w).color(lineColor);
        
        BufferRenderer.drawWithGlobalProgram(bufLines.end());
    }
}
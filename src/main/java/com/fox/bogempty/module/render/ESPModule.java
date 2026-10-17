package com.fox.bogempty.module.render;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ESPModule extends Module {
    private final Setting showPlayers = new Setting("Show Players", true);
    private final Setting showMobs = new Setting("Show Mobs", false);
    private final Setting fill = new Setting("Fill Box", true);
    private final Setting arrows = new Setting("Arrows", true);

    public ESPModule() {
        super("ESP", Category.RENDER);
        addSetting(showPlayers);
        addSetting(showMobs);
        addSetting(fill);
        addSetting(arrows);
    }

    public void onRender(MatrixStack matrices, Camera camera, float tickDelta) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d camPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        int lineColor = 0xFF9D4EDD;
        int fillColor = 0x409D4EDD;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player || !le.isAlive()) continue;
            
            boolean isPlayer = le instanceof PlayerEntity;
            boolean isMob = le instanceof MobEntity;
            
            if (isPlayer && !showPlayers.getBool()) continue;
            if (isMob && !showMobs.getBool()) continue;
            if (!isPlayer && !isMob) continue;

            double x = MathHelper.lerp(tickDelta, le.prevX, le.getX()) - camPos.x;
            double y = MathHelper.lerp(tickDelta, le.prevY, le.getY()) - camPos.y;
            double z = MathHelper.lerp(tickDelta, le.prevZ, le.getZ()) - camPos.z;

            Box box = le.getBoundingBox();
            matrices.push();
            matrices.translate(x, y, z);
            Matrix4f m = matrices.peek().getPositionMatrix();

            float w = (float)(box.maxX - box.minX) / 2f;
            float h = (float)(box.maxY - box.minY);

            drawBox(m, w, h, lineColor, fill.getBool() ? fillColor : 0);
            
            if (arrows.getBool()) {
                drawArrow(matrices, w, h, lineColor);
            }
            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // Рисуем вращающуюся 3D-стрелку (ромб с хвостом)
    private void drawArrow(MatrixStack matrices, float w, float h, int color) {
        float time = (float) (System.currentTimeMillis() % 100000L) / 1000f;
        float angle = time * 90f; // Скорость вращения

        matrices.push();
        // Поднимаем стрелку на уровень головы и отодвигаем от хита
        matrices.translate(0, h * 0.5, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
        matrices.translate(w + 0.3, 0, 0); // Радиус от игрока

        Matrix4f m = matrices.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // Хвост стрелки
        buf.vertex(m, 0, 0, 0).color(color); buf.vertex(m, 0.4f, 0, 0).color(color);
        // Наконечники
        buf.vertex(m, 0.4f, 0, 0).color(color); buf.vertex(m, 0.2f, 0.2f, 0).color(color);
        buf.vertex(m, 0.4f, 0, 0).color(color); buf.vertex(m, 0.2f, -0.2f, 0).color(color);

        BufferRenderer.drawWithGlobalProgram(buf.end());
        matrices.pop();
    }

    private void drawBox(Matrix4f m, float w, float h, int lineColor, int fillColor) {
        Tessellator tess = Tessellator.getInstance();
        if (fillColor != 0) {
            BufferBuilder bufFill = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufFill.vertex(m, -w, 0, -w).color(fillColor); bufFill.vertex(m, w, 0, -w).color(fillColor); bufFill.vertex(m, w, h, -w).color(fillColor); bufFill.vertex(m, -w, h, -w).color(fillColor);
            bufFill.vertex(m, -w, 0, w).color(fillColor); bufFill.vertex(m, -w, h, w).color(fillColor); bufFill.vertex(m, w, h, w).color(fillColor); bufFill.vertex(m, w, 0, w).color(fillColor);
            bufFill.vertex(m, -w, 0, -w).color(fillColor); bufFill.vertex(m, -w, h, -w).color(fillColor); bufFill.vertex(m, -w, h, w).color(fillColor); bufFill.vertex(m, -w, 0, w).color(fillColor);
            bufFill.vertex(m, w, 0, -w).color(fillColor); bufFill.vertex(m, w, 0, w).color(fillColor); bufFill.vertex(m, w, h, w).color(fillColor); bufFill.vertex(m, w, h, -w).color(fillColor);
            BufferRenderer.drawWithGlobalProgram(bufFill.end());
        }

        BufferBuilder bufLines = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufLines.vertex(m, -w, 0, -w).color(lineColor); bufLines.vertex(m, w, 0, -w).color(lineColor);
        bufLines.vertex(m, w, 0, -w).color(lineColor); bufLines.vertex(m, w, 0, w).color(lineColor);
        bufLines.vertex(m, w, 0, w).color(lineColor); bufLines.vertex(m, -w, 0, w).color(lineColor);
        bufLines.vertex(m, -w, 0, w).color(lineColor); bufLines.vertex(m, -w, 0, -w).color(lineColor);
        bufLines.vertex(m, -w, h, -w).color(lineColor); bufLines.vertex(m, w, h, -w).color(lineColor);
        bufLines.vertex(m, w, h, -w).color(lineColor); bufLines.vertex(m, w, h, w).color(lineColor);
        bufLines.vertex(m, w, h, w).color(lineColor); bufLines.vertex(m, -w, h, w).color(lineColor);
        bufLines.vertex(m, -w, h, w).color(lineColor); bufLines.vertex(m, -w, h, -w).color(lineColor);
        bufLines.vertex(m, -w, 0, -w).color(lineColor); bufLines.vertex(m, -w, h, -w).color(lineColor);
        bufLines.vertex(m, w, 0, -w).color(lineColor); bufLines.vertex(m, w, h, -w).color(lineColor);
        bufLines.vertex(m, w, 0, w).color(lineColor); bufLines.vertex(m, w, h, w).color(lineColor);
        bufLines.vertex(m, -w, 0, w).color(lineColor); bufLines.vertex(m, -w, h, w).color(lineColor);
        BufferRenderer.drawWithGlobalProgram(bufLines.end());
    }
}
package com.fox.bogempty.module.render;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ESPModule extends Module {
    private final Setting showPlayers = new Setting("Show Players", true);
    private final Setting showMobs = new Setting("Show Mobs", false);
    private final Setting particles = new Setting("Particles", true);
    private final Setting particleColor = new Setting("Color", "Purple", new String[]{"Purple", "Red", "Green", "Blue"});

    public ESPModule() {
        super("ESP", Category.RENDER);
        addSetting(showPlayers);
        addSetting(showMobs);
        addSetting(particles);
        addSetting(particleColor);
    }

    public void onRender(MatrixStack matrices, Camera camera, float tickDelta) {
        if (!isEnabled() || !particles.getBool()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d camPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        int color = 0xFF9D4EDD;
        if (particleColor.getEnum().equals("Red")) color = 0xFFFF4444;
        if (particleColor.getEnum().equals("Green")) color = 0xFF44FF44;
        if (particleColor.getEnum().equals("Blue")) color = 0xFF4444FF;

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

            matrices.push();
            matrices.translate(x, y + le.getHeight() / 2, z);
            
            float time = (float) (System.currentTimeMillis() % 10000L) / 1000f;
            for (int i = 0; i < 8; i++) {
                float angle = time * 2.0f + (i * (float)(Math.PI / 4));
                float radius = 0.6f;
                float px = MathHelper.cos(angle) * radius;
                float pz = MathHelper.sin(angle) * radius;
                float py = MathHelper.sin(time * 3.0f + i) * 0.3f;

                Matrix4f m = matrices.peek().getPositionMatrix();
                Tessellator tess = Tessellator.getInstance();
                BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                
                float size = 0.05f;
                buf.vertex(m, px - size, py - size, pz - size).color(color);
                buf.vertex(m, px + size, py - size, pz - size).color(color);
                buf.vertex(m, px + size, py + size, pz - size).color(color);
                buf.vertex(m, px - size, py + size, pz - size).color(color);
                
                BufferRenderer.drawWithGlobalProgram(buf.end());
            }
            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public void onRender2D(net.minecraft.client.gui.DrawContext ctx, float tickDelta) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;
        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;
        
        int drawColor = 0xFF9D4EDD;
        if (particleColor.getEnum().equals("Red")) drawColor = 0xFFFF4444;
        if (particleColor.getEnum().equals("Green")) drawColor = 0xFF44FF44;
        if (particleColor.getEnum().equals("Blue")) drawColor = 0xFF4444FF;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == p || !le.isAlive()) continue;
            
            boolean isPlayer = le instanceof PlayerEntity;
            boolean isMob = le instanceof MobEntity;
            if (isPlayer && !showPlayers.getBool()) continue;
            if (isMob && !showMobs.getBool()) continue;
            if (!isPlayer && !isMob) continue;

            Vec3d eye = p.getEyePos();
            Vec3d targetPos = le.getBoundingBox().getCenter();
            Vec3d diff = targetPos.subtract(eye);
            
            double targetYaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
            double yawDelta = MathHelper.wrapDegrees(targetYaw - p.getYaw());

            if (Math.abs(yawDelta) > 180) continue;

            double rad = Math.toRadians(yawDelta - 90);
            double arrowX = centerX + Math.cos(rad) * 60;
            double arrowY = centerY + Math.sin(rad) * 60;

            drawArrow(ctx, (int)arrowX, (int)arrowY, yawDelta, drawColor);
        }
    }

    private void drawArrow(net.minecraft.client.gui.DrawContext ctx, int x, int y, double yawDelta, int color) {
        float s = 10f;
        double rot = Math.toRadians(yawDelta);
        
        int x1 = x + (int)(Math.sin(rot) * s);
        int y1 = y - (int)(Math.cos(rot) * s);
        int x2 = x + (int)(Math.sin(rot + 2.5) * s * 0.7);
        int y2 = y - (int)(Math.cos(rot + 2.5) * s * 0.7);
        int x3 = x + (int)(Math.sin(rot - 2.5) * s * 0.7);
        int y3 = y - (int)(Math.cos(rot - 2.5) * s * 0.7);

        drawLine(ctx, x1, y1, x2, y2, color);
        drawLine(ctx, x2, y2, x3, y3, color);
        drawLine(ctx, x3, y3, x1, y1, color);
    }

    private void drawLine(net.minecraft.client.gui.DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            ctx.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }
}
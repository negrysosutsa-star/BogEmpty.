package com.fox.bogempty.module.render;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Nametags extends Module {
    private final Setting health = new Setting("Health", true);
    private final Setting scale = new Setting("Scale", 1.0f, 0.5f, 2.0f);

    public Nametags() {
        super("Nametags", Category.RENDER);
        addSetting(health);
        addSetting(scale);
    }

    public void onRender(MatrixStack matrices, Camera camera, float tickDelta) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d camPos = camera.getPos();
        TextRenderer textRenderer = mc.textRenderer;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        for (LivingEntity entity : mc.world.getEntitiesByClass(LivingEntity.class, mc.player.getBoundingBox().expand(128), e -> true)) {
            if (entity == mc.player || !entity.isAlive()) continue;

            double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX()) - camPos.x;
            double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY()) - camPos.y + entity.getHeight() + 0.4;
            double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ()) - camPos.z;

            double dist = mc.player.distanceTo(entity);
            float scaleFactor = (float) (Math.max(0.5, Math.min(2.0, 10.0 / dist)) * scale.getFloat());

            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(camera.getRotation());
            matrices.scale(-scaleFactor * 0.025f, -scaleFactor * 0.025f, -scaleFactor * 0.025f);

            String name = entity.getName().getString();
            if (health.getBool() && entity instanceof PlayerEntity p) {
                name += " §c" + (int) Math.ceil(p.getHealth()) + "❤";
            }

            int width = textRenderer.getWidth(name);
            int halfWidth = width / 2;

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buf.vertex(matrix, -halfWidth - 2, -2, 0).color(0, 0, 0, 150);
            buf.vertex(matrix, -halfWidth - 2, 10, 0).color(0, 0, 0, 150);
            buf.vertex(matrix, halfWidth + 2, 10, 0).color(0, 0, 0, 150);
            buf.vertex(matrix, halfWidth + 2, -2, 0).color(0, 0, 0, 150);
            BufferRenderer.drawWithGlobalProgram(buf.end());

            VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
            textRenderer.draw(name, -halfWidth, 0, 0xFFFFFFFF, true, matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            immediate.draw();

            matrices.pop();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
package com.fox.bogempty.module.render;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ESPModule extends Module {
    private final Setting showPlayers = new Setting("Show Players", true);
    private final Setting showMobs = new Setting("Show Mobs", false);
    private final Setting radius = new Setting("Radius", 60f, 30f, 150f);
    private final Setting size = new Setting("Size", 10f, 5f, 25f);
    private final Setting color = new Setting("Color", "Purple", new String[]{"Purple", "Red", "Green", "Blue"});

    public ESPModule() {
        super("ESP", Category.RENDER);
        addSetting(showPlayers);
        addSetting(showMobs);
        addSetting(radius);
        addSetting(size);
        addSetting(color);
    }

    // Теперь это HUD-элемент, поэтому мы переопределяем onRender2D (или вызываем из HudRenderCallback)
    public void onRender2D(DrawContext ctx, float tickDelta) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        ClientPlayerEntity p = mc.player;
        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;
        
        // Выбираем цвет
        int drawColor = 0xFF9D4EDD; // Purple по умолчанию
        if (color.getEnum().equals("Red")) drawColor = 0xFFFF4444;
        if (color.getEnum().equals("Green")) drawColor = 0xFF44FF44;
        if (color.getEnum().equals("Blue")) drawColor = 0xFF4444FF;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == p || !le.isAlive()) continue;
            
            boolean isPlayer = le instanceof PlayerEntity;
            boolean isMob = le instanceof MobEntity;
            if (isPlayer && !showPlayers.getBool()) continue;
            if (isMob && !showMobs.getBool()) continue;
            if (!isPlayer && !isMob) continue;

            // Вычисляем угол между взглядом игрока и целью
            Vec3d eye = p.getEyePos();
            Vec3d targetPos = le.getBoundingBox().getCenter();
            Vec3d diff = targetPos.subtract(eye);
            
            // Угол поворота цели относительно игрока (в градусах)
            double targetYaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
            double yawDelta = MathHelper.wrapDegrees(targetYaw - p.getYaw());

            // Если цель сзади, мы её не показываем (или показываем с краю)
            // Ограничиваем угол от -180 до 180
            if (Math.abs(yawDelta) > 180) continue;

            // Позиция стрелки на экране (по кругу)
            double rad = Math.toRadians(yawDelta - 90); // -90 чтобы 0 градусов было вверху
            double arrowX = centerX + Math.cos(rad) * radius.getFloat();
            double arrowY = centerY + Math.sin(rad) * radius.getFloat();

            drawArrow(ctx, (int)arrowX, (int)arrowY, yawDelta, drawColor);
        }
    }

    // Рисуем треугольник (стрелку) через DrawContext
    private void drawArrow(DrawContext ctx, int x, int y, double yawDelta, int color) {
        float s = size.getFloat();
        
        // Поворачиваем стрелку в сторону цели
        double rot = Math.toRadians(yawDelta);
        
        int x1 = x + (int)(Math.sin(rot) * s);
        int y1 = y - (int)(Math.cos(rot) * s);
        
        int x2 = x + (int)(Math.sin(rot + 2.5) * s * 0.7);
        int y2 = y - (int)(Math.cos(rot + 2.5) * s * 0.7);
        
        int x3 = x + (int)(Math.sin(rot - 2.5) * s * 0.7);
        int y3 = y - (int)(Math.cos(rot - 2.5) * s * 0.7);

        // Заливка треугольника (рисуем линиями, так как в DrawContext нет fillTriangle)
        drawLine(ctx, x1, y1, x2, y2, color);
        drawLine(ctx, x2, y2, x3, y3, color);
        drawLine(ctx, x3, y3, x1, y1, color);
    }

    // Простая отрисовка линии
    private void drawLine(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            ctx.fill(x1, y1, x1 + 1, y1 + 1, color); // Рисуем пиксель
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }
}
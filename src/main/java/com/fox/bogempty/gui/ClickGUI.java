package com.fox.bogempty.gui;

import com.fox.bogempty.config.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    private int panelX = 50, panelY = 50;
    private int dragX = -1, dragY = -1;
    private int tab = 0;
    private int activeSlider = 0;
    private int activeBind = 0;

    // Цвета (Тема Purple)
    private final int ACCENT = 0xFF9D4EDD;
    private final int BG_PANEL = 0xFF161616;
    private final int TEXT = 0xFFDDDDDD;
    private final int TEXT_DIM = 0xFF888888;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // 1. РАЗМЫТИЕ ФОНА
        if (Config.blurEnabled) {
            ctx.fill(0, 0, this.width, this.height, 0x90000000);
            // В полноценном клиенте тут вызывается шейдер размытия, но для базы мы используем затемнение
        } else {
            ctx.fill(0, 0, this.width, this.height, 0x50000000);
        }

        // 2. ПАНЕЛЬ
        ctx.fill(panelX - 1, panelY - 1, panelX + 320 + 1, panelY + 400 + 1, ACCENT); // Бордер
        ctx.fill(panelX, panelY, panelX + 320, panelY + 400, BG_PANEL); // Фон
        ctx.fill(panelX, panelY, panelX + 320, panelY + 30, 0xFF1F1F1F); // Шапка

        ctx.drawText(this.textRenderer, "§lBogEmpty", panelX + 10, panelY + 10, ACCENT, true);
        ctx.drawText(this.textRenderer, "v1.0", panelX + 290, panelY + 10, TEXT_DIM, true);

        // 3. ТАБЫ (ВКЛАДКИ)
        drawTab(ctx, "Combat", 0, panelX + 10, panelY + 35, mouseX, mouseY);
        drawTab(ctx, "Visuals", 1, panelX + 90, panelY + 35, mouseX, mouseY);
        drawTab(ctx, "Config", 2, panelX + 170, panelY + 35, mouseX, mouseY);

        int y = panelY + 65;

        // 4. КОНТЕНТ ВКЛАДОК
        if (tab == 0) {
            // TRIGGER BOT
            ctx.drawText(this.textRenderer, "§7── TriggerBot ──", panelX + 15, y, ACCENT, false); y += 20;
            drawToggle(ctx, "Enabled", Config.triggerEnabled, panelX + 15, y, mouseX, mouseY);
            drawBind(ctx, Config.bindTrigger, 1, panelX + 220, y, mouseX, mouseY); y += 20;
            
            drawEnum(ctx, "Mode", Config.triggerMode.name(), panelX + 15, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Range", (float)Config.triggerRange, 2f, 6f, panelX + 15, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "CPS", Config.triggerCps, 1f, 20f, panelX + 15, y, mouseX, mouseY); y += 25;

            // AIM ASSIST
            ctx.drawText(this.textRenderer, "§7── AimAssist ──", panelX + 15, y, ACCENT, false); y += 20;
            drawToggle(ctx, "Enabled", Config.aimEnabled, panelX + 15, y, mouseX, mouseY);
            drawBind(ctx, Config.bindAim, 2, panelX + 220, y, mouseX, mouseY); y += 20;
            
            drawEnum(ctx, "Mode", Config.aimMode.name(), panelX + 15, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Distance", (float)Config.aimRange, 2f, 8f, panelX + 15, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Speed", Config.aimSpeed, 1f, 10f, panelX + 15, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Shake", Config.aimShake, 0f, 3f, panelX + 15, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Use Sens", Config.useMouseSensitivity, panelX + 15, y, mouseX, mouseY); y += 20;

        } else if (tab == 1) {
            // VISUALS
            ctx.drawText(this.textRenderer, "§7── Visuals ──", panelX + 15, y, ACCENT, false); y += 20;
            drawToggle(ctx, "Watermark", Config.watermarkEnabled, panelX + 15, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Menu Blur", Config.blurEnabled, panelX + 15, y, mouseX, mouseY); y += 20;
            // Тут будут ESP, Tracers и т.д.
        } else if (tab == 2) {
            // CONFIG
            ctx.drawText(this.textRenderer, "§7── Config ──", panelX + 15, y, ACCENT, false); y += 20;
            drawEnum(ctx, "Targets", Config.targetType.name(), panelX + 15, y, mouseX, mouseY); y += 20;
            drawBind(ctx, Config.bindGui, 3, panelX + 15, y, mouseX, mouseY); y += 20;
        }

        // Футер с ссылкой
        ctx.drawText(this.textRenderer, "https://t.me/BogEmptyy", panelX + 10, panelY + 385, 0xFF8888FF, false);

        if (activeBind != 0) {
            ctx.fill(panelX + 40, panelY + 150, panelX + 280, panelY + 200, 0xFF222222);
            ctx.drawText(this.textRenderer, "Нажми клавишу...", panelX + 80, panelY + 170, 0xFFFFAA00, true);
        }
    }

    private void drawTab(DrawContext ctx, String name, int id, int x, int y, int mx, int my) {
        boolean act = tab == id;
        boolean hov = mx >= x && mx <= x + 70 && my >= y && my <= y + 18;
        if (act) ctx.fill(x, y, x + 70, y + 18, ACCENT);
        else if (hov) ctx.fill(x, y, x + 70, y + 18, 0xFF2A2A2A);
        ctx.drawText(this.textRenderer, name, x + 10, y + 5, act ? 0xFFFFFFFF : TEXT, false);
    }

    private void drawToggle(DrawContext ctx, String text, boolean val, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 180 && my >= y && my <= y + 12;
        ctx.drawText(this.textRenderer, text, x, y, hov ? ACCENT : TEXT, false);
        ctx.fill(x + 150, y, x + 170, y + 10, val ? ACCENT : 0xFF333333);
        ctx.fill(val ? x + 162 : x + 152, y + 1, val ? x + 169 : x + 159, y + 9, 0xFFFFFFFF);
    }

    private void drawEnum(DrawContext ctx, String text, String val, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 180 && my >= y && my <= y + 12;
        ctx.drawText(this.textRenderer, text + ": §d" + val, x, y, hov ? ACCENT : TEXT, false);
    }

    private void drawSlider(DrawContext ctx, String text, float val, float min, float max, int x, int y, int mx, int my) {
        int barX = x + 100, barW = 120;
        boolean hov = mx >= barX && mx <= barX + barW && my >= y && my <= y + 12;
        float pct = (val - min) / (max - min);
        ctx.drawText(this.textRenderer, text, x, y, TEXT, false);
        ctx.fill(barX, y + 3, barX + barW, y + 7, 0xFF333333);
        ctx.fill(barX, y + 3, barX + (int)(barW * pct), y + 7, hov ? ACCENT : 0xFF6A1B9A);
        ctx.fill(barX + (int)(barW * pct) - 2, y, barX + (int)(barW * pct) + 2, y + 10, 0xFFFFFFFF);
        ctx.drawText(this.textRenderer, String.format("%.1f", val), barX + barW + 5, y, ACCENT, false);
    }

    private void drawBind(DrawContext ctx, int key, int id, int x, int y, int mx, int my) {
        String name = key == -1 ? "NONE" : InputUtil.fromKeyCode(key, 0).getLocalizedText().getString();
        ctx.drawText(this.textRenderer, "[" + name + "]", x, y, activeBind == id ? 0xFFFFAA00 : ACCENT, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx >= panelX && mx <= panelX + 320 && my >= panelY && my <= panelY + 30) {
            dragX = (int) mx - panelX; dragY = (int) my - panelY; return true;
        }
        if (mx >= panelX + 10 && mx <= panelX + 80 && my >= panelY + 35 && my <= panelY + 53) { tab = 0; return true; }
        if (mx >= panelX + 90 && mx <= panelX + 160 && my >= panelY + 35 && my <= panelY + 53) { tab = 1; return true; }
        if (mx >= panelX + 170 && mx <= panelX + 240 && my >= panelY + 35 && my <= panelY + 53) { tab = 2; return true; }

        int y = panelY + 65;
        if (tab == 0) {
            y += 20;
            if (hit(mx, my, panelX+15, y, 180, 12)) { Config.triggerEnabled = !Config.triggerEnabled; return true; }
            if (hit(mx, my, panelX+220, y, 60, 12)) { activeBind = 1; return true; } y += 20;
            if (hit(mx, my, panelX+15, y, 180, 12)) { Config.TriggerMode[] v = Config.TriggerMode.values(); Config.triggerMode = v[(Config.triggerMode.ordinal()+1)%v.length]; return true; } y += 20;
            if (hit(mx, my, panelX+115, y, 120, 12)) { activeSlider = 1; updateSlider(mx); return true; } y += 20;
            if (hit(mx, my, panelX+115, y, 120, 12)) { activeSlider = 2; updateSlider(mx); return true; } y += 25;

            y += 20;
            if (hit(mx, my, panelX+15, y, 180, 12)) { Config.aimEnabled = !Config.aimEnabled; return true; }
            if (hit(mx, my, panelX+220, y, 60, 12)) { activeBind = 2; return true; } y += 20;
            if (hit(mx, my, panelX+15, y, 180, 12)) { Config.AimMode[] v = Config.AimMode.values(); Config.aimMode = v[(Config.aimMode.ordinal()+1)%v.length]; return true; } y += 20;
            if (hit(mx, my, panelX+115, y, 120, 12)) { activeSlider = 3; updateSlider(mx); return true; } y += 20;
            if (hit(mx, my, panelX+115, y, 120, 12)) { activeSlider = 4; updateSlider(mx); return true; } y += 20;
            if (hit(mx, my, panelX+115, y, 120, 12)) { activeSlider = 5; updateSlider(mx); return true; } y += 20;
            if (hit(mx, my, panelX+15, y, 180, 12)) { Config.useMouseSensitivity = !Config.useMouseSensitivity; return true; }
        } else if (tab == 1) {
            y += 20;
            if (hit(mx, my, panelX+15, y, 180, 12)) { Config.watermarkEnabled = !Config.watermarkEnabled; return true; } y += 20;
            if (hit(mx, my, panelX+15, y, 180, 12)) { Config.blurEnabled = !Config.blurEnabled; return true; }
        } else if (tab == 2) {
            y += 20;
            if (hit(mx, my, panelX+15, y, 180, 12)) { Config.TargetType[] v = Config.TargetType.values(); Config.targetType = v[(Config.targetType.ordinal()+1)%v.length]; return true; } y += 20;
            if (hit(mx, my, panelX+15, y, 60, 12)) { activeBind = 3; return true; }
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void updateSlider(double mx) {
        float pct = (float)((mx - (panelX + 15 + 100)) / 120.0);
        pct = Math.max(0f, Math.min(1f, pct));
        switch (activeSlider) {
            case 1 -> Config.triggerRange = 2f + pct * 4f;
            case 2 -> Config.triggerCps = (int)(1 + pct * 19);
            case 3 -> Config.aimRange = 2f + pct * 6f;
            case 4 -> Config.aimSpeed = 1f + pct * 9f;
            case 5 -> Config.aimShake = pct * 3f;
        }
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragX >= 0) { panelX = (int) mx - dragX; panelY = (int) my - dragY; return true; }
        if (activeSlider != 0) { updateSlider(mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragX = -1; activeSlider = 0;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeBind != 0) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { activeBind = 0; return true; }
            switch (activeBind) {
                case 1 -> Config.bindTrigger = keyCode;
                case 2 -> Config.bindAim = keyCode;
                case 3 -> Config.bindGui = keyCode;
            }
            activeBind = 0;
            return true;
        }
        if (keyCode == Config.bindGui) { this.close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
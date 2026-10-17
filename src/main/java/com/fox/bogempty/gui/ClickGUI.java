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

    private final int BG_PANEL = 0xFF121212;
    private final int BG_HEADER = 0xFF1E1E1E;
    private final int BG_TAB_ACTIVE = 0xFF2C2C2C;
    private final int ACCENT = 0xFF00E5FF;
    private final int TEXT = 0xFFDDDDDD;
    private final int TEXT_DIM = 0xFF777777;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x90000000); 
        ctx.fill(panelX, panelY, panelX + 350, panelY + 250, BG_PANEL);
        ctx.fill(panelX, panelY, panelX + 350, panelY + 25, BG_HEADER);
        ctx.drawText(this.textRenderer, "BogEmpty", panelX + 10, panelY + 8, 0xFFFFFFFF, true);

        drawTab(ctx, "Combat", 0, panelX + 100, panelY, mouseX, mouseY);
        drawTab(ctx, "Visuals", 1, panelX + 180, panelY, mouseX, mouseY);
        drawTab(ctx, "HUD", 2, panelX + 260, panelY, mouseX, mouseY);

        int y = panelY + 35;
        int x = panelX + 15;

        if (tab == 0) {
            ctx.drawText(this.textRenderer, "TriggerBot", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.triggerEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindTrigger, 1, x, y, mouseX, mouseY); y += 20;
            drawEnum(ctx, "Mode", Config.triggerMode, x, y, mouseX, mouseY); y += 20;
            drawEnum(ctx, "Targets", Config.triggerTargets, x, y, mouseX, mouseY); y += 20;
            
            ctx.drawText(this.textRenderer, "AimAssist", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.aimEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindAim, 2, x, y, mouseX, mouseY); y += 20;
        } else if (tab == 1) {
            ctx.drawText(this.textRenderer, "ESP", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.espEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindEsp, 3, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Players", Config.espPlayers, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Mobs", Config.espMobs, x, y, mouseX, mouseY); y += 20;
        } else if (tab == 2) {
            ctx.drawText(this.textRenderer, "HUD", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Watermark", Config.watermarkEnabled, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Blur Menu", Config.blurEnabled, x, y, mouseX, mouseY); y += 20;
        }

        if (activeBind != 0) {
            ctx.fill(panelX, panelY, panelX + 350, panelY + 250, 0xC0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Нажми клавишу...", this.width / 2, this.height / 2, 0xFFFFFFFF);
        }
    }

    private void drawTab(DrawContext ctx, String name, int id, int x, int y, int mx, int my) {
        boolean act = tab == id;
        boolean hov = mx >= x && mx <= x + 70 && my >= y && my <= y + 25;
        if (act) {
            ctx.fill(x, y, x + 70, y + 25, BG_TAB_ACTIVE);
            ctx.fill(x, y + 23, x + 70, y + 25, ACCENT);
        } else if (hov) {
            ctx.fill(x, y, x + 70, y + 25, BG_HEADER);
        }
        ctx.drawText(this.textRenderer, name, x + 15, y + 8, act ? 0xFFFFFFFF : TEXT_DIM, false);
    }

    private void drawToggle(DrawContext ctx, String text, boolean val, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 300 && my >= y && my <= y + 15;
        ctx.drawText(this.textRenderer, text, x, y + 2, hov ? ACCENT : TEXT, false);
        int bx = x + 280;
        ctx.fill(bx, y, bx + 25, y + 12, val ? ACCENT : 0xFF333333);
        ctx.fill(val ? bx + 14 : bx + 2, y + 2, val ? bx + 23 : bx + 11, y + 10, 0xFFFFFFFF);
    }

    private void drawBind(DrawContext ctx, String text, int keyCode, int id, int x, int y, int mx, int my) {
        String keyName = keyCode == -1 ? "NONE" : InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
        boolean hov = mx >= x && mx <= x + 300 && my >= y && my <= y + 15;
        ctx.drawText(this.textRenderer, text + ": §b" + keyName, x, y + 2, hov ? ACCENT : TEXT, false);
    }

    private void drawEnum(DrawContext ctx, String text, String val, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 300 && my >= y && my <= y + 15;
        ctx.drawText(this.textRenderer, text + ": §b" + val, x, y + 2, hov ? ACCENT : TEXT, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (activeBind != 0) { setBind(activeBind, button); activeBind = 0; return true; }
        if (mx >= panelX && mx <= panelX + 350 && my >= panelY && my <= panelY + 25) {
            dragX = (int) mx - panelX; dragY = (int) my - panelY; return true;
        }
        
        if (my >= panelY && my <= panelY + 25) {
            if (mx >= panelX + 100 && mx <= panelX + 170) { tab = 0; return true; }
            if (mx >= panelX + 180 && mx <= panelX + 250) { tab = 1; return true; }
            if (mx >= panelX + 260 && mx <= panelX + 330) { tab = 2; return true; }
        }

        int y = panelY + 35;
        int x = panelX + 15;

        if (tab == 0) {
            y += 15;
            if (hit(mx, my, x+280, y, 25, 12)) { Config.triggerEnabled = !Config.triggerEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 300, 15)) { activeBind = 1; return true; } y += 20;
            if (hit(mx, my, x, y, 300, 15)) { Config.triggerMode = Config.triggerMode.equals("Smart") ? "OnlyCrits" : "Smart"; return true; } y += 20;
            if (hit(mx, my, x, y, 300, 15)) { 
                Config.triggerTargets = Config.triggerTargets.equals("Players") ? "Mobs" : Config.triggerTargets.equals("Mobs") ? "All" : "Players"; 
                return true; 
            } y += 20;
            
            y += 15;
            if (hit(mx, my, x+280, y, 25, 12)) { Config.aimEnabled = !Config.aimEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 300, 15)) { activeBind = 2; return true; } y += 20;
        } else if (tab == 1) {
            y += 15;
            if (hit(mx, my, x+280, y, 25, 12)) { Config.espEnabled = !Config.espEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 300, 15)) { activeBind = 3; return true; } y += 20;
            if (hit(mx, my, x+280, y, 25, 12)) { Config.espPlayers = !Config.espPlayers; return true; } y += 20;
            if (hit(mx, my, x+280, y, 25, 12)) { Config.espMobs = !Config.espMobs; return true; } y += 20;
        } else if (tab == 2) {
            y += 15;
            if (hit(mx, my, x+280, y, 25, 12)) { Config.watermarkEnabled = !Config.watermarkEnabled; return true; } y += 20;
            if (hit(mx, my, x+280, y, 25, 12)) { Config.blurEnabled = !Config.blurEnabled; return true; } y += 20;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeBind != 0) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { activeBind = 0; return true; }
            setBind(activeBind, keyCode);
            activeBind = 0;
            return true;
        }
        if (keyCode == Config.bindGui) { this.close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void setBind(int id, int code) {
        if (id == 1) Config.bindTrigger = code;
        if (id == 2) Config.bindAim = code;
        if (id == 3) Config.bindEsp = code;
    }

    private boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragX >= 0) { panelX = (int) mx - dragX; panelY = (int) my - dragY; return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragX = -1;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean shouldPause() { return false; }
}
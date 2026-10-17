package com.fox.bogempty.gui;

import com.fox.bogempty.config.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    private int panelX = 60, panelY = 60;
    private int dragX = -1, dragY = -1;
    private int category = 0; 
    private double scrollY = 0;
    private int activeSlider = 0;
    private int activeBind = 0;

    private final int ACCENT = 0xFF9D4EDD;
    private final int BG_PANEL = 0xFF161616;
    private final int BG_CAT = 0xFF1A1A1A;
    private final int TEXT = 0xFFDDDDDD;
    private final int TEXT_DIM = 0xFF888888;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x80000000); 
        ctx.fill(panelX - 2, panelY - 2, panelX + 400 + 2, panelY + 280 + 2, 0xFF000000); 
        ctx.fill(panelX, panelY, panelX + 400, panelY + 280, BG_PANEL); 
        ctx.fill(panelX, panelY, panelX + 100, panelY + 280, BG_CAT);
        ctx.fill(panelX, panelY, panelX + 400, panelY + 25, BG_CAT);

        ctx.drawText(this.textRenderer, "§lBogEmpty", panelX + 15, panelY + 8, ACCENT, true);

        drawCat(ctx, "Combat", 0, panelX + 5, panelY + 40, mouseX, mouseY);
        drawCat(ctx, "Render", 1, panelX + 5, panelY + 70, mouseX, mouseY);

        ctx.enableScissor(panelX + 105, panelY + 30, panelX + 400, panelY + 280);
        int y = panelY + 35 + (int) scrollY;
        int x = panelX + 115;

        if (category == 0) {
            ctx.drawText(this.textRenderer, "§7TriggerBot", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.triggerEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindTrigger, 1, x, y, mouseX, mouseY); y += 20;
            drawEnum(ctx, "Mode", Config.triggerMode, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Range", (float)Config.triggerRange, 2f, 6f, x, y, mouseX, mouseY); y += 20;
            
            ctx.drawText(this.textRenderer, "§7AimAssist", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.aimEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindAim, 2, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Distance", (float)Config.aimRange, 2f, 6f, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Speed", (float)Config.aimYawSpeed, 5f, 60f, x, y, mouseX, mouseY); y += 20;
        } else if (category == 1) {
            ctx.drawText(this.textRenderer, "§7ESP", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.espEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindEsp, 3, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Players", Config.espPlayers, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Mobs", Config.espMobs, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Fill", Config.espFill, x, y, mouseX, mouseY); y += 20;
        }
        ctx.disableScissor();

        if (activeBind != 0) {
            ctx.fill(panelX, panelY, panelX + 400, panelY + 280, 0xC0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§dНажми клавишу...", this.width / 2, this.height / 2, 0xFFFFFFFF);
        }
    }

    private void drawCat(DrawContext ctx, String name, int id, int x, int y, int mx, int my) {
        boolean act = category == id;
        boolean hov = mx >= x && mx <= x + 90 && my >= y && my <= y + 20;
        if (act) ctx.fill(x, y, x + 90, y + 20, 0xFF2A2A2A);
        else if (hov) ctx.fill(x, y, x + 90, y + 20, 0xFF222222);
        if (act) ctx.fill(x, y, x + 2, y + 20, ACCENT);
        ctx.drawText(this.textRenderer, name, x + 10, y + 6, act ? 0xFFFFFFFF : TEXT_DIM, false);
    }

    private void drawToggle(DrawContext ctx, String text, boolean val, int x, int y, int mx, int my) {
        ctx.drawText(this.textRenderer, text, x, y + 2, TEXT, false);
        int bx = x + 230;
        ctx.fill(bx, y, bx + 25, y + 12, val ? ACCENT : 0xFF333333);
        ctx.fill(val ? bx + 14 : bx + 2, y + 2, val ? bx + 23 : bx + 11, y + 10, 0xFFFFFFFF);
    }

    private void drawBind(DrawContext ctx, String text, int keyCode, int id, int x, int y, int mx, int my) {
        String keyName = keyCode == -1 ? "NONE" : InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
        ctx.drawText(this.textRenderer, text + ": §d" + keyName, x, y + 2, TEXT, false);
    }

    private void drawEnum(DrawContext ctx, String text, String val, int x, int y, int mx, int my) {
        ctx.drawText(this.textRenderer, text + ": §d" + val, x, y + 2, TEXT, false);
    }

    private void drawSlider(DrawContext ctx, String text, float val, float min, float max, int x, int y, int mx, int my) {
        int barX = x + 100, barW = 100;
        float pct = (val - min) / (max - min);
        ctx.drawText(this.textRenderer, text, x, y + 2, TEXT, false);
        ctx.fill(barX, y + 4, barX + barW, y + 8, 0xFF333333);
        ctx.fill(barX, y + 4, barX + (int)(barW * pct), y + 8, ACCENT);
        ctx.fill(barX + (int)(barW * pct) - 2, y + 1, barX + (int)(barW * pct) + 2, y + 11, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (activeBind != 0) { setBind(activeBind, button); activeBind = 0; return true; }

        if (mx >= panelX && mx <= panelX + 400 && my >= panelY && my <= panelY + 25) {
            dragX = (int) mx - panelX; dragY = (int) my - panelY; return true;
        }
        if (mx >= panelX + 5 && mx <= panelX + 95) {
            if (my >= panelY + 40 && my <= panelY + 60) { category = 0; return true; }
            if (my >= panelY + 70 && my <= panelY + 90) { category = 1; return true; }
        }

        int y = panelY + 35 + (int) scrollY;
        int x = panelX + 115;

        if (category == 0) {
            y += 15;
            if (hit(mx, my, x+230, y, 25, 12)) { Config.triggerEnabled = !Config.triggerEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 250, 15)) { activeBind = 1; return true; } y += 20;
            if (hit(mx, my, x, y, 250, 15)) { Config.triggerMode = Config.triggerMode.equals("Smart") ? "OnlyCrits" : "Smart"; return true; } y += 20;
            if (hit(mx, my, x+100, y, 100, 12)) { activeSlider = 1; updateSlider(mx); return true; } y += 20;
            y += 15;
            if (hit(mx, my, x+230, y, 25, 12)) { Config.aimEnabled = !Config.aimEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 250, 15)) { activeBind = 2; return true; } y += 20;
            if (hit(mx, my, x+100, y, 100, 12)) { activeSlider = 2; updateSlider(mx); return true; } y += 20;
            if (hit(mx, my, x+100, y, 100, 12)) { activeSlider = 3; updateSlider(mx); return true; } y += 20;
        } else if (category == 1) {
            y += 15;
            if (hit(mx, my, x+230, y, 25, 12)) { Config.espEnabled = !Config.espEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 250, 15)) { activeBind = 3; return true; } y += 20;
            if (hit(mx, my, x+230, y, 25, 12)) { Config.espPlayers = !Config.espPlayers; return true; } y += 20;
            if (hit(mx, my, x+230, y, 25, 12)) { Config.espMobs = !Config.espMobs; return true; } y += 20;
            if (hit(mx, my, x+230, y, 25, 12)) { Config.espFill = !Config.espFill; return true; } y += 20;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeBind != 0) { setBind(activeBind, keyCode); activeBind = 0; return true; }
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

    private void updateSlider(double mx) {
        float pct = (float)((mx - (panelX + 115 + 100)) / 100.0);
        pct = Math.max(0f, Math.min(1f, pct));
        switch (activeSlider) {
            case 1 -> Config.triggerRange = 2f + pct * 4f;
            case 2 -> Config.aimRange = 2f + pct * 4f;
            case 3 -> Config.aimYawSpeed = 5f + pct * 55f;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        scrollY += vAmount * 20;
        if (scrollY > 0) scrollY = 0;
        if (scrollY < -80) scrollY = -80;
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
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
    public boolean shouldPause() { return false; }
}
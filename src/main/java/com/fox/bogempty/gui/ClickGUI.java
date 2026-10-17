package com.fox.bogempty.gui;

import com.fox.bogempty.config.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    private int panelX = 40, panelY = 40;
    private int dragX = -1, dragY = -1;
    private int category = 0; 
    private double scrollY = 0;
    private int activeSlider = 0;
    private int activeBind = 0;

    private final int ACCENT = 0xFF9D4EDD;
    private final int BG_PANEL = 0xFF161616;
    private final int BG_CAT = 0xFF1A1A1A;
    private final int BG_CAT_ACTIVE = 0xFF2A2A2A;
    private final int TEXT = 0xFFDDDDDD;
    private final int TEXT_DIM = 0xFF888888;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x80000000); 
        ctx.fill(panelX - 2, panelY - 2, panelX + 450 + 2, panelY + 300 + 2, 0xFF000000); 
        ctx.fill(panelX, panelY, panelX + 450, panelY + 300, BG_PANEL); 
        ctx.fill(panelX, panelY, panelX + 100, panelY + 300, BG_CAT);
        ctx.fill(panelX, panelY, panelX + 450, panelY + 25, BG_CAT);

        ctx.drawText(this.textRenderer, "§lBogEmpty", panelX + 15, panelY + 8, ACCENT, true);
        ctx.drawText(this.textRenderer, "v1.0", panelX + 415, panelY + 8, TEXT_DIM, false);

        drawCat(ctx, "Combat", 0, panelX + 5, panelY + 40, mouseX, mouseY);
        drawCat(ctx, "Render", 1, panelX + 5, panelY + 70, mouseX, mouseY);

        ctx.enableScissor(panelX + 105, panelY + 30, panelX + 450, panelY + 300);
        int y = panelY + 35 + (int) scrollY;
        int x = panelX + 115;

        if (category == 0) {
            ctx.drawText(this.textRenderer, "§7TriggerBot", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.triggerEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindTrigger, 1, x, y, mouseX, mouseY); y += 20;
            
            drawToggle(ctx, "Only Crits", Config.triggerOnlyCrits, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Sprint Reset", Config.triggerSprintReset, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Range", (float)Config.triggerRange, 2f, 6f, x, y, mouseX, mouseY); y += 20;
            
            // Кого бить
            drawToggle(ctx, "Target Players", Config.targetPlayers, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Target Mobs", Config.targetMobs, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Target Animals", Config.targetAnimals, x, y, mouseX, mouseY); y += 25;

            ctx.drawText(this.textRenderer, "§7AimAssist", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.aimEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindAim, 2, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Distance", (float)Config.aimRange, 2f, 6f, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Yaw Speed", (float)Config.aimYawSpeed, 5f, 60f, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Pitch Speed", (float)Config.aimPitchSpeed, 5f, 60f, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "FOV", (float)Config.aimFov, 30f, 180f, x, y, mouseX, mouseY); y += 20;

        } else if (category == 1) {
            ctx.drawText(this.textRenderer, "§7ESP", x, y, TEXT_DIM, false); y += 15;
            drawToggle(ctx, "Enabled", Config.espEnabled, x, y, mouseX, mouseY); y += 20;
            drawBind(ctx, "Bind", Config.bindEsp, 3, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Players", Config.espPlayers, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Mobs", Config.espMobs, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Invisible", Config.espInvisible, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Fill Box", Config.espFill, x, y, mouseX, mouseY); y += 20;
            drawToggle(ctx, "Outline", Config.espOutline, x, y, mouseX, mouseY); y += 20;
            drawSlider(ctx, "Thickness", Config.espLineThickness, 0.5f, 5f, x, y, mouseX, mouseY); y += 20;
        }
        ctx.disableScissor();

        ctx.drawText(this.textRenderer, "https://t.me/BogEmptyy", panelX + 10, panelY + 285, 0xFF8888FF, false);

        if (activeBind != 0) {
            ctx.fill(panelX, panelY, panelX + 450, panelY + 300, 0xC0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§dНажми клавишу или кнопку мыши...", this.width / 2, this.height / 2, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§7Esc — отмена", this.width / 2, this.height / 2 + 15, 0xFF888888);
        }
    }

    private void drawCat(DrawContext ctx, String name, int id, int x, int y, int mx, int my) {
        boolean act = category == id;
        boolean hov = mx >= x && mx <= x + 90 && my >= y && my <= y + 20;
        if (act) ctx.fill(x, y, x + 90, y + 20, BG_CAT_ACTIVE);
        else if (hov) ctx.fill(x, y, x + 90, y + 20, 0xFF222222);
        if (act) ctx.fill(x, y, x + 2, y + 20, ACCENT);
        ctx.drawText(this.textRenderer, name, x + 10, y + 6, act ? 0xFFFFFFFF : TEXT_DIM, false);
    }

    private void drawToggle(DrawContext ctx, String text, boolean val, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 260 && my >= y && my <= y + 15;
        ctx.drawText(this.textRenderer, text, x, y + 2, hov ? ACCENT : TEXT, false);
        int bx = x + 250;
        ctx.fill(bx, y, bx + 25, y + 12, val ? ACCENT : 0xFF333333);
        ctx.fill(val ? bx + 14 : bx + 2, y + 2, val ? bx + 23 : bx + 11, y + 10, 0xFFFFFFFF);
    }

    private void drawBind(DrawContext ctx, String text, int keyCode, int id, int x, int y, int mx, int my) {
        String keyName = "NONE";
        if (keyCode != -1) {
            if (keyCode >= 0 && keyCode <= 7) {
                String[] mouse = {"LMB", "RMB", "MMB", "MB4", "MB5"};
                keyName = keyCode < mouse.length ? mouse[keyCode] : "MOUSE " + keyCode;
            } else {
                keyName = InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
            }
        }
        boolean hov = mx >= x && mx <= x + 260 && my >= y && my <= y + 15;
        ctx.drawText(this.textRenderer, text + ": §d" + keyName, x, y + 2, hov ? ACCENT : TEXT, false);
    }

    private void drawSlider(DrawContext ctx, String text, float val, float min, float max, int x, int y, int mx, int my) {
        int barX = x + 130, barW = 120;
        boolean hov = mx >= barX && mx <= barX + barW && my >= y && my <= y + 12;
        float pct = (val - min) / (max - min);
        ctx.drawText(this.textRenderer, text, x, y + 2, TEXT, false);
        ctx.fill(barX, y + 4, barX + barW, y + 8, 0xFF333333);
        ctx.fill(barX, y + 4, barX + (int)(barW * pct), y + 8, hov ? ACCENT : 0xFF6A1B9A);
        ctx.fill(barX + (int)(barW * pct) - 2, y + 1, barX + (int)(barW * pct) + 2, y + 11, 0xFFFFFFFF);
        ctx.drawText(this.textRenderer, String.format("%.1f", val), barX + barW + 5, y + 2, TEXT_DIM, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (activeBind != 0) {
            if (button == 0) { activeBind = 0; return true; }
            setBind(activeBind, button);
            activeBind = 0;
            return true;
        }

        if (mx >= panelX && mx <= panelX + 450 && my >= panelY && my <= panelY + 25) {
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
            if (hit(mx, my, x+250, y, 25, 12)) { Config.triggerEnabled = !Config.triggerEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 260, 15)) { activeBind = 1; return true; } y += 20;
            
            if (hit(mx, my, x+250, y, 25, 12)) { Config.triggerOnlyCrits = !Config.triggerOnlyCrits; return true; } y += 20;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.triggerSprintReset = !Config.triggerSprintReset; return true; } y += 20;
            if (hit(mx, my, x+130, y, 120, 12)) { activeSlider = 1; updateSlider(mx); return true; } y += 20;
            
            if (hit(mx, my, x+250, y, 25, 12)) { Config.targetPlayers = !Config.targetPlayers; return true; } y += 20;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.targetMobs = !Config.targetMobs; return true; } y += 20;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.targetAnimals = !Config.targetAnimals; return true; } y += 25;

            y += 15;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.aimEnabled = !Config.aimEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 260, 15)) { activeBind = 2; return true; } y += 20;
            if (hit(mx, my, x+130, y, 120, 12)) { activeSlider = 2; updateSlider(mx); return true; } y += 20;
            if (hit(mx, my, x+130, y, 120, 12)) { activeSlider = 3; updateSlider(mx); return true; } y += 20;
            if (hit(mx, my, x+130, y, 120, 12)) { activeSlider = 4; updateSlider(mx); return true; } y += 20;
            if (hit(mx, my, x+130, y, 120, 12)) { activeSlider = 5; updateSlider(mx); return true; } y += 20;

        } else if (category == 1) {
            y += 15;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.espEnabled = !Config.espEnabled; return true; } y += 20;
            if (hit(mx, my, x, y, 260, 15)) { activeBind = 3; return true; } y += 20;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.espPlayers = !Config.espPlayers; return true; } y += 20;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.espMobs = !Config.espMobs; return true; } y += 20;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.espInvisible = !Config.espInvisible; return true; } y += 20;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.espFill = !Config.espFill; return true; } y += 20;
            if (hit(mx, my, x+250, y, 25, 12)) { Config.espOutline = !Config.espOutline; return true; } y += 20;
            if (hit(mx, my, x+130, y, 120, 12)) { activeSlider = 6; updateSlider(mx); return true; }
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

    private void updateSlider(double mx) {
        float pct = (float)((mx - (panelX + 115 + 130)) / 120.0);
        pct = Math.max(0f, Math.min(1f, pct));
        switch (activeSlider) {
            case 1 -> Config.triggerRange = 2f + pct * 4f;
            case 2 -> Config.aimRange = 2f + pct * 4f;
            case 3 -> Config.aimYawSpeed = 5f + pct * 55f;
            case 4 -> Config.aimPitchSpeed = 5f + pct * 55f;
            case 5 -> Config.aimFov = 30f + pct * 150f;
            case 6 -> Config.espLineThickness = 0.5f + pct * 4.5f;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        scrollY += vAmount * 20;
        if (scrollY > 0) scrollY = 0;
        if (scrollY < -100) scrollY = -100;
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
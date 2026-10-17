package com.fox.bogempty.gui;

import com.fox.bogempty.BogEmpty;
import com.fox.bogempty.config.Config;
import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import com.fox.bogempty.utils.Animation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    private int panelX, panelY;
    private int dragX = -1, dragY = -1;
    private Category currentCategory = Category.COMBAT;
    private double scrollY = 0;
    
    private Setting activeSlider = null;
    private Module activeBind = null;

    // Анимация появления
    private final Animation animation = new Animation(2.0f);
    private boolean closing = false;

    private final int ACCENT = 0xFF9D4EDD;
    private final int TEXT = 0xFFF0F0F0;
    private final int TEXT_DIM = 0xFF888888;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
        this.panelX = Config.guiX;
        this.panelY = Config.guiY;
        animation.setTarget(1.0f);
    }

    @Override
    public void close() {
        closing = true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float anim = animation.getValue();
        
        if (closing && anim <= 0.01f) {
            super.close();
            return;
        }

        // Затемнение (пропорционально анимации)
        int alpha = (int) (anim * 180);
        ctx.fill(0, 0, this.width, this.height, (alpha << 24));

        // Масштабирование (эффект зума при открытии)
        ctx.getMatrices().push();
        float scale = 0.9f + (anim * 0.1f);
        ctx.getMatrices().translate(panelX + 240, panelY + 175, 0);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.getMatrices().translate(-(panelX + 240), -(panelY + 175), 0);

        // Тень
        ctx.fill(panelX - 4, panelY - 4, panelX + 480 + 4, panelY + 350 + 4, 0x40000000);
        
        // Панель
        ctx.fillGradient(panelX, panelY, panelX + 480, panelY + 350, 0xFF181818, 0xFF0D0D0D);
        ctx.fill(panelX, panelY, panelX + 480, panelY + 35, 0xFF202020);
        ctx.fill(panelX, panelY + 35, panelX + 480, panelY + 36, ACCENT);
        
        ctx.drawText(this.textRenderer, "§lBogEmpty", panelX + 15, panelY + 12, ACCENT, true);
        
        // FPS Счетчик в меню
        ctx.drawText(this.textRenderer, "§8FPS: §f" + mc.getCurrentFps(), panelX + 420, panelY + 12, TEXT_DIM, false);

        // Категории
        ctx.fill(panelX, panelY + 36, panelX + 120, panelY + 350, 0xFF141414);
        int catY = panelY + 50;
        for (Category cat : Category.values()) {
            drawCategory(ctx, cat, panelX + 10, catY, mouseX, mouseY);
            catY += 30;
        }

        // Модули
        ctx.enableScissor(panelX + 120, panelY + 36, panelX + 480, panelY + 350);
        int y = panelY + 50 + (int) scrollY;
        int x = panelX + 135;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            drawModule(ctx, m, x, y, mouseX, mouseY);
            y += 28;

            if (m.isEnabled()) {
                int settingsHeight = (m.getSettings().size() * 22) + 10;
                ctx.fill(x, y - 4, x + 320, y - 4 + settingsHeight, 0xFF1A1A1A);
                ctx.fill(x, y - 4, x + 2, y - 4 + settingsHeight, ACCENT);

                for (Setting s : m.getSettings()) {
                    drawSetting(ctx, s, x + 15, y, mouseX, mouseY);
                    y += 22;
                }
                y += 8;
            }
        }
        ctx.disableScissor();

        ctx.getMatrices().pop();

        if (activeBind != null) {
            ctx.fill(panelX, panelY, panelX + 480, panelY + 350, 0xE0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§dНажми клавишу для §f" + activeBind.getName(), this.width / 2, this.height / 2, 0xFFFFFFFF);
        }
    }

    private void drawCategory(DrawContext ctx, Category cat, int x, int y, int mx, int my) {
        boolean act = currentCategory == cat;
        boolean hov = mx >= x && mx <= x + 110 && my >= y && my <= y + 24;
        
        if (act) {
            ctx.fillGradient(x, y, x + 110, y + 24, 0xFF2A2A2A, 0xFF1E1E1E);
            ctx.fill(x, y, x + 3, y + 24, ACCENT);
        } else if (hov) {
            ctx.fill(x, y, x + 110, y + 24, 0x10FFFFFF);
        }
        ctx.drawText(this.textRenderer, cat.name, x + 15, y + 8, act ? ACCENT : TEXT_DIM, false);
    }

    private void drawModule(DrawContext ctx, Module m, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 320 && my >= y && my <= y + 22;
        
        if (m.isEnabled() || hov) {
            ctx.fill(x - 5, y - 2, x + 325, y + 22, 0xFF252525);
        }

        int pillX = x + 290;
        ctx.fill(pillX, y + 4, pillX + 24, y + 16, m.isEnabled() ? ACCENT : 0xFF333333);
        ctx.fill(m.isEnabled() ? pillX + 13 : pillX + 2, y + 5, m.isEnabled() ? pillX + 22 : pillX + 11, y + 15, 0xFFFFFFFF);

        ctx.drawText(this.textRenderer, m.getName(), x + 5, y + 6, m.isEnabled() ? 0xFFFFFFFF : TEXT_DIM, false);
        
        String keyName = m.getKey() == -1 ? "..." : InputUtil.fromKeyCode(m.getKey(), 0).getLocalizedText().getString();
        ctx.drawText(this.textRenderer, "§8[§d" + keyName + "§8]", x + 235, y + 6, TEXT_DIM, false);
    }

    private void drawSetting(DrawContext ctx, Setting s, int x, int y, int mx, int my) {
        if (s.getType() == Setting.Type.BOOLEAN) {
            int pillX = x + 270;
            ctx.fill(pillX, y + 5, pillX + 18, y + 13, s.getBool() ? ACCENT : 0xFF333333);
            ctx.fill(s.getBool() ? pillX + 10 : pillX + 2, y + 6, s.getBool() ? pillX + 16 : pillX + 8, y + 12, 0xFFFFFFFF);
            ctx.drawText(this.textRenderer, s.getName(), x + 5, y + 5, TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.ENUM) {
            ctx.drawText(this.textRenderer, s.getName() + ": §d" + s.getEnum(), x + 5, y + 5, TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.SLIDER) {
            int barX = x + 140, barW = 120;
            float pct = (s.getFloat() - s.getMin()) / (s.getMax() - s.getMin());
            
            ctx.drawText(this.textRenderer, s.getName(), x + 5, y + 5, TEXT_DIM, false);
            ctx.fill(barX, y + 8, barX + barW, y + 11, 0xFF2A2A2A);
            ctx.fill(barX, y + 8, barX + (int)(barW * pct), y + 11, ACCENT);
            ctx.fill(barX + (int)(barW * pct) - 2, y + 5, barX + (int)(barW * pct) + 2, y + 14, 0xFFFFFFFF);
            ctx.drawText(this.textRenderer, String.format("%.1f", s.getFloat()), barX + barW + 8, y + 5, TEXT_DIM, false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (closing) return false;

        if (activeBind != null) {
            activeBind.setKey(button);
            activeBind = null;
            return true;
        }

        if (mx >= panelX && mx <= panelX + 480 && my >= panelY && my <= panelY + 35) {
            dragX = (int) mx - panelX; dragY = (int) my - panelY; return true;
        }

        int catY = panelY + 50;
        for (Category cat : Category.values()) {
            if (mx >= panelX + 10 && mx <= panelX + 120 && my >= catY && my <= catY + 24) {
                currentCategory = cat; scrollY = 0; return true;
            }
            catY += 30;
        }

        int y = panelY + 50 + (int) scrollY;
        int x = panelX + 135;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            if (mx >= x && mx <= x + 320 && my >= y && my <= y + 22) {
                if (mx >= x + 235 && mx <= x + 290) activeBind = m;
                else m.toggle();
                return true;
            }
            y += 28;

            if (m.isEnabled()) {
                for (Setting s : m.getSettings()) {
                    if (s.getType() == Setting.Type.BOOLEAN) {
                        if (mx >= x + 15 && mx <= x + 320 && my >= y && my <= y + 22) { s.setBool(!s.getBool()); return true; }
                    } 
                    else if (s.getType() == Setting.Type.ENUM) {
                        if (mx >= x + 15 && mx <= x + 320 && my >= y && my <= y + 22) { s.cycleEnum(); return true; }
                    } 
                    else if (s.getType() == Setting.Type.SLIDER) {
                        if (mx >= x + 15 + 140 && mx <= x + 15 + 260 && my >= y && my <= y + 22) {
                            activeSlider = s; updateSlider(s, mx, x + 15); return true;
                        }
                    }
                    y += 22;
                }
                y += 8;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (closing) return false;
        
        if (activeBind != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { activeBind = null; return true; }
            activeBind.setKey(keyCode);
            activeBind = null;
            return true;
        }
        if (keyCode == BogEmpty.bindGui) { this.close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateSlider(Setting s, double mx, int xOffset) {
        float pct = (float)((mx - (xOffset + 140)) / 120.0);
        pct = Math.max(0f, Math.min(1f, pct));
        s.setFloat(s.getMin() + pct * (s.getMax() - s.getMin()));
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (closing) return false;
        if (dragX >= 0) { 
            panelX = (int) mx - dragX; panelY = (int) my - dragY; 
            Config.guiX = panelX; Config.guiY = panelY; Config.save();
            return true; 
        }
        if (activeSlider != null) {
            updateSlider(activeSlider, mx, panelX + 135 + 15);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragX = -1; activeSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        scrollY += vAmount * 25;
        if (scrollY > 0) scrollY = 0;
        if (scrollY < -300) scrollY = -300;
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    @Override
    public boolean shouldPause() { return false; }
}
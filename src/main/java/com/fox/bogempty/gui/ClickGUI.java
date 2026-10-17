package com.fox.bogempty.gui;

import com.fox.bogempty.BogEmpty;
import com.fox.bogempty.config.Config;
import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
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

    // Палитра (Premium Dark)
    private final int BG_PANEL = 0xFF0A0A0A;
    private final int BG_SIDEBAR = 0xFF111111;
    private final int BG_HEADER = 0xFF151515;
    private final int BG_ACTIVE = 0xFF1E1E1E;
    private final int ACCENT = 0xFF9D4EDD;
    private final int ACCENT_LIGHT = 0xFFC77DFF;
    private final int TEXT = 0xFFEEEEEE;
    private final int TEXT_DIM = 0xFF888888;
    private final int TOGGLE_OFF = 0xFF2A2A2A;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
        this.panelX = Config.guiX;
        this.panelY = Config.guiY;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // 1. Затемнение фона
        ctx.fill(0, 0, this.width, this.height, 0xB0000000);

        // 2. Тень (многослойная)
        ctx.fill(panelX - 5, panelY - 5, panelX + 450 + 5, panelY + 320 + 5, 0x15000000);
        ctx.fill(panelX - 3, panelY - 3, panelX + 450 + 3, panelY + 320 + 3, 0x30000000);
        
        // 3. Градиентный фон панели
        ctx.fillGradient(panelX, panelY, panelX + 450, panelY + 320, BG_PANEL, 0xFF050505);
        
        // 4. Шапка
        ctx.fillGradient(panelX, panelY, panelX + 450, panelY + 25, BG_HEADER, 0xFF0A0A0A);
        ctx.fill(panelX, panelY + 25, panelX + 450, panelY + 26, ACCENT); 

        ctx.drawText(this.textRenderer, "§lBogEmpty", panelX + 10, panelY + 8, ACCENT, true);
        ctx.drawText(this.textRenderer, "§8v1.0", panelX + 420, panelY + 8, TEXT_DIM, false);

        // 5. Сайдбар
        ctx.fill(panelX, panelY + 26, panelX + 100, panelY + 320, BG_SIDEBAR);
        int catY = panelY + 36;
        for (Category cat : Category.values()) {
            drawCategory(ctx, cat, panelX + 5, catY, mouseX, mouseY);
            catY += 25;
        }

        // 6. Модули
        ctx.enableScissor(panelX + 100, panelY + 26, panelX + 450, panelY + 320);
        int y = panelY + 36 + (int) scrollY;
        int x = panelX + 110;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            drawModule(ctx, m, x, y, mouseX, mouseY);
            y += 24; // Чуть больше отступ

            if (m.isEnabled()) {
                // Рисуем фон под настройками
                int settingsHeight = (m.getSettings().size() * 18) + 5;
                ctx.fill(x, y - 4, x + 330, y - 4 + settingsHeight, 0x15FFFFFF);
                
                for (Setting s : m.getSettings()) {
                    drawSetting(ctx, s, x + 10, y, mouseX, mouseY);
                    y += 18;
                }
                y += 5;
            }
        }
        ctx.disableScissor();

        // Окно бинда
        if (activeBind != null) {
            ctx.fill(panelX, panelY, panelX + 450, panelY + 320, 0xE0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§dНажми клавишу для §f" + activeBind.getName(), this.width / 2, this.height / 2, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§7Esc — отмена", this.width / 2, this.height / 2 + 15, TEXT_DIM);
        }
    }

    private void drawCategory(DrawContext ctx, Category cat, int x, int y, int mx, int my) {
        boolean act = currentCategory == cat;
        boolean hov = mx >= x && mx <= x + 90 && my >= y && my <= y + 20;
        
        if (act) {
            ctx.fill(x, y, x + 90, y + 20, BG_ACTIVE);
            ctx.fill(x, y, x + 2, y + 20, ACCENT); // Полоска
        } else if (hov) {
            ctx.fill(x, y, x + 90, y + 20, 0x10FFFFFF); // Легкая подсветка
        }
        ctx.drawText(this.textRenderer, cat.name, x + 12, y + 6, act ? 0xFFFFFFFF : TEXT_DIM, false);
    }

    private void drawModule(DrawContext ctx, Module m, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 330 && my >= y && my <= y + 18;
        
        // Подсветка модуля при наведении
        if (hov) ctx.fill(x - 2, y - 2, x + 332, y + 18, 0x10FFFFFF);

        // Pill-переключатель
        int pillX = x + 300;
        int pillW = 24, pillH = 12;
        int pillColor = m.isEnabled() ? ACCENT : TOGGLE_OFF;
        ctx.fill(pillX, y + 2, pillX + pillW, y + 2 + pillH, pillColor);
        
        // Кружок
        int circleX = m.isEnabled() ? pillX + pillW - 11 : pillX + 1;
        ctx.fill(circleX, y + 3, circleX + 10, y + 13, 0xFFFFFFFF);

        // Имя
        int textColor = m.isEnabled() ? ACCENT_LIGHT : (hov ? 0xFFFFFF : TEXT);
        ctx.drawText(this.textRenderer, m.getName(), x + 5, y + 4, textColor, false);
        
        // Бинд
        String keyName = m.getKey() == -1 ? "NONE" : InputUtil.fromKeyCode(m.getKey(), 0).getLocalizedText().getString();
        ctx.drawText(this.textRenderer, "§8[§d" + keyName + "§8]", x + 240, y + 4, TEXT_DIM, false);
    }

    private void drawSetting(DrawContext ctx, Setting s, int x, int y, int mx, int my) {
        if (s.getType() == Setting.Type.BOOLEAN) {
            boolean hov = mx >= x && mx <= x + 300 && my >= y && my <= y + 15;
            
            // Мини-переключатель
            int pillX = x + 260;
            int pillColor = s.getBool() ? ACCENT : TOGGLE_OFF;
            ctx.fill(pillX, y + 3, pillX + 16, y + 11, pillColor);
            int circleX = s.getBool() ? pillX + 9 : pillX + 1;
            ctx.fill(circleX, y + 3, circleX + 7, y + 11, 0xFFFFFFFF);

            ctx.drawText(this.textRenderer, s.getName(), x, y + 3, hov ? 0xFFFFFF : TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.ENUM) {
            boolean hov = mx >= x && mx <= x + 300 && my >= y && my <= y + 15;
            ctx.drawText(this.textRenderer, s.getName() + ": §d" + s.getEnum(), x, y + 3, hov ? 0xFFFFFF : TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.SLIDER) {
            int barX = x + 130, barW = 130;
            float pct = (s.getFloat() - s.getMin()) / (s.getMax() - s.getMin());
            boolean hov = mx >= barX && mx <= barX + barW && my >= y && my <= y + 15;
            
            ctx.drawText(this.textRenderer, s.getName(), x, y + 3, TEXT_DIM, false);
            
            // Трек
            ctx.fill(barX, y + 5, barX + barW, y + 9, 0xFF1A1A1A);
            // Заполнение
            ctx.fill(barX, y + 5, barX + (int)(barW * pct), y + 9, hov ? ACCENT_LIGHT : ACCENT);
            // Кружок
            ctx.fill(barX + (int)(barW * pct) - 2, y + 2, barX + (int)(barW * pct) + 2, y + 12, 0xFFFFFFFF);
            
            // Значение
            ctx.drawText(this.textRenderer, String.format("%.1f", s.getFloat()), barX + barW + 10, y + 3, TEXT_DIM, false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (activeBind != null) {
            activeBind.setKey(button);
            activeBind = null;
            return true;
        }

        if (mx >= panelX && mx <= panelX + 450 && my >= panelY && my <= panelY + 25) {
            dragX = (int) mx - panelX; dragY = (int) my - panelY; return true;
        }

        int catY = panelY + 36;
        for (Category cat : Category.values()) {
            if (mx >= panelX + 5 && mx <= panelX + 95 && my >= catY && my <= catY + 20) {
                currentCategory = cat;
                scrollY = 0;
                return true;
            }
            catY += 25;
        }

        int y = panelY + 36 + (int) scrollY;
        int x = panelX + 110;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            if (mx >= x && mx <= x + 330 && my >= y && my <= y + 18) {
                if (mx >= x + 240 && mx <= x + 290) {
                    activeBind = m;
                } else {
                    m.toggle();
                }
                return true;
            }
            y += 24;

            if (m.isEnabled()) {
                for (Setting s : m.getSettings()) {
                    if (s.getType() == Setting.Type.BOOLEAN) {
                        if (mx >= x + 10 && mx <= x + 300 && my >= y && my <= y + 15) {
                            s.setBool(!s.getBool());
                            return true;
                        }
                    } 
                    else if (s.getType() == Setting.Type.ENUM) {
                        if (mx >= x + 10 && mx <= x + 300 && my >= y && my <= y + 15) {
                            s.cycleEnum();
                            return true;
                        }
                    } 
                    else if (s.getType() == Setting.Type.SLIDER) {
                        if (mx >= x + 10 + 130 && mx <= x + 10 + 260 && my >= y && my <= y + 15) {
                            activeSlider = s;
                            updateSlider(s, mx, x + 10);
                            return true;
                        }
                    }
                    y += 18;
                }
                y += 5;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
        float pct = (float)((mx - (xOffset + 130)) / 130.0);
        pct = Math.max(0f, Math.min(1f, pct));
        s.setFloat(s.getMin() + pct * (s.getMax() - s.getMin()));
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragX >= 0) { 
            panelX = (int) mx - dragX; panelY = (int) my - dragY; 
            Config.guiX = panelX;
            Config.guiY = panelY;
            Config.save();
            return true; 
        }
        if (activeSlider != null) {
            updateSlider(activeSlider, mx, panelX + 110 + 10);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragX = -1;
        activeSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        scrollY += vAmount * 20;
        if (scrollY > 0) scrollY = 0;
        if (scrollY < -200) scrollY = -200;
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    @Override
    public boolean shouldPause() { return false; }
}
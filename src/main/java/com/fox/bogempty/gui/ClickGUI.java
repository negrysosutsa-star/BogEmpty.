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
    private final int BG_PANEL_TOP = 0xFF1A1A1A;
    private final int BG_PANEL_BOT = 0xFF0F0F0F;
    private final int BG_SIDEBAR = 0xFF141414;
    private final int BG_MODULE_HOVER = 0xFF252525;
    private final int ACCENT = 0xFF9D4EDD; // Фиолетовый
    private final int ACCENT_GLOW = 0xFFC77DFF;
    private final int TEXT = 0xFFEDEDED;
    private final int TEXT_DIM = 0xFF888888;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
        this.panelX = Config.guiX;
        this.panelY = Config.guiY;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // 1. Сильное затемнение фона
        ctx.fill(0, 0, this.width, this.height, 0xB0000000);

        // 2. Тень (Drop Shadow) - создаем эффект парения
        drawShadow(ctx, panelX - 5, panelY - 5, panelX + 460 + 5, panelY + 320 + 5);

        // 3. Основа (Градиент сверху-вниз)
        ctx.fillGradient(panelX, panelY, panelX + 460, panelY + 320, BG_PANEL_TOP, BG_PANEL_BOT);

        // 4. Верхняя панель (Шапка)
        ctx.fill(panelX, panelY, panelX + 460, panelY + 30, 0xFF222222);
        ctx.fill(panelX, panelY + 30, panelX + 460, panelY + 31, ACCENT); // Акцентная линия

        // Лого и версия
        ctx.drawText(this.textRenderer, "§lBogEmpty", panelX + 15, panelY + 11, ACCENT_GLOW, true);
        ctx.drawText(this.textRenderer, "§8v1.0", panelX + 420, panelY + 11, TEXT_DIM, false);

        // 5. Левая панель (Категории)
        ctx.fill(panelX, panelY + 31, panelX + 110, panelY + 320, BG_SIDEBAR);
        int catY = panelY + 45;
        for (Category cat : Category.values()) {
            drawCategory(ctx, cat, panelX + 5, catY, mouseX, mouseY);
            catY += 28;
        }

        // 6. Правая панель (Модули и Настройки)
        ctx.enableScissor(panelX + 110, panelY + 31, panelX + 460, panelY + 320);
        int y = panelY + 45 + (int) scrollY;
        int x = panelX + 120;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            drawModule(ctx, m, x, y, mouseX, mouseY);
            y += 26;

            if (m.isEnabled()) {
                // Рисуем фон под настройки (чуть темнее)
                int settingsHeight = (m.getSettings().size() * 20) + 10;
                ctx.fill(x, y - 4, x + 330, y - 4 + settingsHeight, 0xFF151515);
                ctx.fill(x, y - 4, x + 2, y - 4 + settingsHeight, ACCENT); // Полоска слева

                for (Setting s : m.getSettings()) {
                    drawSetting(ctx, s, x + 15, y, mouseX, mouseY);
                    y += 20;
                }
                y += 8;
            }
        }
        ctx.disableScissor();

        // Окно бинда
        if (activeBind != null) {
            ctx.fill(panelX, panelY, panelX + 460, panelY + 320, 0xE0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§dНажми клавишу для §f" + activeBind.getName(), this.width / 2, this.height / 2, 0xFFFFFFFF);
        }
    }

    // Красивая тень
    private void drawShadow(DrawContext ctx, int x1, int y1, int x2, int y2) {
        ctx.fill(x1, y1, x2, y2, 0x15000000);
        ctx.fill(x1 + 2, y1 + 2, x2 - 2, y2 - 2, 0x30000000);
        ctx.fill(x1 + 4, y1 + 4, x2 - 4, y2 - 4, 0x50000000);
    }

    private void drawCategory(DrawContext ctx, Category cat, int x, int y, int mx, int my) {
        boolean act = currentCategory == cat;
        boolean hov = mx >= x && mx <= x + 100 && my >= y && my <= y + 22;
        
        if (act) {
            ctx.fillGradient(x, y, x + 100, y + 22, 0xFF2A2A2A, 0xFF1E1E1E);
            ctx.fill(x, y, x + 3, y + 22, ACCENT); // Яркая полоска
        } else if (hov) {
            ctx.fill(x, y, x + 100, y + 22, 0x15FFFFFF);
        }
        
        ctx.drawText(this.textRenderer, cat.name, x + 15, y + 7, act ? ACCENT_GLOW : TEXT_DIM, false);
    }

    private void drawModule(DrawContext ctx, Module m, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 330 && my >= y && my <= y + 20;
        
        if (m.isEnabled() || hov) {
            ctx.fill(x - 4, y - 2, x + 334, y + 20, BG_MODULE_HOVER);
        }

        // Pill-тоггл
        int pillX = x + 305;
        int pillW = 24, pillH = 12;
        int pillColor = m.isEnabled() ? ACCENT : 0xFF333333;
        ctx.fill(pillX, y + 2, pillX + pillW, y + 2 + pillH, pillColor);
        
        // Кружок внутри
        int circleX = m.isEnabled() ? pillX + pillW - 11 : pillX + 1;
        ctx.fill(circleX, y + 3, circleX + 10, y + 13, 0xFFFFFFFF);

        // Имя модуля
        ctx.drawText(this.textRenderer, m.getName(), x + 5, y + 4, m.isEnabled() ? 0xFFFFFFFF : TEXT_DIM, false);
        
        // Бинд
        String keyName = m.getKey() == -1 ? "..." : InputUtil.fromKeyCode(m.getKey(), 0).getLocalizedText().getString();
        ctx.drawText(this.textRenderer, "§8[§d" + keyName + "§8]", x + 245, y + 4, TEXT_DIM, false);
    }

    private void drawSetting(DrawContext ctx, Setting s, int x, int y, int mx, int my) {
        if (s.getType() == Setting.Type.BOOLEAN) {
            boolean hov = mx >= x && mx <= x + 300 && my >= y && my <= y + 18;
            
            int pillX = x + 285;
            int pillColor = s.getBool() ? ACCENT : 0xFF333333;
            ctx.fill(pillX, y + 4, pillX + 16, y + 12, pillColor);
            int circleX = s.getBool() ? pillX + 9 : pillX + 1;
            ctx.fill(circleX, y + 4, circleX + 7, y + 12, 0xFFFFFFFF);

            ctx.drawText(this.textRenderer, s.getName(), x + 5, y + 4, hov ? 0xFFFFFF : TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.ENUM) {
            boolean hov = mx >= x && mx <= x + 300 && my >= y && my <= y + 18;
            ctx.drawText(this.textRenderer, s.getName() + ": §d" + s.getEnum(), x + 5, y + 4, hov ? 0xFFFFFF : TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.SLIDER) {
            int barX = x + 140, barW = 120;
            float pct = (s.getFloat() - s.getMin()) / (s.getMax() - s.getMin());
            boolean hov = mx >= barX && mx <= barX + barW && my >= y && my <= y + 18;
            
            ctx.drawText(this.textRenderer, s.getName(), x + 5, y + 4, TEXT_DIM, false);
            
            // Фон слайдера
            ctx.fill(barX, y + 7, barX + barW, y + 10, 0xFF2A2A2A);
            // Заполнение
            ctx.fill(barX, y + 7, barX + (int)(barW * pct), y + 10, hov ? ACCENT_GLOW : ACCENT);
            // Кружок
            ctx.fill(barX + (int)(barW * pct) - 2, y + 4, barX + (int)(barW * pct) + 2, y + 13, 0xFFFFFFFF);
            
            ctx.drawText(this.textRenderer, String.format("%.1f", s.getFloat()), barX + barW + 10, y + 4, TEXT_DIM, false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (activeBind != null) {
            activeBind.setKey(button);
            activeBind = null;
            return true;
        }

        if (mx >= panelX && mx <= panelX + 460 && my >= panelY && my <= panelY + 30) {
            dragX = (int) mx - panelX; dragY = (int) my - panelY; return true;
        }

        int catY = panelY + 45;
        for (Category cat : Category.values()) {
            if (mx >= panelX + 5 && mx <= panelX + 105 && my >= catY && my <= catY + 22) {
                currentCategory = cat;
                scrollY = 0;
                return true;
            }
            catY += 28;
        }

        int y = panelY + 45 + (int) scrollY;
        int x = panelX + 120;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            if (mx >= x && mx <= x + 330 && my >= y && my <= y + 20) {
                if (mx >= x + 245 && mx <= x + 300) {
                    activeBind = m;
                } else {
                    m.toggle();
                }
                return true;
            }
            y += 26;

            if (m.isEnabled()) {
                for (Setting s : m.getSettings()) {
                    if (s.getType() == Setting.Type.BOOLEAN) {
                        if (mx >= x + 15 && mx <= x + 300 && my >= y && my <= y + 18) {
                            s.setBool(!s.getBool());
                            return true;
                        }
                    } 
                    else if (s.getType() == Setting.Type.ENUM) {
                        if (mx >= x + 15 && mx <= x + 300 && my >= y && my <= y + 18) {
                            s.cycleEnum();
                            return true;
                        }
                    } 
                    else if (s.getType() == Setting.Type.SLIDER) {
                        if (mx >= x + 15 + 140 && mx <= x + 15 + 260 && my >= y && my <= y + 18) {
                            activeSlider = s;
                            updateSlider(s, mx, x + 15);
                            return true;
                        }
                    }
                    y += 20;
                }
                y += 8;
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
        float pct = (float)((mx - (xOffset + 140)) / 120.0);
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
            updateSlider(activeSlider, mx, panelX + 120 + 15);
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
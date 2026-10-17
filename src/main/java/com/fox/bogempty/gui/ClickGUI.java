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

    // === PREMIUM DARK PALETTE ===
    private final int BG_PANEL_TOP = 0xFF141414;
    private final int BG_PANEL_BOTTOM = 0xFF0A0A0A;
    private final int BG_SIDEBAR = 0xFF111111;
    private final int BG_MODULE = 0xFF1E1E1E;
    private final int ACCENT = 0xFF9D4EDD;
    private final int ACCENT_GLOW = 0xFFC77DFF;
    private final int TEXT = 0xFFEDEDED;
    private final int TEXT_DIM = 0xFF888888;
    private final int TOGGLE_OFF = 0xFF333333;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
        this.panelX = Config.guiX;
        this.panelY = Config.guiY;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // 1. Фон (сильное затемнение для контраста)
        ctx.fill(0, 0, this.width, this.height, 0xC0000000);

        // 2. Многослойная тень (создает эффект 3D)
        ctx.fill(panelX - 6, panelY - 6, panelX + 460 + 6, panelY + 320 + 6, 0x10000000);
        ctx.fill(panelX - 4, panelY - 4, panelX + 460 + 4, panelY + 320 + 4, 0x20000000);
        ctx.fill(panelX - 2, panelY - 2, panelX + 460 + 2, panelY + 320 + 2, 0x40000000);

        // 3. Градиентная панель (сверху светлее, снизу темнее)
        ctx.fillGradient(panelX, panelY, panelX + 460, panelY + 320, BG_PANEL_TOP, BG_PANEL_BOTTOM);

        // 4. Шапка
        ctx.fill(panelX, panelY, panelX + 460, panelY + 28, 0xFF1A1A1A);
        ctx.fill(panelX, panelY + 28, panelX + 460, panelY + 29, ACCENT);
        
        // Логотип с "тенью"
        ctx.drawText(this.textRenderer, "§lBogEmpty", panelX + 12, panelY + 10, ACCENT_GLOW, true);
        ctx.drawText(this.textRenderer, "§8v1.0", panelX + 425, panelY + 10, TEXT_DIM, false);

        // 5. Сайдбар (Категории)
        ctx.fill(panelX, panelY + 29, panelX + 110, panelY + 320, BG_SIDEBAR);
        int catY = panelY + 42;
        for (Category cat : Category.values()) {
            drawCategory(ctx, cat, panelX + 5, catY, mouseX, mouseY);
            catY += 28;
        }

        // 6. Контент (Модули)
        ctx.enableScissor(panelX + 110, panelY + 29, panelX + 460, panelY + 320);
        int y = panelY + 42 + (int) scrollY;
        int x = panelX + 120;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            drawModule(ctx, m, x, y, mouseX, mouseY);
            y += 26;

            if (m.isEnabled()) {
                // Фон под настройками (чуть темнее модуля)
                int settingsHeight = (m.getSettings().size() * 20) + 6;
                ctx.fill(x, y - 4, x + 330, y - 4 + settingsHeight, 0xFF151515);
                // Акцентная полоска слева от настроек
                ctx.fill(x, y - 4, x + 2, y - 4 + settingsHeight, ACCENT);

                for (Setting s : m.getSettings()) {
                    drawSetting(ctx, s, x + 15, y, mouseX, mouseY);
                    y += 20;
                }
                y += 6;
            }
        }
        ctx.disableScissor();

        // Окно бинда
        if (activeBind != null) {
            ctx.fill(panelX, panelY, panelX + 460, panelY + 320, 0xE0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§dНажми клавишу для §f" + activeBind.getName(), this.width / 2, this.height / 2, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§7Esc — отмена", this.width / 2, this.height / 2 + 15, TEXT_DIM);
        }
    }

    private void drawCategory(DrawContext ctx, Category cat, int x, int y, int mx, int my) {
        boolean act = currentCategory == cat;
        boolean hov = mx >= x && mx <= x + 100 && my >= y && my <= y + 22;
        
        if (act) {
            // Красивый градиент для активной категории
            ctx.fillGradient(x, y, x + 100, y + 22, 0xFF2A2A2A, 0xFF1E1E1E);
            ctx.fill(x, y, x + 3, y + 22, ACCENT); // Полоска
        } else if (hov) {
            ctx.fill(x, y, x + 100, y + 22, 0x15FFFFFF);
        }
        
        ctx.drawText(this.textRenderer, cat.name, x + 15, y + 7, act ? ACCENT_GLOW : TEXT_DIM, false);
    }

    private void drawModule(DrawContext ctx, Module m, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 330 && my >= y && my <= y + 20;
        
        // Фон модуля
        if (m.isEnabled() || hov) {
            ctx.fill(x - 4, y - 2, x + 334, y + 20, BG_MODULE);
        }

        // Тоггл (Pill)
        int pillX = x + 305;
        int pillW = 24, pillH = 12;
        int pillColor = m.isEnabled() ? ACCENT : TOGGLE_OFF;
        ctx.fill(pillX, y + 2, pillX + pillW, y + 2 + pillH, pillColor);
        
        // Белая точка внутри
        int circleX = m.isEnabled() ? pillX + pillW - 11 : pillX + 1;
        ctx.fill(circleX, y + 3, circleX + 10, y + 13, 0xFFFFFFFF);

        // Текст
        ctx.drawText(this.textRenderer, m.getName(), x + 5, y + 4, m.isEnabled() ? 0xFFFFFFFF : TEXT_DIM, false);
        
        // Бинд
        String keyName = m.getKey() == -1 ? "..." : InputUtil.fromKeyCode(m.getKey(), 0).getLocalizedText().getString();
        ctx.drawText(this.textRenderer, "§8[§d" + keyName + "§8]", x + 245, y + 4, TEXT_DIM, false);
    }

    private void drawSetting(DrawContext ctx, Setting s, int x, int y, int mx, int my) {
        if (s.getType() == Setting.Type.BOOLEAN) {
            boolean hov = mx >= x && mx <= x + 300 && my >= y && my <= y + 18;
            
            // Маленький тоггл
            int pillX = x + 285;
            int pillColor = s.getBool() ? ACCENT : TOGGLE_OFF;
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
            
            // Красивый слайдер
            ctx.fill(barX, y + 7, barX + barW, y + 10, 0xFF2A2A2A); // Фон
            ctx.fill(barX, y + 7, barX + (int)(barW * pct), y + 10, hov ? ACCENT_GLOW : ACCENT); // Заполнение
            ctx.fill(barX + (int)(barW * pct) - 2, y + 4, barX + (int)(barW * pct) + 2, y + 13, 0xFFFFFFFF); // Кружок
            
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

        if (mx >= panelX && mx <= panelX + 460 && my >= panelY && my <= panelY + 28) {
            dragX = (int) mx - panelX; dragY = (int) my - panelY; return true;
        }

        int catY = panelY + 42;
        for (Category cat : Category.values()) {
            if (mx >= panelX + 5 && mx <= panelX + 105 && my >= catY && my <= catY + 22) {
                currentCategory = cat;
                scrollY = 0;
                return true;
            }
            catY += 28;
        }

        int y = panelY + 42 + (int) scrollY;
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
                y += 6;
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
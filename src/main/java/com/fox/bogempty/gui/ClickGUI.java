package com.fox.bogempty.gui;

import com.fox.bogempty.BogEmpty;
import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    private int panelX = 50, panelY = 50;
    private int dragX = -1, dragY = -1;
    private Category currentCategory = Category.COMBAT;
    private double scrollY = 0;
    
    // Настройка, которую сейчас двигаем/биндим
    private Setting activeSlider = null;
    private Module activeBind = null;

    private final int BG_PANEL = 0xFF121212;
    private final int BG_HEADER = 0xFF1E1E1E;
    private final int BG_CAT = 0xFF181818;
    private final int BG_ACTIVE = 0xFF2C2C2C;
    private final int ACCENT = 0xFF00E5FF;
    private final int TEXT = 0xFFDDDDDD;
    private final int TEXT_DIM = 0xFF777777;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x90000000); 
        ctx.fill(panelX, panelY, panelX + 400, panelY + 260, BG_PANEL);
        ctx.fill(panelX, panelY, panelX + 400, panelY + 25, BG_HEADER);
        ctx.drawText(this.textRenderer, "BogEmpty", panelX + 10, panelY + 8, 0xFFFFFFFF, true);

        // Левая панель с категориями
        ctx.fill(panelX, panelY + 25, panelX + 100, panelY + 260, BG_CAT);
        int catY = panelY + 35;
        for (Category cat : Category.values()) {
            drawCategory(ctx, cat, panelX + 5, catY, mouseX, mouseY);
            catY += 25;
        }

        // Правая панель с модулями
        ctx.enableScissor(panelX + 100, panelY + 25, panelX + 400, panelY + 260);
        int y = panelY + 35 + (int) scrollY;
        int x = panelX + 110;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            // Рисуем модуль
            drawModule(ctx, m, x, y, mouseX, mouseY);
            y += 20;

            // Если модуль включен, рисуем его настройки
            if (m.isEnabled()) {
                for (Setting s : m.getSettings()) {
                    drawSetting(ctx, m, s, x + 10, y, mouseX, mouseY);
                    y += 18;
                }
                y += 5; // Отступ между модулями
            }
        }
        ctx.disableScissor();

        // Окно бинда
        if (activeBind != null) {
            ctx.fill(panelX, panelY, panelX + 400, panelY + 260, 0xC0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Нажми клавишу для " + activeBind.getName(), this.width / 2, this.height / 2, 0xFFFFFFFF);
        }
    }

    private void drawCategory(DrawContext ctx, Category cat, int x, int y, int mx, int my) {
        boolean act = currentCategory == cat;
        boolean hov = mx >= x && mx <= x + 90 && my >= y && my <= y + 20;
        
        if (act) ctx.fill(x, y, x + 90, y + 20, BG_ACTIVE);
        else if (hov) ctx.fill(x, y, x + 90, y + 20, 0xFF222222);
        if (act) ctx.fill(x, y, x + 2, y + 20, ACCENT); // Полоска слева

        ctx.drawText(this.textRenderer, cat.name, x + 10, y + 6, act ? 0xFFFFFFFF : TEXT_DIM, false);
    }

    private void drawModule(DrawContext ctx, Module m, int x, int y, int mx, int my) {
        boolean hov = mx >= x && mx <= x + 280 && my >= y && my <= y + 15;
        
        // Квадратик (Checkbox)
        ctx.fill(x, y + 2, x + 10, y + 12, m.isEnabled() ? ACCENT : 0xFF333333);
        
        // Имя модуля
        ctx.drawText(this.textRenderer, m.getName(), x + 15, y + 3, m.isEnabled() ? ACCENT : (hov ? 0xFFFFFF : TEXT), false);
        
        // Кнопка бинда справа
        String keyName = m.getKey() == -1 ? "BIND" : InputUtil.fromKeyCode(m.getKey(), 0).getLocalizedText().getString();
        ctx.drawText(this.textRenderer, "[" + keyName + "]", x + 240, y + 3, TEXT_DIM, false);
    }

    private void drawSetting(DrawContext ctx, Module m, Setting s, int x, int y, int mx, int my) {
        if (s.getType() == Setting.Type.BOOLEAN) {
            ctx.fill(x, y + 2, x + 8, y + 10, s.getBool() ? ACCENT : 0xFF333333);
            ctx.drawText(this.textRenderer, s.getName(), x + 12, y + 2, TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.ENUM) {
            ctx.drawText(this.textRenderer, s.getName() + ": §b" + s.getEnum(), x, y + 2, TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.SLIDER) {
            int barX = x + 100, barW = 120;
            float pct = (s.getFloat() - s.getMin()) / (s.getMax() - s.getMin());
            
            ctx.drawText(this.textRenderer, s.getName(), x, y + 2, TEXT_DIM, false);
            ctx.fill(barX, y + 4, barX + barW, y + 8, 0xFF2A2A2A);
            ctx.fill(barX, y + 4, barX + (int)(barW * pct), y + 8, ACCENT);
            ctx.fill(barX + (int)(barW * pct) - 2, y + 1, barX + (int)(barW * pct) + 2, y + 11, 0xFFFFFFFF);
            ctx.drawText(this.textRenderer, String.format("%.1f", s.getFloat()), barX + barW + 5, y + 2, TEXT_DIM, false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Бинд
        if (activeBind != null) {
            activeBind.setKey(button);
            activeBind = null;
            return true;
        }

        // Драг окна
        if (mx >= panelX && mx <= panelX + 400 && my >= panelY && my <= panelY + 25) {
            dragX = (int) mx - panelX; dragY = (int) my - panelY; return true;
        }

        // Клик по категориям
        int catY = panelY + 35;
        for (Category cat : Category.values()) {
            if (mx >= panelX + 5 && mx <= panelX + 95 && my >= catY && my <= catY + 20) {
                currentCategory = cat;
                scrollY = 0;
                return true;
            }
            catY += 25;
        }

        // Клик по модулям и настройкам
        int y = panelY + 35 + (int) scrollY;
        int x = panelX + 110;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            // Клик по модулю (включение)
            if (mx >= x && mx <= x + 230 && my >= y && my <= y + 15) {
                m.toggle();
                return true;
            }
            // Клик по бинду модуля
            if (mx >= x + 240 && mx <= x + 280 && my >= y && my <= y + 15) {
                activeBind = m;
                return true;
            }
            y += 20;

            // Клик по настройкам
            if (m.isEnabled()) {
                for (Setting s : m.getSettings()) {
                    if (s.getType() == Setting.Type.BOOLEAN) {
                        if (mx >= x + 10 && mx <= x + 200 && my >= y && my <= y + 15) {
                            s.setBool(!s.getBool());
                            return true;
                        }
                    } 
                    else if (s.getType() == Setting.Type.ENUM) {
                        if (mx >= x + 10 && mx <= x + 200 && my >= y && my <= y + 15) {
                            s.cycleEnum();
                            return true;
                        }
                    } 
                    else if (s.getType() == Setting.Type.SLIDER) {
                        if (mx >= x + 10 + 100 && mx <= x + 10 + 220 && my >= y && my <= y + 15) {
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
        float pct = (float)((mx - (xOffset + 100)) / 120.0);
        pct = Math.max(0f, Math.min(1f, pct));
        s.setFloat(s.getMin() + pct * (s.getMax() - s.getMin()));
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragX >= 0) { panelX = (int) mx - dragX; panelY = (int) my - dragY; return true; }
        
        if (activeSlider != null) {
            // Находим xOffset (зависит от того, где сейчас меню)
            int x = panelX + 110 + 10;
            updateSlider(activeSlider, mx, x);
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
        if (scrollY < -150) scrollY = -150; // Ограничение, чтобы не улетало
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    @Override
    public boolean shouldPause() { return false; }
}
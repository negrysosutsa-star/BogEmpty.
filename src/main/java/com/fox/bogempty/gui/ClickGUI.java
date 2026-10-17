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

import java.util.HashMap;
import java.util.Map;

public class ClickGUI extends Screen {
    private int panelX = 50, panelY = 50;
    private int dragX = -1, dragY = -1;
    private Category currentCategory = Category.COMBAT;
    private double scrollY = 0;
    
    private Setting activeSlider = null;
    private Module activeBind = null;
    
    // Анимация открытия
    private float animation = 0f;
    private long lastFrame = System.currentTimeMillis();

    private final int BG_PANEL = 0xFF0E0E0E;
    private final int BG_HEADER = 0xFF1A1A1A;
    private final int BG_CAT = 0xFF151515;
    private final int BG_ACTIVE = 0xFF2A2A2A;
    private final int ACCENT = 0xFF9D4EDD; // Фиолетовый (можно поменять на голубой 0xFF00E5FF)
    private final int TEXT = 0xFFCCCCCC;
    private final int TEXT_DIM = 0xFF777777;

    public ClickGUI() {
        super(Text.literal("BogEmpty"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Плавная анимация
        long now = System.currentTimeMillis();
        float dt = (now - lastFrame) / 1000f;
        lastFrame = now;
        if (animation < 1.0f) animation += dt * 5.0f;
        if (animation > 1.0f) animation = 1.0f;

        int alpha = (int) (255 * animation);
        int bgPanel = (alpha << 24) | (BG_PANEL & 0xFFFFFF);
        int bgHeader = (alpha << 24) | (BG_HEADER & 0xFFFFFF);
        int bgCat = (alpha << 24) | (BG_CAT & 0xFFFFFF);

        // Тень (смещение на 3 пикселя)
        ctx.fill(panelX - 3, panelY - 3, panelX + 450 + 3, panelY + 300 + 3, (int)(alpha * 0.4) << 24);
        
        // Основная панель
        ctx.fill(panelX, panelY, panelX + 450, panelY + 300, bgPanel);
        ctx.fill(panelX, panelY, panelX + 450, panelY + 25, bgHeader);
        
        // Линия под заголовком (акцент)
        ctx.fill(panelX, panelY + 25, panelX + 450, panelY + 26, ACCENT);

        ctx.drawText(this.textRenderer, "BogEmpty", panelX + 10, panelY + 8, ACCENT, true);
        ctx.drawText(this.textRenderer, "v1.0", panelX + 420, panelY + 8, TEXT_DIM, false);

        // Левая панель (Категории)
        ctx.fill(panelX, panelY + 26, panelX + 100, panelY + 300, bgCat);
        int catY = panelY + 36;
        for (Category cat : Category.values()) {
            drawCategory(ctx, cat, panelX + 5, catY, mouseX, mouseY, alpha);
            catY += 25;
        }

        // Правая панель (Модули)
        ctx.enableScissor(panelX + 100, panelY + 26, panelX + 450, panelY + 300);
        int y = panelY + 36 + (int) scrollY;
        int x = panelX + 110;

        for (Module m : BogEmpty.modules) {
            if (m.getCategory() != currentCategory) continue;

            drawModule(ctx, m, x, y, mouseX, mouseY, alpha);
            y += 20;

            if (m.isEnabled()) {
                for (Setting s : m.getSettings()) {
                    drawSetting(ctx, s, x + 10, y, mouseX, mouseY, alpha);
                    y += 18;
                }
                y += 5;
            }
        }
        ctx.disableScissor();

        // Окно бинда
        if (activeBind != null) {
            ctx.fill(panelX, panelY, panelX + 450, panelY + 300, 0xD0000000);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Нажми клавишу для §b" + activeBind.getName(), this.width / 2, this.height / 2, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(this.textRenderer, "§7Esc — отмена", this.width / 2, this.height / 2 + 15, TEXT_DIM);
        }
    }

    private void drawCategory(DrawContext ctx, Category cat, int x, int y, int mx, int my, int alpha) {
        boolean act = currentCategory == cat;
        boolean hov = mx >= x && mx <= x + 90 && my >= y && my <= y + 20;
        
        if (act) {
            ctx.fill(x, y, x + 90, y + 20, (alpha << 24) | (BG_ACTIVE & 0xFFFFFF));
            ctx.fill(x, y, x + 2, y + 20, ACCENT); // Акцентная полоска слева
        } else if (hov) {
            ctx.fill(x, y, x + 90, y + 20, 0x20FFFFFF); // Легкая подсветка
        }

        ctx.drawText(this.textRenderer, cat.name, x + 10, y + 6, act ? 0xFFFFFFFF : TEXT_DIM, false);
    }

    private void drawModule(DrawContext ctx, Module m, int x, int y, int mx, int my, int alpha) {
        boolean hov = mx >= x && mx <= x + 320 && my >= y && my <= y + 15;
        
        // Квадрат (Toggle)
        int boxColor = m.isEnabled() ? ACCENT : ((alpha << 24) | 0x333333);
        ctx.fill(x, y + 2, x + 10, y + 12, boxColor);
        
        // Имя
        int textColor = m.isEnabled() ? ACCENT : (hov ? 0xFFFFFF : TEXT);
        ctx.drawText(this.textRenderer, m.getName(), x + 15, y + 3, textColor, false);
        
        // Бинд
        String keyName = m.getKey() == -1 ? "BIND" : InputUtil.fromKeyCode(m.getKey(), 0).getLocalizedText().getString();
        ctx.drawText(this.textRenderer, "[" + keyName + "]", x + 280, y + 3, TEXT_DIM, false);
    }

    private void drawSetting(DrawContext ctx, Setting s, int x, int y, int mx, int my, int alpha) {
        if (s.getType() == Setting.Type.BOOLEAN) {
            boolean hov = mx >= x && mx <= x + 250 && my >= y && my <= y + 15;
            ctx.fill(x, y + 2, x + 8, y + 10, s.getBool() ? ACCENT : 0xFF333333);
            ctx.drawText(this.textRenderer, s.getName(), x + 12, y + 2, hov ? 0xFFFFFF : TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.ENUM) {
            boolean hov = mx >= x && mx <= x + 250 && my >= y && my <= y + 15;
            ctx.drawText(this.textRenderer, s.getName() + ": §b" + s.getEnum(), x, y + 2, hov ? 0xFFFFFF : TEXT_DIM, false);
        } 
        else if (s.getType() == Setting.Type.SLIDER) {
            int barX = x + 130, barW = 120;
            float pct = (s.getFloat() - s.getMin()) / (s.getMax() - s.getMin());
            boolean hov = mx >= barX && mx <= barX + barW && my >= y && my <= y + 15;
            
            ctx.drawText(this.textRenderer, s.getName(), x, y + 2, TEXT_DIM, false);
            
            // Фон слайдера
            ctx.fill(barX, y + 4, barX + barW, y + 8, 0xFF2A2A2A);
            
            // Заполнение
            int fillColor = hov ? 0xFFD18CFF : ACCENT;
            ctx.fill(barX, y + 4, barX + (int)(barW * pct), y + 8, fillColor);
            
            // Кружок
            ctx.fill(barX + (int)(barW * pct) - 2, y + 1, barX + (int)(barW * pct) + 2, y + 11, 0xFFFFFFFF);
            
            // Значение
            ctx.drawText(this.textRenderer, String.format("%.1f", s.getFloat()), barX + barW + 5, y + 2, TEXT_DIM, false);
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

            // Включение
            if (mx >= x && mx <= x + 280 && my >= y && my <= y + 15) {
                m.toggle();
                return true;
            }
            // Бинд
            if (mx >= x + 280 && mx <= x + 320 && my >= y && my <= y + 15) {
                activeBind = m;
                return true;
            }
            y += 20;

            if (m.isEnabled()) {
                for (Setting s : m.getSettings()) {
                    if (s.getType() == Setting.Type.BOOLEAN) {
                        if (mx >= x + 10 && mx <= x + 250 && my >= y && my <= y + 15) {
                            s.setBool(!s.getBool());
                            return true;
                        }
                    } 
                    else if (s.getType() == Setting.Type.ENUM) {
                        if (mx >= x + 10 && mx <= x + 250 && my >= y && my <= y + 15) {
                            s.cycleEnum();
                            return true;
                        }
                    } 
                    else if (s.getType() == Setting.Type.SLIDER) {
                        if (mx >= x + 10 + 130 && mx <= x + 10 + 250 && my >= y && my <= y + 15) {
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
        float pct = (float)((mx - (xOffset + 130)) / 120.0);
        pct = Math.max(0f, Math.min(1f, pct));
        s.setFloat(s.getMin() + pct * (s.getMax() - s.getMin()));
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragX >= 0) { panelX = (int) mx - dragX; panelY = (int) my - dragY; return true; }
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
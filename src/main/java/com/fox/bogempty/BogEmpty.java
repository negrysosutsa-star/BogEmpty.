package com.fox.bogempty;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.gui.ClickGUI;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.module.combat.AimAssist;
import com.fox.bogempty.module.combat.TriggerBot;
import com.fox.bogempty.module.misc.AutoTotem;
import com.fox.bogempty.module.player.AutoSwap;
import com.fox.bogempty.module.player.FastPlace;
import com.fox.bogempty.module.player.InventoryMove;
import com.fox.bogempty.module.render.ESPModule;
import com.fox.bogempty.module.render.Fullbright;
import com.fox.bogempty.module.render.Nametags;
import com.fox.bogempty.module.render.NoHurtCam;
import com.fox.bogempty.module.render.NoRender;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BogEmpty implements ClientModInitializer {
    public static final List<Module> modules = new ArrayList<>();
    
    private static boolean guiKeyWasDown = false;
    private static boolean swapKeyWasDown = false;
    private static boolean trigKeyWasDown = false;
    private static boolean aimKeyWasDown = false;

    @Override
    public void onInitializeClient() {
        Config.load();
        
        modules.add(new TriggerBot());
        modules.add(new AimAssist());
        modules.add(new ESPModule());
        modules.add(new Nametags());
        modules.add(new Fullbright());
        modules.add(new NoHurtCam());
        modules.add(new NoRender());
        modules.add(new FastPlace());
        modules.add(new InventoryMove());
        modules.add(new AutoTotem());
        modules.add(new AutoSwap());

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            long handle = mc.getWindow().getHandle();
            
            // 1. Открытие GUI
            boolean guiDown = isKeyDown(handle, Config.bindGui);
            if (guiDown && !guiKeyWasDown) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGUI());
                else if (mc.currentScreen instanceof ClickGUI) mc.setScreen(null);
            }
            guiKeyWasDown = guiDown;

            // 2. AutoSwap
            boolean swapDown = isKeyDown(handle, Config.bindSwap);
            if (swapDown && !swapKeyWasDown && mc.currentScreen == null) {
                AutoSwap swap = getModule(AutoSwap.class);
                if (swap != null && swap.isEnabled()) swap.onTrigger();
            }
            swapKeyWasDown = swapDown;

            // 3. TriggerBot Toggle
            if (Config.bindTrigger != -1) {
                boolean trigDown = isKeyDown(handle, Config.bindTrigger);
                if (trigDown && !trigKeyWasDown && mc.currentScreen == null) {
                    TriggerBot tb = getModule(TriggerBot.class);
                    if (tb != null) {
                        tb.setEnabled(!tb.isEnabled());
                        Config.showNotification("TriggerBot " + (tb.isEnabled() ? "ON" : "OFF"));
                    }
                }
                trigKeyWasDown = trigDown;
            }

            // 4. AimAssist Toggle
            if (Config.bindAim != -1) {
                boolean aimDown = isKeyDown(handle, Config.bindAim);
                if (aimDown && !aimKeyWasDown && mc.currentScreen == null) {
                    AimAssist aa = getModule(AimAssist.class);
                    if (aa != null) {
                        aa.setEnabled(!aa.isEnabled());
                        Config.showNotification("AimAssist " + (aa.isEnabled() ? "ON" : "OFF"));
                    }
                }
                aimKeyWasDown = aimDown;
            }

            for (Module m : modules) {
                if (m.isEnabled() && !(m instanceof AimAssist)) m.onTick();
            }
        });

        WorldRenderEvents.START.register(ctx -> {
            for (Module m : modules) {
                if (m.isEnabled() && m instanceof AimAssist aim) aim.onRender();
            }
        });

        WorldRenderEvents.LAST.register(ctx -> {
            for (Module m : modules) {
                if (!m.isEnabled()) continue;
                if (m instanceof ESPModule esp) esp.onRender(ctx.matrixStack(), ctx.camera(), ctx.tickCounter().getTickDelta(true));
                else if (m instanceof Nametags nt) nt.onRender(ctx.matrixStack(), ctx.camera(), ctx.tickCounter().getTickDelta(true));
            }
        });

        // HUD Уведомления и FPS
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.currentScreen instanceof ClickGUI) return;

            // FPS
            ctx.drawText(mc.textRenderer, "§fFPS: §a" + mc.getCurrentFps(), 5, 5, 0xFFFFFFFF, true);
            
            // Вотермарка
            if (Config.watermarkEnabled) {
                ctx.drawText(mc.textRenderer, "§lBogEmpty", 5, 20, 0xFF9D4EDD, true);
            }

            // Уведомление
            if (!Config.notification.isEmpty()) {
                long elapsed = System.currentTimeMillis() - Config.notificationTime;
                if (elapsed > 2000) {
                    Config.notification = "";
                } else {
                    int alpha = (int) (255 * (1f - (elapsed / 2000f)));
                    int color = (alpha << 24) | 0x9D4EDD;
                    int centerX = mc.getWindow().getScaledWidth() / 2;
                    ctx.drawCenteredTextWithShadow(mc.textRenderer, "§l" + Config.notification, centerX, 30, color);
                }
            }
        });
    }

    private static boolean isKeyDown(long handle, int key) {
        if (key >= 0 && key <= 7) return GLFW.glfwGetMouseButton(handle, key) == GLFW.GLFW_PRESS;
        return key != -1 && GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
    }

    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (clazz.isInstance(m)) return clazz.cast(m);
        }
        return null;
    }
}
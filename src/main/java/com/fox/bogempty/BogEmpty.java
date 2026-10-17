package com.fox.bogempty;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.gui.ClickGUI;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.module.combat.AimAssist;
import com.fox.bogempty.module.combat.TriggerBot;
import com.fox.bogempty.module.misc.AutoTotem;
import com.fox.bogempty.module.player.FastPlace;
import com.fox.bogempty.module.player.InventoryMove;
import com.fox.bogempty.module.render.ESPModule;
import com.fox.bogempty.module.render.Fullbright;
import com.fox.bogempty.module.render.NoHurtCam;
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
    public static int bindGui = 344;

    @Override
    public void onInitializeClient() {
        Config.load();
        
        modules.add(new TriggerBot());
        modules.add(new AimAssist());
        modules.add(new ESPModule());
        modules.add(new Fullbright());
        modules.add(new NoHurtCam());
        modules.add(new FastPlace());
        modules.add(new InventoryMove());
        modules.add(new AutoTotem());

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            long handle = mc.getWindow().getHandle();
            boolean down = GLFW.glfwGetKey(handle, bindGui) == GLFW.GLFW_PRESS;
            
            if (down && !guiKeyWasDown) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGUI());
                else if (mc.currentScreen instanceof ClickGUI) mc.setScreen(null);
            }
            guiKeyWasDown = down;

            for (Module m : modules) {
                if (m.isEnabled()) m.onTick();
            }
        });

        WorldRenderEvents.LAST.register(ctx -> {
            for (Module m : modules) {
                if (m instanceof ESPModule esp && esp.isEnabled()) {
                    // В 3D рендере tickDelta тоже нужно доставать правильно, если понадобится
                }
            }
        });

        // ИСПРАВЛЕНИЕ ЗДЕСЬ
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.currentScreen instanceof ClickGUI) return;

            float tickDelta = tickCounter.getTickDelta(true); // Достаем float из RenderTickCounter

            for (Module m : modules) {
                if (m instanceof ESPModule esp && esp.isEnabled()) {
                    esp.onRender2D(ctx, tickDelta);
                }
            }
        });
    }
}
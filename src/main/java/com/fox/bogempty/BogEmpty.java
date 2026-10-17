package com.fox.bogempty;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.gui.ClickGUI;
import com.fox.bogempty.module.combat.AimAssist;
import com.fox.bogempty.module.combat.TriggerBot;
import com.fox.bogempty.module.render.ESPModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class BogEmpty implements ClientModInitializer {
    public static TriggerBot triggerBot;
    public static AimAssist aimAssist;
    public static ESPModule espModule;

    private static boolean guiKeyWasDown = false;
    private static boolean trigKeyWasDown = false;
    private static boolean aimKeyWasDown = false;
    private static boolean espKeyWasDown = false;

    @Override
    public void onInitializeClient() {
        Config.load();
        triggerBot = new TriggerBot();
        aimAssist = new AimAssist();
        espModule = new ESPModule();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            mc.options.hudHidden = mc.currentScreen instanceof ClickGUI;
            
            long handle = mc.getWindow().getHandle();

            // Обработка биндов
            if (Config.bindGui != -1) {
                boolean down = GLFW.glfwGetKey(handle, Config.bindGui) == GLFW.GLFW_PRESS;
                if (down && !guiKeyWasDown) {
                    if (mc.currentScreen == null) mc.setScreen(new ClickGUI());
                    else if (mc.currentScreen instanceof ClickGUI) mc.setScreen(null);
                }
                guiKeyWasDown = down;
            }

            if (mc.currentScreen == null) {
                if (Config.bindTrigger != -1) {
                    boolean down = isKeyDown(handle, Config.bindTrigger);
                    if (down && !trigKeyWasDown) Config.triggerEnabled = !Config.triggerEnabled;
                    trigKeyWasDown = down;
                }
                if (Config.bindAim != -1) {
                    boolean down = isKeyDown(handle, Config.bindAim);
                    if (down && !aimKeyWasDown) Config.aimEnabled = !Config.aimEnabled;
                    aimKeyWasDown = down;
                }
                if (Config.bindEsp != -1) {
                    boolean down = isKeyDown(handle, Config.bindEsp);
                    if (down && !espKeyWasDown) Config.espEnabled = !Config.espEnabled;
                    espKeyWasDown = down;
                }
            }

            triggerBot.onTick(mc);
        });

        WorldRenderEvents.START.register(ctx -> aimAssist.onFrame(MinecraftClient.getInstance()));
        WorldRenderEvents.LAST.register(ctx -> espModule.onRender(ctx.matrixStack(), ctx.camera(), ctx.tickCounter().getTickDelta(true)));

        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || !Config.watermarkEnabled || mc.currentScreen instanceof ClickGUI) return;
            String text = "BogEmpty";
            int width = mc.textRenderer.getWidth(text);
            ctx.fill(5, 5, 15 + width, 20, 0xFF161616);
            ctx.fill(5, 5, 7, 20, 0xFF9D4EDD);
            ctx.drawText(mc.textRenderer, text, 10, 9, 0xFFC77DFF, true);
        });
    }

    // Проверяем, нажата ли клавиша ИЛИ кнопка мыши
    private static boolean isKeyDown(long handle, int keyCode) {
        if (keyCode >= 0 && keyCode <= 7) { // Кнопки мыши (0=ЛКМ, 1=ПКМ, 2=СКМ)
            return GLFW.glfwGetMouseButton(handle, keyCode) == GLFW.GLFW_PRESS;
        }
        return GLFW.glfwGetKey(handle, keyCode) == GLFW.GLFW_PRESS;
    }
}
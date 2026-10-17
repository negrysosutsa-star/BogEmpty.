package com.fox.bogempty.utils;

import net.minecraft.client.MinecraftClient;

public class FpsUtil {
    public static int getFps() {
        return MinecraftClient.getInstance().getCurrentFps();
    }
}
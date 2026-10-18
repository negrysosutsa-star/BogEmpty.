package com.fox.bogempty.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    // Бинды по умолчанию
    public static int bindGui = 344;      // RShift
    public static int bindSwap = 71;      // G
    public static int bindTrigger = -1;   // Не задан
    public static int bindAim = -1;       // Не задан

    // Позиция меню
    public static int guiX = 50;
    public static int guiY = 50;

    // Настройки HUD
    public static boolean watermarkEnabled = true;
    public static boolean blurEnabled = true;

    // Временная переменная для уведомления
    public static transient String notification = "";
    public static transient long notificationTime = 0;

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("bogempty.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Config load() {
        if (Files.exists(FILE)) {
            try {
                Config c = GSON.fromJson(Files.readString(FILE), Config.class);
                if (c != null) return c;
            } catch (IOException ignored) {}
        }
        return new Config();
    }

    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(new Config())); } catch (IOException ignored) {}
    }

    public static void showNotification(String text) {
        notification = text;
        notificationTime = System.currentTimeMillis();
    }
}
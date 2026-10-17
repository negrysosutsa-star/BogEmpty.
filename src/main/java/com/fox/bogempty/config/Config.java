package com.fox.bogempty.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static int bindGui = 344;
    
    // Позиция меню
    public static int guiX = 50;
    public static int guiY = 50;

    // Настройки для HUD / Блюра
    public static boolean blurEnabled = true;
    public static boolean watermarkEnabled = true;

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
}
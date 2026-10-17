package com.fox.bogempty.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    // --- ГЛАВНОЕ ---
    public static int bindGui = 344; // Right Shift

    // --- TRIGGER BOT ---
    public static boolean triggerEnabled = false;
    public static int bindTrigger = -1;
    public static double triggerRange = 3.0;
    
    // Режимы и цели
    public static String triggerMode = "Smart"; // "Smart" или "OnlyCrits"
    public static String triggerTargets = "Players"; // "Players", "Mobs", "All"
    
    // Доп. настройки (как в Luminar)
    public static boolean trigThroughEntities = false;
    public static boolean trigCustomCooldown = false;
    public static double trigCustomCooldownValue = 80.0; // Процент кулдауна
    public static boolean trigTpsSync = false;
    public static boolean trigNoSweaps = false;
    public static boolean triggerSprintReset = true;

    // Цели (для TargetUtil и ESP)
    public static boolean targetPlayers = true;
    public static boolean targetMobs = false;
    public static boolean targetAnimals = false;

    // --- AIM ASSIST ---
    public static boolean aimEnabled = false;
    public static int bindAim = -1;
    public static double aimRange = 3.2;
    public static double aimYawSpeed = 31.0;
    public static double aimPitchSpeed = 15.0;
    public static double aimFov = 51.0;

    // --- ESP (Визуал) ---
    public static boolean espEnabled = false;
    public static int bindEsp = -1;
    
    // Кого показывать
    public static boolean espPlayers = true;
    public static boolean espMobs = false;
    public static boolean espInvisible = false;
    
    // Вид и фильтры
    public static boolean espFill = true;
    public static boolean espOutline = true;
    public static float espLineThickness = 2.0f;
    public static boolean espScaleByDistance = true;

    // Цвета (ARGB)
    public static int espLineColor = 0xFF9D4EDD;  // Фиолетовый контур
    public static int espFillColor = 0x409D4EDD;  // Полупрозрачная заливка

    // --- HUD / ВИЗУАЛ ---
    public static boolean watermarkEnabled = true;
    public static boolean blurEnabled = true;

    // --- СИСТЕМА СОХРАНЕНИЯ ---
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
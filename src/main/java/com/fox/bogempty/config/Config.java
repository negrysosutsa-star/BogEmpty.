package com.fox.bogempty.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static int bindGui = 344;

    // === TRIGGER BOT ===
    public static boolean triggerEnabled = false;
    public static int bindTrigger = -1;
    public static double triggerRange = 3.0;
    public static String triggerMode = "Smart"; // Smart, OnlyCrits
    public static boolean triggerOnlyCrits = true;
    public static boolean triggerSmartCrits = true;
    public static boolean triggerSprintReset = true;
    public static boolean trigNoSweaps = false;
    public static boolean trigCustomCooldown = false;
    
    // Цели для триггера
    public static boolean targetPlayers = true;
    public static boolean targetMobs = false;
    public static boolean targetAnimals = false;

    // === AIM ASSIST ===
    public static boolean aimEnabled = false;
    public static int bindAim = -1;
    public static double aimRange = 3.2;
    public static double aimYawSpeed = 31.0;
    public static double aimPitchSpeed = 15.0;
    public static double aimFov = 51.0;

    // === ESP ===
    public static boolean espEnabled = false;
    public static int bindEsp = -1;
    public static boolean espPlayers = true;
    public static boolean espMobs = false;
    public static boolean espInvisible = false;
    public static boolean espFill = true;
    public static boolean espOutline = true;
    public static float espLineThickness = 2.0f;
    public static boolean espScaleByDistance = true;

    public static int espLineColor = 0xFF9D4EDD;
    public static int espFillColor = 0x409D4EDD;

    // === HUD ===
    public static boolean watermarkEnabled = true;
    public static boolean blurEnabled = true;

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
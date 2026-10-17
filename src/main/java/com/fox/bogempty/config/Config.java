package com.fox.bogempty.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static TargetType targetType = TargetType.PLAYERS;
    public static String theme = "Purple";
    public static int bindGui = 344;

    public static boolean triggerEnabled = false;
    public static TriggerMode triggerMode = TriggerMode.SMART_CRITS;
    public static double triggerRange = 3.0;
    public static int triggerCps = 12;
    public static int bindTrigger = -1;

    public static boolean aimEnabled = false;
    public static AimMode aimMode = AimMode.DEFAULT;
    public static double aimRange = 4.5;
    public static float aimSpeed = 3.0f;
    public static float aimShake = 0.5f;
    public static boolean useMouseSensitivity = true;
    public static int bindAim = -1;

    public static boolean watermarkEnabled = true;
    public static boolean blurEnabled = true;
    public static boolean espEnabled = false;

    public enum TargetType { PLAYERS, MOBS, ALL }
    public enum TriggerMode { SMART_CRITS, ONLY_CRITS, NORMAL }
    public enum AimMode { DEFAULT, ASSISTANT, SMOOTH }

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
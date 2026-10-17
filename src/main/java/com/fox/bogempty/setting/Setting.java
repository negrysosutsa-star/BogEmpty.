package com.fox.bogempty.setting;

public class Setting {
    public enum Type { BOOLEAN, SLIDER, ENUM }

    private final String name;
    private final Type type;
    
    // Значения
    private float floatValue;
    private boolean boolValue;
    private String enumValue;
    private final String[] options; // Для Enum

    // Лимиты для слайдера
    private final float min, max;

    // Конструктор для Boolean
    public Setting(String name, boolean defaultValue) {
        this.name = name;
        this.type = Type.BOOLEAN;
        this.boolValue = defaultValue;
        this.options = null;
        this.min = 0; this.max = 1;
    }

    // Конструктор для Slider
    public Setting(String name, float defaultValue, float min, float max) {
        this.name = name;
        this.type = Type.SLIDER;
        this.floatValue = defaultValue;
        this.min = min;
        this.max = max;
        this.options = null;
    }

    // Конструктор для Enum (режимы)
    public Setting(String name, String defaultValue, String[] options) {
        this.name = name;
        this.type = Type.ENUM;
        this.enumValue = defaultValue;
        this.options = options;
        this.min = 0; this.max = 1;
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public String[] getOptions() { return options; }
    public float getMin() { return min; }
    public float getMax() { return max; }

    // Геттеры/Сеттеры
    public boolean getBool() { return boolValue; }
    public void setBool(boolean val) { this.boolValue = val; }

    public float getFloat() { return floatValue; }
    public void setFloat(float val) { this.floatValue = Math.max(min, Math.min(max, val)); }

    public String getEnum() { return enumValue; }
    public void setEnum(String val) { this.enumValue = val; }
    
    // Переключение Enum по клику
    public void cycleEnum() {
        if (options == null || options.length == 0) return;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(enumValue)) {
                enumValue = options[(i + 1) % options.length];
                return;
            }
        }
        enumValue = options[0];
    }
}
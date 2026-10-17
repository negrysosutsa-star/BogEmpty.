package com.fox.bogempty.module;

import com.fox.bogempty.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public class Module {
    private final String name;
    private final Category category;
    private boolean enabled;
    private int key;
    
    // Все настройки модуля
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
        this.key = -1;
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean state) { this.enabled = state; }
    public void toggle() { this.enabled = !this.enabled; }
    
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }
    
    // Добавить настройку
    public void addSetting(Setting setting) {
        settings.add(setting);
    }

    public List<Setting> getSettings() { return settings; }

    // Переопределяются в наследниках
    public void onTick() {}
    public void onRender() {}
}
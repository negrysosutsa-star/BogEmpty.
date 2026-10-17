package com.fox.bogempty.module;

public enum Category {
    COMBAT("Combat"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc"),
    HUD("HUD");

    public final String name;

    Category(String name) {
        this.name = name;
    }
}
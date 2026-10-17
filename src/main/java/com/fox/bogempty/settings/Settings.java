package com.fox.bogempty.setting;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Setting<T> {
    private final String name;
    private T value;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public Setting(String name, T defaultValue, Supplier<T> getter, Consumer<T> setter) {
        this.name = name;
        this.value = defaultValue;
        this.getter = getter;
        this.setter = setter;
    }

    public String getName() { return name; }
    public T getValue() { return getter.get(); }
    public void setValue(T val) { setter.accept(val); }
}
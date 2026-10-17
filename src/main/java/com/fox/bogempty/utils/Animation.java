package com.fox.bogempty.utils;

public class Animation {
    private float value;
    private float target;
    private final float speed;
    private long lastUpdate;

    public Animation(float speed) {
        this.speed = speed;
        this.lastUpdate = System.currentTimeMillis();
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getValue() {
        update();
        return value;
    }

    private void update() {
        long now = System.currentTimeMillis();
        float delta = (now - lastUpdate) / 1000f;
        lastUpdate = now;

        if (value < target) {
            value += speed * delta * 5f; // Скорость анимации
            if (value > target) value = target;
        } else if (value > target) {
            value -= speed * delta * 5f;
            if (value < target) value = target;
        }
    }
}
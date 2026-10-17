package com.fox.bogempty;

import net.fabricmc.api.ClientModInitializer;
import com.fox.bogempty.config.Config;

public class BogEmpty implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        System.out.println("[BogEmpty] Клиент успешно загружен!");
    }
}
package com.fox.bogempty.module.render;

import com.fox.bogempty.module.Category;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.setting.Setting;

public class NoRender extends Module {
    // Создаем галочки для самых популярных отключений
    private final Setting hurtCam = new Setting("No Hurt Cam", true);
    private final Setting fire = new Setting("No Fire Overlay", true);
    private final Setting nausea = new Setting("No Nausea", false);
    private final Setting weather = new Setting("No Weather", false);

    public NoRender() {
        super("NoRender", Category.RENDER);
        addSetting(hurtCam);
        addSetting(fire);
        addSetting(nausea);
        addSetting(weather);
    }

    // Эти методы будут вызываться из миксина
    public boolean isHurtCam() { return isEnabled() && hurtCam.getBool(); }
    public boolean isFire() { return isEnabled() && fire.getBool(); }
    public boolean isNausea() { return isEnabled() && nausea.getBool(); }
    public boolean isWeather() { return isEnabled() && weather.getBool(); }
}
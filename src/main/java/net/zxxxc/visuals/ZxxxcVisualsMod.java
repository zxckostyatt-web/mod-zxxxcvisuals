package net.zxxxc.visuals;

import net.fabricmc.api.ClientModInitializer;

public class ZxxxcVisualsMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Регистрируем нашу кастомную плашку при запуске клиента
        PulseWatermarkRenderer.register();
    }
}

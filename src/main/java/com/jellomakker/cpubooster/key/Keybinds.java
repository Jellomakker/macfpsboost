package com.jellomakker.cpubooster.key;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.ConfigManager;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    private static KeyBinding toggleKey;
    private static KeyBinding debugOverlayKey;

    public static void register() {
        try {
            toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.cpubooster.toggle",
                    GLFW.GLFW_KEY_Y,
                    "category.cpubooster"
            ));

            debugOverlayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.cpubooster.debug_overlay",
                    GLFW.GLFW_KEY_H,
                    "category.cpubooster"
            ));

            ClientTickEvents.END_CLIENT_TICK.register(client -> onTick(client));
        } catch (NoClassDefFoundError e) {
            // Fabric API not present at runtime; ignore registration
        }
    }

    private static void onTick(MinecraftClient client) {
        if (client == null || client.player == null) return;

        // Toggle main mod
        if (toggleKey != null && toggleKey.wasPressed()) {
            CpuBoosterConfig cfg = ConfigManager.get();
            cfg.enabled = !cfg.enabled;
            ConfigManager.set(cfg);
            String msg = cfg.enabled ? "CPU Booster on" : "CPU Booster off";
            client.player.sendMessage(Text.literal(msg), false);
            CpuBoosterMod.LOGGER.info("CPU Booster toggled: {}", cfg.enabled);
        }

        // Toggle debug overlay
        if (debugOverlayKey != null && debugOverlayKey.wasPressed()) {
            CpuBoosterConfig cfg = ConfigManager.get();
            cfg.debugOverlayEnabled = !cfg.debugOverlayEnabled;
            ConfigManager.set(cfg);
            String msg = cfg.debugOverlayEnabled ? "Debug overlay on" : "Debug overlay off";
            client.player.sendMessage(Text.literal(msg), false);
            CpuBoosterMod.LOGGER.info("Debug overlay toggled: {}", cfg.debugOverlayEnabled);
        }
    }
}

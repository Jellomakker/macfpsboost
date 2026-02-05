package com.jellomakker.cpubooster.compat;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import com.jellomakker.cpubooster.CpuBoosterMod;
import net.minecraft.client.MinecraftClient;

/**
 * Environment detection: logs OS, architecture, and GPU renderer string.
 *
 * Used for diagnostic purposes and to understand what hardware the user
 * is running on.
 */
public class EnvironmentDetector {
    private static String osName;
    private static String osArch;
    private static String rendererString;
    private static boolean detectionAttempted = false;

    /**
     * Detect and log environment information.
     * This can be called early in initialization.
     */
    public static void detectAndLog() {
        if (detectionAttempted) return;
        detectionAttempted = true;

        try {
            osName = System.getProperty("os.name", "Unknown");
            osArch = System.getProperty("os.arch", "Unknown");

            CpuBoosterMod.LOGGER.info("=== Environment Detection ===");
            CpuBoosterMod.LOGGER.info("OS: {} ({})", osName, osArch);

            // Try to get GPU renderer string (GL context may not exist yet, so wrapped)
            try {
                rendererString = getGpuRendererString();
                if (rendererString != null && !rendererString.isEmpty()) {
                    CpuBoosterMod.LOGGER.info("GPU Renderer: {}", rendererString);
                }
            } catch (Exception e) {
                CpuBoosterMod.LOGGER.debug("Could not detect GPU renderer (GL context not ready): {}", e.getMessage());
            }

            // Log Apple Silicon specifically
            if (osName.toLowerCase().contains("mac")) {
                if (osArch.toLowerCase().contains("aarch64") || osArch.toLowerCase().contains("arm")) {
                    CpuBoosterMod.LOGGER.info("Apple Silicon detected (ARM64)");
                } else if (osArch.toLowerCase().contains("x86_64") || osArch.toLowerCase().contains("x64")) {
                    CpuBoosterMod.LOGGER.info("macOS Intel (x86_64)");
                }
            }
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.debug("Environment detection failed: {}", e.getMessage());
        }
    }

    /**
     * Try to get GPU renderer string from OpenGL (if context is available).
     * This is a safe, deferred call that returns null if GL is not ready.
     */
    private static String getGpuRendererString() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.getWindow() == null) {
                return null;
            }

            // Check if GL context is available
            if (!GL.getCapabilities().OpenGL11) {
                return null;
            }

            // Try to get from GL (safe as long as we're in a GL context)
            String renderer = GL11.glGetString(GL11.GL_RENDERER);
            String vendor = GL11.glGetString(GL11.GL_VENDOR);
            String version = GL11.glGetString(GL11.GL_VERSION);

            StringBuilder sb = new StringBuilder();
            if (vendor != null && !vendor.isEmpty()) sb.append(vendor).append(" ");
            if (renderer != null && !renderer.isEmpty()) sb.append(renderer).append(" ");
            if (version != null && !version.isEmpty()) sb.append(version);
            return sb.toString().trim();
        } catch (Exception e) {
            // GL context not available or other GL error; fail silently
            return null;
        }
    }

    /**
     * Get OS name.
     */
    public static String getOsName() {
        return osName != null ? osName : "Unknown";
    }

    /**
     * Get OS architecture.
     */
    public static String getOsArch() {
        return osArch != null ? osArch : "Unknown";
    }

    /**
     * Get renderer string (GPU info).
     */
    public static String getRendererString() {
        return rendererString != null ? rendererString : "Unknown";
    }

    /**
     * Check if running on macOS.
     */
    public static boolean isMacOS() {
        return getOsName().toLowerCase().contains("mac");
    }

    /**
     * Check if running on Apple Silicon (ARM64).
     */
    public static boolean isAppleSilicon() {
        if (!isMacOS()) return false;
        String arch = getOsArch().toLowerCase();
        return arch.contains("aarch64") || arch.contains("arm");
    }

    /**
     * Check if running on macOS with Intel (x86_64).
     */
    public static boolean isMacOSIntel() {
        if (!isMacOS()) return false;
        String arch = getOsArch().toLowerCase();
        return arch.contains("x86_64") || arch.contains("x64");
    }
}

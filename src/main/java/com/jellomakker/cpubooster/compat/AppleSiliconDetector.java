package com.jellomakker.cpubooster.compat;

import com.jellomakker.cpubooster.CpuBoosterMod;

/**
 * Detects Apple Silicon (M1/M2/M3) Macs and provides platform-specific info.
 * Used to apply Mac-specific optimizations safely.
 * 
 * NOTE: GL queries are deferred until after OpenGL context is available.
 */
public class AppleSiliconDetector {
    private static final boolean IS_APPLE_SILICON = detectAppleSilicon();
    private static volatile String GPU_VENDOR = null;
    private static volatile boolean GPU_VENDOR_QUERIED = false;

    public static boolean isAppleSilicon() {
        return IS_APPLE_SILICON;
    }

    public static String getGpuVendor() {
        // Lazy-load GPU vendor after OpenGL context is ready
        if (!GPU_VENDOR_QUERIED) {
            try {
                GPU_VENDOR = safeGetGpuVendor();
                GPU_VENDOR_QUERIED = true;
            } catch (Exception e) {
                GPU_VENDOR = "unknown";
                GPU_VENDOR_QUERIED = true;
            }
        }
        return GPU_VENDOR != null ? GPU_VENDOR : "unknown";
    }

    private static boolean detectAppleSilicon() {
        try {
            String osName = System.getProperty("os.name", "").toLowerCase();
            String osArch = System.getProperty("os.arch", "").toLowerCase();

            // Check OS: macOS
            if (!osName.contains("mac")) {
                return false;
            }

            // Check architecture: aarch64 (Apple Silicon) vs x86_64 (Intel)
            if (!osArch.contains("aarch64") && !osArch.contains("arm64")) {
                return false;
            }

            CpuBoosterMod.LOGGER.debug("Detected Apple Silicon Mac architecture");
            return true;
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.debug("Apple Silicon detection failed: {}", e.getMessage());
            return false;
        }
    }

    private static String safeGetGpuVendor() {
        try {
            // Try to get GL vendor - this requires OpenGL context to be initialized
            // If context isn't ready, we'll get an exception and return "unknown"
            try {
                org.lwjgl.opengl.GL11 gl11 = null;
                // Safe reflection-based approach to avoid early GL context issues
                Class<?> gl11Class = Class.forName("org.lwjgl.opengl.GL11");
                java.lang.reflect.Method glGetStringMethod = gl11Class.getMethod("glGetString", int.class);
                java.lang.reflect.Field glVendorField = gl11Class.getField("GL_VENDOR");
                
                int GL_VENDOR = (int) glVendorField.get(null);
                String vendor = (String) glGetStringMethod.invoke(null, GL_VENDOR);
                
                if (vendor != null && !vendor.isEmpty()) {
                    CpuBoosterMod.LOGGER.debug("Detected GPU Vendor: {}", vendor);
                    return vendor;
                }
            } catch (Exception e) {
                // Expected if GL context not ready - silent
                CpuBoosterMod.LOGGER.debug("GL vendor query failed (context not ready): {}", e.getMessage());
            }
            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static void logDetectionInfo() {
        CpuBoosterMod.LOGGER.info("=== Apple Silicon Detection ===");
        CpuBoosterMod.LOGGER.info("OS: {} ({})", System.getProperty("os.name"), System.getProperty("os.arch"));
        CpuBoosterMod.LOGGER.info("Apple Silicon detected: {}", IS_APPLE_SILICON);
        // Don't query GPU vendor here - it's not ready yet, just log that it will be queried later
        CpuBoosterMod.LOGGER.info("GPU Vendor: will be queried on first render");
    }
}

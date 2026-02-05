package com.jellomakker.cpubooster.patches;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.compat.AppleSiliconDetector;
import java.util.*;

/**
 * Manages all performance patches: registration, initialization, and toggling.
 */
public class PatchManager {
    private final Map<String, Patch> patches = new LinkedHashMap<>();
    private final Map<String, Boolean> patchStates = new HashMap<>();

    public PatchManager() {
        registerPatch(new FramePacingPatch());
        registerPatch(new UBOPreallocPatch(2));
        registerPatch(new AllocationReducerPatch());
    }

    public void registerPatch(Patch patch) {
        patches.put(patch.getId(), patch);
        patchStates.put(patch.getId(), true); // Default: enabled
    }

    public void initializeEnabledPatches() {
        for (Map.Entry<String, Patch> entry : patches.entrySet()) {
            String id = entry.getKey();
            Patch patch = entry.getValue();

            // Check if should be enabled based on config
            boolean shouldEnable = patchStates.getOrDefault(id, true);
            if (!shouldEnable) {
                CpuBoosterMod.LOGGER.debug("Patch {} is disabled in config", id);
                continue;
            }

            // Check if patch is specific to Apple Silicon
            if (patch.isAppleSiliconSpecific() && !AppleSiliconDetector.isAppleSilicon()) {
                CpuBoosterMod.LOGGER.debug("Patch {} is Apple Silicon-specific, skipping on this platform", id);
                continue;
            }

            try {
                patch.initialize();
                CpuBoosterMod.LOGGER.info("✓ Initialized patch: {} - {}", id, patch.getName());
            } catch (Throwable e) {
                // FAIL-SOFT: Log error but don't crash
                CpuBoosterMod.LOGGER.warn("✗ Failed to initialize patch {} ({}): {}", id, patch.getName(), e.getMessage());
                CpuBoosterMod.LOGGER.debug("Patch initialization error details:", e);
                // Disable this patch so it doesn't try to run
                patchStates.put(id, false);
            }
        }
    }

    public void disableAllPatches() {
        for (Patch patch : patches.values()) {
            try {
                patch.disable();
            } catch (Exception e) {
                CpuBoosterMod.LOGGER.error("Error disabling patch: {}", e.getMessage());
            }
        }
    }

    public Patch getPatch(String id) {
        return patches.get(id);
    }

    public FramePacingPatch getFramePacingPatch() {
        return (FramePacingPatch) patches.get("framePacing");
    }

    public Map<String, String> getPatchInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        for (Map.Entry<String, Patch> entry : patches.entrySet()) {
            Patch patch = entry.getValue();
            boolean enabled = patchStates.getOrDefault(entry.getKey(), true);
            String status = enabled ? "ENABLED" : "DISABLED";
            info.put(patch.getId(), String.format("%s - %s", patch.getName(), status));
        }
        return info;
    }

    public void setPatchState(String id, boolean enabled) {
        if (patches.containsKey(id)) {
            patchStates.put(id, enabled);
        }
    }

    public boolean getPatchState(String id) {
        return patchStates.getOrDefault(id, true);
    }

    public List<String> getEnabledPatchIds() {
        List<String> enabled = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : patchStates.entrySet()) {
            if (entry.getValue()) {
                enabled.add(entry.getKey());
            }
        }
        return enabled;
    }
}

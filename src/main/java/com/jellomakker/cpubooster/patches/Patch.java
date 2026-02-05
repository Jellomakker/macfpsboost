package com.jellomakker.cpubooster.patches;

/**
 * Base interface for performance patches.
 * Each patch represents one specific optimization that can be toggled independently.
 */
public interface Patch {
    /**
     * @return Unique identifier for this patch (used in config)
     */
    String getId();

    /**
     * @return Human-readable name
     */
    String getName();

    /**
     * @return Description of what this patch optimizes
     */
    String getDescription();

    /**
     * Called during mod initialization if patch is enabled.
     * Should register mixins, event listeners, or apply changes.
     */
    void initialize();

    /**
     * Called during mod shutdown or config reload.
     * Should clean up resources if needed.
     */
    void disable();

    /**
     * @return Whether this patch is only effective on Apple Silicon
     */
    boolean isAppleSiliconSpecific();
}

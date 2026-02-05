package com.jellomakker.cpubooster.profiles;

/**
 * Enum of different gameplay profiles/modes detected by AdaptiveProfileDetector.
 */
public enum GameProfile {
    PVP("PVP", "Player-versus-player combat detected"),
    EXPLORATION("EXPLORATION", "Exploration/gathering detected"),
    BUILDING("BUILDING", "Block placement/building detected"),
    AFK("AFK", "Away from keyboard/idle detected");

    private final String displayName;
    private final String description;

    GameProfile(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static GameProfile fromString(String name) {
        try {
            return GameProfile.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GameProfile.EXPLORATION; // Default
        }
    }
}

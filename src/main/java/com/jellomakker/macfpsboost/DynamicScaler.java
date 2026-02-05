package com.jellomakker.macfpsboost;

public class DynamicScaler {
    private volatile double scale = 1.0; // 1.0 = native
    private final double minScale = 0.6;
    private final double maxScale = 1.0;

    public synchronized void decreaseScale(double delta) {
        scale = Math.max(minScale, scale - delta);
    }

    public synchronized void increaseScale(double delta) {
        scale = Math.min(maxScale, scale + delta);
    }

    public double getScale() { return scale; }

    // TODO: applyScale should interact with Minecraft's render pipeline.
    public void applyScale() {
        // placeholder: actual hook via mixin needed to change framebuffer/resolution
    }
}

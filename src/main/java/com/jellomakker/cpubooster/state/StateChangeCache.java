package com.jellomakker.cpubooster.state;

/**
 * Cache last camera/window state to reduce redundant computations inside our mod.
 * Consumers call `hasSignificantChange(...)` to decide whether to recompute.
 */
public class StateChangeCache {
    private double lastCamX = Double.NaN;
    private double lastCamY = Double.NaN;
    private double lastCamZ = Double.NaN;
    private float lastFov = Float.NaN;
    private int lastWidth = -1;
    private int lastHeight = -1;

    private double cameraDeltaThreshold = 0.01;
    private double rotationDeltaThreshold = 0.25;
    private boolean enabled = true;

    public void updateConfig(boolean enabled, double camThresh, double rotThresh) {
        this.enabled = enabled;
        this.cameraDeltaThreshold = camThresh;
        this.rotationDeltaThreshold = rotThresh;
    }

    public boolean hasSignificantChange(double camX, double camY, double camZ, float fov, int width, int height) {
        if (!enabled) return true;
        double dx = camX - lastCamX;
        double dy = camY - lastCamY;
        double dz = camZ - lastCamZ;
        double dist2 = dx*dx + dy*dy + dz*dz;
        boolean camMoved = Double.isNaN(lastCamX) || dist2 > cameraDeltaThreshold;
        boolean fovChanged = Float.isNaN(lastFov) || Math.abs(fov - lastFov) > rotationDeltaThreshold;
        boolean sizeChanged = width != lastWidth || height != lastHeight;
        if (camMoved || fovChanged || sizeChanged) {
            lastCamX = camX; lastCamY = camY; lastCamZ = camZ; lastFov = fov; lastWidth = width; lastHeight = height;
            return true;
        }
        return false;
    }
}

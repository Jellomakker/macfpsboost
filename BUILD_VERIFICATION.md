# Build Verification Guide

This document explains how the macfpsboost mod is built and verified as a valid Fabric mod.

## Quick Build Command

```bash
./gradlew build
```

This will compile the mod and produce `build/libs/macfpsboost-1.0.0.jar`.

## Verification Checklist

The build process automatically verifies that the jar is a valid Fabric mod. During the build, you should see:

```
✓ Jar file exists: /workspaces/macfpsboost/build/libs/macfpsboost-1.0.0.jar (4749 bytes)
✓ Contains: fabric.mod.json
✓ Contains: com/jellomakker/macfpsboost/MacFpsBoostMod.class
✓ Contains: com/jellomakker/macfpsboost/FrameTimeMonitor.class
✓ Contains: com/jellomakker/macfpsboost/AdaptiveParticleGovernor.class
✓ fabric.mod.json is valid and properly configured

✓ BUILD VERIFICATION PASSED: Jar is a valid Fabric mod!
```

## Manual Verification Steps

If you want to manually verify the jar, use these commands:

### 1. Check fabric.mod.json is in the jar
```bash
jar tf build/libs/macfpsboost-1.0.0.jar | grep fabric.mod.json
```

Expected output:
```
fabric.mod.json
```

### 2. Extract and view fabric.mod.json
```bash
unzip -p build/libs/macfpsboost-1.0.0.jar fabric.mod.json
```

Expected output should show:
- `"id": "macfpsboost"`
- `"version": "1.0.0"`
- `"environment": "client"`
- `"entrypoints": { "client": ["com.jellomakker.macfpsboost.MacFpsBoostMod"] }`

### 3. List all jar contents
```bash
jar tf build/libs/macfpsboost-1.0.0.jar
```

Expected files:
- `META-INF/MANIFEST.MF`
- `fabric.mod.json` ← Fabric mod metadata (REQUIRED)
- `com/jellomakker/macfpsboost/MacFpsBoostMod.class` ← Client mod initializer
- `com/jellomakker/macfpsboost/FrameTimeMonitor.class`
- `com/jellomakker/macfpsboost/AdaptiveParticleGovernor.class`
- `macfpsboost.accesswidener` ← Access widener for Fabric Loom
- `mixins.macfpsboost.json` ← Mixin configuration

### 4. Check jar size
```bash
ls -lh build/libs/macfpsboost-1.0.0.jar
```

The jar should be **at least 4.7 KB** (not 261 bytes!).

## What Makes It a Valid Fabric Mod Jar

✅ **fabric.mod.json** exists in jar root with valid mod metadata
✅ **environment** field is set to "client" (client-side only mod)
✅ **entrypoints.client** points to `com.jellomakker.macfpsboost.MacFpsBoostMod`
✅ **MacFpsBoostMod** class implements `net.fabricmc.api.ClientModInitializer`
✅ **Compiled classes** are included in the jar
✅ **Fabric Loader** can find and load the mod: it will appear in "Loading X mods" log

## Installation

1. Build the mod:
   ```bash
   ./gradlew build
   ```

2. Find the jar:
   ```bash
   ls -lh build/libs/macfpsboost-1.0.0.jar
   ```

3. Install in Minecraft:
   - Copy `build/libs/macfpsboost-1.0.0.jar` to your `.minecraft/mods/` folder
   - On macOS, this is typically: `~/Library/Application Support/minecraft/mods/`

4. Launch Minecraft with Fabric Loader and the mod should appear in the mod list

5. Check Minecraft logs for:
   ```
   [Client thread/INFO] [macfpsboost]: Mac FPS Boost loaded! Frame time monitoring active.
   ```

## Troubleshooting

### Jar is not recognized in "Loading X mods"
- Verify `fabric.mod.json` is in jar root: `jar tf build/libs/macfpsboost-1.0.0.jar | grep fabric.mod.json`
- Verify fabric.mod.json has correct "id" and "environment"
- Verify jar size is > 4KB
- Check that you're using the release jar from `build/libs/`, not the dev jar from `build/devlibs/`

### Mod loads but doesn't do anything
- Check Minecraft logs for initialization message
- The current version has core services but no runtime hooks (mixins are disabled)
- This is expected - the framework is in place, but feature hooks need to be implemented

## Build Configuration

The build uses:
- **Gradle**: 9.0.0 (configured via wrapper)
- **Fabric Loom**: 1.8.1 (handles Minecraft mapping and remapping)
- **Yarn Mappings**: 1.21.8+build.1 (Fabric's Minecraft deobfuscation mappings)
- **Java**: 21 (configured in gradle.properties)
- **Fabric Loader**: 0.18.4

See `build.gradle` for full configuration.

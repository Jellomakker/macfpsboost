# macfpsboost

A Fabric 1.21.8 client-side mod for macOS (Apple Silicon) that improves FPS by monitoring frame times and adapting quality settings dynamically.

## Features

- **Frame Time Monitoring**: Tracks per-frame millisecond timings to detect FPS drops
- **Adaptive Particle Governor**: Three-level particle quality system (ALL → DECREASED → MINIMAL) with exponential smoothing
- **FPS-Based Quality Scaling**: Automatically adjusts quality when frames drop below target fps

## Installation

### Prerequisites
- Fabric Loader 0.18.4+ 
- Minecraft 1.21.8
- Java 21 (for compiling from source)

### Quick Install
1. Download the latest release jar: `macfpsboost-1.0.0.jar`
2. Place it in your `.minecraft/mods/` folder:
   - **macOS**: `~/Library/Application Support/minecraft/mods/`
   - **Linux**: `~/.minecraft/mods/`
   - **Windows**: `%APPDATA%\.minecraft\mods\`
3. Launch Minecraft with Fabric Loader
4. Check your logs for: `[macfpsboost]: Mac FPS Boost loaded! Frame time monitoring active.`

## Building from Source

### Requirements
- JDK 21+
- Gradle (wrapper included)

### Build Steps
```bash
git clone https://github.com/Jellomakker/macfpsboost
cd macfpsboost
./gradlew build
```

The build will automatically verify that the jar is a valid Fabric mod. You should see:

```
✓ BUILD VERIFICATION PASSED: Jar is a valid Fabric mod!
```

The compiled mod jar will be at: `build/libs/macfpsboost-1.0.0.jar`

### Manual Verification
To verify the jar contains a proper Fabric mod structure:
```bash
jar tf build/libs/macfpsboost-1.0.0.jar | grep fabric.mod.json
```

See [BUILD_VERIFICATION.md](BUILD_VERIFICATION.md) for detailed build and verification information.

## Architecture

### Core Services

**MacFpsBoostMod** (`com.jellomakker.macfpsboost.MacFpsBoostMod`)
- Implements `ClientModInitializer` entrypoint
- Manages singleton instances of monitoring services
- Logs initialization message to Minecraft logs

**FrameTimeMonitor** (`com.jellomakker.macfpsboost.FrameTimeMonitor`)
- Tracks frame render time in milliseconds
- Uses `System.nanoTime()` for high-precision timing
- Calculates delta time between frames

**AdaptiveParticleGovernor** (`com.jellomakker.macfpsboost.AdaptiveParticleGovernor`)
- Three-level particle quality state (ALL, DECREASED, MINIMAL)
- Exponential smoothing (α=0.08) of FPS measurements
- 40-tick cooldown between quality state changes
- Thresholds: degrades at 0.85×targetFps, restores at 1.05×targetFps

## Configuration

Currently, the mod has no configurable settings. Quality thresholds and smoothing factors are defined in:
- [AdaptiveParticleGovernor.java](src/main/java/com/jellomakker/macfpsboost/AdaptiveParticleGovernor.java)

Future versions will support:
- Custom FPS target and thresholds
- Quality scaling aggressiveness
- Particle rendering overrides

## Development

### Project Structure
```
src/main/java/com/jellomakker/macfpsboost/
├── MacFpsBoostMod.java           # Mod entry point
├── FrameTimeMonitor.java         # Frame timing service
└── AdaptiveParticleGovernor.java # Quality scaling logic

src/main/resources/
├── fabric.mod.json               # Fabric mod metadata
├── macfpsboost.accesswidener     # Access widener declarations
└── mixins.macfpsboost.json       # Mixin configuration
```

### Gradle Configuration
- **Loom Version**: 1.8.1
- **Gradle Version**: 9.0.0 (pinned via wrapper)
- **Java Version**: 21
- **Fabric Loader**: 0.18.4
- **Yarn Mappings**: 1.21.8+build.1

See [build.gradle](build.gradle) for complete build configuration.

## Troubleshooting

### Mod doesn't appear in mod list
- Verify jar is at least 4KB: `ls -lh build/libs/macfpsboost-1.0.0.jar`
- Confirm fabric.mod.json is in jar: `jar tf build/libs/macfpsboost-1.0.0.jar | grep fabric.mod.json`
- Check Minecraft logs for duplicate mod IDs
- Ensure you're using the jar from `build/libs/`, not `build/devlibs/`

### Build fails
- Ensure Java 21+ is in PATH: `java -version`
- Clear Gradle cache: `rm -rf .gradle build`
- Rebuild: `./gradlew clean build`

See [BUILD_VERIFICATION.md](BUILD_VERIFICATION.md) for more detailed troubleshooting.

## License

MIT License - see LICENSE file for details

## Author

Jellomakker

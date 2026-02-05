```markdown
# macfpsboost

This repository contains a Fabric client-side mod skeleton (1.21.8 target) implementing an MVP of macOS-focused performance improvements:

- Dynamic resolution scaler (runtime render-scale adjustments)
- Chunk rebuild budgeting (rate-limit and queue chunk mesh rebuilds to avoid spikes)

What I added here:

- `src/main/java/com/jellomakker/macfpsboost` : core classes (`MacFpsBoostMod`, `FrameTimeMonitor`, `DynamicScaler`, `ChunkRebuildBudgeter`)
- `src/main/java/com/jellomakker/macfpsboost/mixin` : mixin stubs for render loop and chunk rebuild scheduling
- `src/main/resources/fabric.mod.json` and `mixins.macfpsboost.json` : mod + mixin config

Next steps I can take for the MVP (choose one in the prompt):

1. Flesh out the mixins with correct target classes/methods for Fabric 1.21.8 mappings and wire the hooks into the render pipeline (preferred next step).
2. Add a Gradle Loom `build.gradle` and a runnable dev environment.
3. Implement polish: config options, smoothing, and safer heuristics for scale changes.

To proceed, tell me which next step you want (I can implement 1 and 2 in sequence).

```
# macfpsboost
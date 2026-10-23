# Realistic Water — Fabric 26.3

A starter Fabric mod project adding a separate **Realistic Water Bucket**. It leaves the vanilla water bucket unchanged.

## Current prototype features

- Adds a new item named **Realistic Water Bucket**.
- Right-click a block to place water in the adjacent space.
- Consumes the custom bucket and returns an empty vanilla bucket in survival.
- Tracks water placed by the custom bucket and performs a simple gravity-biased, voxel-style spread step.
- Uses vanilla water for appearance and broad compatibility.

## Important limitation

This is a **first playable prototype**, not a physically accurate replacement fluid engine yet. Vanilla water's normal flow engine still runs, so the custom simulation does not fully isolate its water from nearby vanilla water. The swirling Blender-style placement animation is also not included in this initial source version. Those require a dedicated custom fluid/rendering implementation and client-side particle/render hooks.

## Requirements

- Minecraft Java Edition 26.3
- Java JDK 25
- Fabric Loader 0.19.5 or newer compatible with 26.3
- Fabric API for 26.3
- Gradle 9.6 (wrapper properties configured for 9.6.0)

Fabric's current version page lists Minecraft 26.3, Loader 0.19.5, Fabric API 0.164.0+26.3, and recommends Loom 1.18-SNAPSHOT. Verify the recommended Loom version at https://fabricmc.net/develop/ before building, because it can change.

## Build

1. Install JDK 25 and confirm `java -version` reports 25.
2. Open this folder in IntelliJ IDEA as a Gradle project.
3. Let Gradle sync and download Minecraft/Fabric dependencies.
4. Run the Gradle task `build`.
   - Windows: `gradlew.bat build`
   - macOS/Linux: `./gradlew build`
5. The mod JAR should be in `build/libs/realistic-water-1.0.0.jar`.

This archive does not include the Gradle wrapper executable JAR or Windows wrapper script. If you use IntelliJ, use its Gradle tool window; alternatively generate wrapper files with a local Gradle 9.6 installation (`gradle wrapper --gradle-version 9.6.0`).

## Install for testing

1. Install Fabric Loader for Minecraft 26.3.
2. Put the matching Fabric API JAR into the same Minecraft instance's `mods` folder.
3. Put `realistic-water-1.0.0.jar` into `mods`.
4. Launch the Fabric profile.
5. In Creative, find **Realistic Water Bucket** in the item search. For a quick test, use `/give @s realistic_water:realistic_water_bucket`.

## Recipe

No recipe is included in this prototype yet. The item is available through Creative inventory and `/give`.

## Project layout

```text
realistic-water/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradle/wrapper/gradle-wrapper.properties
└── src/main/
    ├── java/dev/example/realisticwater/RealisticWaterMod.java
    └── resources/
        ├── fabric.mod.json
        └── assets/realistic_water/
            ├── lang/en_us.json
            └── models/item/realistic_water_bucket.json
```


## Easiest build option: build online with GitHub Actions

This project includes `.github/workflows/build.yml` to build the mod in GitHub's cloud, so you do not need to install Java or Gradle locally.

1. Sign in to GitHub and create a **new empty repository** (do not add a README or license).
2. Open the new repository, choose **Add file → Upload files**, and upload the *contents* of this project folder, including the hidden `.github` folder. Commit the files to `main`.
3. Open the repository's **Actions** tab and select **Build Realistic Water mod**.
4. If the workflow has not run automatically, choose **Run workflow**.
5. Open the completed run. Under **Artifacts**, download `realistic-water-mod-jar`.
6. Extract the downloaded artifact ZIP. It contains the `.jar` to place in your CurseForge profile's `mods` folder.

If the workflow fails, it will not produce a JAR. Open the failed workflow run and copy the first error from the **Build mod** step; the source may need Minecraft 26.3 API fixes before it can compile.

## Feature status

This is still a starter prototype. It registers a separate bucket and attempts basic gravity-biased spreading using vanilla water. It does not yet contain the requested Blender-style vortex animation or a fully independent custom fluid engine, and it has not been compiled/tested against Minecraft 26.3 yet.

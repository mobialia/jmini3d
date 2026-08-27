# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.9.7] - 2026-08-27

### Added
- Added `keep.xml` to the Android library so the Android resource shrinker preserves the bundled GLSL shaders (`@raw/vertex_shader`, `@raw/fragment_shader`, `@raw/vertex_shader_lens_distortion`, `@raw/fragment_shader_opengl_axis`).
- Added a GitHub Packages badge and this changelog to the readme.

### Fixed
- Fixed a static `shaderKey` shared across all `ShaderPlugin` subclasses.
- Fixed `normalMatrix` being set to `null` when the model matrix is singular.
- Fixed Android `GpuUploader` cube map texture bugs.
- Fixed GWT `GpuUploader` texture leaks and variable shadowing.
- Fixed `useLigths` → `useLights` typo.
- Fixed grammatical errors in the readme.

## [0.9.6] - 2026-07-26

### Changed
- Migrated distribution from JitPack to GitHub Packages.
- Added a GitHub Actions workflow to publish packages on release.
- Updated Gradle to 9.6.1 and targeted Java 17.

### Fixed
- Removed Internet Explorer specific code (no longer a thing).
- Fixed a MIME type warning in the front-end build.
- JitPack fix.

### Optimized
- Shortened build strings.
- Optimized fragment shaders.

## [0.9.5] - 2023-09-13

### Changed
- Updated Android Gradle plugin to 8.1.1 (requires Java 17), Gradle wrapper to 8.2, buildToolsVersion 33.0.0.
- Use class name for `TAG` constants.
- Geometries generator now uses Java array syntax.
- `build.gradle`: Java 11 & `archiveClassifier`.
- Added `sourceCompatibility` and `targetCompatibility` inside the `java {}` block of the Android demo.
- Reorganized `.gitignore` and cleaned GWT directories on `clean`.

### Added
- Allow setting text with `char` arrays, avoiding the creation of new strings.
- Configurable `InputController.onTouch()` redraw: force a redraw and draw 4 frames after a surface change.

### Fixed
- Bug in `lineIntersectsQuad()`.
- Fixed Android library publishing.
- `Traslucent` → `translucent` typo.

### Optimized
- Small memory optimization in `Font`.
- Removed unnecessary boxing.

## [0.9.4] - 2022-10-05

### Changed
- Moved from Bintray to JitPack for distribution.
- Updated Gradle wrapper to 7.5.1, GWT to 2.10.0, `compileSdkVersion` to 32.
- Updated GWT to 2.9.0 and Gretty to 3.0.3.
- Updated Gradle to 6.1.1, Android tools to 4.0.1, `buildToolsVersion` to 29.0.2.
- Replaced the deprecated Jetty plugin with `org.akhikhl.gretty`; the GWT demo now runs with `gradle appRun`.
- Added the `android:exported` attribute and moved Android versions to `build.gradle`.

### Added
- Added Page Down / Page Up key codes.
- Exposed the `program` variable in `ProgramPlugin` as `protected` for subclass access.

### Fixed
- Fixed wrong key codes.
- A scene redraw is now forced after loading shaders; `TextureLoadedListener` was renamed to `GpuUploaderListener` and is also called after a shader is uploaded to the GPU.
- Ignored Android lint errors.

### Removed
- Removed the Rubik demo due to a flickering problem.

## [0.9.3] - 2016-06-29

### Optimized
- Save memory by setting `shaderDefines = null` after shader initialization.

## [0.9.2] - 2016-06-27

### Optimized
- Do not enable `vertexAttribArray` until it is going to be set.

## [0.9.1] - 2016-06-22

### Optimized
- Cache attribute buffers, avoiding redundant GPU calls. Replaced the hashmap with single variables for better performance.

## [0.9] - 2016-06-20

### Changed
- New `HudCamera` and `PerspectiveCamera` extending the abstract `Camera` class.
- The HUD is now a separate scene with its own camera, rendered on top of the other scenes.
- `SceneController` is now a `ScreenController` with a `render()` method that renders one (or more) scenes.
- FPS is now calculated in `GlSurfaceView3d` or `Canvas3d` instead of the renderer.
- Removed the `ShaderPlugins` exception with the `SpriteMaterial`.

## [0.8.1] - 2016-06-16

### Changed
- New version 0.8.1.

## [0.8] - 2016-06-16

### Changed
- Readme for version 0.8.

## [0.7.2] - 2016-06-14

### Fixed
- `bintrayUpload` now also depends on `assembleRelease`.

## [0.7.1] - 2016-06-12

### Changed
- Version 0.7.1.

## [0.7] - 2016-06-10

### Changed
- New version 0.7.

## [0.6.1] - 2016-06-09

### Fixed
- Fix spelling.

## [0.6] - 2014-10-15

### Added
- Added scale to `Canvas` and `InputController` to support `<meta name="viewport" content="width=device-width">`.

## [0.5] - 2014-10-10

### Changed
- Light intensity is now carried in the alpha channel; it may be passed in the light constructor.

## [0.1] - 2014-02-08

### Changed
- Renamed shaders extension from `.txt` to `.glsl`.

[0.9.7]: https://github.com/mobialia/jmini3d/releases/tag/0.9.7
[0.9.6]: https://github.com/mobialia/jmini3d/releases/tag/0.9.6
[0.9.5]: https://github.com/mobialia/jmini3d/releases/tag/0.9.5
[0.9.4]: https://github.com/mobialia/jmini3d/releases/tag/0.9.4
[0.9.3]: https://github.com/mobialia/jmini3d/releases/tag/0.9.3
[0.9.2]: https://github.com/mobialia/jmini3d/releases/tag/0.9.2
[0.9.1]: https://github.com/mobialia/jmini3d/releases/tag/0.9.1
[0.9]: https://github.com/mobialia/jmini3d/releases/tag/0.9
[0.8.1]: https://github.com/mobialia/jmini3d/releases/tag/0.8.1
[0.8]: https://github.com/mobialia/jmini3d/releases/tag/0.8
[0.7.2]: https://github.com/mobialia/jmini3d/releases/tag/0.7.2
[0.7.1]: https://github.com/mobialia/jmini3d/releases/tag/0.7.1
[0.7]: https://github.com/mobialia/jmini3d/releases/tag/0.7
[0.6.1]: https://github.com/mobialia/jmini3d/releases/tag/0.6.1
[0.6]: https://github.com/mobialia/jmini3d/releases/tag/0.6
[0.5]: https://github.com/mobialia/jmini3d/releases/tag/0.5
[0.1]: https://github.com/mobialia/jmini3d/releases/tag/0.1
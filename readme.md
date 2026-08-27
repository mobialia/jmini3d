JMini3d
=======
[![Release](https://img.shields.io/github/v/release/mobialia/jmini3d.svg)](https://github.com/mobialia/jmini3d/releases)

A minimalistic OpenGL2 3D engine for mobile apps that supports Android and GWT (Google Web Toolkit, for creating HTML5 apps using Java).

HTML5 demo: http://www.mobialia.com/jmini3d-demo-gwt/

It also includes an input abstraction library (with key & pointer events) for Android and GWT.

JMini3d is used in 3D Mobialia games (http://www.mobialia.com) with Android and HTML5 versions like:
* Mobialia Chess: http://chess.mobialia.com http://www.mobialia.com/apps/chess
* Slot Racing: http://slot.mobialia.com http://www.mobialia.com/apps/slot
* Mobialia Four in a Row: http://fourinarow.mobialia.com http://www.mobialia.com/apps/fourinarow

Features
========
* Optimized for speed and smooth rendering
* Extremely fast model loading (converts OBJ models to Java classes)
* Phong lighting model with multiple lights (Ambient/Point/Directional), no attenuation with the distance
* Reflections with cube environment mapping
* HUD and 2D sprites support
* Normal maps
* Vertex colors
* Bitmap font support

Subprojects
===========
* *core:* includes the common classes between Android and GWT projects
* *android:* the Android library project implementing the OpenGLES 2.0 `Renderer3d`, `Activity3d`, `GlSurfaceView3d`, etc.
* *gwt:* the GWT library project implementing the WebGL `Renderer3d`, `EntryPoint3d`, `Canvas3d`, etc.
* *utils:* includes utilities to generate Geometry classes from OBJ models and Font classes from FNT files
* *demo-common:* The common files for the demo project with a `SceneController` and multiple Scenes
* *demo-android:* The Android demo application
* *demo-gwt:* The demo project in GWT

Axis
====
This library uses by default the same axis system as Blender, Z is up, Y is front. It is right handed.

```
 z   y
 |  /
 | /
 |------x
```
The axis system can be changed to the standard OpenGL system (-Z front, Y up) calling `JMini3d.useOpenglAxisSystem()`

HUD Scenes
==========
Jmini3D has a `HudCamera` and a `HudScene` to render scenes in 2D with textures as sprites.
There is also a `SpriteGeometry` and a `SpriteMaterial`.
The hud scenes may be rendered on top of other 3D scenes.

The coordinates for HUD sprites start in the top left corner with width and height in screen pixels:
```
       x
  0------- width
  |
y |
  |
height
```
When the screen size changes, the Scene is notified with `setViewPort(int width, int height)` and it must readjust the HUD elements.

Android
=======
To use the library in an Android app you can extend the `Activity3d` class or use the `GlSurfaceView3d`.

The image resources must be in the "drawable-nodpi" resource folder.
The `ResourceLoader` is initialized with a reference to the Android context.

GWT
===
In GWT you must extend the EntryPoint3d or use the Canvas3d wrapper (wraps a DOM canvas element).

The image resources must be in a subfolder of the web project location (`src/main/webapp/`),
the `ResourceLoader` is initialized with `./resources/` by default.
The GLSL shaders should be copied manually to a web project folder, `./shaders/` by default.

In mobile devices when using:
```
<meta name="viewport" content="width=device-width">
```
the canvas must be upscaled by canvas3d.getDevicePixelRatio(). The scale must be set also in the InputController,
you can see how it works in the demo.

Generate Geometries from OBJ files
==================================
Export to OBJ from Blender (faces must be triangulated and normals outside) with these options:

* Write Normals
* Include UVs
* Triangulate faces
* Y Forward
* Z up

And convert to a Java class with:
```
cd utils
../gradlew jar
java -cp ./build/libs/jmini3d-utils-0.9.3.jar jmini3d.utils.Obj2Class teapot.obj TeapotGeometry.java jmini3d.demo
```

The generated `TeapotGeometry.java` is a Java class in the `jmini3d.demo` package extending `Geometry`.

Generate Fonts from FNT files
=============================
Jmini3D can use FNT bitmap fonts in text format exported, for example, with the SnowB Bitmap Font generator https://snowb.org/.

To convert the FNT file to a Java class:
```
cd utils
../gradlew jar
java -cp ./build/libs/jmini3d-utils-0.9.3.jar jmini3d.utils.Fnt2Class arial.fnt ArialFont.java jmini3d.demo
```
The font texture must be placed in the images folder. It supports fonts with only one texture.

Materials and Textures
======================
* A Material can receive a Texture, for example:
```
Material m1 = new Material(new Texture("texture.png"));
```
* Textures support PNG and JPEG formats
* A same Texture object can be shared between different materials, and it is uploaded only once to the GPU
* Material has a `setBlending` method to enable texture transparency

Build
=====
This project is built with the Gradle build tool, you can download it from http://www.gradle.org

Build Android demo and install it on the connected device or emulator:
```
cd demo-android
../gradlew installDebug
```

Build the GWT demo in the demo-gwt/src/main/webapp/ directory:
```
cd demo-gwt
../gradlew compileGwt
```

You can start a local webserver:
```
cd demo-gwt
../gradlew appRun
```
then access it with your web browser at http://localhost:8080/jmini3d/

Using JMini3d in other projects
===============================
This library's JARs and AARs are distributed via GitHub Packages at
https://github.com/mobialia/jmini3d/packages.

GitHub Packages requires authentication even to read public packages, so you need a GitHub
Personal Access Token (PAT) with at least `read:packages` scope. Create one at
https://github.com/settings/tokens, then set the `GITHUB_ACTOR` and `GITHUB_TOKEN` environment
variables (or use gradle properties) before building.

Gradle `build.gradle`:

```groovy
repositories {
    maven {
        url = uri('https://maven.pkg.github.com/mobialia/jmini3d')
        credentials {
            username = project.findProperty('gpr.user') ?: System.getenv('GITHUB_USERNAME')
            password = project.findProperty('gpr.key')  ?: System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'com.github.mobialia:jmini3d-core:0.9.7'
    implementation 'com.github.mobialia:jmini3d-android:0.9.7@aar'
}
```

Available modules:
* `jmini3d-core` — common classes shared between Android and GWT
* `jmini3d-android` — Android library (AAR)
* `jmini3d-gwt` — GWT library (JAR + sources)
* `jmini3d-utils` — OBJ/FNT converters (JAR + sources)

Resource Shrinking (Release Builds)
==================================
The shaders (`res/raw/*.glsl`) and textures (`res/drawable-nodpi/*`) are loaded
at runtime via `Resources.getIdentifier(...)`, a dynamic lookup the Android
resource shrinker cannot trace statically. If your app enables
`shrinkResources true` in its release `buildType`, these resources will be
stripped from the release APK, causing a crash at draw time:

```
java.lang.RuntimeException: Could not initialize shaders
    at jmini3d.android.Program.init(Program.java:59)
```

Since jmini3d 0.9.7 the AAR ships a `res/raw/keep.xml` that retains the four
shader resources, so consuming apps are protected automatically. You only
need to ensure you're depending on jmini3d-android 0.9.7 or newer.

For the textures you load by name through `ResourceLoader.getImage()`, add your
own `keep.xml` in your app's `res/raw/` listing every `@drawable/...` resource
you reference dynamically, for example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@drawable/texture_a,@drawable/texture_b,@drawable/texture_c" />
```

Alternatively, set `shrinkResources false` in your release build type to keep
all resources at the cost of a slightly larger APK.

Licenses
========

It's released under the MIT License, so feel free to use it anywhere.

The cube texture in the demos is CC licensed from Humus http://www.humus.name
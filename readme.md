JMini3D
=======

Minimalistic OpenGL 2.0 3D library with support for Android and GWT (Google Web Toolkit, to create HTML5 apps developing in Java).

This library helps you to develop 3D apps compatible with Android and GWT projects. The CocoonJS linker allows to develop apps for CocoonJs's Canvas+ and use them in iOS.

Subprojects:
* *core:* includes the common classes between Android and GWT projects
* *android:* the Android OpenGL ES 2.0 Renderer
* *android_demo:* An Android demo application
* *gwt:* the GWT library project implementing a WebGL Renderer
* *gwt_cocoonjs:* GWT Linker to make Jmini3D work with the CocoonJS framework (it can create apps for iOS)
* *gwt_demo:* The demo project in GWT, you can view it at http://www.mobialia.com/jmini3d_gwt_demo/
* *utils:* includes an utility to generate Geometry files from OBJ models
* *gwtgl:* a dependency for the gwt project

Also includes a touch abstraction library (JMini3D Input) for Android and GWT.

JMini3D is used in 3D Mobialia games (http://www.mobialia.com) with Android and HTML5 versions like:
* Mobialia Chess: http://chess.mobialia.com http://www.mobialia.com/apps/chess
* Slot Racing: http://slot.mobialia.com http://www.mobialia.com/apps/slot
* Mobialia Four in a Row: http://fourinarow.mobialia.com http://www.mobialia.com/apps/fourinarow

Axis
====

This library uses the same axis system than Blender, z is up, y is front.

```
 z   y
 |  /
 | /
 |------x
```

Generate Geometries from OBJ files
==================================

Objects must have UV mapping.

Export to OBJ from Blender with this options:

* Write Normals
* Include UVs
* Triangulate faces
* Y Forward
* Z up

An convert to a Java class with:
```
cd utils
gradle jar
java -cp ./build/libs/jmini3d-utils-0.2.jar jmini3d.utils.Obj2Class teapot.obj TeapotGeometry.java jmini3d.android.demo
```

The generated TeapotGeometry.java is a Java class in the jmini3d.android.demo package extending Geometry.

Build
=====

This project is built with Gradle.

Buld Android demo and install it to the connected device or emulator:
```
cd android_demo
gradle installDebug
```

Build gwt demo in the gwt_demo/src/main/webapp/ directory, first you need to install the gwtgl artifact in your maven local repo:
```
cd gwtgl
gradle install
cd ../gwt_demo
gradle compileGwt
```

Install this library's JARs and AARs to the local Maven repo:
```
gradle install
```

Licenses
========

It's released under the MIT License, so feel free to use it anywhere.

The cube texture in the demos is CC licensed from Humus http://www.humus.name
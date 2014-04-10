JMini3D
=======

Minimalistic 3D library with support for Android and GWT (Google Web Toolkit, to create HTML5 apps developing in Java).

This library helps you to develop 3D apps compatible with Android and GWT projects.

The Android OpenGL 1.0/1.1 Renderer is extremely optimized for real time games. We are working to optimize also the Android OpenGL 2.0 and WebGL renderers.

Subprojects:
* *core:* includes the common classes between Android and GWT projects
* *android:* the Android OpenGL ES 1.0/1.1 Renderer
* *android2:* the Android OpenGL ES 2.0 Renderer
* *android_demo:* An Android demo application (we need more demos...)
* *gwt:* the GWT library project implementing a WebGL Renderer (based on OpenGL ES 2.0 like android2)
* *gwt_cocoonjs:* GWT Linker to make Jmini3D work with the CocoonJS framework (it can create apps for iOS)
* *gwt_demo:* a small demo project in GWT (we also need more demos here), you can compile the project with "gradle compileGwt"
* *utils:* includes an utility to generate Geometry files from OBJ models
* *gwtgl:* a dependency for the gwt project, do a 'gradle install' in this folder to copy the gwtgl artifact to your maven local repo

This project is built with Gradle. Use "gradle assemble" to build the project, "gradle install" to upload this library's JARs and AARs to your local maven repo.

Also includes a touch abstraction library (JMini3D Input) for Android and GWT.

JMini3D is used in 3D Mobialia games (http://www.mobialia.com) with Android and HTML5 versions like:
* Mobialia Chess: http://chess.mobialia.com http://www.mobialia.com/apps/chess
* Slot Racing: http://slot.mobialia.com http://www.mobialia.com/apps/slot
* Mobialia Four in a Row: http://fourinarow.mobialia.com http://www.mobialia.com/apps/fourinarow

It's released under the MIT License, so feel free to use it anywhere. 
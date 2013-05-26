JMini3D
=======

Minimalistic 3D library with support for Android and GWT (Google Web Toolkit, to create HTML5 apps developing in Java).

This library helps you to develop 3D apps compatible with Android and GWT projects.

Subprojects:
* *core:*" includes the common classes between Android and GWT projects 
* *android:* the android renderer (OpenGL ES 1.1 Renderer). Use "gradle uploadArchives" to upload this library's AAR to your local maven repo.
* *gwt:* the GWT library project implementing a WebGL Renderer (based on OpenGL ES 2)
* *gwt_demo:* a small demo project in GWT (we need more demos, sorry...)
* *utils:* includes an utility to generate Geometry files from OBJ models
* *gwtgl:* a dependency for the gwt project, do a 'gradle install' in this folder to copy the gwtgl artifact to your maven local repo

Also includes a touch abstraction library (Mini3d Input) for Android and GWT.

This library is used in games from Mobialia (http://www.mobialia.com) with Android and HTML5 versions like:
* Mobialia Chess: http://chess.mobialia.com http://www.mobialia.com/apps/chess
* Mobialia Four in a Row: http://fourinarow.mobialia.com http://www.mobialia.com/apps/fourinarow
* Slot Racing: http://slot.mobialia.com http://www.mobialia.com/apps/slot

It's released under the MIT License, so feel free to use it anywhere. 
package jmini3d.android;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jmini3d.Blending;
import jmini3d.MatrixUtils;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.android.compat.CompatibilityWrapper5;
import jmini3d.android.input.TouchController;
import jmini3d.input.TouchListener;

public class Renderer implements GLSurfaceView.Renderer {
	public static final String TAG = "Renderer";
	public static boolean needsRedraw = true;

	private GL10 gl;

	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 10000;
	private boolean logFps = false;
	private long frameCount = 0;
	private float fps = 0;
	private long timeLastSample;

	Scene scene;
	private ResourceLoader resourceLoader;
	private GpuUploader gpuUploader;
	private TouchController touchController;

	public float[] ortho = new float[16];

	boolean stop = false;

	Blending blending;
	Program currentProgram = null;

	int width;
	int height;

	public GLSurfaceView glSurfaceView;
	private ActivityManager activityManager;
	private ActivityManager.MemoryInfo memoryInfo;

	public Renderer(Context context, Scene scene, ResourceLoader resourceLoader, boolean traslucent) {
		this.scene = scene;
		this.resourceLoader = resourceLoader;

		activityManager = (ActivityManager) resourceLoader.getContext().getSystemService(Context.ACTIVITY_SERVICE);
		memoryInfo = new ActivityManager.MemoryInfo();

		MatrixUtils.ortho(ortho, 0, 1, 0, 1, -5, 1);

		gpuUploader = new GpuUploader(resourceLoader);

		glSurfaceView = new GLSurfaceView(context);
		glSurfaceView.setEGLContextClientVersion(2);

		// TODO
		//glSurfaceView.setPreserveEGLContextOnPause(true);

		if (traslucent) {
			if (Build.VERSION.SDK_INT >= 5) {
				CompatibilityWrapper5.setZOrderOnTop(glSurfaceView);
			}
			glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}

		glSurfaceView.setRenderer(this);
		glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
		Log.i(TAG, "onSurfaceCreated()");
		this.gl = gl;

		width = -1;
		height = -1;
	}

	public void onSurfaceChanged(GL10 unused, int w, int h) {
		Log.i(TAG, "onSurfaceChanged() w=" + w + " h= " + h);
		if (w != width || h != height) {
			setSize(w, h);
			width = w;
			height = h;
		}
	}

	public void onPause() {
		glSurfaceView.onPause();
	}

	public void onResume() {
		glSurfaceView.onResume();
	}

	public void setSize(int width, int height) {
		scene.camera.setWidth(width);
		scene.camera.setHeight(height);

		GLES20.glViewport(0, 0, scene.camera.getWidth(), scene.camera.getHeight());

		// Scene reload on size changed, needed to keep aspect ratios
		reset();
		gpuUploader.reset();
		scene.reset();
		scene.sceneController.initScene();

		needsRedraw = true;
	}

	public void reset() {
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glClearDepthf(1f);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthRangef(0, 1f);
		GLES20.glDepthMask(true);

		// For performance
		GLES20.glDisable(GLES20.GL_DITHER);

		// For transparency
		GLES20.glDisable(GLES20.GL_BLEND);
		blending = Blending.NoBlending;

		// CCW frontfaces only, by default
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
	}

	public void onDrawFrame(GL10 unused) {
		boolean sceneUpdated = scene.sceneController.updateScene();
		boolean cameraChanged = scene.camera.updateMatrices();

		if (!needsRedraw && !sceneUpdated && !cameraChanged) {
			return;
		}

		for (int i = 0; i < scene.unload.size(); i++) {
			gpuUploader.unload(scene.unload.get(i));
		}
		scene.unload.clear();

		needsRedraw = false;

		GLES20.glClearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		currentProgram = null;

		for (int i = 0; i < scene.children.size(); i++) {
			Object3d o3d = scene.children.get(i);
			if (o3d.visible) {
				o3d.updateMatrices();
				drawObject(o3d, scene.camera.perspectiveMatrix);

				if (o3d.clearDepthAfterDraw) {
					GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
				}
			}
		}

		for (int i = 0; i < scene.hud.size(); i++) {
			Object3d o3d = scene.hud.get(i);
			if (o3d.visible) {
				o3d.updateMatrices();
				drawObject(o3d, ortho);
			}
		}
		if (logFps) {
			doFps();
		}
	}

	private void drawObject(Object3d o3d, float[] perspectiveMatrix) {
		Program program = gpuUploader.getProgram(scene, o3d.material);

		if (program != currentProgram) {
			GLES20.glUseProgram(program.webGLProgram);
			program.setSceneUniforms(scene);
			currentProgram = program;
		}

		if (blending != o3d.material.blending) {
			setBlending(o3d.material.blending);
		}

		program.drawObject(gpuUploader, o3d, perspectiveMatrix);
	}

	private void setBlending(Blending blending) {
		this.blending = blending;

		switch (blending) {
			case NoBlending:
				GLES20.glDisable(GLES20.GL_BLEND);
				break;
			case NormalBlending:
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
				break;
			case AdditiveBlending:
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
				break;
			case SubtractiveBlending:
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_ZERO, GLES20.GL_ONE_MINUS_SRC_COLOR);
				break;
			case MultiplyBlending:
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_ZERO, GLES20.GL_SRC_COLOR);
				break;
		}
	}

	public void requestRender() {
		needsRedraw = true;
	}

	public void stop() {
		stop = true;
	}

	public GpuUploader getGpuUploader() {
		return gpuUploader;
	}

	/**
	 * If true, framerate and memory is periodically calculated and Log'ed, and
	 * gettable thru fps()
	 */
	public void setLogFps(boolean b) {
		logFps = b;

		if (logFps) { // init
			timeLastSample = System.currentTimeMillis();
			frameCount = 0;
		}
	}

	private void doFps() {
		frameCount++;

		long now = System.currentTimeMillis();
		long delta = now - timeLastSample;
		if (delta >= FRAMERATE_SAMPLEINTERVAL_MS) {
			fps = frameCount / (delta / 1000f);

			activityManager.getMemoryInfo(memoryInfo);
			Log.v(TAG, "FPS: " + fps + ", availMem: " + Math.round(memoryInfo.availMem / 1048576) + "MB");

			timeLastSample = now;
			frameCount = 0;
		}
	}

	/**
	 * Returns last sampled framerate (logFps must be set to true)
	 */
	public float getFps() {
		return fps;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setTouchListener(TouchListener listener) {
		if (touchController == null) {
			touchController = new TouchController(glSurfaceView);
		}
		touchController.setListener(listener);
	}

	public GLSurfaceView getView() {
		return glSurfaceView;
	}

	public GL10 getGl() {
		return gl;
	}
}
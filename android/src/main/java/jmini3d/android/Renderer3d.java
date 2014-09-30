package jmini3d.android;

import android.opengl.GLES20;
import android.util.Log;

import jmini3d.Blending;
import jmini3d.MatrixUtils;
import jmini3d.Object3d;
import jmini3d.Scene;

public class Renderer3d {
	public static final String TAG = "Renderer";

	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 10000;
	private boolean logFps = false;
	private long frameCount = 0;
	private float fps = 0;
	private long timeLastSample;

	private ResourceLoader resourceLoader;
	private GpuUploader gpuUploader;

	public float[] ortho = new float[16];

	Blending blending;
	Program currentProgram = null;
	Integer mapTextureId = -1;
	Integer envMapTextureId = -1;

	int width = -1;
	int height = -1;

	public Renderer3d(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
		gpuUploader = new GpuUploader(resourceLoader);
	}

	public void reset() {
		mapTextureId = -1;
		envMapTextureId = -1;
		gpuUploader.reset();

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

	public void render(Scene scene) {
		scene.camera.updateMatrices();

		if (width != scene.camera.getWidth() || height != scene.camera.getHeight()) {
			width = scene.camera.getWidth();
			height = scene.camera.getHeight();
			GLES20.glViewport(0, 0, width, height);
			MatrixUtils.ortho(ortho, 0, width, height, 0, -5, 1);
		}

		for (int i = 0; i < scene.unload.size(); i++) {
			gpuUploader.unload(scene.unload.get(i));
		}
		scene.unload.clear();

		GLES20.glClearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		currentProgram = null;

		for (int i = 0; i < scene.children.size(); i++) {
			Object3d o3d = scene.children.get(i);
			if (o3d.visible) {
				o3d.updateMatrices();
				drawObject(scene, o3d, scene.camera.perspectiveMatrix);

				if (o3d.clearDepthAfterDraw) {
					GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
				}
			}
		}

		for (int i = 0; i < scene.hud.size(); i++) {
			Object3d o3d = scene.hud.get(i);
			if (o3d.visible) {
				o3d.updateMatrices();
				drawObject(scene, o3d, ortho);
			}
		}
		if (logFps) {
			doFps();
		}
	}

	private void drawObject(Scene scene, Object3d o3d, float[] perspectiveMatrix) {
		Program program = gpuUploader.getProgram(scene, o3d.material);

		if (program != currentProgram) {
			GLES20.glUseProgram(program.webGLProgram);
			program.setSceneUniforms(scene);
			currentProgram = program;
		}

		if (blending != o3d.material.blending) {
			setBlending(o3d.material.blending);
		}

		program.drawObject(this, gpuUploader, o3d, perspectiveMatrix);
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

			Log.v(TAG, "FPS: " + fps);

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
}
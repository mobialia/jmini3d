package jmini3d.android;

import android.opengl.GLES20;

import jmini3d.Blending;
import jmini3d.Object3d;
import jmini3d.Scene;

public class Renderer3d implements jmini3d.Renderer3d {
	public static final String TAG = "Renderer";

	private ResourceLoader resourceLoader;
	private GpuUploader gpuUploader;

	Blending blending = null;
	Program currentProgram = null;
	Integer mapTextureId = -1;
	Integer envMapTextureId = -1;
	Integer normalMapTextureId = -1;
	int activeTexture = -1;

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
		setBlending(Blending.NoBlending);

		// CCW frontfaces only, by default
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
	}

	public void setViewPort(int width, int height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			GLES20.glViewport(0, 0, width, height);
		}
	}

	public void render(Scene scene) {
		scene.setViewPort(width, height);
		scene.camera.updateMatrices();
		render(scene, scene.camera.projectionMatrix, scene.camera.viewMatrix);
	}

	public void render(Scene scene, float[] projectionMatrix, float[] viewMatrix) {
		currentProgram = null;

		for (int i = 0; i < scene.unload.size(); i++) {
			gpuUploader.unload(scene.unload.get(i));
		}
		scene.unload.clear();

		if (scene.backgroundColor != null) {
			GLES20.glClearColor(scene.backgroundColor.r, scene.backgroundColor.g, scene.backgroundColor.b, scene.backgroundColor.a);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		} else {
			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		}

		for (int i = 0; i < scene.children.size(); i++) {
			Object3d o3d = scene.children.get(i);
			if (o3d.visible) {
				o3d.updateMatrices();
				drawObject(scene, o3d, projectionMatrix, viewMatrix);

				if (o3d.clearDepthAfterDraw) {
					GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
				}
			}
		}
	}

	private void drawObject(Scene scene, Object3d o3d, float[] projectionMatrix, float[] viewMatrix) {
		Program program = gpuUploader.getProgram(scene, o3d.material);

		if (program != currentProgram) {
			GLES20.glUseProgram(program.webGLProgram);
			program.setSceneUniforms(scene);
			currentProgram = program;
		}

		if (blending != o3d.material.blending) {
			setBlending(o3d.material.blending);
		}

		program.drawObject(this, gpuUploader, o3d, projectionMatrix, viewMatrix);
		for (int i = 0; i < o3d.getChildren().size(); i++) {
			drawObject(scene, o3d.getChildren().get(i), projectionMatrix, viewMatrix);
		}
	}

	private void setBlending(Blending blending) {
		if (this.blending == null || !this.blending.equals(blending)) {
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
	}

	public GpuUploader getGpuUploader() {
		return gpuUploader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
}
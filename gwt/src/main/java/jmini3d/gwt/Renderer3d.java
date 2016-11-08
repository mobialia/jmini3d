package jmini3d.gwt;

import com.googlecode.gwtgl.binding.WebGLRenderingContext;

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

	WebGLRenderingContext GLES20;

	public Renderer3d(WebGLRenderingContext gl, ResourceLoader resourceLoader, GpuUploaderListener gpuUploaderListener) {
		this.GLES20 = gl;
		this.resourceLoader = resourceLoader;
		gpuUploader = new GpuUploader(gl, resourceLoader, gpuUploaderListener);
		reset();
	}

	public void reset() {
		mapTextureId = null;
		envMapTextureId = null;
		gpuUploader.reset();

		GLES20.enable(WebGLRenderingContext.DEPTH_TEST);
		GLES20.clearDepth(1f);
		GLES20.depthFunc(WebGLRenderingContext.LEQUAL);
		GLES20.depthRange(0, 1f);
		GLES20.depthMask(true);

		// For performance
		GLES20.disable(WebGLRenderingContext.DITHER);

		// For transparency
		setBlending(Blending.NoBlending);

		// CCW frontfaces only, by default
		GLES20.frontFace(WebGLRenderingContext.CCW);
		GLES20.cullFace(WebGLRenderingContext.BACK);
		GLES20.enable(WebGLRenderingContext.CULL_FACE);
	}

	public void setViewPort(int width, int height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			GLES20.viewport(0, 0, width, height);
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
			GLES20.clearColor(scene.backgroundColor.r, scene.backgroundColor.g, scene.backgroundColor.b, scene.backgroundColor.a);
			GLES20.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);
		} else {
			GLES20.clear(WebGLRenderingContext.DEPTH_BUFFER_BIT);
		}

		for (int i = 0; i < scene.children.size(); i++) {
			Object3d o3d = scene.children.get(i);
			if (o3d.visible) {
				o3d.updateMatrices();
				drawObject(scene, o3d, projectionMatrix, viewMatrix);

				if (o3d.clearDepthAfterDraw) {
					GLES20.clear(WebGLRenderingContext.DEPTH_BUFFER_BIT);
				}
			}
		}
	}

	private void drawObject(Scene scene, Object3d o3d, float[] projectionMatrix, float[] viewMatrix) {
		Program program = gpuUploader.getProgram(scene, o3d.material);

		if (program != currentProgram) {
			GLES20.useProgram(program.webGLProgram);
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
					GLES20.disable(WebGLRenderingContext.BLEND);
					break;
				case NormalBlending:
					GLES20.enable(WebGLRenderingContext.BLEND);
					GLES20.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
					break;
				case AdditiveBlending:
					GLES20.enable(WebGLRenderingContext.BLEND);
					GLES20.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE);
					break;
				case SubtractiveBlending:
					GLES20.enable(WebGLRenderingContext.BLEND);
					GLES20.blendFunc(WebGLRenderingContext.ZERO, WebGLRenderingContext.ONE_MINUS_SRC_COLOR);
					break;
				case MultiplyBlending:
					GLES20.enable(WebGLRenderingContext.BLEND);
					GLES20.blendFunc(WebGLRenderingContext.ZERO, WebGLRenderingContext.SRC_COLOR);
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
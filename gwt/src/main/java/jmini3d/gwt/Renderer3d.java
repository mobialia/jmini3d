package jmini3d.gwt;

import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLTexture;

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
	WebGLTexture mapTextureId = null;
	WebGLTexture envMapTextureId = null;

	int width = -1;
	int height = -1;

	WebGLRenderingContext gl;

	public Renderer3d(WebGLRenderingContext gl, ResourceLoader resourceLoader, TextureLoadedListener textureLoadedListener) {
		this.gl = gl;
		this.resourceLoader = resourceLoader;
		MatrixUtils.ortho(ortho, 0, 1, 0, 1, -5, 1);
		gpuUploader = new GpuUploader(gl, resourceLoader, textureLoadedListener);
		reset();
	}

	public void reset() {
		mapTextureId = null;
		envMapTextureId = null;
		gpuUploader.reset();

		gl.enable(WebGLRenderingContext.DEPTH_TEST);
		gl.clearDepth(1f);
		gl.depthFunc(WebGLRenderingContext.LEQUAL);
		gl.depthRange(0, 1f);
		gl.depthMask(true);

		// For performance
		gl.disable(WebGLRenderingContext.DITHER);

		// For transparency
		gl.disable(WebGLRenderingContext.BLEND);
		blending = Blending.NoBlending;

		// CCW frontfaces only, by default
		gl.frontFace(WebGLRenderingContext.CCW);
		gl.cullFace(WebGLRenderingContext.BACK);
		gl.enable(WebGLRenderingContext.CULL_FACE);
	}

	public void render(Scene scene) {
		scene.camera.updateMatrices();

		if (width != scene.camera.getWidth() || height != scene.camera.getHeight()) {
			width = scene.camera.getWidth();
			height = scene.camera.getHeight();
			gl.viewport(0, 0, width, height);
			MatrixUtils.ortho(ortho, 0, width, height, 0, -5, 1);
		}

		for (int i = 0; i < scene.unload.size(); i++) {
			gpuUploader.unload(scene.unload.get(i));
		}
		scene.unload.clear();

		gl.clearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);

		currentProgram = null;

		for (int i = 0; i < scene.children.size(); i++) {
			Object3d o3d = scene.children.get(i);
			if (o3d.visible) {
				o3d.updateMatrices();
				drawObject(scene, o3d, scene.camera.perspectiveMatrix);

				if (o3d.clearDepthAfterDraw) {
					gl.clear(WebGLRenderingContext.DEPTH_BUFFER_BIT);
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
			gl.useProgram(program.webGLProgram);
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
				gl.disable(WebGLRenderingContext.BLEND);
				break;
			case NormalBlending:
				gl.enable(WebGLRenderingContext.BLEND);
				gl.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
				break;
			case AdditiveBlending:
				gl.enable(WebGLRenderingContext.BLEND);
				gl.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE);
				break;
			case SubtractiveBlending:
				gl.enable(WebGLRenderingContext.BLEND);
				gl.blendFunc(WebGLRenderingContext.ZERO, WebGLRenderingContext.ONE_MINUS_SRC_COLOR);
				break;
			case MultiplyBlending:
				gl.enable(WebGLRenderingContext.BLEND);
				gl.blendFunc(WebGLRenderingContext.ZERO, WebGLRenderingContext.SRC_COLOR);
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

			log("FPS: " + fps);

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

	native void log(String message) /*-{
		console.log(message);
    }-*/;
}
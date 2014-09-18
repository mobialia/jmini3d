package jmini3d.gwt;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusWidget;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;

import java.util.HashMap;

import jmini3d.Blending;
import jmini3d.MatrixUtils;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.gwt.input.TouchController;
import jmini3d.input.TouchListener;
import jmini3d.Material;

public class Renderer implements AnimationCallback {
	public static final String TAG = "Renderer";
	public static boolean needsRedraw = true;

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

	boolean stop = true;

	Blending blending;
	Program currentProgram = null;

	private FocusWidget webGLCanvas;
	private WebGLRenderingContext gl;


	public Renderer(ResourceLoader resourceLoader, Scene scene, int width, int height) {
		this.scene = scene;
		this.resourceLoader = resourceLoader;

		MatrixUtils.ortho(ortho, 0, 1, 0, 1, -5, 1);

		webGLCanvas = Canvas.createIfSupported();
		gl = (WebGLRenderingContext) ((Canvas) webGLCanvas).getContext("webgl");
		if (gl == null) {
			gl = (WebGLRenderingContext) ((Canvas) webGLCanvas).getContext("experimental-webgl");
		}
		if (gl == null) {
			Window.alert("Sorry, Your browser doesn't support WebGL. Please Install the last version of Firefox, Chrome or Internet Explorer (>=10).");
			return;
		}

		gpuUploader = new GpuUploader(gl, resourceLoader);

		setSize(width, height);
	}

	public void onResume() {
		if (stop) {
			stop = false;
			AnimationScheduler.get().requestAnimationFrame(this);
		}
	}

	public void onPause() {
		stop = true;
	}

	@Override
	public void execute(double timestamp) {
		if (!stop) {
			onDrawFrame();
			AnimationScheduler.get().requestAnimationFrame(this);
		}
	}

	public void setSize(int width, int height) {
		scene.camera.setWidth(width);
		scene.camera.setHeight(height);

		if (webGLCanvas instanceof Canvas) {
			((Canvas) webGLCanvas).setCoordinateSpaceWidth(width);
			((Canvas) webGLCanvas).setCoordinateSpaceHeight(height);
		}
		webGLCanvas.setWidth(width + "px");
		webGLCanvas.setHeight(height + "px");

		gl.viewport(0, 0, scene.camera.getWidth(), scene.camera.getHeight());

		// Scene reload on size changed, needed to keep aspect ratios
		gpuUploader.reset();
		scene.reset();
		scene.sceneController.initScene();
		reset();

		needsRedraw = true;
	}

	public void reset() {
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

	public void onDrawFrame() {
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

		gl.clearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);

		currentProgram = null;

		for (int i = 0; i < scene.children.size(); i++) {
			Object3d o3d = scene.children.get(i);
			if (o3d.visible) {
				o3d.updateMatrices();
				drawObject(o3d, scene.camera.perspectiveMatrix);

				if (o3d.clearDepthAfterDraw) {
					gl.clear(WebGLRenderingContext.DEPTH_BUFFER_BIT);
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
			gl.useProgram(program.webGLProgram);
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

	public void setTouchListener(TouchListener listener) {
		if (touchController == null) {
			touchController = new TouchController(webGLCanvas);
		}
		touchController.setListener(listener);
	}

	public FocusWidget getCanvas() {
		return webGLCanvas;
	}

	native void log(String message) /*-{
		console.log(message);
    }-*/;
}
package jmini3d.gwt;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.dom.client.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;

import jmini3d.ScreenController;

public class Canvas3d implements AnimationScheduler.AnimationCallback, TextureLoadedListener {

	Element webGLCanvas;
	WebGLRenderingContext gl;

	boolean stopped = false;
	int width, height;
	float scale = 1;

	public Renderer3d renderer3d;
	ScreenController screenController;
	boolean forceRedraw = false;

	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 10000;
	private boolean logFps = false;
	private long frameCount = 0;
	private float fps = 0;
	private long timeLastSample;

	public Canvas3d(String resourcePath, String shaderPath) {

		webGLCanvas = DOM.createElement("canvas");
		webGLCanvas.setAttribute("tabindex", "0"); // Workaround to receive key events

		gl = (WebGLRenderingContext) getContext(webGLCanvas, "webgl");
		if (gl == null) {
			gl = (WebGLRenderingContext) getContext(webGLCanvas, "experimental-webgl");
		}
		if (gl == null) {
			Window.alert("Sorry, Your browser doesn't support WebGL. Please install the last version of Firefox, Chrome, Safari, or Internet Explorer and check that WebGL is enabled.");
			return;
		}

		renderer3d = new Renderer3d(gl, new ResourceLoader(resourcePath, shaderPath), this);
	}

	public final native Context getContext(Element el, String contextId) /*-{
		return el.getContext(contextId);
 	}-*/;

	public final native float getDevicePixelRatio() /*-{
		 return window.devicePixelRatio || 1;
 	}-*/;

	public void setSize(int width, int height) {
		this.width = (int) (width * scale);
		this.height = (int) (height * scale);
		webGLCanvas.setAttribute("width", String.valueOf(this.width));
		webGLCanvas.setAttribute("height", String.valueOf(this.height));
		webGLCanvas.setAttribute("style", "width: " + width + "px; height: " + height + "px;");
		renderer3d.setViewPort(this.width, this.height);
		requestRender();
	}

	public void onResume() {
		stopped = false;
		AnimationScheduler.get().requestAnimationFrame(this);
	}

	public void onPause() {
		stopped = true;
	}

	@Override
	public void execute(double timestamp) {
		if (!stopped) {
			if (screenController != null) {
				if (screenController.onNewFrame(forceRedraw) || forceRedraw) {
					screenController.render(renderer3d);
					forceRedraw = false;
				}
			}

			AnimationScheduler.get().requestAnimationFrame(this);

			if (logFps) {
				doFps();
			}
		}
	}

	public void requestRender() {
		forceRedraw = true;
	}

	public void setScreenController(ScreenController screenController) {
		this.screenController = screenController;
	}

	public Renderer3d getRenderer3d() {
		return renderer3d;
	}

	public Element getElement() {
		return webGLCanvas;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public float getScale() {
		return scale;
	}

	@Override
	public void onTextureLoaded() {
		requestRender();
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

	public static native void log(String message) /*-{
		console.log(message);
    }-*/;
}

package jmini3d.gwt;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.dom.client.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;

import jmini3d.Scene;
import jmini3d.SceneController;

public class Canvas3d implements AnimationScheduler.AnimationCallback, TextureLoadedListener {

	Element webGLCanvas;
	WebGLRenderingContext gl;

	boolean stopped = false;
	int width, height;
	float scale = 1;

	public Renderer3d renderer3d;
	SceneController sceneController;
	boolean forceRedraw = false;

	public Canvas3d(String resourceLoaderDir) {

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

		renderer3d = new Renderer3d(gl, new ResourceLoader(resourceLoaderDir), this);
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
			if (sceneController != null) {
				if (sceneController.updateScene(width, height) || forceRedraw) {
					Scene scene = sceneController.getScene();
					if (scene != null) {
						forceRedraw = false;
						renderer3d.render(scene);
					}
				}
			}
			AnimationScheduler.get().requestAnimationFrame(this);
		}
	}

	public void requestRender() {
		forceRedraw = true;
	}

	public void setSceneController(SceneController sceneController) {
		this.sceneController = sceneController;
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
}

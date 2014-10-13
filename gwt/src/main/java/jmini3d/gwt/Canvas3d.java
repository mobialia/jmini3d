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

	public Renderer3d renderer3d;
	SceneController sceneController;
	boolean renderContinuously;

	public Canvas3d(String resourceLoaderDir, boolean renderContinuously) {
		this.renderContinuously = renderContinuously;

		webGLCanvas = DOM.createElement("canvas");

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

	public void setSize(int width, int heigth) {
		this.width = width;
		this.height = heigth;
		webGLCanvas.setAttribute("width", width + "px");
		webGLCanvas.setAttribute("height", heigth + "px");

		requestRender();
	}

	public void onResume() {
		stopped = false;
		if (renderContinuously) {
			AnimationScheduler.get().requestAnimationFrame(this);
		} else {
			requestRender();
		}
	}

	public void onPause() {
		stopped = true;
	}

	@Override
	public void execute(double timestamp) {
		if (!stopped && renderContinuously && sceneController != null) {
			Scene scene = sceneController.getScene(width, height);
			if (scene != null) {
				renderer3d.render(scene);
			}
			AnimationScheduler.get().requestAnimationFrame(this);
		}
	}

	public void requestRender() {
		if (renderContinuously || sceneController == null) {
			return;
		}
		Scene scene = sceneController.getScene(width, height);
		if (scene != null) {
			renderer3d.render(scene);
		}
	}

	public void setRenderContinuously(boolean renderContinuously) {
		if (this.renderContinuously != renderContinuously) {
			this.renderContinuously = renderContinuously;

			if (renderContinuously) {
				AnimationScheduler.get().requestAnimationFrame(this);
			}
		}
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

	@Override
	public void onTextureLoaded() {
		requestRender();
	}
}

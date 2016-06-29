package jmini3d;

import java.util.ArrayList;

import jmini3d.camera.Camera;
import jmini3d.camera.PerspectiveCamera;
import jmini3d.light.Light;
import jmini3d.shader.ShaderPlugin;

public class Scene {
	public int shaderKey = -1;
	public Camera camera;
	public ArrayList<Object3d> children = new ArrayList<>();
	public ArrayList<Light> lights = new ArrayList<>();
	public ArrayList<Object> unload = new ArrayList<>();
	public ShaderPlugin shaderPlugin;

	public Color4 backgroundColor;

	public int width = -1, height = -1;

	public Scene() {
		this(new PerspectiveCamera());
	}

	public Scene(Camera camera) {
		this.camera = camera;
		backgroundColor = new Color4(0, 0, 0, 255);
	}

	public Color4 getBackgroundColor() {
		return backgroundColor;
	}

	public void addLight(Light l) {
		lights.add(l);
		shaderKey = -1;
	}

	public void addChild(Object3d o) {
		if (children.contains(o)) {
			return;
		}
		children.add(o);
	}

	/**
	 * Having childs sorted by geometry saves calls to OpenGL
	 */
	public void addChildNearSameGeometry(Object3d o) {
		if (children.contains(o)) {
			return;
		}
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).geometry3d.equals(o.geometry3d)) {
				children.add(i, o);
				return;
			}
		}
		children.add(o);
	}

	public void removeChild(Object3d o) {
		if (children.contains(o)) {
			children.remove(o);
		}
	}

	public Camera getCamera() {
		return camera;
	}

	public void unload(Object obj) {
		unload.add(obj);
	}

	public void reset() {
		children.clear();
		unload.clear();
		lights.clear();
	}

	public void setViewPort(int width, int height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;

			camera.setWidth(width);
			camera.setHeight(height);
			onViewPortChanged(width, height);
		}
	}

	public void onViewPortChanged(int width, int height) {

	}

	public void setShaderPlugin(ShaderPlugin shaderPlugin) {
		this.shaderPlugin = shaderPlugin;
		shaderKey = -1;
	}
}
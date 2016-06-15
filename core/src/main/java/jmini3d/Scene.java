package jmini3d;

import java.util.ArrayList;

import jmini3d.shader.ShaderPlugin;
import jmini3d.light.Light;

public class Scene {
	public int shaderKey = -1;
	public Camera camera = new Camera();
	public ArrayList<Object3d> children = new ArrayList<>();
	public ArrayList<Object3d> hud = new ArrayList<>();
	public ArrayList<Light> lights = new ArrayList<>();
	public ArrayList<Object> unload = new ArrayList<>();
	public ArrayList<ShaderPlugin> shaderPlugins = new ArrayList<>();

	public Color4 backgroundColor = new Color4(0, 0, 0, 255);

	int width = -1, height = -1;

	public Color4 getBackgroundColor() {
		return backgroundColor;
	}

	public void addLight(Light l) {
		lights.add(l);
		shaderKey = -1;
	}

	public void addShaderPlugin(ShaderPlugin e) {
		shaderPlugins.add(e);
		shaderKey = -1;
	}

	public void addChild(Object3d o) {
		if (children.contains(o)) {
			return;
		}
		children.add(o);
	}

	public void removeChild(Object3d o) {
		if (children.contains(o)) {
			children.remove(o);
		}
	}

	public void addHudElement(Object3d o) {
		if (hud.contains(o)) {
			return;
		}
		hud.add(o);
	}

	public Camera getCamera() {
		return camera;
	}

	public void unload(Object obj) {
		unload.add(obj);
	}

	public void reset() {
		children.clear();
		hud.clear();
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
}
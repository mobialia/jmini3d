package jmini3d;

import java.util.ArrayList;

import jmini3d.light.Light;

public class Scene {
	public int shaderKey = -1;
	public SceneController sceneController;
	public Camera camera = new Camera();
	public ArrayList<Object3d> children = new ArrayList<Object3d>();
	public ArrayList<Object3d> hud = new ArrayList<Object3d>();
	public ArrayList<Light> lights = new ArrayList<Light>();

	public ArrayList<Object> unload = new ArrayList<Object>();

	public Color4 backgroundColor = new Color4(0f, 0f, 0f, 1f);

	public Scene(SceneController sceneController) {
		this.sceneController = sceneController;
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
}
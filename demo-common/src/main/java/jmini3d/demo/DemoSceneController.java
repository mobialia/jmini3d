package jmini3d.demo;

import java.util.HashMap;

import jmini3d.Scene;
import jmini3d.SceneController;
import jmini3d.Vector3;
import jmini3d.input.KeyListener;
import jmini3d.input.TouchListener;
import jmini3d.input.TouchPointer;

public class DemoSceneController implements SceneController, TouchListener, KeyListener {
	float cameraAngle;
	long initialTime;

	int sceneIndex = 0;
	Scene scenes[] = {new TeapotScene(), new CubeScene(), new EnvMapCubeScene(), new CubesScene(), new NormalMapScene(), new ChildObjectScene()};
	int cameraModes[] = {0, 0, 0, 0, 1, 0};

	public DemoSceneController() {
		initialTime = System.currentTimeMillis();
	}

	@Override
	public Scene getScene() {
		return scenes[sceneIndex];
	}

	@Override
	public boolean updateScene(int width, int height) {
		scenes[sceneIndex].setViewPort(width, height);

		// Rotate camera...
		cameraAngle = 0.0005f * (System.currentTimeMillis() - initialTime);

		float d = 5;
		Vector3 target = scenes[sceneIndex].getCamera().getTarget();
		scenes[sceneIndex].getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
				(float) (target.y - d * Math.sin(cameraAngle)), //
				target.z + (cameraModes[sceneIndex] == 0 ? (float) (d * Math.sin(cameraAngle)) : d / 2)//
		);
		((ParentScene) scenes[sceneIndex]).update();

		return true;
	}

	private void nextScene() {
		if (sceneIndex >= scenes.length - 1) {
			sceneIndex = 0;
		} else {
			sceneIndex++;
		}
	}

	private void previousScene() {
		if (sceneIndex <= 0) {
			sceneIndex = scenes.length - 1;
		} else {
			sceneIndex--;
		}
	}

	@Override
	public boolean onTouch(HashMap<Integer, TouchPointer> pointers) {
		for (Integer key : pointers.keySet()) {
			TouchPointer pointer = pointers.get(key);
			if (pointer.status == TouchPointer.TOUCH_DOWN) {
				if (pointer.x > scenes[sceneIndex].getCamera().getWidth() / 2) {
					nextScene();
				} else {
					previousScene();
				}
			}
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int key) {
		switch (key) {
			case KeyListener.KEY_RIGHT:
				nextScene();
				return true;
			case KeyListener.KEY_LEFT:
				previousScene();
				return true;
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int key) {
		return false;
	}
}

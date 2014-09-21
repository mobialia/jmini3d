package jmini3d;

public interface SceneController {

	/**
	 * Called at FPS to update and return the scene
	 */
	public Scene getScene(int width, int height);

}
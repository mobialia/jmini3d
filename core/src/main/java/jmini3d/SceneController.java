package jmini3d;

public interface SceneController {

	/**
	 * if updateScene() returns true gets the scene to draw
	 */
	public Scene getScene();

	/**
	 * Called at FPS to update and return the scene
	 */
	public boolean updateScene(int width, int height);

}
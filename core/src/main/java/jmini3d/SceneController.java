package jmini3d;

public interface SceneController {

	/**
	 * If updateScene() returns true, this method is called to get the scene to draw
	 */
	public Scene getScene();

	/**
	 * Called at FPS to update and return the scene
	 */
	public boolean updateScene(int width, int height);

}
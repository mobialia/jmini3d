package jmini3d;

public interface SceneController {
	public void initScene();

	/**
	 * Returns true if something has changed, called at FPS
	 */
	public boolean updateScene();

}
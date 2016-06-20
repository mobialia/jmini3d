package jmini3d;

public interface ScreenController {

	/**
	 * Called at FPS to update the scenes
	 *
	 * @param forceRedraw if true , the scene will be always redrawn
	 * @returns true if the scene changed and a render must be done done (when any scene changed)
	 */
	public boolean onNewFrame(boolean forceRedraw);

	/**
	 * Called when the scenes must be rendered
	 */
	public void render(Renderer3d renderer3d);

}
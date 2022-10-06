package jmini3d;

public interface Renderer3d {

	void render(Scene scene);

	void render(Scene scene, float[] projectionMatrix, float[] viewMatrix);

}

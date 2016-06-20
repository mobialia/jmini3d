package jmini3d.camera;

import java.util.Arrays;

import jmini3d.MatrixUtils;
import jmini3d.Vector3;

/**
 * Gets a camera ray (a vector from the camera) from the screen coordinates
 */
public class CameraRay {

	float[] cachedProjectionMatrix = new float[16];
	float[] cachedViewMatrix = new float[16];

	float[] cameraMatrix = new float[16];
	float[] invertMatrix = new float[16];
	float[] in = new float[4];
	float[] out = new float[4];

	public void getRay(Camera camera, int x, int y, Vector3 ray) {
		gluUnProject(x, camera.getHeight() - y, 0.9f, camera, ray);

		// Ray vector
		ray.x -= camera.getPosition().x;
		ray.y -= camera.getPosition().y;
		ray.z -= camera.getPosition().z;
	}

	public boolean gluUnProject(float winx, float winy, float winz, Camera camera, Vector3 outCoord) {

		// Cache the inverted matrix
		if (!Arrays.equals(camera.projectionMatrix, cachedProjectionMatrix)
				|| !Arrays.equals(camera.viewMatrix, cachedViewMatrix)) {
			MatrixUtils.copyMatrix(camera.projectionMatrix, cachedProjectionMatrix);
			MatrixUtils.copyMatrix(camera.viewMatrix, cachedViewMatrix);
			MatrixUtils.multiply(cachedProjectionMatrix, cachedViewMatrix, cameraMatrix);
			if (!MatrixUtils.invert(cameraMatrix, invertMatrix)) {
				return false;
			}
		}

		// Map x and y to 0..1 and to -1..1
		in[0] = (winx / camera.width) * 2 - 1;
		in[1] = (winy / camera.height) * 2 - 1;
		in[2] = winz * 2 - 1;
		in[3] = 1.0f;

		MatrixUtils.multiplyVector(invertMatrix, in, out);
		if (out[3] == 0) {
			return false;
		}
		out[0] /= out[3];
		out[1] /= out[3];
		out[2] /= out[3];

		outCoord.setAll(out[0], out[1], out[2]);
		return true;
	}
}
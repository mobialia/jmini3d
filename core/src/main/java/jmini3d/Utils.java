package jmini3d;

public class Utils {
	public static float PI = (float) Math.PI;

	public static void getRay(Camera camera, int x, int y, Vector3 ray) {
		int[] viewport = {0, 0, camera.getWidth(), camera.getHeight()};

		gluUnProject(x, camera.getHeight() - y, 0.9f, camera.perspectiveMatrix, viewport, ray);

		// Ray vector
		ray.x -= camera.getPosition().x;
		ray.y -= camera.getPosition().y;
		ray.z -= camera.getPosition().z;
	}

	public static boolean gluUnProject(float winx, float winy, float winz, final float[] perspectiveMatrix,
									   final int[] viewport, Vector3 outCoord) {
		float[] finalMatrix = new float[16];
		float[] in = new float[4];
		float[] out = new float[4];

		// TODO cache
		if (!MatrixUtils.invert(perspectiveMatrix, finalMatrix)) {
			return false;
		}

		in[0] = winx;
		in[1] = winy;
		in[2] = winz;
		in[3] = 1.0f;

		// Map x and y to 0..1
		in[0] = (in[0] - viewport[0]) / viewport[2];
		in[1] = (in[1] - viewport[1]) / viewport[3];

		// Map to range -1..1
		in[0] = in[0] * 2 - 1;
		in[1] = in[1] * 2 - 1;
		in[2] = in[2] * 2 - 1;

		MatrixUtils.multiplyVector(finalMatrix, in, out);
		if (out[3] == 0) {
			return false;
		}
		out[0] /= out[3];
		out[1] /= out[3];
		out[2] /= out[3];

		outCoord.setAll(out[0], out[1], out[2]);
		return true;
	}

	public static float p256(float n) {
		return (2f * n - 1f) / (2f * 256f);
	}

	public static float p512(float n) {
		return (2f * n - 1f) / (2f * 512f);
	}

	public static float p1024(float n) {
		return (2f * n - 1f) / (2f * 1024f);
	}

	public static float p2048(float n) {
		return (2f * n - 1f) / (2f * 2048f);
	}

	public static float p4096(float n) {
		return (2f * n - 1f) / (2f * 4096f);
	}

}

package jmini3d;

public class Utils {
	public static float PI = (float) Math.PI;

	/**
	 * Calculates if a line crosses a regular quad defined by 3 points in the space avoiding
	 * object creations
	 */
	public static boolean lineIntersectsQuad(Vector3 l0, Vector3 l, Vector3 a, Vector3 b, Vector3 c) {
		// First, compute the line-plane intersection https://en.wikipedia.org/wiki/Line%E2%80%93plane_intersection
		float abx = b.x - a.x;
		float aby = b.y - a.y;
		float abz = b.z - a.z;

		float acx = c.x - a.x;
		float acy = c.y - a.y;
		float acz = c.z - a.z;

		// Normal to the plane
		float nx = (acy * abz) - (acz * aby);
		float ny = (acz * abx) - (acx * abz);
		float nz = (acx * aby) - (acy * abx);

		// Dot product
		float ln = l.x * nx + l.y * ny + l.z * nz;

		if (ln == 0) {
			// It do no crosses the plane, it may be contained in the plane, but we return false
			return false;
		}
		float al0x = a.x - l0.x;
		float al0y = a.y - l0.y;
		float al0z = a.z - l0.z;
		float al0n = al0x * nx + al0y * ny + al0z * nz;

		float d = al0n / ln;

		// Intersection point
		float ix = l0.x + d * l.x;
		float iy = l0.y + d * l.y;
		float iz = l0.z + d * l.z;

		// Now that we have the intersection point, compute if it is inside the QUAD checking that
		// the dot product between ab * ai and ac * ai is between 0 and each vector length^2
		float aix = ix - a.x;
		float aiy = iy - a.y;
		float aiz = iz - a.z;

		float dot1 = abx * aix + aby * aiy + abz * aiz;
		float dot2 = acx * aix + acy * aiy + acz * aiz;

		float ablength = (float) Math.sqrt(abx * abx + aby * aby + abz * abz);
		float aclength = (float) Math.sqrt(abx * abx + aby * aby + abz * abz);

		return 0 <= dot1 && dot1 <= ablength * ablength &&
				0 <= dot2 && dot2 <= aclength * aclength;
	}

	public static float p(float n, float x) {
		return (2f * n - 1f) / (2f * x);
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

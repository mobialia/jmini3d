package jmini3d;

public class MatrixUtils {

	public static float[] IDENTITY4 = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1,};

	/**
	 * This is the old gl.glFustrum
	 */
	public static void perspective(float destination[], float fovy, float aspectRatio, float near, float far) {
		float top = near * (float) Math.tan(fovy * Math.PI / 360.0);
		float bottom = -top;
		float right = top * aspectRatio;
		float left = -right;

		float a = (right + left) / (right - left);
		float b = (top + bottom) / (top - bottom);
		float c = -(far + near) / (far - near);
		float d = -2 * far * near / (far - near);
		float x = 2 * near / (right - left);
		float y = 2 * near / (top - bottom);

		destination[0] = x;
		destination[1] = 0;
		destination[2] = a;
		destination[3] = 0;

		destination[4] = 0;
		destination[5] = y;
		destination[6] = b;
		destination[7] = 0;

		destination[8] = 0;
		destination[9] = 0;
		destination[10] = c;
		destination[11] = -1;

		destination[12] = 0;
		destination[13] = 0;
		destination[14] = d;
		destination[15] = 0;
	}

	/**
	 * GLU.glLookAt vx, vy, vz are auxiliars to avoid allocations
	 */
	public static void lookAt(float destination[], Vector3 eye, Vector3 target, Vector3 up, Vector3 vx, Vector3 vy, Vector3 vz) {
		vz.setAllFrom(eye);
		vz.subtract(target);
		vz.normalize();

		Vector3.cross(vx, vz, up);
		vx.normalize();
		Vector3.cross(vy, vx, vz);

		destination[0] = vx.x;
		destination[1] = vy.x;
		destination[2] = vz.x;
		destination[3] = 0;

		destination[4] = vx.y;
		destination[5] = vy.y;
		destination[6] = vz.y;
		destination[7] = 0;

		destination[8] = vx.z;
		destination[9] = vy.z;
		destination[10] = vz.z;
		destination[11] = 0;

		destination[12] = -Vector3.dot(vx, eye);
		destination[13] = -Vector3.dot(vy, eye);
		destination[14] = -Vector3.dot(vz, eye);
		destination[15] = 1;
	}

	public static void ortho(float matrix[], float left, float right, float bottom, float top, float near, float far) {
		float r_l = right - left;
		float t_b = top - bottom;
		float f_n = far - near;
		float tx = -(right + left) / (right - left);
		float ty = -(top + bottom) / (top - bottom);
		float tz = -(far + near) / (far - near);

		matrix[0] = 2.0f / r_l;
		matrix[1] = 0.0f;
		matrix[2] = 0.0f;
		matrix[3] = 0;

		matrix[4] = 0.0f;
		matrix[5] = 2.0f / t_b;
		matrix[6] = 0.0f;
		matrix[7] = 0;

		matrix[8] = 0.0f;
		matrix[9] = 0.0f;
		matrix[10] = 2.0f / f_n;
		matrix[11] = 0;

		matrix[12] = tx;
		matrix[13] = ty;
		matrix[14] = tz;
		matrix[15] = 1.0f;
	}

	public static void translate(float[] destination, Vector3 pos) {
		destination[0] = 1;
		destination[1] = 0;
		destination[2] = 0;
		destination[3] = 0;

		destination[4] = 0;
		destination[5] = 1;
		destination[6] = 0;
		destination[7] = 0;

		destination[8] = 0;
		destination[9] = 0;
		destination[10] = 1;
		destination[11] = 0;

		destination[12] = pos.x;
		destination[13] = pos.y;
		destination[14] = pos.z;
		destination[15] = 1;
	}

	public static void rotate(float[] destination, Vector3 direction, Vector3 up, Vector3 side) {
		destination[0] = side.x;
		destination[1] = side.y;
		destination[2] = side.z;
		destination[3] = 0;
		destination[4] = direction.x;
		destination[5] = direction.y;
		destination[6] = direction.z;
		destination[7] = 0;
		destination[8] = up.x;
		destination[9] = up.y;
		destination[10] = up.z;
		destination[11] = 0;
		destination[12] = 0;
		destination[13] = 0;
		destination[14] = 0;
		destination[15] = 1;
	}

	public static void getRotation(float[] source, Vector3 direction, Vector3 up, Vector3 side) {
		side.x = source[0];
		side.y = source[1];
		side.z = source[2];
		direction.x = source[4];
		direction.y = source[5];
		direction.z = source[6];
		up.x = source[8];
		up.y = source[9];
		up.z = source[10];
	}

	public static float[] cloneMatrix(float[] in) {
		return new float[]{ //
				in[0], in[1], in[2], in[3], //
				in[4], in[5], in[6], in[7], //
				in[8], in[9], in[10], in[11], //
				in[12], in[13], in[14], in[15]};
	}

	public static void copyMatrix(float[] origin, float destination[]) {
		System.arraycopy(origin, 0, destination, 0, origin.length);
	}

	public static float[] toInverseMat3(float[] mat, float[] dest) {

		// Cache the matrix values (makes for huge speed increases!)
		float a00 = mat[0], a01 = mat[1], a02 = mat[2];
		float a10 = mat[4], a11 = mat[5], a12 = mat[6];
		float a20 = mat[8], a21 = mat[9], a22 = mat[10];

		float b01 = a22 * a11 - a12 * a21;
		float b11 = -a22 * a10 + a12 * a20;
		float b21 = a21 * a10 - a11 * a20;

		float d = a00 * b01 + a01 * b11 + a02 * b21;
		if (d == 0) {
			return null;
		}
		float id = 1 / d;

		if (dest == null) {
			dest = new float[9];
		}

		dest[0] = b01 * id;
		dest[1] = (-a22 * a01 + a02 * a21) * id;
		dest[2] = (a12 * a01 - a02 * a11) * id;
		dest[3] = b11 * id;
		dest[4] = (a22 * a00 - a02 * a20) * id;
		dest[5] = (-a12 * a00 + a02 * a10) * id;
		dest[6] = b21 * id;
		dest[7] = (-a21 * a00 + a01 * a20) * id;
		dest[8] = (a11 * a00 - a01 * a10) * id;

		return dest;
	}

	public static boolean invert(float[] m, float[] invOut) {
		float[] inv = new float[16];
		float det;
		int i;
		inv[0] = m[5] * m[10] * m[15] - m[5] * m[11] * m[14] - m[9] * m[6] * m[15] + m[9] * m[7] * m[14] + m[13] * m[6] * m[11] - m[13] * m[7] * m[10];
		inv[4] = -m[4] * m[10] * m[15] + m[4] * m[11] * m[14] + m[8] * m[6] * m[15] - m[8] * m[7] * m[14] - m[12] * m[6] * m[11] + m[12] * m[7] * m[10];
		inv[8] = m[4] * m[9] * m[15] - m[4] * m[11] * m[13] - m[8] * m[5] * m[15] + m[8] * m[7] * m[13] + m[12] * m[5] * m[11] - m[12] * m[7] * m[9];
		inv[12] = -m[4] * m[9] * m[14] + m[4] * m[10] * m[13] + m[8] * m[5] * m[14] - m[8] * m[6] * m[13] - m[12] * m[5] * m[10] + m[12] * m[6] * m[9];
		inv[1] = -m[1] * m[10] * m[15] + m[1] * m[11] * m[14] + m[9] * m[2] * m[15] - m[9] * m[3] * m[14] - m[13] * m[2] * m[11] + m[13] * m[3] * m[10];
		inv[5] = m[0] * m[10] * m[15] - m[0] * m[11] * m[14] - m[8] * m[2] * m[15] + m[8] * m[3] * m[14] + m[12] * m[2] * m[11] - m[12] * m[3] * m[10];
		inv[9] = -m[0] * m[9] * m[15] + m[0] * m[11] * m[13] + m[8] * m[1] * m[15] - m[8] * m[3] * m[13] - m[12] * m[1] * m[11] + m[12] * m[3] * m[9];
		inv[13] = m[0] * m[9] * m[14] - m[0] * m[10] * m[13] - m[8] * m[1] * m[14] + m[8] * m[2] * m[13] + m[12] * m[1] * m[10] - m[12] * m[2] * m[9];
		inv[2] = m[1] * m[6] * m[15] - m[1] * m[7] * m[14] - m[5] * m[2] * m[15] + m[5] * m[3] * m[14] + m[13] * m[2] * m[7] - m[13] * m[3] * m[6];
		inv[6] = -m[0] * m[6] * m[15] + m[0] * m[7] * m[14] + m[4] * m[2] * m[15] - m[4] * m[3] * m[14] - m[12] * m[2] * m[7] + m[12] * m[3] * m[6];
		inv[10] = m[0] * m[5] * m[15] - m[0] * m[7] * m[13] - m[4] * m[1] * m[15] + m[4] * m[3] * m[13] + m[12] * m[1] * m[7] - m[12] * m[3] * m[5];
		inv[14] = -m[0] * m[5] * m[14] + m[0] * m[6] * m[13] + m[4] * m[1] * m[14] - m[4] * m[2] * m[13] - m[12] * m[1] * m[6] + m[12] * m[2] * m[5];
		inv[3] = -m[1] * m[6] * m[11] + m[1] * m[7] * m[10] + m[5] * m[2] * m[11] - m[5] * m[3] * m[10] - m[9] * m[2] * m[7] + m[9] * m[3] * m[6];
		inv[7] = m[0] * m[6] * m[11] - m[0] * m[7] * m[10] - m[4] * m[2] * m[11] + m[4] * m[3] * m[10] + m[8] * m[2] * m[7] - m[8] * m[3] * m[6];
		inv[11] = -m[0] * m[5] * m[11] + m[0] * m[7] * m[9] + m[4] * m[1] * m[11] - m[4] * m[3] * m[9] - m[8] * m[1] * m[7] + m[8] * m[3] * m[5];
		inv[15] = m[0] * m[5] * m[10] - m[0] * m[6] * m[9] - m[4] * m[1] * m[10] + m[4] * m[2] * m[9] + m[8] * m[1] * m[6] - m[8] * m[2] * m[5];

		det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];

		if (det == 0) {
			return false;
		}

		det = 1.0f / det;

		for (i = 0; i < 16; i++) {
			invOut[i] = inv[i] * det;
		}

		return true;
	}

	public static float[] transpose(float[] mat, float[] dest) {
		// If we are transposing ourselves we can skip a few steps but have to
		// cache some values
		if (dest == null || mat == dest) {
			float a0 = mat[1], a2 = mat[2], a5 = mat[5];
			mat[1] = mat[3];
			mat[2] = mat[6];
			mat[3] = a0;
			mat[5] = mat[7];
			mat[6] = a2;
			mat[7] = a5;
			return mat;
		}

		dest[0] = mat[0];
		dest[1] = mat[3];
		dest[2] = mat[6];
		dest[3] = mat[1];
		dest[4] = mat[4];
		dest[5] = mat[7];
		dest[6] = mat[2];
		dest[7] = mat[5];
		dest[8] = mat[8];
		return dest;
	}

	public static float[] multiply(float[] mat, float[] mat2, float[] dest) {
		if (dest == null) {
			dest = mat;
		}

		// Cache the matrix values (makes for huge speed increases!)
		float a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
		float a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
		float a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
		float a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];

		// Cache only the current line of the second matrix
		float b0 = mat2[0], b1 = mat2[1], b2 = mat2[2], b3 = mat2[3];
		dest[0] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30;
		dest[1] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31;
		dest[2] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32;
		dest[3] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33;

		b0 = mat2[4];
		b1 = mat2[5];
		b2 = mat2[6];
		b3 = mat2[7];
		dest[4] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30;
		dest[5] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31;
		dest[6] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32;
		dest[7] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33;

		b0 = mat2[8];
		b1 = mat2[9];
		b2 = mat2[10];
		b3 = mat2[11];
		dest[8] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30;
		dest[9] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31;
		dest[10] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32;
		dest[11] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33;

		b0 = mat2[12];
		b1 = mat2[13];
		b2 = mat2[14];
		b3 = mat2[15];
		dest[12] = b0 * a00 + b1 * a10 + b2 * a20 + b3 * a30;
		dest[13] = b0 * a01 + b1 * a11 + b2 * a21 + b3 * a31;
		dest[14] = b0 * a02 + b1 * a12 + b2 * a22 + b3 * a32;
		dest[15] = b0 * a03 + b1 * a13 + b2 * a23 + b3 * a33;

		return dest;
	}

	public static float[] multiplyVectorPost(float[] mat, float[] vec, float[] dest) {
		if (dest == null) {
			dest = vec;
		}

		float vec0 = vec[0];
		float vec1 = vec[1];
		float vec2 = vec[2];
		float vec3 = vec[3];

		dest[0] = mat[0] * vec0 + mat[1] * vec1 + mat[2] * vec2 + mat[3] * vec3;
		dest[1] = mat[4] * vec0 + mat[5] * vec1 + mat[6] * vec2 + mat[7] * vec3;
		dest[2] = mat[8] * vec0 + mat[9] * vec1 + mat[10] * vec2 + mat[11] * vec3;
		dest[3] = mat[12] * vec0 + mat[13] * vec1 + mat[14] * vec2 + mat[15] * vec3;

		return dest;
	}

	public static float[] multiplyVector(float[] mat, float[] vec, float[] dest) {
		if (dest == null) {
			dest = vec;
		}

		float vec0 = vec[0];
		float vec1 = vec[1];
		float vec2 = vec[2];
		float vec3 = vec[3];

		dest[0] = mat[0] * vec0 + mat[4] * vec1 + mat[8] * vec2 + mat[12] * vec3;
		dest[1] = mat[1] * vec0 + mat[5] * vec1 + mat[9] * vec2 + mat[13] * vec3;
		dest[2] = mat[2] * vec0 + mat[6] * vec1 + mat[10] * vec2 + mat[14] * vec3;
		dest[3] = mat[3] * vec0 + mat[7] * vec1 + mat[11] * vec2 + mat[15] * vec3;

		return dest;
	}
}
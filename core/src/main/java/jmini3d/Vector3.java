package jmini3d;

public class Vector3 {
	public float x;
	public float y;
	public float z;

	private static Vector3 tmp = new Vector3();

	public Vector3() {
		x = 0;
		y = 0;
		z = 0;
	}

	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3(Vector3 n) {
		x = n.x;
		y = n.y;
		z = n.z;
	}

	public void setAll(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setAll(float[] array, int offset) {
		x = array[offset];
		y = array[offset + 1];
		z = array[offset + 2];
	}

	public void setAllFrom(Vector3 n) {
		x = n.x;
		y = n.y;
		z = n.z;
	}

	public void normalize() {
		float mod = (float) Math.sqrt(x * x + y * y + z * z);

		if (mod != 0 && mod != 1) {
			mod = 1 / mod;
			x *= mod;
			y *= mod;
			z *= mod;
		}
	}

	public void add(Vector3 n) {
		x += n.x;
		y += n.y;
		z += n.z;
	}

	public void subtract(Vector3 n) {
		x -= n.x;
		y -= n.y;
		z -= n.z;
	}

	public void multiply(float f) {
		x *= f;
		y *= f;
		z *= f;
	}

	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	public Vector3 clone() {
		return new Vector3(x, y, z);
	}

	public void rotateX(float angle) {
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		tmp.setAll(x, y, z);

		y = (tmp.y * cosRY) - (tmp.z * sinRY);
		z = (tmp.y * sinRY) + (tmp.z * cosRY);
	}

	public void rotateY(float angle) {
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		tmp.setAll(x, y, z);

		x = (tmp.x * cosRY) + (tmp.z * sinRY);
		z = (tmp.x * -sinRY) + (tmp.z * cosRY);
	}

	public void rotateZ(float angle) {
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		tmp.setAll(x, y, z);

		x = (tmp.x * cosRY) - (tmp.y * sinRY);
		y = (tmp.x * sinRY) + (tmp.y * cosRY);
	}

	public void rotateAxis(Vector3 a, float angle) {
		float c = (float) Math.cos(angle);
		float s = (float) Math.sin(angle);
		float t = 1 - (float) Math.cos(angle);

		float nx = x * (t * a.x * a.x + c) + y * (t * a.x * a.y - s * a.z) + z * (t * a.x * a.z + s * a.y);
		float ny = x * (t * a.x * a.y + s * a.z) + y * (t * a.y * a.y + c) + z * (t * a.y * a.z - s * a.x);
		float nz = x * (t * a.x * a.z - s * a.y) + y * (t * a.y * a.z + s * a.x) + z * (t * a.z * a.z + c);

		x = nx;
		y = ny;
		z = nz;
	}

	public static Vector3 add(Vector3 a, Vector3 b) {
		return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public static Vector3 subtract(Vector3 a, Vector3 b) {
		return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static Vector3 multiply(Vector3 a, Vector3 b) {
		return new Vector3(a.x * b.x, a.y * b.y, a.z * b.z);
	}

	public static Vector3 cross(Vector3 v, Vector3 w) {
		return new Vector3((w.y * v.z) - (w.z * v.y), (w.z * v.x) - (w.x * v.z), (w.x * v.y) - (w.y * v.x));
	}

	public static void cross(Vector3 result, Vector3 v, Vector3 w) {
		result.setAll((w.y * v.z) - (w.z * v.y), (w.z * v.x) - (w.x * v.z), (w.x * v.y) - (w.y * v.x));
	}

	public static float dot(Vector3 v, Vector3 w) {
		return (v.x * w.x + v.y * w.y + v.z * w.z);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Vector3)) {
			return false;
		}
		return x == (((Vector3) o).x) && (y == ((Vector3) o).y) && (z == ((Vector3) o).z);
	}

	@Override
	public String toString() {
		return x + "," + y + "," + z;
	}
}

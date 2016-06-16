package jmini3d;

public class Camera {
	public Vector3 position = new Vector3();
	public Vector3 target = new Vector3();
	public Vector3 upAxis = new Vector3();

	private Vector3 vx = new Vector3();
	private Vector3 vy = new Vector3();
	private Vector3 vz = new Vector3();

	private float fovy = 45;
	private int width = 0;
	private int height = 0;
	private float near = 1;
	private float far = 1000;

	public float[] projectionMatrix = new float[16];
	public float[] viewMatrix = new float[16];

	public boolean needsMatrixUpdate = true;

	public Camera() {
		setTarget(0, 0, 0);
		setUpAxis(0, 0, 1);
	}

	public void setPosition(float x, float y, float z) {
		if (this.position.x != x || this.position.y != y || this.position.z != z) {
			position.setAll(x, y, z);
			needsMatrixUpdate = true;
		}
	}

	public void setTarget(float x, float y, float z) {
		if (this.target.x != x || this.target.y != y || this.target.z != z) {
			target.setAll(x, y, z);
			needsMatrixUpdate = true;
		}
	}

	public void setUpAxis(float x, float y, float z) {
		if (this.upAxis.x != x || this.upAxis.y != y || this.upAxis.z != z) {
			upAxis.setAll(x, y, z);
			needsMatrixUpdate = true;
		}
	}

	public void setFovy(float fovy) {
		if (this.fovy != fovy) {
			this.fovy = fovy;
			needsMatrixUpdate = true;
		}
	}

	public void setNear(float near) {
		if (this.near != near) {
			this.near = near;
			needsMatrixUpdate = true;
		}
	}

	public void setFar(float far) {
		if (this.far != far) {
			this.far = far;
			needsMatrixUpdate = true;
		}
	}

	public void setWidth(int width) {
		if (this.width != width) {
			this.width = width;
			needsMatrixUpdate = true;
		}
	}

	public void setHeight(int height) {
		if (this.height != height) {
			this.height = height;
			needsMatrixUpdate = true;
		}
	}

	public Vector3 getPosition() {
		return position;
	}

	public Vector3 getTarget() {
		return target;
	}

	public Vector3 getUpAxis() {
		return upAxis;
	}

	public float getFovy() {
		return fovy;
	}

	public float getFovx() {
		return (float) (360 / Utils.PI * Math.atan(Math.tan(fovy * Utils.PI / 360) * width / height));
	}

	public float getNear() {
		return near;
	}

	public float getFar() {
		return far;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean updateMatrices() {
		if (needsMatrixUpdate) {
			MatrixUtils.perspective(projectionMatrix, fovy, ((float) width) / height, near, far);
			MatrixUtils.lookAt(viewMatrix, position, target, upAxis, vx, vy, vz);

			needsMatrixUpdate = false;
			return true;
		}
		return false;
	}

	public void updateViewMatrix() {
		MatrixUtils.lookAt(viewMatrix, position, target, upAxis, vx, vy, vz);
	}
}
package jmini3d.camera;

import jmini3d.Vector3;

public abstract class Camera {

	public int width = 0;
	public int height = 0;

	public Vector3 position = new Vector3();
	public Vector3 target = new Vector3();
	public Vector3 upAxis = new Vector3();

	public float[] projectionMatrix = new float[16];
	public float[] viewMatrix = new float[16];
	public boolean needsMatrixUpdate = true;

	public float near = 1;
	public float far = 1000;

	public Camera() {
		setTarget(0, 0, 0);
		setUpAxis(0, 0, 1);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		if (this.width != width) {
			this.width = width;
			needsMatrixUpdate = true;
		}
	}

	public int getHeight() {
		return height;
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

	public void setPosition(float x, float y, float z) {
		if (this.position.x != x || this.position.y != y || this.position.z != z) {
			position.setAll(x, y, z);
			needsMatrixUpdate = true;
		}
	}

	public Vector3 getTarget() {
		return target;
	}

	public void setTarget(float x, float y, float z) {
		if (this.target.x != x || this.target.y != y || this.target.z != z) {
			target.setAll(x, y, z);
			needsMatrixUpdate = true;
		}
	}

	public Vector3 getUpAxis() {
		return upAxis;
	}

	public void setUpAxis(float x, float y, float z) {
		if (this.upAxis.x != x || this.upAxis.y != y || this.upAxis.z != z) {
			upAxis.setAll(x, y, z);
			needsMatrixUpdate = true;
		}
	}

	public float getNear() {
		return near;
	}

	public void setNear(float near) {
		if (this.near != near) {
			this.near = near;
			needsMatrixUpdate = true;
		}
	}

	public float getFar() {
		return far;
	}

	public void setFar(float far) {
		if (this.far != far) {
			this.far = far;
			needsMatrixUpdate = true;
		}
	}

	public abstract boolean updateMatrices();

	public abstract void updateViewMatrix();
}

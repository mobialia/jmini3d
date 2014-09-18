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

	public float[] perspectiveMatrix = new float[16];
	private float[] modelViewMatrix = new float[16];

	private boolean needsMatrixUpdate = true;

	public void setPosition(float x, float y, float z) {
		position.setAll(x, y, z);
		needsMatrixUpdate = true;
	}

	public void setTarget(float x, float y, float z) {
		target.setAll(x, y, z);
		needsMatrixUpdate = true;
	}

	public void setUpAxis(float x, float y, float z) {
		upAxis.setAll(x, y, z);
		needsMatrixUpdate = true;
	}

	public void setFovy(float fovy) {
		this.fovy = fovy;
		needsMatrixUpdate = true;
	}

	public void setNear(float near) {
		this.near = near;
		needsMatrixUpdate = true;
	}

	public void setFar(float far) {
		this.far = far;
		needsMatrixUpdate = true;
	}

	public void setWidth(int width) {
		this.width = width;
		needsMatrixUpdate = true;
	}

	public void setHeight(int height) {
		this.height = height;
		needsMatrixUpdate = true;
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
		return (float) (360 / Utils.PI * Math.atan( Math.tan(fovy * Utils.PI / 360) * width / height ));
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
			MatrixUtils.perspective(perspectiveMatrix, fovy, ((float) width) / height, near, far);
			MatrixUtils.lookAt(modelViewMatrix, position, target, upAxis, vx, vy, vz);
			MatrixUtils.multiply(perspectiveMatrix, modelViewMatrix, perspectiveMatrix);

			needsMatrixUpdate = false;
			return true;
		}
		return false;
	}
}
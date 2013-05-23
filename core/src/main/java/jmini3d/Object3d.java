package jmini3d;

public class Object3d {

	public Geometry3d geometry3d;
	public Material material;

	public boolean visible = true;
	public boolean clearDepthAfterDraw = false; 

	private Vector3 position;

	public float[] modelViewMatrix = new float[16];
	public float[] normalMatrix = new float[9];

	private float rotationMatrix[];
	private float[] translationMatrix = new float[16];

	private boolean needsMatrixUpdate = true;

	public Object3d(Geometry3d geometry3d, Material material) {
		this.geometry3d = geometry3d;
		this.material = material;

		position = new Vector3();
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position.setAllFrom(position);
		needsMatrixUpdate = true;
	}

	public void setPosition(float x, float y, float z) {
		this.position.setAll(x, y, z);
		needsMatrixUpdate = true;
	}

	public void setRotationMatrix(Vector3 direction, Vector3 up, Vector3 side) {
		if (rotationMatrix == null) {
			rotationMatrix = new float[16];
		}
		MatrixUtils.rotate(rotationMatrix, direction, up, side);

		needsMatrixUpdate = true;
	}

	public void updateMatrices(float[] cameraModelViewMatrix, boolean forceUpdate) {
		if (needsMatrixUpdate || forceUpdate) {
			needsMatrixUpdate = false;

			MatrixUtils.copyMatrix(cameraModelViewMatrix, modelViewMatrix);

			if (position != null) {
				MatrixUtils.translate(translationMatrix, position);
				MatrixUtils.multiply(modelViewMatrix, translationMatrix, modelViewMatrix);
			}

			if (rotationMatrix != null) {
				MatrixUtils.multiply(modelViewMatrix, rotationMatrix, modelViewMatrix);
			}

			normalMatrix = MatrixUtils.toInverseMat3(modelViewMatrix, normalMatrix);
			if (normalMatrix != null) {
				normalMatrix = MatrixUtils.transpose(normalMatrix, normalMatrix);
			}
		}
	}
}
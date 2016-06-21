package jmini3d;

import java.util.ArrayList;

import jmini3d.geometry.Geometry;
import jmini3d.geometry.VariableGeometry;
import jmini3d.material.Material;

public class Object3d {

	public Geometry geometry3d;
	public Material material;
	public VertexColors vertexColors;

	public boolean visible = true;
	public boolean clearDepthAfterDraw = false;

	private Vector3 position;

	public float[] modelMatrix = new float[16];
	public float[] normalMatrix = new float[9];

	public float rotationMatrix[];
	private float scaleMatrix[];
	private float[] translationMatrix = new float[16];

	public ArrayList<Object3d> children;

	float scale = 1;

	protected boolean needsMatrixUpdate = true;

	public Object3d() {
		this(new VariableGeometry(0, 0), new Material(), null);
	}

	public Object3d(Geometry geometry3d, Material material) {
		this(geometry3d, material, null);
	}

	public Object3d(Geometry geometry3d, Material material, VertexColors vertexColors) {
		this.geometry3d = geometry3d;
		this.material = material;
		this.vertexColors = vertexColors;

		position = new Vector3();
		children = new ArrayList<>();
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

	public Geometry getGeometry3d() {
		return geometry3d;
	}

	public void setRotationMatrix(Vector3 direction, Vector3 up, Vector3 side) {
		if (rotationMatrix == null) {
			rotationMatrix = new float[16];
		}
		MatrixUtils.rotate(rotationMatrix, direction, up, side);
		needsMatrixUpdate = true;
	}

	public void getRotationMatrix(Vector3 direction, Vector3 up, Vector3 side) {
		if (rotationMatrix == null) {
			side.setAll(1, 0, 0);
			direction.setAll(0, 1, 0);
			up.setAll(0, 0, 1);
		} else {
			MatrixUtils.getRotation(rotationMatrix, direction, up, side);
		}
	}

	public void setScale(float scale) {
		if (this.scale != scale) {
			this.scale = scale;
			if (scaleMatrix == null) {
				scaleMatrix = new float[16];
				MatrixUtils.copyMatrix(MatrixUtils.IDENTITY4, scaleMatrix);
			}
			scaleMatrix[0] = scale;
			scaleMatrix[5] = scale;
			scaleMatrix[10] = scale;
			needsMatrixUpdate = true;
		}
	}

	public float getScale() {
		return scale;
	}

	public void updateMatrices() {
		updateMatrices(MatrixUtils.IDENTITY4, false);
	}

	private boolean doesMatrixNeedUpdate() {
		boolean result = needsMatrixUpdate;
		if (!result) {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).doesMatrixNeedUpdate()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	public void updateMatrices(float[] initialMatrix, boolean forceUpdate) {
		if (doesMatrixNeedUpdate() || forceUpdate) {
			needsMatrixUpdate = false;

			MatrixUtils.copyMatrix(initialMatrix, modelMatrix);

			if (position != null) {
				MatrixUtils.translate(translationMatrix, position);
				MatrixUtils.multiply(modelMatrix, translationMatrix, modelMatrix);
			}

			if (rotationMatrix != null) {
				MatrixUtils.multiply(modelMatrix, rotationMatrix, modelMatrix);
			}

			if (scale != 1 && scaleMatrix != null) {
				MatrixUtils.multiply(modelMatrix, scaleMatrix, modelMatrix);
			}

			normalMatrix = MatrixUtils.toInverseMat3(modelMatrix, normalMatrix);
			if (normalMatrix != null) {
				normalMatrix = MatrixUtils.transpose(normalMatrix, normalMatrix);
			}

			for (int i = 0; i < children.size(); i++) {
				children.get(i).updateMatrices(modelMatrix, true);
			}
		}
	}

	public void addChild(Object3d object3d) {
		children.add(object3d);
	}

	public void removeChild(Object3d object3d) {
		children.remove(object3d);
	}

	public ArrayList<Object3d> getChildren() {
		return children;
	}

	public VertexColors getVertexColors() {
		return vertexColors;
	}

	public void setVertexColors(VertexColors vertexColors) {
		this.vertexColors = vertexColors;
	}
}
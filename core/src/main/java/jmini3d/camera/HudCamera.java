package jmini3d.camera;

import jmini3d.MatrixUtils;

public class HudCamera extends Camera {

	public HudCamera() {
		super();
		MatrixUtils.copyMatrix(MatrixUtils.IDENTITY4, viewMatrix);
	}

	public boolean updateMatrices() {
		if (needsMatrixUpdate) {
			MatrixUtils.ortho(projectionMatrix, 0, width, height, 0, -5, 1);
			needsMatrixUpdate = false;
			return true;
		}
		return false;
	}

	public void updateViewMatrix() {
	}
}
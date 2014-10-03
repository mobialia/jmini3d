package jmini3d.geometry;

import jmini3d.Vector3;

public class PlaneGeometry extends VariableGeometry {

	public PlaneGeometry(float radius) {
		super(4, 2);
		addQuad(new Vector3(-radius, radius, 0), new Vector3(radius, radius, 0), //
				new Vector3(-radius, -radius, 0), new Vector3(radius, -radius, 0));
	}
}

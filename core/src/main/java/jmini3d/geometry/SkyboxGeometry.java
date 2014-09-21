package jmini3d.geometry;

import jmini3d.Vector3;

public class SkyboxGeometry extends VariableGeometry {

	public SkyboxGeometry(float radius) {
		super(24, 12);
		addQuad(new Vector3(radius, radius, radius), new Vector3(radius, -radius, radius), new Vector3(radius, radius, -radius), new Vector3(radius, -radius, -radius));
		addQuad(new Vector3(-radius, -radius, radius), new Vector3(-radius, radius, radius), new Vector3(-radius, -radius, -radius), new Vector3(-radius, radius, -radius));
		addQuad(new Vector3(-radius, radius, radius), new Vector3(radius, radius, radius), new Vector3(-radius, radius, -radius), new Vector3(radius, radius, -radius));
		addQuad(new Vector3(radius, -radius, radius), new Vector3(-radius, -radius, radius), new Vector3(radius, -radius, -radius), new Vector3(-radius, -radius, -radius));
		addQuad(new Vector3(-radius, -radius, radius), new Vector3(radius, -radius, radius), new Vector3(-radius, radius, radius), new Vector3(radius, radius, radius));
		addQuad(new Vector3(-radius, radius, -radius), new Vector3(radius, radius, -radius), new Vector3(-radius, -radius, -radius), new Vector3(radius, -radius, -radius));
	}
}

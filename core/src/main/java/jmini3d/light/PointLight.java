package jmini3d.light;

import jmini3d.Color4;
import jmini3d.Vector3;

public class PointLight extends Light {
	public Vector3 position;

	public PointLight(Vector3 position, Color4 color) {
		this.position = position;
		this.color = color;
	}
}

package jmini3d.light;

import jmini3d.Color4;
import jmini3d.Vector3;

public class DirectionalLight extends Light {

	public Vector3 direction;

	public DirectionalLight(Vector3 direction, Color4 color) {
		this.direction = direction;
		this.color = color;
	}

	public DirectionalLight(Vector3 direction, Color4 color, float intensity) {
		this.direction = direction;
		this.color = color;
		this.color.a = intensity;
	}
}

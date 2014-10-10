package jmini3d.light;

import jmini3d.Color4;

public class AmbientLight extends Light {

	public AmbientLight(Color4 color) {
		this.color = color;
	}

	public AmbientLight(Color4 color, float intensity) {
		this.color = color;
		this.color.a = intensity;
	}
}

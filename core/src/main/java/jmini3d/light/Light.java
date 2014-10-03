package jmini3d.light;

import jmini3d.Color4;

public class Light {
	public Color4 color;
	public float diffuse[] = {0.6f, 0.6f, 0.6f, 0f};
	public float specular[] = {0.8f, 0.8f, 0.8f, 0f};
	public float emission[] = {0f, 0f, 0f, 0f};
	public float direction[] = {0f, 0f, -1f};
	public float spotCutoffAngle = 180;
	public float spotExponent = 0f;
	public float attenuation[] = {0.5f, 0f, 0f};

	public Color4 getColor() {
		return color;
	}

	public void setColor(Color4 color) {
		this.color = color;
	}

	public void setDiffuse(int r, int g, int b, int a) {
		diffuse[0] = r / 255.0f;
		diffuse[1] = g / 255.0f;
		diffuse[2] = b / 255.0f;
		diffuse[3] = a / 255.0f;
	}

	public void setSpecular(int r, int g, int b, int a) {
		specular[0] = r / 255.0f;
		specular[1] = g / 255.0f;
		specular[2] = b / 255.0f;
		specular[3] = a / 255.0f;
	}
}
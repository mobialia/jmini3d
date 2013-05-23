package jmini3d;

public class Light {
	// position is x,y,z + w, w=0 to create a directional light, w=1 to create a positional light
	public float position[] = { 0f, 0f, 0f, 1 };
	public float ambient[] = { 0.2f, 0.2f, 0.2f, 0f };
	public float diffuse[] = { 0.6f, 0.6f, 0.6f, 0f };
	public float specular[] = { 0.8f, 0.8f, 0.8f, 0f };
	public float emission[] = { 0f, 0f, 0f, 0f };
	public float direction[] = { 0f, 0f, -1f };
	public float spotCutoffAngle = 180;
	public float spotExponent = 0f;
	public float attenuation[] = { 0.5f, 0f, 0f };

	public void setPosition(float x, float y, float z) {
		position[0] = x;
		position[1] = y;
		position[2] = z;
	}

	public void setAmbient(int r, int g, int b, int a) {
		ambient[0] = r / 255.0f;
		ambient[1] = g / 255.0f;
		ambient[2] = b / 255.0f;
		ambient[3] = a / 255.0f;
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
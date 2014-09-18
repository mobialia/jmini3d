package jmini3d;

public class Material {
	public int shaderKey = -1;
	public boolean lighting = true;
	public Blending blending = Blending.NoBlending;
	public Color4 color;
	public Texture map;
	public CubeMapTexture envMap;
	public boolean useEnvMapAsMap = false;
	public float reflectivity = 0f;

	public Material() {
		color = new Color4(0, 0, 0, 0);
	}

	public Material(Color4 color) {
		this.color = color;
	}

	public Material(Texture texture) {
		this.map = texture;
		color = new Color4(0, 0, 0, 0);
	}

	public Material(Texture texture, CubeMapTexture envMapTexture, float reflectivity) {
		this.map = texture;
		this.envMap = envMapTexture;
		this.reflectivity = reflectivity;
		color = new Color4(0, 0, 0, 0);
	}

	public void setBlending(Blending blending) {
		this.blending = blending;
	}

	public String getFragmentShader() {
		return "";
	}

	public void setLighting(boolean lighting) {
		this.lighting = lighting;
		shaderKey = -1;
	}
}

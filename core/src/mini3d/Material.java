package mini3d;

public class Material {
	public Color4 color;
	public Texture texture;
	public CubeMapTexture envMapTexture;
	public float reflectivity = 0f;

	public Material() {
		color = new Color4(0, 0, 0, 0);
	}

	public Material(Texture texture) {
		this.texture = texture;
		color = new Color4(0, 0, 0, 0);
	}

	public Material(Texture texture, CubeMapTexture envMapTexture, float reflectivity) {
		this.texture = texture;
		this.envMapTexture = envMapTexture;
		this.reflectivity = reflectivity;
		color = new Color4(0, 0, 0, 0);
	}
}

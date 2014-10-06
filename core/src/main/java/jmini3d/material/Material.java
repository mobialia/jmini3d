package jmini3d.material;

import jmini3d.Blending;
import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Texture;

public class Material {
	public int shaderKey = -1;
	public boolean lighting = true;
	public Blending blending = Blending.NoBlending;
	public Color4 color;
	public Texture map;
	public CubeMapTexture envMap;
	public Texture normalMap;
	public boolean useEnvMapAsMap = false;
	public boolean applyColorToAlpha = false;
	public float reflectivity = 0f;

	public Material() {
		color = new Color4();
	}

	public Material(Color4 color) {
		this.color = color;
	}

	public Material(Texture texture) {
		this.map = texture;
		color = new Color4();
	}

	public Material(Texture texture, CubeMapTexture envMapTexture, float reflectivity) {
		this.map = texture;
		this.envMap = envMapTexture;
		this.reflectivity = reflectivity;
		color = new Color4();
	}

	public void setBlending(Blending blending) {
		this.blending = blending;
	}

	public void setLighting(boolean lighting) {
		this.lighting = lighting;
		shaderKey = -1;
	}

	public void setApplyColorToAlpha(boolean applyColorToAlpha) {
		this.applyColorToAlpha = applyColorToAlpha;
		shaderKey = -1;
	}

	public void setMap(Texture map) {
		this.map = map;
	}

	public void setNormalMap(Texture normalMap) {
		this.normalMap = normalMap;
	}

	public void setEnvMap(CubeMapTexture envMap) {
		this.envMap = envMap;
	}
}

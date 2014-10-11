package jmini3d.material;

import jmini3d.Blending;
import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Texture;

public class Material {
	public int shaderKey = -1;
	public Blending blending = Blending.NoBlending;
	public Color4 color;
	public Texture map;
	public CubeMapTexture envMap;
	public Texture normalMap;
	public boolean useEnvMapAsMap = false;
	public boolean applyColorToAlpha = false;
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

	public void setBlending(Blending blending) {
		this.blending = blending;
	}

	public void setUseEnvMapAsMap(boolean useEnvMapAsMap) {
		this.useEnvMapAsMap = useEnvMapAsMap;
		shaderKey = -1;
	}

	public void setApplyColorToAlpha(boolean applyColorToAlpha) {
		this.applyColorToAlpha = applyColorToAlpha;
		shaderKey = -1;
	}

	public void setMap(Texture map) {
		this.map = map;
	}

	public void setEnvMap(CubeMapTexture envMap) {
		this.envMap = envMap;
	}

	public void setEnvMap(CubeMapTexture envMap, float reflectivity) {
		this.envMap = envMap;
		this.reflectivity = reflectivity;
	}

	public void setNormalMap(Texture normalMap) {
		this.normalMap = normalMap;
	}
}

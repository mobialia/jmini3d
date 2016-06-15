package jmini3d.shader;


import jmini3d.Scene;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.Light;
import jmini3d.light.PointLight;
import jmini3d.material.Material;
import jmini3d.material.PhongMaterial;

/**
 * Generates a unique shader key for scenes and materials.
 * Allows to detect if two scene/material combinations can share the same shader code before
 * generating the shader.
 * The final shader key is the scene shader key BITWISE AND the material key
 * <p/>
 * 0xffff0000 is reserved for the lights
 * 0x0000ff00 is reserved for the shader plugins
 */
public class ShaderKey {

	public static int getSceneKey(Scene scene) {
		boolean useAmbientlight = false;
		int maxPointLights = 0;
		int maxDirLights = 0;

		for (Light light : scene.lights) {
			if (light instanceof AmbientLight) {
				useAmbientlight = true;
			}

			if (light instanceof PointLight) {
				maxPointLights++;
			}

			if (light instanceof DirectionalLight) {
				maxDirLights++;
			}
		}

		int key = 0xff;

		for (ShaderPlugin sp : scene.shaderPlugins) {
			key |= sp.getShaderKey();
		}


		return key |
				(useAmbientlight ? 0x10000 : 0) |
				(maxPointLights * 0x0100000) |
				(maxDirLights * 0x1000000);
	}

	public static int getMaterialKey(Material material) {
		boolean useLight = material instanceof PhongMaterial;
		boolean useMap = material.map != null;
		boolean useEnvMap = material.envMap != null;
		boolean useEnvMapAsMap = material.useEnvMapAsMap;
		boolean useNormalMap = material.normalMap != null;
		boolean useApplyColorToAlpha = material.applyColorToAlpha;
		boolean useVertexColors = material.useVertexColors;

		return (useLight ? 0xffffff00 : 0) |
				(useMap ? 0x01 : 0) |
				(useEnvMap ? 0x02 : 0) |
				(useEnvMapAsMap ? 0x04 : 0) |
				(useNormalMap ? 0x08 : 0) |
				(useApplyColorToAlpha ? 0x10 : 0) |
				(useVertexColors ? 0x20 : 0);
	}
}

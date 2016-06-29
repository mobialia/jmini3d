package jmini3d.shader;


import jmini3d.Scene;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.Light;
import jmini3d.light.PointLight;
import jmini3d.material.Material;

/**
 * Generates a unique shader key for scenes and materials.
 * Allows to detect if two scene/material combinations can share the same shader code before
 * generating the shader.
 * The final shader key is the scene shader key BITWISE AND the material key
 * <p/>
 * 0x00ffff00 is reserved for the lights
 * 0xff000000 is reserved for the shader plugins
 */
public class ShaderKey {
	public static int SHADER_PLUGIN_MASK = 0xff000000;
	public static int SHADER_PLUGIN_SCENE_MASK = 0xf0000000;
	public static int SHADER_PLUGIN_MATERIAL_MASK = 0x0f000000;

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

		if (scene.shaderPlugin != null) {
			key |= (scene.shaderPlugin.getShaderKey() << 28);
		}

		return SHADER_PLUGIN_MATERIAL_MASK |
				key |
				(useAmbientlight ? 0x100 : 0) |
				(maxPointLights * 0x01000) |
				(maxDirLights * 0x10000);
	}

	public static int getMaterialKey(Material material) {
		boolean useLights = material.useLigths;
		boolean useMap = material.map != null;
		boolean useEnvMap = material.envMap != null;
		boolean useEnvMapAsMap = material.useEnvMapAsMap;
		boolean useNormalMap = material.normalMap != null;
		boolean useApplyColorToAlpha = material.applyColorToAlpha;
		boolean useVertexColors = material.useVertexColors;

		int key = 0;

		if (material.shaderPlugin != null) {
			key |= (material.shaderPlugin.getShaderKey() << 24);
		}

		return SHADER_PLUGIN_SCENE_MASK |
				key |
				(useLights ? 0xffff00 : 0) |
				(useMap ? 0x01 : 0) |
				(useEnvMap ? 0x02 : 0) |
				(useEnvMapAsMap ? 0x04 : 0) |
				(useNormalMap ? 0x08 : 0) |
				(useApplyColorToAlpha ? 0x10 : 0) |
				(useVertexColors ? 0x20 : 0);
	}
}

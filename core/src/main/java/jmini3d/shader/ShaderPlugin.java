package jmini3d.shader;

/**
 * The ShaderPlugins modify the shaders at the scene or material Level
 */
public abstract class ShaderPlugin {

	private static int nextSceneShadePluginKey = 1;
	private static int nextMaterialShadePluginrKey = 1;
	public static int shaderKey;

	public static int getNextSceneShaderPluginKey() {
		int shaderKey = ShaderPlugin.nextSceneShadePluginKey;
		ShaderPlugin.nextSceneShadePluginKey <<= 1;
		return shaderKey;
	}

	public static int getNextMaterialShaderPluginKey() {
		int shaderKey = ShaderPlugin.nextMaterialShadePluginrKey;
		ShaderPlugin.nextMaterialShadePluginrKey <<= 1;
		return shaderKey;
	}

	/**
	 * A bit < 256 for the shader key, unique for each shader plugin
	 */
	public int getShaderKey() {
		return shaderKey;
	}

	/**
	 * Each program instance has its own ProgramPlugin object
	 *
	 * @param program
	 * @return
	 */
	public abstract ProgramPlugin getProgramPlugin(Program program);
}
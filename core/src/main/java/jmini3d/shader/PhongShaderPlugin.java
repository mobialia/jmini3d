package jmini3d.shader;

public class PhongShaderPlugin extends ShaderPlugin {

	private static int KEY;

	static {
		KEY = getNextMaterialShaderPluginKey();
	}

	public PhongShaderPlugin() {
		shaderKey = KEY;
	}

	public ProgramPlugin getProgramPlugin(Program program) {
		return new PhongProgramPlugin(program);
	}
}

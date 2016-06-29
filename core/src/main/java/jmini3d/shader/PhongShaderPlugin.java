package jmini3d.shader;

public class PhongShaderPlugin extends ShaderPlugin {

	static {
		shaderKey = getNextMaterialShaderPluginKey();
	}

	public ProgramPlugin getProgramPlugin(Program program) {
		return new PhongProgramPlugin(program);
	}
}

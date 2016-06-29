package jmini3d.shader;

public class LensDistortionShaderPlugin extends ShaderPlugin {

	float c1, c2, c3, c4, c5, c6, maxRadiusSQ;

	static {
		shaderKey = getNextSceneShaderPluginKey();
	}

	public LensDistortionShaderPlugin(float c1, float c2, float c3, float c4, float c5, float c6, float maxRadiusSQ) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.c4 = c4;
		this.c5 = c5;
		this.c6 = c6;
		this.maxRadiusSQ = maxRadiusSQ;
	}

	@Override
	public ProgramPlugin getProgramPlugin(Program program) {
		return new LensDistortionProgramPlugin(program, this);
	}
}

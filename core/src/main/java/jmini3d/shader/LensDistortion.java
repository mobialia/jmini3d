package jmini3d.shader;

import java.util.ArrayList;

/**
 * A barrel lens distortion shader plugin applied at vertex level
 */
public class LensDistortion extends ShaderPlugin {

	float c1, c2, c3, c4, c5, c6, maxRadiusSQ;

	public LensDistortion(float c1, float c2, float c3, float c4, float c5, float c6, float maxRadiusSQ) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.c4 = c4;
		this.c5 = c5;
		this.c6 = c6;
		this.maxRadiusSQ = maxRadiusSQ;
	}

	@Override
	public int getShaderKey() {
		return 0x1;
	}

	@Override
	public String getVertexShaderName() {
		return "lens_distortion_vertex_shader.glsl";
	}

	@Override
	public void addShaderDefines(ArrayList<String> defines) {
		defines.add("USE_LENS_DISTORTION");
	}

	@Override
	public void addUniformNames(ArrayList<String> uniformNames) {
		uniformNames.add("lensDistortionC1");
		uniformNames.add("lensDistortionC2");
		uniformNames.add("lensDistortionC3");
		uniformNames.add("lensDistortionC4");
		uniformNames.add("lensDistortionC5");
		uniformNames.add("lensDistortionC6");
		uniformNames.add("lensDistortionMaxRadiusSQ");
	}

	@Override
	public void setSceneUniforms(UniformSetter setter) {
		setter.setUniform("lensDistortionC1", c1);
		setter.setUniform("lensDistortionC2", c2);
		setter.setUniform("lensDistortionC3", c3);
		setter.setUniform("lensDistortionC4", c4);
		setter.setUniform("lensDistortionC5", c5);
		setter.setUniform("lensDistortionC6", c6);
		setter.setUniform("lensDistortionMaxRadiusSQ", maxRadiusSQ);
	}
}
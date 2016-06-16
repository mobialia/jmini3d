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
		return "vertex_shader_lens_distortion.glsl";
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
	public void setSceneUniforms(Program program) {
		float[] cachedValues = program.getValueCache("lensDistortion", 7);

		program.setUniformIfCachedValueChanged("lensDistortionC1", c1, cachedValues, 0);
		program.setUniformIfCachedValueChanged("lensDistortionC2", c2, cachedValues, 1);
		program.setUniformIfCachedValueChanged("lensDistortionC3", c3, cachedValues, 2);
		program.setUniformIfCachedValueChanged("lensDistortionC4", c4, cachedValues, 3);
		program.setUniformIfCachedValueChanged("lensDistortionC5", c5, cachedValues, 4);
		program.setUniformIfCachedValueChanged("lensDistortionC6", c6, cachedValues, 5);
		program.setUniformIfCachedValueChanged("lensDistortionMaxRadiusSQ", maxRadiusSQ, cachedValues, 5);
	}
}
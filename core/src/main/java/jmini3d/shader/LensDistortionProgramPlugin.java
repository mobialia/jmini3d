package jmini3d.shader;

import jmini3d.Scene;
import jmini3d.material.Material;

/**
 * A barrel lens distortion shader plugin applied at vertex level
 */
public class LensDistortionProgramPlugin extends ProgramPlugin {

	LensDistortionShaderPlugin lensDistortionShaderPlugin;

	int lensDistortionC1Uniform;
	int lensDistortionC2Uniform;
	int lensDistortionC3Uniform;
	int lensDistortionC4Uniform;
	int lensDistortionC5Uniform;
	int lensDistortionC6Uniform;
	int lensDistortionMaxRadiusSQUniform;

	float lensDistortionC1Last;
	float lensDistortionC2Last;
	float lensDistortionC3Last;
	float lensDistortionC4Last;
	float lensDistortionC5Last;
	float lensDistortionC6Last;
	float lensDistortionMaxRadiusSQLast;

	public LensDistortionProgramPlugin(Program program, LensDistortionShaderPlugin lensDistortionShaderPlugin) {
		super(program);
		this.lensDistortionShaderPlugin = lensDistortionShaderPlugin;
	}

	@Override
	public void prepareShader(Scene scene, Material material) {
		//
		// Change the vertex shader
		//
		program.vertexShaderName = "vertex_shader_lens_distortion.glsl";

		program.shaderDefines.put("USE_LENS_DISTORTION", null);
	}

	@Override
	public void onShaderLoaded() {
		lensDistortionC1Uniform = program.getUniformLocation("lensDistortionC1");
		lensDistortionC2Uniform = program.getUniformLocation("lensDistortionC2");
		lensDistortionC3Uniform = program.getUniformLocation("lensDistortionC3");
		lensDistortionC4Uniform = program.getUniformLocation("lensDistortionC4");
		lensDistortionC5Uniform = program.getUniformLocation("lensDistortionC5");
		lensDistortionC6Uniform = program.getUniformLocation("lensDistortionC6");
		lensDistortionMaxRadiusSQUniform = program.getUniformLocation("lensDistortionMaxRadiusSQ");
	}

	@Override
	public void onSetSceneUniforms(Scene scene) {
		lensDistortionC1Last = program.setFloatUniformIfValueChanged(lensDistortionC1Uniform, lensDistortionShaderPlugin.c1, lensDistortionC1Last);
		lensDistortionC2Last = program.setFloatUniformIfValueChanged(lensDistortionC2Uniform, lensDistortionShaderPlugin.c2, lensDistortionC2Last);
		lensDistortionC3Last = program.setFloatUniformIfValueChanged(lensDistortionC3Uniform, lensDistortionShaderPlugin.c3, lensDistortionC3Last);
		lensDistortionC4Last = program.setFloatUniformIfValueChanged(lensDistortionC4Uniform, lensDistortionShaderPlugin.c4, lensDistortionC4Last);
		lensDistortionC5Last = program.setFloatUniformIfValueChanged(lensDistortionC5Uniform, lensDistortionShaderPlugin.c5, lensDistortionC5Last);
		lensDistortionC6Last = program.setFloatUniformIfValueChanged(lensDistortionC6Uniform, lensDistortionShaderPlugin.c6, lensDistortionC6Last);
		lensDistortionMaxRadiusSQLast = program.setFloatUniformIfValueChanged(lensDistortionMaxRadiusSQUniform, lensDistortionShaderPlugin.maxRadiusSQ, lensDistortionMaxRadiusSQLast);
	}
}
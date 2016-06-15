package jmini3d.shader;

import java.util.ArrayList;

/**
 * A barrel lens distortion shader plugin applied at vertex level
 */
public class BarrelLensDistortion extends ShaderPlugin {

	float barrelDistortion;

	public BarrelLensDistortion(float barrelDistortion) {
		this.barrelDistortion = barrelDistortion;
	}

	@Override
	public int getShaderKey() {
		return 0x1;
	}

	@Override
	public void addShaderDefines(ArrayList<String> defines) {
		defines.add("USE_BARREL_DISTORTION");
	}

	@Override
	public void addUniformNames(ArrayList<String> uniformNames) {
		uniformNames.add("barrelDistortion");
	}

	@Override
	public void setSceneUniforms(UniformSetter program) {
		program.setUniform("barrelDistortion", barrelDistortion);
	}
}
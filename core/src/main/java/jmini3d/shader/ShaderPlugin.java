package jmini3d.shader;

import java.util.ArrayList;

/**
 * The shaderPlugins modify the shaders at the scene Level
 */
public abstract class ShaderPlugin {

	/**
	 * A unique bit < 256 for the shader key
	 */
	public abstract int getShaderKey();

	/**
	 * Override with the custom vertex shader name
	 */
	public String getVertexShaderName() {
		return null;
	}

	/**
	 * Override with the custom fragment shader name
	 */
	public String getFragmentShaderName() {
		return null;
	}

	/**
	 * Defines to be included in both fragment and vertex shader
	 */
	public void addShaderDefines(ArrayList<String> defines) {
	}

	/**
	 * Uniforms names that the shader uses
	 */
	public void addUniformNames(ArrayList<String> uniformNames) {
	}

	/**
	 * Initialize the scene uniforms
	 */
	public void setSceneUniforms(UniformSetter program) {
	}

}

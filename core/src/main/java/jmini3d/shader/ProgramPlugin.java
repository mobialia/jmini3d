package jmini3d.shader;

import java.util.HashMap;

import jmini3d.Object3d;
import jmini3d.Scene;

public class ProgramPlugin {

	Program program;

	public ProgramPlugin(Program program) {
		this.program = program;
	}

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
	 * Prepare shaders
	 *
	 * @defines to be included in both fragment and vertex shader
	 */
	public void prepareShader(Scene scene, HashMap<String, String> defines) {
	}

	/**
	 * Initialize uniforms, etc
	 */
	public void onShaderLoaded() {
	}

	/**
	 * Initialize the scene uniforms before each frame
	 */
	public void onSetSceneUniforms(Scene scene) {
	}

	/**
	 * Called before each object is drawn
	 */
	public void onDrawObject(Object3d o3d) {
	}
}

package jmini3d.shader;

import jmini3d.Color4;

public abstract class Program {

	public static String DEFAULT_VERTEX_SHADER = "vertex_shader.glsl";
	public static String DEFAULT_FRAGMENT_SHADER = "fragment_shader.glsl";

	public static final int TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516;
	public static final int TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518;
	public static final int TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519;

	// Relative to the axis system
	public static int[] CUBE_MAP_SIDES = {TEXTURE_CUBE_MAP_POSITIVE_X, TEXTURE_CUBE_MAP_NEGATIVE_X,
			TEXTURE_CUBE_MAP_POSITIVE_Z, TEXTURE_CUBE_MAP_NEGATIVE_Z,
			TEXTURE_CUBE_MAP_POSITIVE_Y, TEXTURE_CUBE_MAP_NEGATIVE_Y};

	public ProgramPlugin sceneProgramPlugin;
	public ProgramPlugin materialProgramPlugin;

	/**
	 * To be used from the Shader plugins
	 */
	public abstract int getUniformLocation(String uniformName);

	/**
	 * @returns uniformValue
	 */
	public abstract float setFloatUniformIfValueChanged(int uniform, float uniformValue, float lastValue);

	public abstract void setColorUniformIfChanged(int colorUniform, Color4 newColor, Color4 lastColor);

	public abstract void setUniform3fv(int location, int count, float[] v);

	public abstract void setUniform4fv(int location, int count, float[] v);

	public abstract void setUseNormals(boolean useNormals);

	public abstract void setUseCameraPosition(boolean useCameraPosition);

}

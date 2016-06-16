package jmini3d.shader;

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

	/**
	 * To be used from the Shader plugins
	 */
	public abstract void setUniform(String uniformName, float value);
}

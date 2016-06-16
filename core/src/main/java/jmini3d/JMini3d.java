package jmini3d;

import jmini3d.shader.Program;

public class JMini3d {

	public static void useOpenglAxisSystem() {
		Program.DEFAULT_FRAGMENT_SHADER = "fragment_shader_opengl_axis.glsl";

		Program.CUBE_MAP_SIDES = new int[]{Program.TEXTURE_CUBE_MAP_POSITIVE_X, Program.TEXTURE_CUBE_MAP_NEGATIVE_X,
				Program.TEXTURE_CUBE_MAP_POSITIVE_Y, Program.TEXTURE_CUBE_MAP_NEGATIVE_Y,
				Program.TEXTURE_CUBE_MAP_POSITIVE_Z, Program.TEXTURE_CUBE_MAP_NEGATIVE_Z};
	}
}

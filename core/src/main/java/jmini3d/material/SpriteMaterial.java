package jmini3d.material;

import jmini3d.Blending;
import jmini3d.Texture;

public class SpriteMaterial extends Material {

	public SpriteMaterial(Texture texture) {
		super(texture);
		setBlending(Blending.NormalBlending);
		setLighting(false);
	}
}

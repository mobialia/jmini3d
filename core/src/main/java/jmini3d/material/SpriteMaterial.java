package jmini3d.material;

import jmini3d.Blending;
import jmini3d.Color4;
import jmini3d.Texture;

public class SpriteMaterial extends Material {

	public SpriteMaterial() {
		super();
		setBlending(Blending.NormalBlending);
	}

	public SpriteMaterial(Color4 color) {
		super(color);
		setBlending(Blending.NormalBlending);
	}

	public SpriteMaterial(Texture texture) {
		super(texture);
		setBlending(Blending.NormalBlending);
	}
}

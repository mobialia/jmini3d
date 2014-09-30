package jmini3d.demo;

import jmini3d.CubeMapTexture;
import jmini3d.Object3d;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;
import jmini3d.material.Material;

public class EnvMapCubeScene extends ParentScene {

	public EnvMapCubeScene() {
		super("Cube with a cube map texture");

		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx.png", "negx.png", "posy.png", "negy.png", "posz.png", "negz.png"});

		Material material1 = new Material(null, envMap, 0);
		material1.useEnvMapAsMap = true;

		Geometry geometry = new BoxGeometry(1);
		Object3d o3d = new Object3d(geometry, material1);

		addChild(o3d);
	}
}

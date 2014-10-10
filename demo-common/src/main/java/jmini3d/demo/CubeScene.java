package jmini3d.demo;

import jmini3d.Color4;
import jmini3d.Object3d;
import jmini3d.Texture;
import jmini3d.Vector3;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.PointLight;
import jmini3d.material.Material;

public class CubeScene extends ParentScene {

	public CubeScene() {
		super("A simple cube with different lights");

		Texture map = new Texture("texture.png");
		Material material1 = new Material(map);
		Geometry geometry = new BoxGeometry(1);
		Object3d o3d = new Object3d(geometry, material1);

		addChild(o3d);

		addLight(new AmbientLight(new Color4(100, 100, 100), 0.5f));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0, 0, 255)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(255, 0, 0), 2));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0, 255, 0)));
	}
}

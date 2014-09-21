package jmini3d.demo;

import jmini3d.Color4;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.Vector3;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.PointLight;

public class CubeScene extends Scene {

	public CubeScene() {
		Texture map = new Texture("texture");
		Material material1 = new Material(map);
		Geometry geometry = new BoxGeometry(1);
		Object3d o3d = new Object3d(geometry, material1);

		addChild(o3d);

		addLight(new AmbientLight(new Color4(0.4f, 0.4f, 0.4f, 1)));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0f, 0f, 1f, 1)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(1f, 0f, 0f, 1)));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0f, 1f, 0f, 1)));
	}
}

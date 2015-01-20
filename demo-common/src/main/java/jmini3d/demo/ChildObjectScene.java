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
import jmini3d.material.PhongMaterial;

public class ChildObjectScene extends ParentScene {

	public ChildObjectScene() {
		super("Child Objects demo");

		Texture map = new Texture("texture.png");
		Color4 white = new Color4(255, 255, 255, 255);
		Material material1 = new PhongMaterial(map, white, white, white);
		Geometry geometry = new BoxGeometry(1);
		Object3d o3d = new Object3d(geometry, material1);
		Object3d o3dChild1 = new Object3d(geometry, material1);
		Object3d o3dChild2 = new Object3d(geometry, material1);

		addChild(o3d);
		o3d.setPosition(1, 0, 0);
		o3d.setScale(0.3f);

		o3d.addChild(o3dChild1);
		o3d.addChild(o3dChild2);

		o3dChild1.setPosition(-3, 0, 0);
		o3dChild1.setScale(0.5f);
		o3dChild2.setPosition(3, 0, 0);
		o3dChild2.setScale(1.5f);

		addLight(new AmbientLight(new Color4(100, 100, 100), 0.5f));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0, 0, 255)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(255, 0, 0), 1.1f));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0, 255, 0)));
	}
}

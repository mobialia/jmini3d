package jmini3d.demo;

import java.util.Random;

import jmini3d.Color4;
import jmini3d.Object3d;
import jmini3d.Vector3;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.PointLight;
import jmini3d.material.PhongMaterial;

public class CubesScene extends ParentScene {

	public CubesScene() {
		super("Multiple cubes with a light");

		Random r = new Random();

		addLight(new AmbientLight(new Color4(255, 255, 255), 0f));
		addLight(new PointLight(new Vector3(0, 0, 0), new Color4(255, 255, 255), 1.1f));

		Color4 ambient = new Color4(255, 255, 255, 255);
		Color4 red = new Color4(255, 0, 0, 255);
		Color4 green = new Color4(0, 255, 0, 255);
		Color4 blue = new Color4(0, 0, 255, 255);
		PhongMaterial material1 = new PhongMaterial(ambient, red, red);
		PhongMaterial material2 = new PhongMaterial(ambient, green, green);
		PhongMaterial material3 = new PhongMaterial(ambient, blue, blue);

		for (int i = 0; i < 200; i++) {
			float x = r.nextFloat() * 50 - 25;
			float y = r.nextFloat() * 50 - 25;
			float z = r.nextFloat() * 50 - 25;
			Geometry geometry = new BoxGeometry(1);
			Object3d o3d;
			if (i % 3 == 0) {
				o3d = new Object3d(geometry, material1);
			} else if (i % 3 == 1) {
				o3d = new Object3d(geometry, material2);
			} else {
				o3d = new Object3d(geometry, material3);
			}
			o3d.setPosition(x, y, z);
			addChild(o3d);
		}
	}
}

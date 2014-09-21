package jmini3d.demo;

import java.util.Random;

import jmini3d.Blending;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;

public class CubesScene extends Scene {

	public CubesScene() {
		Random r = new Random();

		Texture map = new Texture("texture");

		Material material1 = new Material(map);
		material1.setBlending(Blending.AdditiveBlending);

		Material material2 = new Material(map);
		material2.color.setAll(255, 0, 0, 128);

		Material material3 = new Material(map);
		material3.color.setAll(0, 0, 255, 128);

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

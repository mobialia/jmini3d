package jmini3d.demo;

import jmini3d.Color4;
import jmini3d.Object3d;
import jmini3d.Texture;
import jmini3d.Vector3;
import jmini3d.geometry.Geometry;
import jmini3d.geometry.PlaneGeometry;
import jmini3d.light.PointLight;
import jmini3d.material.Material;

public class NormalMapScene extends ParentScene {

	PointLight pointLight;

	public NormalMapScene() {
		super("Normal map in one of the two textures");

		Texture map = new Texture("wall.png");
		Texture normalMap = new Texture("wall_normal.png");

		Material material = new Material();
		material.setMap(map);

		Material materialMap = new Material();
		materialMap.setMap(map);
		materialMap.setNormalMap(normalMap);

		Geometry planeGeometry = new PlaneGeometry(1);

		Object3d plane1 = new Object3d(planeGeometry, material);
		plane1.setPosition(-1, 0, 0);
		addChild(plane1);

		Object3d plane2 = new Object3d(planeGeometry, materialMap);
		plane2.setPosition(1, 0, 0);
		addChild(plane2);

		pointLight = new PointLight(new Vector3(0, 0, 0.5f), new Color4(128, 128, 128), 3);
		addLight(pointLight);
	}

	@Override
	public void update() {
		pointLight.position.setAllFrom(camera.getPosition());
	}
}

package jmini3d.demo;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Object3d;
import jmini3d.Texture;
import jmini3d.Vector3;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;
import jmini3d.geometry.SkyboxGeometry;
import jmini3d.geometry.VariableGeometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.PointLight;
import jmini3d.material.Material;
import jmini3d.material.PhongMaterial;

public class ChildObjectsScene extends ParentScene {

	long initialTime;
	Vector3 direction = new Vector3(1, 0, 0);
	Vector3 up = new Vector3(0, 0, 1);
	Vector3 side = new Vector3(0, 1, 0);

	Object3d o3d, o3dChild1, o3dChild2;

	public ChildObjectsScene() {
		super("Child Objects demo");
		initialTime = System.currentTimeMillis();

		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx.png", "negx.png", "posy.png", "negy.png", "posz.png", "negz.png"});

		VariableGeometry skyboxGeometry = new SkyboxGeometry(300);
		Material skyboxMaterial = new Material();
		skyboxMaterial.setEnvMap(envMap, 0);
		skyboxMaterial.setUseEnvMapAsMap(true);
		Object3d skybox = new Object3d(skyboxGeometry, skyboxMaterial);
		addChild(skybox);

		Texture map = new Texture("texture.png");
		Color4 white = new Color4(255, 255, 255, 255);
		Material material1 = new PhongMaterial(map, white, white, white);
		Geometry geometry = new BoxGeometry(1);
		o3d = new Object3d(geometry, material1);
		o3dChild1 = new Object3d(geometry, material1);
		o3dChild2 = new Object3d(geometry, material1);

		addChild(o3d);
		o3d.setPosition(0, 0, 0);
		o3d.setScale(0.3f);

		o3d.addChild(o3dChild1);
		o3d.addChild(o3dChild2);

		o3dChild1.setPosition(-3, 0, 0);
		o3dChild1.setScale(0.5f);
		o3dChild2.setPosition(3, 0, 0);
		o3dChild2.setScale(0.5f);

		addLight(new AmbientLight(new Color4(100, 100, 100), 0.5f));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0, 0, 255)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(255, 0, 0), 1.1f));
	}

	public void update() {
		float step = (float) ((System.currentTimeMillis() - initialTime) * 2 * Math.PI / 5000f);
		up.setAll(0, (float) Math.cos(step), (float) -Math.sin(step));
		side.setAll(0, (float) Math.sin(step), (float) Math.cos(step));

		o3d.setRotationMatrix(direction, up, side);

		o3dChild1.setRotationMatrix(direction, up, side);
		o3dChild2.setRotationMatrix(direction, up, side);
	}
}

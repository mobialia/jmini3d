package jmini3d.demo;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Object3d;
import jmini3d.Vector3;
import jmini3d.geometry.Geometry;
import jmini3d.geometry.SkyboxGeometry;
import jmini3d.geometry.VariableGeometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.PointLight;
import jmini3d.material.Material;

public class TeapotScene extends ParentScene {

	public TeapotScene() {
		super("Skybox and cube map reflections");

		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx.png", "negx.png", "posy.png", "negy.png", "posz.png", "negz.png"});

		Material mirrorMat = new Material(null, envMap, 0.8f);

		VariableGeometry skyboxGeometry = new SkyboxGeometry(300);
		Material skyboxMaterial = new Material(null, envMap, 0);
		skyboxMaterial.lighting = false;
		skyboxMaterial.useEnvMapAsMap = true;
		Object3d skybox = new Object3d(skyboxGeometry, skyboxMaterial);
		addChild(skybox);

		Geometry teapotGeometry = new TeapotGeometry();
		Object3d teapotO3d = new Object3d(teapotGeometry, mirrorMat);
		teapotO3d.setPosition(0, 0, -0.5f);
		addChild(teapotO3d);

		addLight(new AmbientLight(new Color4(255, 255, 255), 0.5f));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0, 0, 255)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(255, 0, 0), 2));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0, 255, 0)));
	}
}

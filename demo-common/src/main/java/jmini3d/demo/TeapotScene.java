package jmini3d.demo;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Vector3;
import jmini3d.geometry.Geometry;
import jmini3d.geometry.SkyboxGeometry;
import jmini3d.geometry.VariableGeometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.PointLight;

public class TeapotScene extends Scene {

	public TeapotScene() {
		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx", "negx", "posy", "negy", "posz", "negz"});
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

		addLight(new AmbientLight(new Color4(0.4f, 0.4f, 0.4f, 1)));
		addLight(new PointLight(new Vector3(0, 2, 0), new Color4(0f, 0f, 1f, 1)));
		addLight(new PointLight(new Vector3(0, -2f, 0), new Color4(1f, 0f, 0f, 1)));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0f, 1f, 0f, 1)));
	}
}

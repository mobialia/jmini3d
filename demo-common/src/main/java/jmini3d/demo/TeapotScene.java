package jmini3d.demo;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.Vector3;
import jmini3d.geometry.Geometry;
import jmini3d.geometry.SkyboxGeometry;
import jmini3d.geometry.SpriteGeometry;
import jmini3d.geometry.VariableGeometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.PointLight;
import jmini3d.material.Material;
import jmini3d.material.SpriteMaterial;

public class TeapotScene extends Scene {

	Object3d buttonRight;
	Object3d buttonLeft;

	public TeapotScene() {
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

		SpriteMaterial buttonRightMaterial = new SpriteMaterial(new Texture("arrow_right.png"));
		SpriteGeometry buttonRightGeometry = new SpriteGeometry(1);
		buttonRightGeometry.addSprite(0, 0, 0, 0);
		buttonRight = new Object3d(buttonRightGeometry, buttonRightMaterial);
		addHudElement(buttonRight);

		SpriteMaterial buttonLeftMaterial = new SpriteMaterial(new Texture("arrow_left.png"));
		SpriteGeometry buttonLeftGeometry = new SpriteGeometry(1);
		buttonLeftGeometry.addSprite(0, 0, 0, 0);
		buttonLeft = new Object3d(buttonLeftGeometry, buttonLeftMaterial);
		addHudElement(buttonLeft);

		addLight(new AmbientLight(new Color4(150, 150, 150, 255)));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0, 0, 255, 255)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(255, 0, 0, 255)));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0, 255, 0, 255)));
	}

	/**
	 * Hud elements need viewport size, this method is also called each time that the viewport is changed
	 */
	@Override
	public void onViewPortChanged(int width, int height) {
		float buttonWidth = Math.min(width, height) / 5;

		((SpriteGeometry) buttonLeft.getGeometry3d()).setSpritePosition(0, 0, height - buttonWidth, buttonWidth, height);
		((SpriteGeometry) buttonRight.getGeometry3d()).setSpritePosition(0, width - buttonWidth, height - buttonWidth, width, height);
	}
}

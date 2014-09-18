package jmini3d.android.demo;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.Random;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Texture;
import jmini3d.Utils;
import jmini3d.Vector3;
import jmini3d.android.RendererActivity;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;
import jmini3d.geometry.SkyboxGeometry;
import jmini3d.geometry.VariableGeometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.PointLight;

public class DemoActivity extends RendererActivity {

	public static final String TAG = "DemoActivity";
	float cameraAngle;

	long initialTime;
	Object3d skybox;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		renderer.setLogFps(true);
	}

	public void onCreateSetContentView() {
		setContentView(R.layout.main);
		LinearLayout root = ((LinearLayout) findViewById(R.id.root));
		root.addView(renderer.getView());
	}


	public void initScene() {
		initialTime = System.currentTimeMillis();

		scene.backgroundColor.setAll(0x00000000);
		scene.getCamera().setTarget(0, 0, 0);
		scene.getCamera().setUpAxis(0, 0, 1);

		Random r = new Random();

		Texture map = new Texture("texture");

		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx", "negx", "posy", "negy", "posz", "negz"});

		VariableGeometry skyboxGeometry = new SkyboxGeometry(300);
		Material skyboxMaterial = new Material(null, envMap, 0);
		skyboxMaterial.lighting = false;
		skyboxMaterial.useEnvMapAsMap = true;
		skybox = new Object3d(skyboxGeometry, skyboxMaterial);
		scene.addChild(skybox);

//		Material material1 = new Material(map);
//		material1.setBlending(Blending.AdditiveBlending);
//
//		Material material2 = new Material(map);
//		material2.color.setAll(255, 0, 0, 128);
//
//		Material material3 = new Material(map, envMap, 0.3f);

//		for (int i = 0; i < 50; i++) {
//			float x = r.nextFloat() * 10 - 5;
//			float y = r.nextFloat() * 10 - 5;
//			float z = r.nextFloat() * 10 - 5;
//
//			VariableGeometry3d geometry = new VariableGeometry3d(24, 12);
//			geometry.addBox(new Vector3(-1, -1, 1), new Vector3(1, -1, 1), //
//					new Vector3(-1, -1, -1), new Vector3(1, -1, -1), //
//					new Vector3(-1, 1, 1), new Vector3(1, 1, 1), //
//					new Vector3(-1, 1, -1), new Vector3(1, 1, -1));
//			Object3d o3d;
//			if (i % 3 == 0) {
//				o3d = new Object3d(geometry, material1);
//			} else if (i % 3 == 1) {
//				o3d = new Object3d(geometry, material2);
//			} else {
//				o3d = new Object3d(geometry, material3);
//			}
//			o3d.setPosition(x, y, z);
//			scene.addChild(o3d);
//		}

		Material mirrorMat = new Material(null, envMap, 0.8f);
		Material textureMat = new Material(map);

		VariableGeometry geometry = new BoxGeometry(1);
		Object3d o3d = new Object3d(geometry, textureMat);
		scene.addChild(o3d);

		VariableGeometry sphereGeo = new VariableGeometry(40000, 20000);
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				sphereGeo.addQuad(getVectorSphere(i, j, 100, 100, 1), getVectorSphere(i + 1, j, 100, 100, 1), //
						getVectorSphere(i, j + 1, 100, 100, 1), getVectorSphere(i + 1, j + 1, 100, 100, 1));
			}
		}
		Object3d sphereO3d = new Object3d(sphereGeo, mirrorMat);
		sphereO3d.setPosition(0, -3, 0);
		scene.addChild(sphereO3d);

		Geometry planeGeometry = new TeapotGeometry();
		Object3d planeO3d = new Object3d(planeGeometry, mirrorMat);
		planeO3d.setPosition(0, 3, 0);
		scene.addChild(planeO3d);

		scene.addLight(new AmbientLight(new Color4(0.4f, 0.4f, 0.4f, 1)));
		scene.addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0f, 0f, 1f, 1)));
		scene.addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(1f, 0f, 0f, 1)));
		scene.addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0f, 1f, 0f, 1)));
	}

	@Override
	public boolean updateScene() {
		// Rotate camera...
		cameraAngle = 0.0005f * (System.currentTimeMillis() - initialTime);

		float d = 10;
		Vector3 target = scene.getCamera().getTarget();
		scene.getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
				(float) (target.y - d * Math.sin(cameraAngle)), //
				target.z + (float) (d * Math.sin(cameraAngle)) //
		);

		// Center the skybox in the camera to get better viewing results
		skybox.setPosition(scene.getCamera().getPosition().x, scene.getCamera().getPosition().y, scene.getCamera().getPosition().z);

		return true;
	}

	private Vector3 getVectorSphere(int m, int n, int maxm, int maxn, float r) {
		return new Vector3(r * ((float) (Math.sin(Utils.PI * m / maxm) * Math.sin(2 * Utils.PI * n / maxn))), //
				r * ((float) (Math.sin(Utils.PI * m / maxm) * Math.cos(2 * Utils.PI * n / maxn))), //
				r * ((float) Math.cos(Utils.PI * m / maxm)));
	}
}
package jmini3d.android.demo;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.Random;

import jmini3d.Blending;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Texture;
import jmini3d.VariableGeometry3d;
import jmini3d.Vector3;
import jmini3d.android.RendererActivity;

public class DemoActivity extends RendererActivity {

	public static final String TAG = "DemoActivity";
	float cameraAngle;

	//CubeMapTexture envMapTexture = new CubeMapTexture(new String[] { "posx", "negx", "posy", "negy", "posz", "negz" });
	//Material material = new Material(new Texture("texture"), envMapTexture, // 0.2f);
	Material material = new Material(new Texture("texture"));

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
		material.setBlending(Blending.AdditiveBlending);

		scene.backgroundColor.setAll(0x00000000);
		scene.getCamera().setTarget(0, 0, 0);
		scene.getCamera().setUpAxis(0, 0, 1);

		Random r = new Random();

		for (int i = 0; i < 75; i++) {
			float x = r.nextFloat() * 10 - 5;
			float y = r.nextFloat() * 10 - 5;
			float z = r.nextFloat() * 10 - 5;

			VariableGeometry3d geometry = new VariableGeometry3d(24, 12);
			geometry.addBox(new Vector3(-1, -1, 1), new Vector3(1, -1, 1), //
					new Vector3(-1, -1, -1), new Vector3(1, -1, -1), //
					new Vector3(-1, 1, 1), new Vector3(1, 1, 1), //
					new Vector3(-1, 1, -1), new Vector3(1, 1, -1));
			Object3d o3d = new Object3d(geometry, material);
			o3d.setPosition(x, y, z);
			scene.addChild(o3d);
		}
	}

	@Override
	public boolean updateScene() {
		// Rotate camera...
		cameraAngle += 0.01;

		float d = 20;
		Vector3 target = scene.getCamera().getTarget();
		scene.getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
				(float) (target.y - d * Math.sin(cameraAngle)), //
				target.z + d);

		return true;
	}
}
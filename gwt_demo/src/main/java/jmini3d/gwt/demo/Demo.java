package jmini3d.gwt.demo;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

import java.util.Random;

import jmini3d.Blending;
import jmini3d.CubeMapTexture;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.SceneController;
import jmini3d.Texture;
import jmini3d.VariableGeometry3d;
import jmini3d.Vector3;
import jmini3d.gwt.Renderer;
import jmini3d.gwt.ResourceLoader;

public class Demo implements EntryPoint, SceneController {

	public static final String TAG = "Demo";
	public Renderer renderer;
	public Scene scene;
	float cameraAngle;

	CubeMapTexture envMapTexture = new CubeMapTexture(new String[]{"posx", "negx", "posy", "negy", "posz", "negz"});
	// Material material = new Material(new Texture("texture"), envMapTexture,
	// 0.2f);

	Material material = new Material(new Texture("texture"));

	@Override
	public void onModuleLoad() {

		scene = new Scene(this);
		renderer = new Renderer(new ResourceLoader("./"), scene, Window.getClientWidth(), Window.getClientHeight());
		renderer.setLogFps(true);

		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				renderer.setSize(event.getWidth(), event.getHeight());
			}
		});

		appendToBody(renderer.getCanvas().getElement());

		renderer.onResume();
	}

	public static native void appendToBody(Element element) /*-{
		$doc.body.appendChild(element);
	}-*/;

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
package mini3d.gwt.demo;

import mini3d.CubeMapTexture;
import mini3d.Material;
import mini3d.Object3d;
import mini3d.Scene;
import mini3d.SceneController;
import mini3d.Texture;
import mini3d.VariableGeometry3d;
import mini3d.Vector3;
import mini3d.gwt.Renderer;
import mini3d.gwt.ResourceLoader;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class Demo implements EntryPoint, SceneController {

	public static final String TAG = "Demo";
	public Renderer renderer;
	public Scene scene;
	float cameraAngle;
	
	CubeMapTexture envMapTexture = new CubeMapTexture(new String[] { "posx", "negx", "posy", "negy", "posz", "negz" });
	// Material material = new Material(new Texture("texture"), envMapTexture,
	// 0.2f);

	Material material = new Material(new Texture("texture"));

	@Override
	public void onModuleLoad() {

		scene = new Scene(this);
		renderer = new Renderer(new ResourceLoader(), scene, Window.getClientWidth(), Window.getClientHeight());
		renderer.onResume();
		
		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				renderer.setSize(event.getWidth(), event.getHeight());
			}
		});

		RootPanel.get("canvas").add(renderer.getCanvas());
	}

	public void initScene() {
		scene.backgroundColor.setAll(0x00000000);
		scene.getCamera().setTarget(0, 0, 0);
		scene.getCamera().setUpAxis(0, 0, 1);

		VariableGeometry3d geometry = new VariableGeometry3d(24, 12);
		geometry.addBox(new Vector3(-1, -1, 1), new Vector3(1, -1, 1), //
				new Vector3(-1, -1, -1), new Vector3(1, -1, -1), //
				new Vector3(-1, 1, 1), new Vector3(1, 1, 1), //
				new Vector3(-1, 1, -1), new Vector3(1, 1, -1));
		scene.addChild(new Object3d(geometry, material));
	}

	@Override
	public boolean updateScene() {
		// Rotate camera...
		cameraAngle += 0.01;

		float d = 10;
		Vector3 target = scene.getCamera().getTarget();
		scene.getCamera().setPosition((float) (target.x - d * Math.cos(cameraAngle)), //
				(float) (target.y - d * Math.sin(cameraAngle)), //
				(float) (target.z + d));

		return true;
	}
}
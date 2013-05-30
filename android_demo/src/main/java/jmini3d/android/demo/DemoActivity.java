package jmini3d.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import jmini3d.CubeMapTexture;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.VariableGeometry3d;
import jmini3d.Vector3;
import jmini3d.android.Renderer;
import jmini3d.android.RendererActivity;

public class DemoActivity extends RendererActivity {

    public static final String TAG = "Demo";
    float cameraAngle;

    //CubeMapTexture envMapTexture = new CubeMapTexture(new String[] { "posx", "negx", "posy", "negy", "posz", "negz" });
    //Material material = new Material(new Texture("texture"), envMapTexture, // 0.2f);
    Material material = new Material(new Texture("texture"));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onCreateSetContentView() {
        setContentView(R.layout.main);
        LinearLayout root = ((LinearLayout) findViewById(R.id.root));
        root.addView(renderer.getView());
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
                target.z + d);

        return true;
    }
}
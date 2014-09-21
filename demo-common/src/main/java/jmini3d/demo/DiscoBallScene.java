package jmini3d.demo;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Utils;
import jmini3d.Vector3;
import jmini3d.geometry.VariableGeometry;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.PointLight;

public class DiscoBallScene extends Scene {

	public DiscoBallScene() {
		CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx", "negx", "posy", "negy", "posz", "negz"});
		Material mirrorMat = new Material(null, envMap, 0.8f);

		VariableGeometry sphereGeo = new VariableGeometry(40000, 20000);
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				sphereGeo.addQuad(getVectorSphere(i, j, 100, 100, 1), getVectorSphere(i + 1, j, 100, 100, 1), //
						getVectorSphere(i, j + 1, 100, 100, 1), getVectorSphere(i + 1, j + 1, 100, 100, 1));
			}
		}
		Object3d sphereO3d = new Object3d(sphereGeo, mirrorMat);
		addChild(sphereO3d);

		addLight(new AmbientLight(new Color4(0.4f, 0.4f, 0.4f, 1)));
		addLight(new PointLight(new Vector3(0, 50, 0), new Color4(0f, 0f, 1f, 1)));
		addLight(new PointLight(new Vector3(0, -1.1f, 0), new Color4(1f, 0f, 0f, 1)));
		addLight(new DirectionalLight(new Vector3(1, 0, 0), new Color4(0f, 1f, 0f, 1)));
	}

	private Vector3 getVectorSphere(int m, int n, int maxm, int maxn, float r) {
		return new Vector3(r * ((float) (Math.sin(Utils.PI * m / maxm) * Math.sin(2 * Utils.PI * n / maxn))), //
				r * ((float) (Math.sin(Utils.PI * m / maxm) * Math.cos(2 * Utils.PI * n / maxn))), //
				r * ((float) Math.cos(Utils.PI * m / maxm)));
	}
}

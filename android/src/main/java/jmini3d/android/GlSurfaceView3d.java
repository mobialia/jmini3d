package jmini3d.android;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jmini3d.Scene;
import jmini3d.SceneController;
import jmini3d.android.compat.CompatibilityWrapper5;

public class GlSurfaceView3d extends GLSurfaceView implements GLSurfaceView.Renderer {
	public static final String TAG = "GlSurfaceView3d";

	int width, height;

	Renderer3d renderer3d;
	SceneController sceneController;
	boolean renderContinuously;
	int forceRedraw = 0;

	GL10 gl;

	public GlSurfaceView3d(Context ctx, boolean renderContinuously, boolean traslucent) {
		super(ctx);
		this.renderContinuously = renderContinuously;

		setEGLContextClientVersion(2);
		if (traslucent) {
			if (Build.VERSION.SDK_INT >= 5) {
				CompatibilityWrapper5.setZOrderOnTop(this);
			}
			setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			getHolder().setFormat(PixelFormat.TRANSLUCENT);
		} else {
			setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			getHolder().setFormat(PixelFormat.RGBA_8888);
		}

		renderer3d = new Renderer3d(new ResourceLoader(ctx));

		setRenderer(this);
		setRenderMode(renderContinuously ? RENDERMODE_CONTINUOUSLY : RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
		this.gl = gl;
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int w, int h) {
		width = w;
		height = h;
		renderer3d.reset();
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		if (sceneController != null) {
			if (sceneController.updateScene(width, height)) {
				// due to buffer swapping we need to redraw three frames
				forceRedraw = 3;
			}

			if (forceRedraw > 0) {
				Scene scene = sceneController.getScene();
				if (scene != null) {
					forceRedraw--;
					renderer3d.render(scene);
				}
			}
		}
	}

	public void requestRender() {
		forceRedraw = 3;
		if (renderContinuously) {
			return;
		}
		super.requestRender();
	}

	public void setRenderContinuously(boolean renderContinuously) {
		if (this.renderContinuously != renderContinuously) {
			this.renderContinuously = renderContinuously;
			setRenderMode(renderContinuously ? RENDERMODE_CONTINUOUSLY : RENDERMODE_WHEN_DIRTY);
		}
	}

	public void setSceneController(SceneController sceneController) {
		this.sceneController = sceneController;
	}

	public Renderer3d getRenderer3d() {
		return renderer3d;
	}

	public GL10 getGl() {
		return gl;
	}
}

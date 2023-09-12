package jmini3d.android;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jmini3d.ScreenController;
import jmini3d.android.compat.CompatibilityWrapper5;

public class GlSurfaceView3d extends GLSurfaceView implements GLSurfaceView.Renderer {
	public static final String TAG = GlSurfaceView3d.class.getName();
	public static final int FRAMES_TO_DRAW = 4;

	int width, height;

	Renderer3d renderer3d;
	ScreenController screenController;
	int forceRedraw = 0;

	GL10 gl;

	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 10000;
	private boolean logFps = false;
	private long frameCount = 0;
	private float fps = 0;
	private long timeLastSample;

	public GlSurfaceView3d(Context ctx, boolean translucent) {
		super(ctx);

		setEGLContextClientVersion(2);
		if (translucent) {
			CompatibilityWrapper5.setZOrderOnTop(this);
			setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			getHolder().setFormat(PixelFormat.TRANSLUCENT);
		} else {
			setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			getHolder().setFormat(PixelFormat.RGBA_8888);
		}

		renderer3d = new Renderer3d(new ResourceLoader(ctx));

		setRenderer(this);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
		this.gl = gl;
		renderer3d.reset();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int w, int h) {
		width = w;
		height = h;
		renderer3d.setViewPort(width, height);
		requestRender();
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		if (screenController != null) {

			if (screenController.onNewFrame(forceRedraw > 0)) {
				// due to buffer swapping we need to redraw three frames
				forceRedraw = FRAMES_TO_DRAW;
			}

			if (forceRedraw > 0) {
				screenController.render(renderer3d);
				forceRedraw--;
			}
		}
		if (logFps) {
			doFps();
		}
	}

	public void requestRender() {
		forceRedraw = FRAMES_TO_DRAW;
	}

	public void setScreenController(ScreenController screenController) {
		this.screenController = screenController;
	}

	public Renderer3d getRenderer3d() {
		return renderer3d;
	}

	public GL10 getGl() {
		return gl;
	}

	/**
	 * If true, framerate and memory is periodically calculated and Log'ed, and
	 * gettable thru fps()
	 */
	public void setLogFps(boolean b) {
		logFps = b;

		if (logFps) { // init
			timeLastSample = System.currentTimeMillis();
			frameCount = 0;
		}
	}

	private void doFps() {
		frameCount++;

		long now = System.currentTimeMillis();
		long delta = now - timeLastSample;
		if (delta >= FRAMERATE_SAMPLEINTERVAL_MS) {
			fps = frameCount / (delta / 1000f);

			Log.v(TAG, "FPS: " + fps);

			timeLastSample = now;
			frameCount = 0;
		}
	}

	/**
	 * Returns last sampled framerate (logFps must be set to true)
	 */
	public float getFps() {
		return fps;
	}
}

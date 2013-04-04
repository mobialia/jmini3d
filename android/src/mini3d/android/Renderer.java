package mini3d.android;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import mini3d.Light;
import mini3d.MatrixUtils;
import mini3d.Object3d;
import mini3d.Scene;
import mini3d.Texture;
import mini3d.android.compat.CompatibilityWrapper5;
import mini3d.android.input.TouchController;
import mini3d.input.TouchListener;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;

public class Renderer implements GLSurfaceView.Renderer {
	public static final String TAG = "Renderer";

	private GL10 gl;

	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 1000;
	private boolean logFps = false;
	private long frameCount = 0;
	private float fps = 0;
	private long timeLastSample;

	Scene scene;
	GpuUploader gpuUploader;
	ResourceLoader resourceLoader;

	public static boolean needsRedraw;

	float openGlVersion = 1.0f;
	private ActivityManager activityManager;
	private ActivityManager.MemoryInfo memoryInfo;

	public float[] ortho = new float[16];

	public GLSurfaceView glSurfaceView;

	TouchController touchController;

	public Renderer(Context context, Scene scene, ResourceLoader resourceLoader, boolean traslucent) {
		this.scene = scene;
		this.resourceLoader = resourceLoader;

		activityManager = (ActivityManager) resourceLoader.getContext().getSystemService(Context.ACTIVITY_SERVICE);
		memoryInfo = new ActivityManager.MemoryInfo();

		MatrixUtils.ortho(ortho, 0, 1, 0, 1, -5, 1);

		gpuUploader = new GpuUploader(resourceLoader);

		glSurfaceView = new GLSurfaceView(context);

		if (traslucent) {
			if (Build.VERSION.SDK_INT >= 5) {
				CompatibilityWrapper5.setZOrderOnTop(glSurfaceView);
			}
			glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		} else {
			// glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // FAILS with EGL_BAD_MATCH in some devices (Galaxy Mini, Xperia X)
			glSurfaceView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
				public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
					int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
					EGLConfig[] configs = new EGLConfig[1];
					int[] result = new int[1];
					egl.eglChooseConfig(display, attributes, configs, 1, result);
					return configs[0];
				}
			});
		}

		glSurfaceView.setRenderer(this);
		glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		glSurfaceView.setFocusable(true); // make sure we get key events
		glSurfaceView.setFocusableInTouchMode(true);
		glSurfaceView.requestFocus();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
		Log.i(TAG, "onSurfaceCreated()");
		gpuUploader.setGl(gl);
		setGl(gl);
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		Log.i(TAG, "onSurfaceChanged()");
		setSize(w, h);
	}
	
	public void setSize(int width, int height) {
		scene.camera.setWidth(width);
		scene.camera.setHeight(height);
		gl.glViewport(0, 0, scene.camera.getWidth(), scene.camera.getHeight());

		reset();
		gpuUploader.reset();
		scene.reset();
		scene.sceneController.initScene();
	}

	public void onPause() {
		glSurfaceView.onPause();
	}

	public void onResume() {
		glSurfaceView.onResume();
	}

	private void reset() {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glClearDepthf(1f);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glDepthRangef(0, 1f);
		gl.glDepthMask(true);

		gl.glDisable(GL10.GL_DITHER); // For performance
		// Without this looks horrible in the emulator
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

		// Alpha enabled
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		// CCW frontfaces only, by default
		gl.glFrontFace(GL10.GL_CCW);
		gl.glCullFace(GL10.GL_BACK);
		gl.glEnable(GL10.GL_CULL_FACE);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

		// Optimizations
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_COLOR_MATERIAL);

		// Disable lights by default
		for (int i = GL10.GL_LIGHT0; i <= GL10.GL_LIGHT7; i++) {
			gl.glDisable(i);
		}
	}

	public void onDrawFrame(GL10 gl) {
		boolean sceneUpdated = scene.sceneController.updateScene();
		boolean cameraChanged = scene.getCamera().updateMatrices();

		if (!needsRedraw && !sceneUpdated && !cameraChanged) {
			return;
		}

		for (Object o : scene.unload) {
			gpuUploader.unload(o);
		}
		scene.unload.clear();

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMultMatrixf(scene.camera.perspectiveMatrix, 0);

		// Camera
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glMultMatrixf(scene.camera.modelViewMatrix, 0);

		drawSetupLights();

		// Background color
		gl.glClearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		for (Object3d o3d : scene.children) {
			o3d.updateMatrices(MatrixUtils.IDENTITY4, false);

			gl.glPushMatrix();
			gl.glMultMatrixf(o3d.modelViewMatrix, 0);
			drawObject(o3d, cameraChanged);
			gl.glPopMatrix();

			if (o3d.clearDepthAfterDraw) {
				gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);
			}
		}

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMultMatrixf(ortho, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);

		for (Object3d o3d : scene.hud) {
			o3d.updateMatrices(MatrixUtils.IDENTITY4, false);

			gl.glPushMatrix();
			gl.glMultMatrixf(o3d.modelViewMatrix, 0);
			drawObject(o3d, false);
			gl.glPopMatrix();
		}

		if (logFps) {
			doFps();
		}
	}

	protected void drawSetupLights() {
		boolean hasLights = false;
		int lightIndex = GL10.GL_LIGHT0;
		for (Light light : scene.lights) {
			hasLights = true;

			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(lightIndex);

			gl.glLightfv(lightIndex, GL10.GL_POSITION, light.position, 0);
			gl.glLightfv(lightIndex, GL10.GL_AMBIENT, light.ambient, 0);
			gl.glLightfv(lightIndex, GL10.GL_DIFFUSE, light.diffuse, 0);
			gl.glLightfv(lightIndex, GL10.GL_SPECULAR, light.specular, 0);
			gl.glLightfv(lightIndex, GL10.GL_EMISSION, light.emission, 0);

			gl.glLightfv(lightIndex, GL10.GL_SPOT_DIRECTION, light.direction, 0);
			gl.glLightf(lightIndex, GL10.GL_SPOT_CUTOFF, light.spotCutoffAngle);
			gl.glLightf(lightIndex, GL10.GL_SPOT_EXPONENT, light.spotExponent);

			gl.glLightf(lightIndex, GL10.GL_CONSTANT_ATTENUATION, light.attenuation[0]);
			gl.glLightf(lightIndex, GL10.GL_LINEAR_ATTENUATION, light.attenuation[1]);
			gl.glLightf(lightIndex, GL10.GL_QUADRATIC_ATTENUATION, light.attenuation[2]);

			lightIndex++;
		}

		if (hasLights) {
			gl.glShadeModel(GL10.GL_SMOOTH);
		}
	}

	protected void drawObject(Object3d o3d, boolean cameraChanged) {
		if (!o3d.visible) {
			return;
		}

		GeometryBuffers geometryBuffers = gpuUploader.upload(o3d.geometry3d);

		Texture texture = o3d.material.texture;
		if (texture != null) {
			gpuUploader.upload(texture);
		}

		// if (o3d.material.envMapTexture != null) {
		// gpuUploader.upload(o3d.material.envMapTexture);
		// }

		// Normals
		if (openGlVersion >= 1.1) {
			((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, geometryBuffers.normalsBufferId);
			((GL11) gl).glNormalPointer(GL10.GL_FLOAT, 0, 0);
		} else {
			geometryBuffers.normalsBuffer.position(0);
			gl.glNormalPointer(GL10.GL_FLOAT, 0, geometryBuffers.normalsBuffer);
		}
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

		// } else {
		// gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		// }

		// boolean useLighting = (scene.getLightingEnabled() && o.hasNormals()
		// && o.normalsEnabled() && o.lightingEnabled());
		// if (useLighting) {
		// gl.glEnable(GL10.GL_LIGHTING);
		// } else {
		// gl.glDisable(GL10.GL_LIGHTING);
		// }

		if (texture != null) {
			gl.glActiveTexture(GL10.GL_TEXTURE0);
			gl.glClientActiveTexture(GL10.GL_TEXTURE0);

			if (openGlVersion >= 1.1) {
				((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, geometryBuffers.uvsBufferId);
				((GL11) gl).glTexCoordPointer(2, GL10.GL_FLOAT, 0, 0);
				((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			} else {
				FloatBuffer uvsBuffer = geometryBuffers.uvsBuffer;
				uvsBuffer.position(0);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, uvsBuffer);
			}

			int glId = gpuUploader.textures.get(texture);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, glId);
			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		} else {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
			gl.glDisable(GL10.GL_TEXTURE_2D);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}

		if (scene.lights.size() > 0) {
			if (o3d.material.color != null && o3d.material.color.a != 0) {
				float params[] = { o3d.material.color.r, o3d.material.color.g, o3d.material.color.b, 1 };
				float materialShininess = 50f;
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, params, 0);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, params, 0);
				gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, materialShininess);
			} else {
				float materialAmbientAndDiffuse[] = { 1f, 1f, 1f, 1f };
				float materialSpecular[] = { 1f, 1f, 1f, 1f };
				float materialShininess = 50f;
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, materialAmbientAndDiffuse, 0);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, materialSpecular, 0);
				gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, materialShininess);
			}
		}

		// Draw
		if (openGlVersion >= 1.1) {
			((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, geometryBuffers.vertexBufferId);
			((GL11) gl).glVertexPointer(3, GL10.GL_FLOAT, 0, 0);
			((GL11) gl).glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, geometryBuffers.facesBufferId);
			((GL11) gl).glDrawElements(GL10.GL_TRIANGLES, o3d.geometry3d.facesLength, GL10.GL_UNSIGNED_SHORT, 0);

			((GL11) gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			((GL11) gl).glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
		} else {
			FloatBuffer vertexBuffer = geometryBuffers.vertexBuffer;
			vertexBuffer.position(0);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

			ShortBuffer facesBuffer = geometryBuffers.facesBuffer;
			facesBuffer.position(0);
			gl.glDrawElements(GL10.GL_TRIANGLES, o3d.geometry3d.facesLength, GL10.GL_UNSIGNED_SHORT, facesBuffer);
		}
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

	private void setGl(GL10 gl) {
		this.gl = gl;

		// OpenGL ES version
		if (gl instanceof GL11) {
			openGlVersion = 1.1f;
		} else {
			openGlVersion = 1.0f;
		}
	}

	public GL10 getGl() {
		return gl;
	}

	public GpuUploader getGpuUploader() {
		return gpuUploader;
	}

	private void doFps() {
		frameCount++;

		long now = System.currentTimeMillis();
		long delta = now - timeLastSample;
		if (delta >= FRAMERATE_SAMPLEINTERVAL_MS) {
			fps = frameCount / (delta / 1000f);

			activityManager.getMemoryInfo(memoryInfo);
			Log.v(TAG, "FPS: " + Math.round(fps) + ", availMem: " + Math.round(memoryInfo.availMem / 1048576) + "MB");

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

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public GLSurfaceView getView() {
		return glSurfaceView;
	}

	public void setTouchListener(TouchListener listener) {
		if (touchController == null) {
			touchController = new TouchController(glSurfaceView);
		}
		touchController.setListener(listener);
	}

}
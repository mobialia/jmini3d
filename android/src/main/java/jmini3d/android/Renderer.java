package jmini3d.android;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import jmini3d.Blending;
import jmini3d.Light;
import jmini3d.MatrixUtils;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.android.compat.CompatibilityWrapper5;
import jmini3d.android.input.TouchController;
import jmini3d.input.TouchListener;

public class Renderer implements GLSurfaceView.Renderer {
	public static final String TAG = "Renderer";
	public static boolean needsRedraw = true;

	private GL10 gl;

	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 1000;
	private boolean logFps = false;
	private long frameCount = 0;
	private float fps = 0;
	private long timeLastSample;

	Scene scene;
	private ResourceLoader resourceLoader;
	private GpuUploader gpuUploader;
	private TouchController touchController;

	public float[] ortho = new float[16];

	int width;
	int height;

	public GLSurfaceView glSurfaceView;
	private ActivityManager activityManager;
	private ActivityManager.MemoryInfo memoryInfo;

	Blending blending;

	float openGlVersion = 1.0f;

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
					int[] attributes = new int[]{EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE};
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

		width = -1;
		height = -1;
	}

	public void onSurfaceChanged(GL10 unused, int w, int h) {
		Log.i(TAG, "onSurfaceChanged() w=" + w + " h= " + h);
		if (w != width || h != height) {
			setSize(w, h);
			width = w;
			height = h;
		}
	}

	public void onPause() {
		glSurfaceView.onPause();
	}

	public void onResume() {
		glSurfaceView.onResume();
	}

	public void setSize(int width, int height) {
		scene.camera.setWidth(width);
		scene.camera.setHeight(height);
		GLES10.glViewport(0, 0, scene.camera.getWidth(), scene.camera.getHeight());

		// Scene reload on size changed, needed to keep aspect ratios
		reset();
		gpuUploader.reset();
		scene.reset();
		scene.sceneController.initScene();
	}

	private void reset() {
		GLES10.glMatrixMode(GLES10.GL_PROJECTION);
		GLES10.glLoadIdentity();

		GLES10.glEnable(GLES10.GL_DEPTH_TEST);
		GLES10.glClearDepthf(1f);
		GLES10.glDepthFunc(GLES10.GL_LEQUAL);
		GLES10.glDepthRangef(0, 1f);
		GLES10.glDepthMask(true);

		GLES10.glDisable(GLES10.GL_DITHER); // For performance
		// Without this looks horrible in the emulator
		GLES10.glHint(GLES10.GL_PERSPECTIVE_CORRECTION_HINT, GLES10.GL_NICEST);

		// For transparency
		GLES10.glDisable(GLES10.GL_BLEND);
		blending = Blending.NoBlending;

		// CCW frontfaces only, by default
		GLES10.glFrontFace(GLES10.GL_CCW);
		GLES10.glCullFace(GLES10.GL_BACK);
		GLES10.glEnable(GLES10.GL_CULL_FACE);

		GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
		GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);

		// Optimizations
		GLES10.glDisable(GLES10.GL_LIGHTING);
		GLES10.glDisable(GLES10.GL_COLOR_MATERIAL);

		// Disable lights by default
		for (int i = GLES10.GL_LIGHT0; i <= GLES10.GL_LIGHT7; i++) {
			GLES10.glDisable(i);
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

		GLES10.glMatrixMode(GLES10.GL_PROJECTION);
		GLES10.glLoadIdentity();
		GLES10.glMultMatrixf(scene.camera.perspectiveMatrix, 0);

		// Camera
		GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
		GLES10.glLoadIdentity();
		GLES10.glMultMatrixf(scene.camera.modelViewMatrix, 0);

		drawSetupLights();

		// Background color
		GLES10.glClearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT | GLES10.GL_DEPTH_BUFFER_BIT);

		for (Object3d o3d : scene.children) {
			if (o3d.visible) {
                o3d.updateMatrices(MatrixUtils.IDENTITY4, false);
    
                GLES10.glPushMatrix();
                GLES10.glMultMatrixf(o3d.modelViewMatrix, 0);
                drawObject(o3d, cameraChanged);
                GLES10.glPopMatrix();
    
                if (o3d.clearDepthAfterDraw) {
                    GLES10.glClear(GLES10.GL_DEPTH_BUFFER_BIT);
                }
			}
		}

		GLES10.glMatrixMode(GLES10.GL_PROJECTION);
		GLES10.glLoadIdentity();
		GLES10.glMultMatrixf(ortho, 0);
		GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
		GLES10.glLoadIdentity();
		GLES10.glDisable(GLES10.GL_LIGHTING);
		GLES10.glClear(GLES10.GL_DEPTH_BUFFER_BIT);

		for (Object3d o3d : scene.hud) {
			if (o3d.visible) {
                o3d.updateMatrices(MatrixUtils.IDENTITY4, false);
    
                GLES10.glPushMatrix();
                GLES10.glMultMatrixf(o3d.modelViewMatrix, 0);
                drawObject(o3d, false);
                GLES10.glPopMatrix();
			}
		}

		if (logFps) {
			doFps();
		}
	}

	protected void drawSetupLights() {
		boolean hasLights = false;
		int lightIndex = GLES10.GL_LIGHT0;
		for (Light light : scene.lights) {
			hasLights = true;

			GLES10.glEnable(GLES10.GL_LIGHTING);
			GLES10.glEnable(lightIndex);

			GLES10.glLightfv(lightIndex, GLES10.GL_POSITION, light.position, 0);
			GLES10.glLightfv(lightIndex, GLES10.GL_AMBIENT, light.ambient, 0);
			GLES10.glLightfv(lightIndex, GLES10.GL_DIFFUSE, light.diffuse, 0);
			GLES10.glLightfv(lightIndex, GLES10.GL_SPECULAR, light.specular, 0);
			GLES10.glLightfv(lightIndex, GLES10.GL_EMISSION, light.emission, 0);

			GLES10.glLightfv(lightIndex, GLES10.GL_SPOT_DIRECTION, light.direction, 0);
			GLES10.glLightf(lightIndex, GLES10.GL_SPOT_CUTOFF, light.spotCutoffAngle);
			GLES10.glLightf(lightIndex, GLES10.GL_SPOT_EXPONENT, light.spotExponent);

			GLES10.glLightf(lightIndex, GLES10.GL_CONSTANT_ATTENUATION, light.attenuation[0]);
			GLES10.glLightf(lightIndex, GLES10.GL_LINEAR_ATTENUATION, light.attenuation[1]);
			GLES10.glLightf(lightIndex, GLES10.GL_QUADRATIC_ATTENUATION, light.attenuation[2]);

			lightIndex++;
		}

		if (hasLights) {
			GLES10.glShadeModel(GLES10.GL_SMOOTH);
		}
	}

	protected void drawObject(Object3d o3d, boolean cameraChanged) {
		GeometryBuffers geometryBuffers = gpuUploader.upload(o3d.geometry3d);

		if (blending != o3d.material.blending) {
			setBlending(o3d.material.blending);
		}

		Texture texture = o3d.material.texture;
		if (texture != null) {
			gpuUploader.upload(texture);
		}

		// if (o3d.material.envMapTexture != null) {
		// gpuUploader.upload(o3d.material.envMapTexture);
		// }

		// Normals
		if (openGlVersion >= 1.1) {
			GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, geometryBuffers.normalsBufferId);
			GLES11.glNormalPointer(GLES10.GL_FLOAT, 0, 0);
		} else {
			geometryBuffers.normalsBuffer.position(0);
			GLES10.glNormalPointer(GLES10.GL_FLOAT, 0, geometryBuffers.normalsBuffer);
		}
		GLES10.glEnableClientState(GLES10.GL_NORMAL_ARRAY);

		// } else {
		// GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
		// }

		// boolean useLighting = (scene.getLightingEnabled() && o.hasNormals()
		// && o.normalsEnabled() && o.lightingEnabled());
		// if (useLighting) {
		// GLES10.glEnable(GLES10.GL_LIGHTING);
		// } else {
		// GLES10.glDisable(GLES10.GL_LIGHTING);
		// }

		if (texture != null) {
			GLES10.glActiveTexture(GLES10.GL_TEXTURE0);
			GLES10.glClientActiveTexture(GLES10.GL_TEXTURE0);

			if (openGlVersion >= 1.1) {
				((GL11) gl).glBindBuffer(GLES11.GL_ARRAY_BUFFER, geometryBuffers.uvsBufferId);
				((GL11) gl).glTexCoordPointer(2, GLES10.GL_FLOAT, 0, 0);
				((GL11) gl).glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
			} else {
				FloatBuffer uvsBuffer = geometryBuffers.uvsBuffer;
				uvsBuffer.position(0);
				GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, uvsBuffer);
			}

			int glId = gpuUploader.textures.get(texture);
			GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, glId);
			GLES10.glEnable(GLES10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
		} else {
			GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, 0);
			GLES10.glDisable(GLES10.GL_TEXTURE_2D);
			GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
		}

		if (scene.lights.size() > 0) {
			if (o3d.material.color != null && o3d.material.color.a != 0) {
				float params[] = {o3d.material.color.r, o3d.material.color.g, o3d.material.color.b, 1};
				float materialShininess = 50f;
				GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_AMBIENT_AND_DIFFUSE, params, 0);
				GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SPECULAR, params, 0);
				GLES10.glMaterialf(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SHININESS, materialShininess);
			} else {
				float materialAmbientAndDiffuse[] = {1f, 1f, 1f, 1f};
				float materialSpecular[] = {1f, 1f, 1f, 1f};
				float materialShininess = 50f;
				GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_AMBIENT_AND_DIFFUSE, materialAmbientAndDiffuse, 0);
				GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SPECULAR, materialSpecular, 0);
				GLES10.glMaterialf(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SHININESS, materialShininess);
			}
		}

		// Draw
		if (openGlVersion >= 1.1) {
			((GL11) gl).glBindBuffer(GLES11.GL_ARRAY_BUFFER, geometryBuffers.vertexBufferId);
			((GL11) gl).glVertexPointer(3, GLES10.GL_FLOAT, 0, 0);
			((GL11) gl).glBindBuffer(GLES11.GL_ELEMENT_ARRAY_BUFFER, geometryBuffers.facesBufferId);
			((GL11) gl).glDrawElements(GLES10.GL_TRIANGLES, o3d.geometry3d.facesLength, GLES10.GL_UNSIGNED_SHORT, 0);

			((GL11) gl).glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
			((GL11) gl).glBindBuffer(GLES11.GL_ELEMENT_ARRAY_BUFFER, 0);
		} else {
			FloatBuffer vertexBuffer = geometryBuffers.vertexBuffer;
			vertexBuffer.position(0);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, vertexBuffer);

			ShortBuffer facesBuffer = geometryBuffers.facesBuffer;
			facesBuffer.position(0);
			GLES10.glDrawElements(GLES10.GL_TRIANGLES, o3d.geometry3d.facesLength, GLES10.GL_UNSIGNED_SHORT, facesBuffer);
		}
	}

	private void setBlending(Blending blending) {
		this.blending = blending;

		switch(blending) {
			case NoBlending:
				GLES10.glDisable(GLES10.GL_BLEND);
				break;
			case NormalBlending:
				GLES10.glEnable(GLES10.GL_BLEND);
				GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
				break;
			case AdditiveBlending:
				GLES10.glEnable(GLES10.GL_BLEND);
				GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE);
				break;
			case SubtractiveBlending:
				GLES10.glEnable(GLES10.GL_BLEND);
				GLES10.glBlendFunc(GLES10.GL_ZERO, GLES10.GL_ONE_MINUS_SRC_COLOR);
				break;
			case MultiplyBlending:
				GLES10.glEnable(GLES10.GL_BLEND);
				GLES10.glBlendFunc(GLES10.GL_ZERO, GLES10.GL_SRC_COLOR);
				break;
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
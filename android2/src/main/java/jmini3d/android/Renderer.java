package jmini3d.android;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;

import com.mobialia.min3d.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jmini3d.Blending;
import jmini3d.GpuObjectStatus;
import jmini3d.MatrixUtils;
import jmini3d.Object3d;
import jmini3d.Scene;
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

	boolean stop = false;

	private int shaderProgram;
	private int vertexPositionAttribute, vertexNormalAttribute, textureCoordAttribute;
	private int uPerspectiveMatrix, uModelViewMatrix, //
			uNormalMatrix, uUseLighting, uAmbientColor, uPointLightingLocation, //
			uPointLightingColor, uSampler, uEnvMap, uReflectivity, uObjectColor, uObjectColorTrans;

	public Renderer(Context context, Scene scene, ResourceLoader resourceLoader, boolean traslucent) {
		this.scene = scene;
		this.resourceLoader = resourceLoader;

		activityManager = (ActivityManager) resourceLoader.getContext().getSystemService(Context.ACTIVITY_SERVICE);
		memoryInfo = new ActivityManager.MemoryInfo();

		MatrixUtils.ortho(ortho, 0, 1, 0, 1, -5, 1);

		gpuUploader = new GpuUploader(resourceLoader);

		glSurfaceView = new GLSurfaceView(context);
		glSurfaceView.setEGLContextClientVersion(2);

		// TODO
		//glSurfaceView.setPreserveEGLContextOnPause(true);

		if (traslucent) {
			if (Build.VERSION.SDK_INT >= 5) {
				CompatibilityWrapper5.setZOrderOnTop(glSurfaceView);
			}
			glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}

		glSurfaceView.setRenderer(this);
		glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		glSurfaceView.setFocusable(true); // make sure we get key events
		glSurfaceView.setFocusableInTouchMode(true);
		glSurfaceView.requestFocus();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
		Log.i(TAG, "onSurfaceCreated()");
		this.gl = gl;
		initShaders();

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

		GLES20.glViewport(0, 0, scene.camera.getWidth(), scene.camera.getHeight());

		// Scene reload on size changed, needed to keep aspect ratios
		reset();
		gpuUploader.reset();
		scene.reset();
		scene.sceneController.initScene();

		needsRedraw = true;
	}

	private void reset() {
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glClearDepthf(1f);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthRangef(0, 1f);
		GLES20.glDepthMask(true);

		// For performance
		GLES20.glDisable(GLES20.GL_DITHER);

		// For transparency
		GLES20.glDisable(GLES20.GL_BLEND);
		blending = Blending.NoBlending;

		// CCW frontfaces only, by default
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
	}

	public void onDrawFrame(GL10 unused) {
		boolean sceneUpdated = scene.sceneController.updateScene();
		boolean cameraChanged = scene.camera.updateMatrices();

		if (!needsRedraw && !sceneUpdated && !cameraChanged) {
			return;
		}

		for (int i = 0; i < scene.unload.size(); i++) {
			gpuUploader.unload(scene.unload.get(i));
		}
		scene.unload.clear();

		needsRedraw = false;

		GLES20.glClearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		setSceneUniforms();

		for (int i = 0; i < scene.children.size(); i++) {
			Object3d o3d = scene.children.get(i);
			if (o3d.visible) {
				o3d.updateMatrices(scene.camera.modelViewMatrix, cameraChanged);
				drawObject(o3d);

				if (o3d.clearDepthAfterDraw) {
					GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
				}
			}
		}

		GLES20.glUniformMatrix4fv(uPerspectiveMatrix, 1, false, ortho, 0);

		for (int i = 0; i < scene.hud.size(); i++) {
			Object3d o3d = scene.hud.get(i);
			if (o3d.visible) {
				o3d.updateMatrices(MatrixUtils.IDENTITY4, false);
				drawObject(o3d);
			}
		}
		if (logFps) {
			doFps();
		}
	}

	private void drawObject(Object3d o3d) {
		GeometryBuffers buffers = gpuUploader.upload(o3d.geometry3d);

		if (blending != o3d.material.blending) {
			setBlending(o3d.material.blending);
		}

		if (o3d.material.texture != null) {
			gpuUploader.upload(o3d.material.texture);
			if ((o3d.material.texture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (o3d.material.envMapTexture != null) {
			gpuUploader.upload(o3d.material.envMapTexture);
			if ((o3d.material.envMapTexture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}

		setMaterialUniforms(o3d);
		setObjectUniforms(o3d);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.vertexBufferId);
		GLES20.glVertexAttribPointer(vertexPositionAttribute, 3, GLES20.GL_FLOAT, false, 0, 0);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.normalsBufferId);
		GLES20.glVertexAttribPointer(vertexNormalAttribute, 3, GLES20.GL_FLOAT, false, 0, 0);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.uvsBufferId);
		GLES20.glVertexAttribPointer(textureCoordAttribute, 2, GLES20.GL_FLOAT, false, 0, 0);

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gpuUploader.textures.get(o3d.material.texture));

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, o3d.geometry3d.facesLength, GLES20.GL_UNSIGNED_SHORT, 0);
	}

	private void setSceneUniforms() {
		GLES20.glUniform1i(uSampler, 0);

		GLES20.glUniform1i(uUseLighting, 1);
		GLES20.glUniform3f(uAmbientColor, scene.ambientColor.r, scene.ambientColor.g, scene.ambientColor.b);
		GLES20.glUniform3f(uPointLightingColor, scene.pointLightColor.r, scene.pointLightColor.g, scene.pointLightColor.b);
		GLES20.glUniform3f(uPointLightingLocation, scene.pointLightLocation.x, scene.pointLightLocation.y, scene.pointLightLocation.z);

		GLES20.glUniformMatrix4fv(uPerspectiveMatrix, 1, false, scene.camera.perspectiveMatrix, 0);
	}

	private void setMaterialUniforms(Object3d o3d) {
		GLES20.glUniform3f(uObjectColor, o3d.material.color.r, o3d.material.color.g, o3d.material.color.b);
		GLES20.glUniform1f(uObjectColorTrans, o3d.material.color.a);

		GLES20.glUniform1f(uReflectivity, o3d.material.reflectivity);

		GLES20.glUniform1i(uEnvMap, 1); // This out the if or fails
		if (o3d.material.envMapTexture != null) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, gpuUploader.cubeMapTextures.get(o3d.material.envMapTexture));
		}
	}

	private void setObjectUniforms(Object3d o3d) {
		GLES20.glUniformMatrix4fv(uModelViewMatrix, 1, false, o3d.modelViewMatrix, 0);
		if (o3d.normalMatrix != null) { // BUG
			GLES20.glUniformMatrix3fv(uNormalMatrix, 1, false, o3d.normalMatrix, 0);
		}
	}

	private void setBlending(Blending blending) {
		this.blending = blending;

		switch (blending) {
			case NoBlending:
				GLES20.glDisable(GLES20.GL_BLEND);
				break;
			case NormalBlending:
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
				break;
			case AdditiveBlending:
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
				break;
			case SubtractiveBlending:
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_ZERO, GLES20.GL_ONE_MINUS_SRC_COLOR);
				break;
			case MultiplyBlending:
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_ZERO, GLES20.GL_SRC_COLOR);
				break;
		}
	}

	private void initShaders() {
		int fragmentShader = getShader(GLES20.GL_FRAGMENT_SHADER, resourceLoader.loadRawResource(R.raw.fragment_shader));
		int vertexShader = getShader(GLES20.GL_VERTEX_SHADER, resourceLoader.loadRawResource(R.raw.vertex_shader));

		shaderProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(shaderProgram, vertexShader);
		GLES20.glAttachShader(shaderProgram, fragmentShader);
		GLES20.glLinkProgram(shaderProgram);

		int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES20.GL_TRUE) {
			Log.e(TAG, "Could not link program: ");
			Log.e(TAG, GLES20.glGetProgramInfoLog(shaderProgram));
			GLES20.glDeleteProgram(shaderProgram);
			shaderProgram = 0;
		}

		GLES20.glUseProgram(shaderProgram);

		vertexPositionAttribute = GLES20.glGetAttribLocation(shaderProgram, "vertexPosition");
		GLES20.glEnableVertexAttribArray(vertexPositionAttribute);

		textureCoordAttribute = GLES20.glGetAttribLocation(shaderProgram, "textureCoord");
		GLES20.glEnableVertexAttribArray(textureCoordAttribute);

		vertexNormalAttribute = GLES20.glGetAttribLocation(shaderProgram, "vertexNormal");
		GLES20.glEnableVertexAttribArray(vertexNormalAttribute);

		uPerspectiveMatrix = GLES20.glGetUniformLocation(shaderProgram, "perspectiveMatrix");
		uModelViewMatrix = GLES20.glGetUniformLocation(shaderProgram, "modelViewMatrix");
		uNormalMatrix = GLES20.glGetUniformLocation(shaderProgram, "normalMatrix");
		uUseLighting = GLES20.glGetUniformLocation(shaderProgram, "useLighting");
		uAmbientColor = GLES20.glGetUniformLocation(shaderProgram, "ambientColor");
		uPointLightingLocation = GLES20.glGetUniformLocation(shaderProgram, "pointLightingLocation");
		uPointLightingColor = GLES20.glGetUniformLocation(shaderProgram, "pointLightingColor");
		uSampler = GLES20.glGetUniformLocation(shaderProgram, "sampler");
		uObjectColor = GLES20.glGetUniformLocation(shaderProgram, "objectColor");
		uObjectColorTrans = GLES20.glGetUniformLocation(shaderProgram, "objectColorTrans");

		uReflectivity = GLES20.glGetUniformLocation(shaderProgram, "reflectivity");
		uEnvMap = GLES20.glGetUniformLocation(shaderProgram, "envMap");
	}

	private int getShader(int type, String source) {
		int shader = GLES20.glCreateShader(type);

		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);

		return shader;
	}

	public void requestRender() {
		needsRedraw = true;
	}

	public void stop() {
		stop = true;
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
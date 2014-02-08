package jmini3d.gwt;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusWidget;
import com.googlecode.gwtgl.binding.WebGLProgram;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLShader;
import com.googlecode.gwtgl.binding.WebGLUniformLocation;

import jmini3d.Blending;
import jmini3d.GpuObjectStatus;
import jmini3d.MatrixUtils;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.gwt.input.TouchController;
import jmini3d.input.TouchListener;

public class Renderer implements AnimationCallback {
	public static final String TAG = "Renderer";
	public static boolean needsRedraw = true;

	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 10000;
	private boolean logFps = false;
	private long frameCount = 0;
	private float fps = 0;
	private long timeLastSample;

	Scene scene;
	private ResourceLoader resourceLoader;
	private GpuUploader gpuUploader;
	private TouchController touchController;

	public float[] ortho = new float[16];

	boolean stop = true;

	Blending blending;

	private int vertexPositionAttribute, vertexNormalAttribute, textureCoordAttribute;
	private WebGLUniformLocation uPerspectiveMatrix, uModelViewMatrix, //
			uNormalMatrix, uUseLighting, uAmbientColor, uPointLightingLocation, //
			uPointLightingColor, uSampler, uEnvMap, uReflectivity, uObjectColor, uObjectColorTrans;

	// cached values
	Texture texture;
	float sampler, useLighting, ambientColorR, ambientColorG, ambientColorB, pointLightingColorR, pointLightingColorG, pointLightingColorB, //
			pointLightLocationX, pointLightLocationY, pointLightLocationZ, objectColorR, objectColorG, objectColorB, objectColorTrans, reflectivity, envMap;

	private FocusWidget webGLCanvas;
	private WebGLRenderingContext gl;

	public Renderer(ResourceLoader resourceLoader, Scene scene, int width, int height) {
		this.scene = scene;
		this.resourceLoader = resourceLoader;

		MatrixUtils.ortho(ortho, 0, 1, 0, 1, -5, 1);

		webGLCanvas = Canvas.createIfSupported();
		gl = (WebGLRenderingContext) ((Canvas) webGLCanvas).getContext("webgl");
		if (gl == null) {
			gl = (WebGLRenderingContext) ((Canvas) webGLCanvas).getContext("experimental-webgl");
		}
		if (gl == null) {
			Window.alert("Sorry, Your browser doesn't support WebGL. Please Install the last version of Firefox, Chrome or Internet Explorer (>=11).");
			return;
		}

		gpuUploader = new GpuUploader(gl, resourceLoader);

		initShaders();

		setSize(width, height);
	}

	public void onResume() {
		if (stop) {
			stop = false;
			AnimationScheduler.get().requestAnimationFrame(this);
		}
	}

	public void onPause() {
		stop = true;
	}

	@Override
	public void execute(double timestamp) {
		if (!stop) {
			onDrawFrame();
			AnimationScheduler.get().requestAnimationFrame(this);
		}
	}

	public void setSize(int width, int height) {
		scene.camera.setWidth(width);
		scene.camera.setHeight(height);

		if (webGLCanvas instanceof Canvas) {
			((Canvas) webGLCanvas).setCoordinateSpaceWidth(width);
			((Canvas) webGLCanvas).setCoordinateSpaceHeight(height);
		}
		webGLCanvas.setWidth(width + "px");
		webGLCanvas.setHeight(height + "px");

		gl.viewport(0, 0, scene.camera.getWidth(), scene.camera.getHeight());

		// Scene reload on size changed, needed to keep aspect ratios
		gpuUploader.reset();
		scene.reset();
		scene.sceneController.initScene();
		reset();

		needsRedraw = true;
	}

	public void reset() {
		texture = null;
		sampler = useLighting = ambientColorR = ambientColorG = ambientColorB = pointLightingColorR = pointLightingColorG = pointLightingColorB = //
				pointLightLocationX = pointLightLocationY = pointLightLocationZ = objectColorR = objectColorG = objectColorB = objectColorTrans = reflectivity = envMap = -9999;

		gl.enable(WebGLRenderingContext.DEPTH_TEST);
		gl.clearDepth(1f);
		gl.depthFunc(WebGLRenderingContext.LEQUAL);
		gl.depthRange(0, 1f);
		gl.depthMask(true);

		// For performance
		gl.disable(WebGLRenderingContext.DITHER);

		// For transparency
		gl.disable(WebGLRenderingContext.BLEND);
		blending = Blending.NoBlending;

		// CCW frontfaces only, by default
		gl.frontFace(WebGLRenderingContext.CCW);
		gl.cullFace(WebGLRenderingContext.BACK);
		gl.enable(WebGLRenderingContext.CULL_FACE);
	}

	public void onDrawFrame() {
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

		gl.clearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);

		setSceneUniforms();

		for (int i = 0; i < scene.children.size(); i++) {
			Object3d o3d = scene.children.get(i);
			if (o3d.visible) {
				o3d.updateMatrices(scene.camera.modelViewMatrix, cameraChanged);
				drawObject(o3d);

				if (o3d.clearDepthAfterDraw) {
					gl.clear(WebGLRenderingContext.DEPTH_BUFFER_BIT);
				}
			}
		}

		gl.uniformMatrix4fv(uPerspectiveMatrix, false, ortho);

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

		gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.vertexBufferId);
		gl.vertexAttribPointer(vertexPositionAttribute, 3, WebGLRenderingContext.FLOAT, false, 0, 0);

		gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.normalsBufferId);
		gl.vertexAttribPointer(vertexNormalAttribute, 3, WebGLRenderingContext.FLOAT, false, 0, 0);

		gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.uvsBufferId);
		gl.vertexAttribPointer(textureCoordAttribute, 2, WebGLRenderingContext.FLOAT, false, 0, 0);

		gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);

		if (texture != o3d.material.texture) {
			gl.activeTexture(WebGLRenderingContext.TEXTURE0);
			gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, gpuUploader.textures.get(o3d.material.texture));
			texture = o3d.material.texture;
		}

		gl.drawElements(WebGLRenderingContext.TRIANGLES, o3d.geometry3d.facesLength, WebGLRenderingContext.UNSIGNED_SHORT, 0);
	}

	private void setSceneUniforms() {
		if (sampler != 0) {
			gl.uniform1i(uSampler, 0);
			sampler = 0;
		}
		if (useLighting != 1) {
			gl.uniform1i(uUseLighting, 1);
			useLighting = 1;
		}
		if (ambientColorR != scene.ambientColor.r || ambientColorG != scene.ambientColor.g || ambientColorB != scene.ambientColor.b) {
			gl.uniform3f(uAmbientColor, scene.ambientColor.r, scene.ambientColor.g, scene.ambientColor.b);
			ambientColorR = scene.ambientColor.r;
			ambientColorG = scene.ambientColor.g;
			ambientColorB = scene.ambientColor.b;
		}
		if (pointLightingColorR != scene.pointLightColor.r || pointLightingColorG != scene.pointLightColor.g || pointLightingColorB != scene.pointLightColor.b) {
			gl.uniform3f(uPointLightingColor, scene.pointLightColor.r, scene.pointLightColor.g, scene.pointLightColor.b);
			pointLightingColorR = scene.pointLightColor.r;
			pointLightingColorG = scene.pointLightColor.g;
			pointLightingColorB = scene.pointLightColor.b;
		}
		if (pointLightLocationX != scene.pointLightLocation.x || pointLightLocationY != scene.pointLightLocation.y || pointLightLocationZ != scene.pointLightLocation.z) {
			gl.uniform3f(uPointLightingLocation, scene.pointLightLocation.x, scene.pointLightLocation.y, scene.pointLightLocation.z);
			pointLightLocationX = scene.pointLightLocation.x;
			pointLightLocationY = scene.pointLightLocation.y;
			pointLightLocationZ = scene.pointLightLocation.z;
		}
		gl.uniformMatrix4fv(uPerspectiveMatrix, false, scene.camera.perspectiveMatrix);
	}

	private void setMaterialUniforms(Object3d o3d) {
		if (objectColorR != o3d.material.color.r || objectColorG != o3d.material.color.g || objectColorB != o3d.material.color.b) {
			gl.uniform3f(uObjectColor, o3d.material.color.r, o3d.material.color.g, o3d.material.color.b);
			objectColorR = o3d.material.color.r;
			objectColorG = o3d.material.color.g;
			objectColorB = o3d.material.color.b;
		}
		if (objectColorTrans != o3d.material.color.a) {
			gl.uniform1f(uObjectColorTrans, o3d.material.color.a);
			objectColorTrans = o3d.material.color.a;
		}
		if (reflectivity != o3d.material.reflectivity) {
			gl.uniform1f(uReflectivity, o3d.material.reflectivity);
			reflectivity = o3d.material.reflectivity;
		}
		if (envMap != 1) {
			gl.uniform1i(uEnvMap, 1); // This out the if or fails
			envMap = 1;
		}
		if (o3d.material.envMapTexture != null) {
			gl.activeTexture(WebGLRenderingContext.TEXTURE1);
			gl.bindTexture(WebGLRenderingContext.TEXTURE_CUBE_MAP, gpuUploader.cubeMapTextures.get(o3d.material.envMapTexture));
		}
	}

	private void setObjectUniforms(Object3d o3d) {
		gl.uniformMatrix4fv(uModelViewMatrix, false, o3d.modelViewMatrix);
		if (o3d.normalMatrix != null) { // BUG
			gl.uniformMatrix3fv(uNormalMatrix, false, o3d.normalMatrix);
		}
	}

	private void setBlending(Blending blending) {
		this.blending = blending;

		switch (blending) {
			case NoBlending:
				gl.disable(WebGLRenderingContext.BLEND);
				break;
			case NormalBlending:
				gl.enable(WebGLRenderingContext.BLEND);
				gl.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
				break;
			case AdditiveBlending:
				gl.enable(WebGLRenderingContext.BLEND);
				gl.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE);
				break;
			case SubtractiveBlending:
				gl.enable(WebGLRenderingContext.BLEND);
				gl.blendFunc(WebGLRenderingContext.ZERO, WebGLRenderingContext.ONE_MINUS_SRC_COLOR);
				break;
			case MultiplyBlending:
				gl.enable(WebGLRenderingContext.BLEND);
				gl.blendFunc(WebGLRenderingContext.ZERO, WebGLRenderingContext.SRC_COLOR);
				break;
		}
	}

	private void initShaders() {
		WebGLShader fragmentShader = getShader(WebGLRenderingContext.FRAGMENT_SHADER, EngineResources.INSTANCE.fragmentShader().getText());
		WebGLShader vertexShader = getShader(WebGLRenderingContext.VERTEX_SHADER, EngineResources.INSTANCE.vertexShader().getText());

		WebGLProgram shaderProgram = gl.createProgram();
		gl.attachShader(shaderProgram, vertexShader);
		gl.attachShader(shaderProgram, fragmentShader);
		gl.linkProgram(shaderProgram);

		if (!gl.getProgramParameterb(shaderProgram, WebGLRenderingContext.LINK_STATUS)) {
			throw new RuntimeException("Could not initialize shaders");
		}
		gl.useProgram(shaderProgram);

		vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "vertexPosition");
		gl.enableVertexAttribArray(vertexPositionAttribute);

		textureCoordAttribute = gl.getAttribLocation(shaderProgram, "textureCoord");
		gl.enableVertexAttribArray(textureCoordAttribute);

		vertexNormalAttribute = gl.getAttribLocation(shaderProgram, "vertexNormal");
		gl.enableVertexAttribArray(vertexNormalAttribute);

		uPerspectiveMatrix = gl.getUniformLocation(shaderProgram, "perspectiveMatrix");
		uModelViewMatrix = gl.getUniformLocation(shaderProgram, "modelViewMatrix");
		uNormalMatrix = gl.getUniformLocation(shaderProgram, "normalMatrix");
		uUseLighting = gl.getUniformLocation(shaderProgram, "useLighting");
		uAmbientColor = gl.getUniformLocation(shaderProgram, "ambientColor");
		uPointLightingLocation = gl.getUniformLocation(shaderProgram, "pointLightingLocation");
		uPointLightingColor = gl.getUniformLocation(shaderProgram, "pointLightingColor");
		uSampler = gl.getUniformLocation(shaderProgram, "sampler");
		uObjectColor = gl.getUniformLocation(shaderProgram, "objectColor");
		uObjectColorTrans = gl.getUniformLocation(shaderProgram, "objectColorTrans");

		uReflectivity = gl.getUniformLocation(shaderProgram, "reflectivity");
		uEnvMap = gl.getUniformLocation(shaderProgram, "envMap");
	}

	private WebGLShader getShader(int type, String source) {
		WebGLShader shader = gl.createShader(type);

		gl.shaderSource(shader, source);
		gl.compileShader(shader);

		if (!gl.getShaderParameterb(shader, WebGLRenderingContext.COMPILE_STATUS)) {
			throw new RuntimeException(gl.getShaderInfoLog(shader));
		}
		return shader;
	}

	public void requestRender() {
		needsRedraw = true;
	}

	public void stop() {
		stop = true;
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

			log("FPS: " + fps);

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

	public void setTouchListener(TouchListener listener) {
		if (touchController == null) {
			touchController = new TouchController(webGLCanvas);
		}
		touchController.setListener(listener);
	}

	public FocusWidget getCanvas() {
		return webGLCanvas;
	}

	native void log(String message) /*-{
		console.log(message);
    }-*/;
}
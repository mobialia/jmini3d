package mini3d.gwt;

import mini3d.GpuObjectStatus;
import mini3d.MatrixUtils;
import mini3d.Object3d;
import mini3d.Scene;
import mini3d.gwt.input.TouchController;
import mini3d.input.TouchListener;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusWidget;
import com.googlecode.gwtgl.binding.WebGLProgram;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLShader;
import com.googlecode.gwtgl.binding.WebGLUniformLocation;

public class Renderer implements AnimationCallback {
	public static boolean needsRedraw = true;

	private FocusWidget webGLCanvas;
	private WebGLRenderingContext gl;
	private WebGLProgram shaderProgram;
	private GpuUploader gpuUploader;
	
	private TouchController touchController; 

	private int vertexPositionAttribute, vertexNormalAttribute, textureCoordAttribute;
	WebGLUniformLocation uPerspectiveMatrix, uModelViewMatrix, //
			uNormalMatrix, uUseLighting, uAmbientColor, uPointLightingLocation, //
			uPointLightingColor, uSampler, uEnvMap, uReflectivity, uObjectColor, uObjectColorTrans;

	Scene scene;
	public float[] ortho = new float[16];
	
	boolean stop = false;

//	public static native String getUserAgent() /*-{
//		return navigator.userAgent.toLowerCase();
//	}-*/;

	public Renderer(ResourceLoader resourceLoader, Scene scene, int width, int height) {
		this.scene = scene;

		webGLCanvas = Canvas.createIfSupported();
		gl = (WebGLRenderingContext) ((Canvas) webGLCanvas).getContext("webgl");
		if (gl == null) {
			gl = (WebGLRenderingContext)  ((Canvas) webGLCanvas).getContext("experimental-webgl");
		}
//		if (gl == null && getUserAgent().contains("msie")) {
//			webGLCanvas = new IeWebGLWidget();
//			gl = ((IeWebGLWidget) webGLCanvas).getContext("webgl");
//		}
		if (gl == null) {
			Window.alert("Sorry, Your browser doesn't support WebGL. Please Install Chrome or Firefox.");
			return;
		}
		
		MatrixUtils.ortho(ortho, 0, 1, 0, 1, -5, 1);

		gpuUploader = new GpuUploader(gl, resourceLoader);
		initShaders();

		setSize(width, height);
	}

	public void onResume() {
		stop = false;
		AnimationScheduler.get().requestAnimationFrame(this);
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

		// Scene reload on size changed, needed to keep aspect ratios
		reset();
		gpuUploader.reset();
		scene.reset();
		scene.sceneController.initScene();

		needsRedraw = true;
	}

	public void reset() {
		gl.clearDepth(1.0f);
		gl.enable(WebGLRenderingContext.DEPTH_TEST);
		gl.depthFunc(WebGLRenderingContext.LEQUAL);

		// For transparency
		gl.enable(WebGLRenderingContext.BLEND);
		gl.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
	}

	public FocusWidget getCanvas() {
		return webGLCanvas;
	}

	public void onDrawFrame() {
		boolean sceneUpdated = scene.sceneController.updateScene();
		boolean cameraChanged = scene.camera.updateMatrices();

		if (!needsRedraw && !sceneUpdated && !cameraChanged) {
			return;
		}

		for (Object o : scene.unload) {
			gpuUploader.unload(o);
		}
		scene.unload.clear();

		needsRedraw = false;

		gl.viewport(0, 0, scene.camera.getWidth(), scene.camera.getHeight());

		gl.clearColor(scene.getBackgroundColor().r, scene.getBackgroundColor().g, scene.getBackgroundColor().b, scene.getBackgroundColor().a);
		gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);

		setSceneUniforms();

		for (Object3d o3d : scene.children) {
			if (o3d.visible) {
				o3d.updateMatrices(scene.camera.modelViewMatrix, cameraChanged);
				drawObject(o3d);
			}
		}

		gl.uniformMatrix4fv(uPerspectiveMatrix, false, ortho);

		for (Object3d o3d : scene.hud) {
			if (o3d.visible) {
				o3d.updateMatrices(MatrixUtils.IDENTITY4, false);
				drawObject(o3d);
			}
		}
	}

	private void drawObject(Object3d o3d) {
		GeometryBuffers buffers = gpuUploader.upload(o3d.geometry3d);
		if (o3d.material.texture != null) {
			gpuUploader.upload(o3d.material.texture);
			if ((o3d.material.texture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0)
				return;
		}
		if (o3d.material.envMapTexture != null) {
			gpuUploader.upload(o3d.material.envMapTexture);
			if ((o3d.material.envMapTexture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0)
				return;
		}

		gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.vertexBufferId);
		gl.vertexAttribPointer(vertexPositionAttribute, 3, WebGLRenderingContext.FLOAT, false, 0, 0);

		gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.normalsBufferId);
		gl.vertexAttribPointer(vertexNormalAttribute, 3, WebGLRenderingContext.FLOAT, false, 0, 0);

		gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.uvsBufferId);
		gl.vertexAttribPointer(textureCoordAttribute, 2, WebGLRenderingContext.FLOAT, false, 0, 0);

		gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);

		gl.activeTexture(WebGLRenderingContext.TEXTURE0);
		gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, gpuUploader.textures.get(o3d.material.texture));

		setMaterialUniforms(o3d);
		gl.drawElements(WebGLRenderingContext.TRIANGLES, o3d.geometry3d.facesLength, WebGLRenderingContext.UNSIGNED_SHORT, 0);
	}

	private void setSceneUniforms() {
		gl.uniform1i(uSampler, 0);

		gl.uniform1i(uUseLighting, 1);
		gl.uniform3f(uAmbientColor, scene.ambientColor.r, scene.ambientColor.g, scene.ambientColor.b);
		gl.uniform3f(uPointLightingColor, scene.pointLightColor.r, scene.pointLightColor.g, scene.pointLightColor.b);
		gl.uniform3f(uPointLightingLocation, scene.pointLightLocation.x, scene.pointLightLocation.y, scene.pointLightLocation.z);

		gl.uniformMatrix4fv(uPerspectiveMatrix, false, scene.camera.perspectiveMatrix);
	}

	private void setMaterialUniforms(Object3d o3d) {
		gl.uniformMatrix4fv(uModelViewMatrix, false, o3d.modelViewMatrix);
		if (o3d.normalMatrix != null) { // BUG
			gl.uniformMatrix3fv(uNormalMatrix, false, o3d.normalMatrix);
		}

		gl.uniform3f(uObjectColor, o3d.material.color.r, o3d.material.color.g, o3d.material.color.b);
		gl.uniform1f(uObjectColorTrans, o3d.material.color.a);

		gl.uniform1f(uReflectivity, o3d.material.reflectivity);

		gl.uniform1i(uEnvMap, 1); // This out the if or fails
		if (o3d.material.envMapTexture != null) {
			gl.activeTexture(WebGLRenderingContext.TEXTURE1);
			gl.bindTexture(WebGLRenderingContext.TEXTURE_CUBE_MAP, gpuUploader.cubeMapTextures.get(o3d.material.envMapTexture));
		}
	}

	private void initShaders() {
		WebGLShader fragmentShader = getShader(WebGLRenderingContext.FRAGMENT_SHADER, EngineResources.INSTANCE.fragmentShader().getText());
		WebGLShader vertexShader = getShader(WebGLRenderingContext.VERTEX_SHADER, EngineResources.INSTANCE.vertexShader().getText());

		shaderProgram = gl.createProgram();
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

	public void setTouchListener(TouchListener listener) {
		if (touchController == null)  {
			touchController = new TouchController(webGLCanvas);
		}
		touchController.setListener(listener);
	}

	public void stop() {
		stop = true;
	}
}
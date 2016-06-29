package jmini3d.gwt;

import com.googlecode.gwtgl.binding.WebGLProgram;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLShader;

import java.util.HashMap;

import jmini3d.Color4;
import jmini3d.GpuObjectStatus;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Vector3;
import jmini3d.material.Material;
import jmini3d.shader.ShaderKey;

public class Program extends jmini3d.shader.Program {
	static final String TAG = "Program";

	int key = -1;

	WebGLProgram webGLProgram;

	boolean useNormals = false;
	boolean useMap = false;
	boolean useEnvMap = false;
	boolean useNormalMap = false;
	boolean useVertexColors = false;
	boolean useCameraPosition = false;
	boolean useShaderPlugins = false;

	int vertexPositionAttribLocation = -1;
	int vertexNormalAttribLocation = -1;
	int textureCoordAttribLocation = -1;
	int vertexColorAttribLocation = -1;

	// Cached values to avoid setting buffers with an already existing value
	Integer activeVertexPosition = null;
	Integer activeVertexNormal = null;
	Integer activeTextureCoord = null;
	Integer activeVertexColor = null;
	Integer activeFacesBuffer = null;

	int projectionMatrixUniform;
	int modelMatrixUniform;
	int viewMatrixUniform;
	int normalMatrixUniform;

	int mapUniform;
	int reflectivityUniform;
	int envMapUniform;
	int normalMapUniform;
	int cameraPositionUniform;
	int objectColorUniform;

	// Cached values to avoid setting uniforms two times
	int map = -1;
	int envMap = -1;
	int normalMap = -1;
	float projectionMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	float viewMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	float modelMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	float normalMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	Color4 objectColor = Color4.fromFloat(-1, -1, -1, -1);
	Vector3 cameraPosition = new Vector3(0, 0, 0);
	float reflectivity;

	private WebGLRenderingContext GLES20;

	boolean shaderLoaded = false;

	String vertexShaderLoaded;
	String fragmentShaderLoaded;

	public Program(WebGLRenderingContext GLES20) {
		this.GLES20 = GLES20;
	}

	native void log(String message) /*-{
		console.log(message);
    }-*/;

	public void init(Scene scene, Material material, ResourceLoader resourceLoader) {
		useShaderPlugins = (scene.shaderKey & material.shaderKey & ShaderKey.SHADER_PLUGIN_MASK) != 0;

		String vertexShaderName = DEFAULT_VERTEX_SHADER;
		String fragmentShaderName = DEFAULT_FRAGMENT_SHADER;

		if (useShaderPlugins) {
			if (scene.shaderPlugin != null) {
				sceneProgramPlugin = scene.shaderPlugin.getProgramPlugin(this);
				if (sceneProgramPlugin.getVertexShaderName() != null) {
					vertexShaderName = sceneProgramPlugin.getVertexShaderName();
				}
				if (sceneProgramPlugin.getFragmentShaderName() != null) {
					fragmentShaderName = sceneProgramPlugin.getFragmentShaderName();
				}
			}
			if (material.shaderPlugin != null) {
				materialProgramPlugin = material.shaderPlugin.getProgramPlugin(this);
				if (materialProgramPlugin.getVertexShaderName() != null) {
					vertexShaderName = materialProgramPlugin.getVertexShaderName();
				}
				if (materialProgramPlugin.getFragmentShaderName() != null) {
					fragmentShaderName = materialProgramPlugin.getFragmentShaderName();
				}
			}
		}

		resourceLoader.loadShader(vertexShaderName, new ResourceLoader.OnTextResourceLoaded() {
			@Override
			public void onResourceLoaded(String text) {
				vertexShaderLoaded = text;
				if (vertexShaderLoaded != null && fragmentShaderLoaded != null) {
					finishShaderLoad(scene, material);
				}
			}
		});
		resourceLoader.loadShader(fragmentShaderName, new ResourceLoader.OnTextResourceLoaded() {
			@Override
			public void onResourceLoaded(String text) {
				fragmentShaderLoaded = text;
				if (vertexShaderLoaded != null && fragmentShaderLoaded != null) {
					finishShaderLoad(scene, material);
				}
			}
		});
	}

	public void finishShaderLoad(Scene scene, Material material) {
		HashMap<String, String> defines = new HashMap<>();

		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.prepareShader(scene, defines);
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.prepareShader(scene, defines);
		}

		if (material.map != null) {
			defines.put("USE_MAP", null);
			useMap = true;
		}

		if (material.envMap != null) {
			defines.put("USE_ENVMAP", null);
			useNormals = true;
			useEnvMap = true;
			useCameraPosition = true;
		}

		if (material.useEnvMapAsMap) {
			defines.put("USE_ENVMAP_AS_MAP", null);
		}

		if (material.applyColorToAlpha) {
			defines.put("APPLY_COLOR_TO_ALPHA", null);
		}

		if (material.useVertexColors) {
			useVertexColors = true;
			defines.put("USE_VERTEX_COLORS", null);
		}

		if (useCameraPosition) {
			defines.put("USE_CAMERA_POSITION", null);
		}

		if (useNormals) {
			if (material.normalMap != null) {
				defines.put("USE_NORMAL_MAP", null);
				useNormalMap = true;
				useNormals = false;
			} else {
				defines.put("USE_NORMALS", null);
			}
		}

		StringBuffer vertexShaderStringBuffer = new StringBuffer();
		StringBuffer fragmentShaderStringBuffer = new StringBuffer();

		// TODO precision
		for (String k : defines.keySet()) {
			if (defines.get(k) == null) {
				vertexShaderStringBuffer.append("#define " + k + "\n");
				fragmentShaderStringBuffer.append("#define " + k + "\n");
			} else {
				vertexShaderStringBuffer.append("#define " + k + " " + defines.get(k) + "\n");
				fragmentShaderStringBuffer.append("#define " + k + " " + defines.get(k) + "\n");
			}
		}

		vertexShaderStringBuffer.append(vertexShaderLoaded);
		fragmentShaderStringBuffer.append(fragmentShaderLoaded);

		String vertexShaderString = vertexShaderStringBuffer.toString();
		String fragmentShaderString = fragmentShaderStringBuffer.toString();

		WebGLShader vertexShader = getShader(WebGLRenderingContext.VERTEX_SHADER, vertexShaderString);
		WebGLShader fragmentShader = getShader(WebGLRenderingContext.FRAGMENT_SHADER, fragmentShaderString);

		webGLProgram = GLES20.createProgram();
		GLES20.attachShader(webGLProgram, vertexShader);
		GLES20.attachShader(webGLProgram, fragmentShader);
		GLES20.linkProgram(webGLProgram);

		if (!GLES20.getProgramParameterb(webGLProgram, WebGLRenderingContext.LINK_STATUS)) {
			throw new RuntimeException("Could not initialize shaders");
		}
		GLES20.useProgram(webGLProgram);

		projectionMatrixUniform = GLES20.getUniformLocation(webGLProgram, "projectionMatrix");
		viewMatrixUniform = GLES20.getUniformLocation(webGLProgram, "viewMatrix");
		modelMatrixUniform = GLES20.getUniformLocation(webGLProgram, "modelMatrix");
		objectColorUniform = GLES20.getUniformLocation(webGLProgram, "objectColor");

		if (useNormals || useNormalMap) {
			normalMatrixUniform = GLES20.getUniformLocation(webGLProgram, "normalMatrix");
		}
		if (useNormalMap) {
			normalMapUniform = GLES20.getUniformLocation(webGLProgram, "normalMap");
		}
		if (useMap) {
			mapUniform = GLES20.getUniformLocation(webGLProgram, "map");
		}
		if (useEnvMap) {
			reflectivityUniform = GLES20.getUniformLocation(webGLProgram, "reflectivity");
			envMapUniform = GLES20.getUniformLocation(webGLProgram, "envMap");
		}
		if (useCameraPosition) {
			cameraPositionUniform = GLES20.getUniformLocation(webGLProgram, "cameraPosition");
		}
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onShaderLoaded();
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onShaderLoaded();
		}

		// Initialize attrib locations
		vertexPositionAttribLocation = getAndEnableAttribLocation("vertexPosition");

		GLES20.deleteShader(vertexShader);
		GLES20.deleteShader(fragmentShader);

		vertexShaderLoaded = null;
		fragmentShaderLoaded = null;
		shaderLoaded = true;
	}

	private int getAndEnableAttribLocation(String attribName) {
		int attribLocation = GLES20.getAttribLocation(webGLProgram, attribName);
		GLES20.enableVertexAttribArray(attribLocation);
		return attribLocation;
	}

	public void setSceneUniforms(Scene scene) {
		if (!shaderLoaded) {
			return;
		}

		// Reset cached attribs at the beginning of each frame
		activeVertexPosition = null;
		activeVertexNormal = null;
		activeTextureCoord = null;
		activeVertexColor = null;
		activeFacesBuffer = null;

		if (useMap && map != 0) {
			GLES20.uniform1i(mapUniform, 0);
			map = 0;
		}
		if (useEnvMap && envMap != 1) {
			GLES20.uniform1i(envMapUniform, 1);
			envMap = 1;
		}
		if (useCameraPosition && !cameraPosition.equals(scene.camera.position)) {
			GLES20.uniform3f(cameraPositionUniform, scene.camera.position.x, scene.camera.position.y, scene.camera.position.z);
			cameraPosition.setAllFrom(scene.camera.position);
		}
		if (useNormalMap && normalMap != 2) {
			GLES20.uniform1i(normalMapUniform, 2);
			normalMap = 2;
		}
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onSetSceneUniforms(scene);
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onSetSceneUniforms(scene);
		}
	}

	public void drawObject(Renderer3d renderer3d, GpuUploader gpuUploader, Object3d o3d, float[] projectionMatrix, float viewMatrix[]) {
		if (!shaderLoaded) {
			return;
		}

		setMatrix4UniformIfChanged(projectionMatrixUniform, projectionMatrix, this.projectionMatrix);
		setMatrix4UniformIfChanged(viewMatrixUniform, viewMatrix, this.viewMatrix);
		setMatrix4UniformIfChanged(modelMatrixUniform, o3d.modelMatrix, this.modelMatrix);
		if ((useNormals || useNormalMap) && o3d.normalMatrix != null) {
			setMatrix3UniformIfChanged(normalMatrixUniform, o3d.normalMatrix, this.normalMatrix);
		}

		GeometryBuffers buffers = gpuUploader.upload(o3d.geometry3d);
		Integer vertexColorsBufferId = null;
		if (useVertexColors) {
			vertexColorsBufferId = gpuUploader.upload(o3d.vertexColors);
		}

		if (useMap) {
			gpuUploader.upload(renderer3d, o3d.material.map, WebGLRenderingContext.TEXTURE0);
			if ((o3d.material.map.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (useEnvMap) {
			gpuUploader.upload(renderer3d, o3d.material.envMap, WebGLRenderingContext.TEXTURE1);
			if ((o3d.material.envMap.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (useNormalMap) {
			gpuUploader.upload(renderer3d, o3d.material.normalMap, WebGLRenderingContext.TEXTURE2);
			if ((o3d.material.normalMap.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}

		if (activeVertexPosition == null || activeVertexPosition != buffers.vertexBufferId) {
			activeVertexPosition = buffers.vertexBufferId;
			GLES20.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.vertexBufferId);
			GLES20.vertexAttribPointer(vertexPositionAttribLocation, 3, WebGLRenderingContext.FLOAT, false, 0, 0);
		}

		if (useNormals) {
			if (activeVertexNormal == null || activeVertexNormal != buffers.normalsBufferId) {
				activeVertexNormal = buffers.normalsBufferId;
				if (vertexNormalAttribLocation == -1) {
					vertexNormalAttribLocation = getAndEnableAttribLocation("vertexNormal");
				}
				GLES20.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.normalsBufferId);
				GLES20.vertexAttribPointer(vertexNormalAttribLocation, 3, WebGLRenderingContext.FLOAT, false, 0, 0);
			}
		}

		if (useMap) {
			if (activeTextureCoord == null || activeTextureCoord != buffers.uvsBufferId) {
				activeTextureCoord = buffers.uvsBufferId;
				if (textureCoordAttribLocation == -1) {
					textureCoordAttribLocation = getAndEnableAttribLocation("textureCoord");
				}
				GLES20.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.uvsBufferId);
				GLES20.vertexAttribPointer(textureCoordAttribLocation, 2, WebGLRenderingContext.FLOAT, false, 0, 0);
			}
		}

		if (useVertexColors && (vertexColorsBufferId != null)) {
			if (activeVertexColor == null || activeVertexColor != vertexColorsBufferId) {
				activeVertexColor = vertexColorsBufferId;
				if (vertexColorAttribLocation == -1) {
					vertexColorAttribLocation = getAndEnableAttribLocation("vertexColor");
				}
				GLES20.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexColorsBufferId);
				GLES20.vertexAttribPointer(vertexColorAttribLocation, 4, WebGLRenderingContext.FLOAT, false, 0, 0);
			}
		}

		if (activeFacesBuffer == null || activeFacesBuffer != buffers.facesBufferId) {
			GLES20.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
			activeFacesBuffer = buffers.facesBufferId;
		}

		setColorUniformIfChanged(objectColorUniform, o3d.material.color, objectColor);

		if (useMap) {
			Integer mapTextureId = gpuUploader.textures.get(o3d.material.map);
			if (renderer3d.mapTextureId == null || renderer3d.mapTextureId != mapTextureId) {
				if (renderer3d.activeTexture != WebGLRenderingContext.TEXTURE0) {
					GLES20.activeTexture(WebGLRenderingContext.TEXTURE0);
					renderer3d.activeTexture = WebGLRenderingContext.TEXTURE0;
				}
				GLES20.bindTexture(WebGLRenderingContext.TEXTURE_2D, mapTextureId);
				renderer3d.mapTextureId = mapTextureId;
			}
		}
		if (useEnvMap) {
			if (reflectivity != o3d.material.reflectivity) {
				GLES20.uniform1f(reflectivityUniform, o3d.material.reflectivity);
				reflectivity = o3d.material.reflectivity;
			}
			Integer envMapTextureId = gpuUploader.cubeMapTextures.get(o3d.material.envMap);
			if (renderer3d.envMapTextureId == null || renderer3d.envMapTextureId != envMapTextureId) {
				if (renderer3d.activeTexture != WebGLRenderingContext.TEXTURE1) {
					GLES20.activeTexture(WebGLRenderingContext.TEXTURE1);
					renderer3d.activeTexture = WebGLRenderingContext.TEXTURE1;
				}
				GLES20.bindTexture(WebGLRenderingContext.TEXTURE_CUBE_MAP, envMapTextureId);
				renderer3d.envMapTextureId = envMapTextureId;
			}
		}
		if (useNormalMap) {
			Integer normalMapTextureId = gpuUploader.textures.get(o3d.material.normalMap);
			if (renderer3d.normalMapTextureId != normalMapTextureId) {
				if (renderer3d.activeTexture != WebGLRenderingContext.TEXTURE2) {
					GLES20.activeTexture(WebGLRenderingContext.TEXTURE2);
					renderer3d.activeTexture = WebGLRenderingContext.TEXTURE2;
				}
				GLES20.bindTexture(WebGLRenderingContext.TEXTURE_2D, gpuUploader.textures.get(o3d.material.normalMap));
				renderer3d.normalMapTextureId = normalMapTextureId;
			}
		}
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onDrawObject(o3d);
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onDrawObject(o3d);
		}
		GLES20.drawElements(WebGLRenderingContext.TRIANGLES, o3d.geometry3d.facesLength, WebGLRenderingContext.UNSIGNED_SHORT, 0);
	}

	@Override
	public int getUniformLocation(String uniformName) {
		return GLES20.getUniformLocation(webGLProgram, uniformName);
	}

	@Override
	public float setFloatUniformIfValueChanged(int uniform, float uniformValue, float lastValue) {
		if (lastValue != uniformValue) {
			GLES20.uniform1f(uniform, uniformValue);
		}
		return uniformValue;
	}

	private void setMatrix3UniformIfChanged(int matrixUniform, float newMatrix[], float lastMatrix[]) {
		if (lastMatrix[0] != newMatrix[0]
				|| lastMatrix[1] != newMatrix[1]
				|| lastMatrix[2] != newMatrix[2]
				|| lastMatrix[3] != newMatrix[3]
				|| lastMatrix[4] != newMatrix[4]
				|| lastMatrix[5] != newMatrix[5]
				|| lastMatrix[6] != newMatrix[6]
				|| lastMatrix[7] != newMatrix[7]
				|| lastMatrix[8] != newMatrix[8]) {
			GLES20.uniformMatrix3fv(matrixUniform, false, newMatrix);
			lastMatrix[0] = newMatrix[0];
			lastMatrix[1] = newMatrix[1];
			lastMatrix[2] = newMatrix[2];
			lastMatrix[3] = newMatrix[3];
			lastMatrix[4] = newMatrix[4];
			lastMatrix[5] = newMatrix[5];
			lastMatrix[6] = newMatrix[6];
			lastMatrix[7] = newMatrix[7];
			lastMatrix[8] = newMatrix[8];
		}
	}

	private void setMatrix4UniformIfChanged(int matrixUniform, float newMatrix[], float lastMatrix[]) {
		if (lastMatrix[0] != newMatrix[0]
				|| lastMatrix[1] != newMatrix[1]
				|| lastMatrix[2] != newMatrix[2]
				|| lastMatrix[3] != newMatrix[3]
				|| lastMatrix[4] != newMatrix[4]
				|| lastMatrix[5] != newMatrix[5]
				|| lastMatrix[6] != newMatrix[6]
				|| lastMatrix[7] != newMatrix[7]
				|| lastMatrix[8] != newMatrix[8]
				|| lastMatrix[9] != newMatrix[9]
				|| lastMatrix[10] != newMatrix[10]
				|| lastMatrix[11] != newMatrix[11]
				|| lastMatrix[12] != newMatrix[12]
				|| lastMatrix[13] != newMatrix[13]
				|| lastMatrix[14] != newMatrix[14]
				|| lastMatrix[15] != newMatrix[15]) {
			GLES20.uniformMatrix4fv(matrixUniform, false, newMatrix);
			lastMatrix[0] = newMatrix[0];
			lastMatrix[1] = newMatrix[1];
			lastMatrix[2] = newMatrix[2];
			lastMatrix[3] = newMatrix[3];
			lastMatrix[4] = newMatrix[4];
			lastMatrix[5] = newMatrix[5];
			lastMatrix[6] = newMatrix[6];
			lastMatrix[7] = newMatrix[7];
			lastMatrix[8] = newMatrix[8];
			lastMatrix[9] = newMatrix[9];
			lastMatrix[10] = newMatrix[10];
			lastMatrix[11] = newMatrix[11];
			lastMatrix[12] = newMatrix[12];
			lastMatrix[13] = newMatrix[13];
			lastMatrix[14] = newMatrix[14];
			lastMatrix[15] = newMatrix[15];
		}
	}

	@Override
	public void setColorUniformIfChanged(int colorUniform, Color4 newColor, Color4 lastColor) {
		if (newColor.r != lastColor.r || newColor.g != lastColor.g || newColor.b != lastColor.b || newColor.a != lastColor.a) {
			GLES20.uniform4f(colorUniform, newColor.r, newColor.g, newColor.b, newColor.a);
			lastColor.r = newColor.r;
			lastColor.g = newColor.g;
			lastColor.b = newColor.b;
			lastColor.a = newColor.a;
		}
	}

	@Override
	public void setUniform3fv(int location, int count, float[] v) {
		GLES20.uniform3fv(location, v);
	}

	@Override
	public void setUniform4fv(int location, int count, float[] v) {
		GLES20.uniform4fv(location, v);
	}

	@Override
	public void setUseNormals(boolean useNormals) {
		this.useNormals = useNormals;
	}

	@Override
	public void setUseCameraPosition(boolean useCameraPosition) {
		this.useCameraPosition = useCameraPosition;
	}


	private WebGLShader getShader(int type, String source) {
		WebGLShader shader = GLES20.createShader(type);

		GLES20.shaderSource(shader, source);
		GLES20.compileShader(shader);

		if (!GLES20.getShaderParameterb(shader, WebGLRenderingContext.COMPILE_STATUS)) {
			throw new RuntimeException(GLES20.getShaderInfoLog(shader));
		}
		return shader;
	}
}

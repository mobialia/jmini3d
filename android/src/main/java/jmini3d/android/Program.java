package jmini3d.android;

import android.opengl.GLES20;
import android.util.Log;

import java.util.HashMap;

import jmini3d.Color4;
import jmini3d.GpuObjectStatus;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Vector3;
import jmini3d.material.Material;
import jmini3d.shader.ShaderKey;

public class Program extends jmini3d.shader.Program {
	public static final String TAG = "Program";

	int key = -1;

	int webGLProgram;

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

	public void log(String message) {
		Log.d(TAG, message);
	}

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

		vertexShaderStringBuffer.append(resourceLoader.loadRawString(vertexShaderName));
		fragmentShaderStringBuffer.append(resourceLoader.loadRawString(fragmentShaderName));

		String vertexShaderString = vertexShaderStringBuffer.toString();
		String fragmentShaderString = fragmentShaderStringBuffer.toString();

		int vertexShader = getShader(GLES20.GL_VERTEX_SHADER, vertexShaderString);
		int fragmentShader = getShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderString);

		webGLProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(webGLProgram, vertexShader);
		GLES20.glAttachShader(webGLProgram, fragmentShader);
		GLES20.glLinkProgram(webGLProgram);

		int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(webGLProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES20.GL_TRUE) {
			Log.e(TAG, "Could not link program: ");
			Log.e(TAG, GLES20.glGetProgramInfoLog(webGLProgram));
			GLES20.glDeleteProgram(webGLProgram);
			throw new RuntimeException("Could not initialize shaders");
		}
		GLES20.glUseProgram(webGLProgram);

		projectionMatrixUniform = GLES20.glGetUniformLocation(webGLProgram, "projectionMatrix");
		viewMatrixUniform = GLES20.glGetUniformLocation(webGLProgram, "viewMatrix");
		modelMatrixUniform = GLES20.glGetUniformLocation(webGLProgram, "modelMatrix");
		objectColorUniform = GLES20.glGetUniformLocation(webGLProgram, "objectColor");

		if (useNormals || useNormalMap) {
			normalMatrixUniform = GLES20.glGetUniformLocation(webGLProgram, "normalMatrix");
		}
		if (useNormalMap) {
			normalMapUniform = GLES20.glGetUniformLocation(webGLProgram, "normalMap");
		}
		if (useMap) {
			mapUniform = GLES20.glGetUniformLocation(webGLProgram, "map");
		}
		if (useEnvMap) {
			reflectivityUniform = GLES20.glGetUniformLocation(webGLProgram, "reflectivity");
			envMapUniform = GLES20.glGetUniformLocation(webGLProgram, "envMap");
		}
		if (useCameraPosition) {
			cameraPositionUniform = GLES20.glGetUniformLocation(webGLProgram, "cameraPosition");
		}
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onShaderLoaded();
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onShaderLoaded();
		}

		// Initialize attrib locations
		vertexPositionAttribLocation = getAndEnableAttribLocation("vertexPosition");

		GLES20.glDeleteShader(vertexShader);
		GLES20.glDeleteShader(fragmentShader);
	}

	private int getAndEnableAttribLocation(String attribName) {
		int attribLocation = GLES20.glGetAttribLocation(webGLProgram, attribName);
		GLES20.glEnableVertexAttribArray(attribLocation);
		return attribLocation;
	}

	public void setSceneUniforms(Scene scene) {
		// Reset cached attribs at the beginning of each frame
		activeVertexPosition = null;
		activeVertexNormal = null;
		activeTextureCoord = null;
		activeVertexColor = null;
		activeFacesBuffer = null;

		if (useMap && map != 0) {
			GLES20.glUniform1i(mapUniform, 0);
			map = 0;
		}
		if (useEnvMap && envMap != 1) {
			GLES20.glUniform1i(envMapUniform, 1);
			envMap = 1;
		}
		if (useCameraPosition && !cameraPosition.equals(scene.camera.position)) {
			GLES20.glUniform3f(cameraPositionUniform, scene.camera.position.x, scene.camera.position.y, scene.camera.position.z);
			cameraPosition.setAllFrom(scene.camera.position);
		}
		if (useNormalMap && normalMap != 2) {
			GLES20.glUniform1i(normalMapUniform, 2);
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
			gpuUploader.upload(renderer3d, o3d.material.map, GLES20.GL_TEXTURE0);
			if ((o3d.material.map.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (useEnvMap) {
			gpuUploader.upload(renderer3d, o3d.material.envMap, GLES20.GL_TEXTURE1);
			if ((o3d.material.envMap.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (useNormalMap) {
			gpuUploader.upload(renderer3d, o3d.material.normalMap, GLES20.GL_TEXTURE2);
			if ((o3d.material.normalMap.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}

		if (activeVertexPosition == null || activeVertexPosition != buffers.vertexBufferId) {
			activeVertexPosition = buffers.vertexBufferId;
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.vertexBufferId);
			GLES20.glVertexAttribPointer(vertexPositionAttribLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
		}

		if (useNormals) {
			if (activeVertexNormal == null || activeVertexNormal != buffers.normalsBufferId) {
				activeVertexNormal = buffers.normalsBufferId;
				if (vertexNormalAttribLocation == -1) {
					vertexNormalAttribLocation = getAndEnableAttribLocation("vertexNormal");
				}
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.normalsBufferId);
				GLES20.glVertexAttribPointer(vertexNormalAttribLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
			}
		}

		if (useMap) {
			if (activeTextureCoord == null || activeTextureCoord != buffers.uvsBufferId) {
				activeTextureCoord = buffers.uvsBufferId;
				if (textureCoordAttribLocation == -1) {
					textureCoordAttribLocation = getAndEnableAttribLocation("textureCoord");
				}
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.uvsBufferId);
				GLES20.glVertexAttribPointer(textureCoordAttribLocation, 2, GLES20.GL_FLOAT, false, 0, 0);
			}
		}

		if (useVertexColors && (vertexColorsBufferId != null)) {
			if (activeVertexColor == null || activeVertexColor != vertexColorsBufferId) {
				activeVertexColor = vertexColorsBufferId;
				if (vertexColorAttribLocation == -1) {
					vertexColorAttribLocation = getAndEnableAttribLocation("vertexColor");
				}
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexColorsBufferId);
				GLES20.glVertexAttribPointer(vertexColorAttribLocation, 4, GLES20.GL_FLOAT, false, 0, 0);
			}
		}

		if (activeFacesBuffer == null || activeFacesBuffer != buffers.facesBufferId) {
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
			activeFacesBuffer = buffers.facesBufferId;
		}

		setColorUniformIfChanged(objectColorUniform, o3d.material.color, objectColor);

		if (useMap) {
			Integer mapTextureId = gpuUploader.textures.get(o3d.material.map);
			if (renderer3d.mapTextureId != mapTextureId) {
				if (renderer3d.activeTexture != GLES20.GL_TEXTURE0) {
					GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
					renderer3d.activeTexture = GLES20.GL_TEXTURE0;
				}
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gpuUploader.textures.get(o3d.material.map));
				renderer3d.mapTextureId = mapTextureId;
			}
		}
		if (useEnvMap) {
			if (reflectivity != o3d.material.reflectivity) {
				GLES20.glUniform1f(reflectivityUniform, o3d.material.reflectivity);
				reflectivity = o3d.material.reflectivity;
			}
			Integer envMapTextureId = gpuUploader.cubeMapTextures.get(o3d.material.envMap);
			if (renderer3d.envMapTextureId != envMapTextureId) {
				if (renderer3d.activeTexture != GLES20.GL_TEXTURE1) {
					GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
					renderer3d.activeTexture = GLES20.GL_TEXTURE1;
				}
				GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, envMapTextureId);
				renderer3d.envMapTextureId = envMapTextureId;
			}
		}
		if (useNormalMap) {
			Integer normalMapTextureId = gpuUploader.textures.get(o3d.material.normalMap);
			if (renderer3d.normalMapTextureId != normalMapTextureId) {
				if (renderer3d.activeTexture != GLES20.GL_TEXTURE2) {
					GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
					renderer3d.activeTexture = GLES20.GL_TEXTURE2;
				}
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gpuUploader.textures.get(o3d.material.normalMap));
				renderer3d.normalMapTextureId = normalMapTextureId;
			}
		}
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onDrawObject(o3d);
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onDrawObject(o3d);
		}
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, o3d.geometry3d.facesLength, GLES20.GL_UNSIGNED_SHORT, 0);
	}

	@Override
	public int getUniformLocation(String uniformName) {
		return GLES20.glGetUniformLocation(webGLProgram, uniformName);
	}

	@Override
	public float setFloatUniformIfValueChanged(int uniform, float uniformValue, float lastValue) {
		if (lastValue != uniformValue) {
			GLES20.glUniform1f(uniform, uniformValue);
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
			GLES20.glUniformMatrix3fv(matrixUniform, 1, false, newMatrix, 0);
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
			GLES20.glUniformMatrix4fv(matrixUniform, 1, false, newMatrix, 0);
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
			GLES20.glUniform4f(colorUniform, newColor.r, newColor.g, newColor.b, newColor.a);
			lastColor.r = newColor.r;
			lastColor.g = newColor.g;
			lastColor.b = newColor.b;
			lastColor.a = newColor.a;
		}
	}

	@Override
	public void setUniform3fv(int location, int count, float[] v) {
		GLES20.glUniform3fv(location, count, v, 0);
	}

	@Override
	public void setUniform4fv(int location, int count, float[] v) {
		GLES20.glUniform4fv(location, count, v, 0);
	}

	@Override
	public void setUseNormals(boolean useNormals) {
		this.useNormals = useNormals;
	}

	@Override
	public void setUseCameraPosition(boolean useCameraPosition) {
		this.useCameraPosition = useCameraPosition;
	}

	private int getShader(int type, String source) {
		int shader = GLES20.glCreateShader(type);

		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);

		return shader;
	}
}
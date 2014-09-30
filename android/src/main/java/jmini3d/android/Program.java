package jmini3d.android;

import android.opengl.GLES20;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import jmini3d.Color4;
import jmini3d.GpuObjectStatus;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Vector3;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.Light;
import jmini3d.light.PointLight;
import jmini3d.material.Material;

public class Program {
	public static final String TAG = "Program";

	int key = -1;

	int webGLProgram;

	int maxPointLights;
	int maxDirLights;

	boolean useLighting = false;
	boolean useNormals = false;
	boolean useMap = false;
	boolean useEnvMap = false;

	String vertexShaderString;
	String fragmentShaderString;
	float pointLightPositions[];
	float pointLightColors[];
	float dirLightDirections[];
	float dirLightColors[];

	HashMap<String, Integer> attributes = new HashMap<String, Integer>();
	HashMap<String, Integer> uniforms = new HashMap<String, Integer>();

	// *********************** BEGIN cached values to avoid setting uniforms two times
	int map = -1;
	int envMap = -1;
	float perspectiveMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	float modelViewMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	float normalMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	Color4 objectColor = new Color4();
	Color4 ambientColor = new Color4();
	Vector3 cameraPosition = new Vector3(0, 0, 0);
	float reflectivity;
	// *********************** END

	public Program() {

	}

	public void log(String message) {
		Log.d(TAG, message);
	}

	/**
	 * Generates a unique signature for the shaders
	 */
	public static int getSceneKey(Scene scene) {
		boolean useAmbientlight = false;
		int maxPointLights = 0;
		int maxDirLights = 0;

		for (Light light : scene.lights) {
			if (light instanceof AmbientLight) {
				useAmbientlight = true;
			}

			if (light instanceof PointLight) {
				maxPointLights++;
			}

			if (light instanceof DirectionalLight) {
				maxDirLights++;
			}
		}
		return 0xffff + //
				(useAmbientlight ? 0x10000 : 0) + //
				(maxPointLights * 0x0100000) + //
				(maxDirLights * 0x1000000);
	}

	public static int getMaterialKey(Material material) {
		boolean useLight = material.lighting;
		boolean useMap = material.map != null;
		boolean useEnvMap = material.envMap != null;
		boolean useEnvMapAsMap = material.useEnvMapAsMap;

		return (useLight ? 0xffff0000 : 0) + //
				(useMap ? 0x01 : 0) + //
				(useEnvMap ? 0x02 : 0) + //
				(useEnvMapAsMap ? 0x04 : 0);
	}

	public void init(Scene scene, Material material, ResourceLoader resourceLoader) {
		ArrayList<String> attributesInit = new ArrayList<String>();
		ArrayList<String> uniformsInit = new ArrayList<String>();

		ArrayList<String> defines = new ArrayList<String>();
		HashMap<String, String> definesValues = new HashMap<String, String>();

		attributesInit.add("vertexPosition");

		uniformsInit.add("perspectiveMatrix");
		uniformsInit.add("modelViewMatrix");
		uniformsInit.add("cameraPosition");
		uniformsInit.add("objectColor");

		if (material.map != null) {
			defines.add("USE_MAP");
			useMap = true;
			uniformsInit.add("map");
			attributesInit.add("textureCoord");
		}

		if (material.envMap != null) {
			defines.add("USE_ENVMAP");
			useNormals = true;
			useEnvMap = true;
			uniformsInit.add("reflectivity");
			uniformsInit.add("envMap");
		}

		if (material.useEnvMapAsMap) {
			defines.add("USE_ENVMAP_AS_MAP");
		}

		maxPointLights = 0;
		maxDirLights = 0;

		if (material.lighting && scene.lights.size() > 0) {
			defines.add("USE_LIGHTING");
			useLighting = true;

			for (Light light : scene.lights) {

				if (light instanceof AmbientLight) {
					defines.add("USE_AMBIENT_LIGHT");
					uniformsInit.add("ambientColor");
				}

				if (light instanceof PointLight) {
					maxPointLights++;
				}

				if (light instanceof DirectionalLight) {
					maxDirLights++;
				}
			}

			if (maxPointLights > 0) {
				defines.add("USE_POINT_LIGHT");
				uniformsInit.add("pointLightPosition");
				uniformsInit.add("pointLightColor");

				pointLightPositions = new float[maxPointLights * 3];
				pointLightColors = new float[maxPointLights * 3];
			}

			if (maxDirLights > 0) {
				defines.add("USE_DIR_LIGHT");
				uniformsInit.add("dirLightDirection");
				uniformsInit.add("dirLightColor");

				dirLightDirections = new float[maxDirLights * 3];
				dirLightColors = new float[maxDirLights * 3];
			}

			useNormals = true;
		}

		definesValues.put("MAX_POINT_LIGHTS", String.valueOf(maxPointLights));
		definesValues.put("MAX_DIR_LIGHTS", String.valueOf(maxDirLights));

		if (useNormals) {
			defines.add("USE_NORMALS");
			attributesInit.add("vertexNormal");
			uniformsInit.add("normalMatrix");
		}

		StringBuffer vertexShaderStringBuffer = new StringBuffer();
		StringBuffer fragmentShaderStringBuffer = new StringBuffer();

		// TODO precision
		for (String d : defines) {
			vertexShaderStringBuffer.append("#define " + d + "\n");
			fragmentShaderStringBuffer.append("#define " + d + "\n");
		}

		for (String k : definesValues.keySet()) {
			vertexShaderStringBuffer.append("#define " + k + " " + definesValues.get(k) + "\n");
			fragmentShaderStringBuffer.append("#define " + k + " " + definesValues.get(k) + "\n");
		}

		fragmentShaderStringBuffer.append(resourceLoader.loadRawString(R.raw.fragment_shader));
		vertexShaderStringBuffer.append(resourceLoader.loadRawString(R.raw.vertex_shader));

		vertexShaderString = vertexShaderStringBuffer.toString();
		fragmentShaderString = fragmentShaderStringBuffer.toString();

//		log(vertexShaderStringBuffer.toString());
//		log(fragmentShaderStringBuffer.toString());

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

		for (String s : attributesInit) {
			Integer attribLocation = GLES20.glGetAttribLocation(webGLProgram, s);
//			log("attribute: " + s + " = " + attribLocation);
			GLES20.glEnableVertexAttribArray(attribLocation);
			attributes.put(s, attribLocation);
		}

		for (String s : uniformsInit) {
			uniforms.put(s, GLES20.glGetUniformLocation(webGLProgram, s));
//			log("uniform: " + s + " = " + GLES20.glGetUniformLocation(webGLProgram, s));
		}
		GLES20.glDeleteShader(vertexShader);
		GLES20.glDeleteShader(fragmentShader);
	}

	public void setSceneUniforms(Scene scene) {
		if (useMap && map != 0) {
			GLES20.glUniform1i(uniforms.get("map"), 0);
			map = 0;
		}
		if (useEnvMap) {
			if (envMap != 1) {
				GLES20.glUniform1i(uniforms.get("envMap"), 1);
				envMap = 1;
			}
			if (!cameraPosition.equals(scene.camera.position)) {
				GLES20.glUniform3f(uniforms.get("cameraPosition"), scene.camera.position.x, scene.camera.position.y, scene.camera.position.z);
				cameraPosition.setAllFrom(scene.camera.position);
			}
		}

		if (useLighting) {
			int pointLightIndex = 0;
			int dirLightIndex = 0;

			for (int i = 0; i < scene.lights.size(); i++) {
				Light light = scene.lights.get(i);
				if (light instanceof AmbientLight) {
					if (!ambientColor.equals(light.color)) {
						GLES20.glUniform3f(uniforms.get("ambientColor"), light.color.r, light.color.g, light.color.b);
						ambientColor.setAllFrom(light.color);
					}
				}

				if (light instanceof PointLight) {
					pointLightPositions[pointLightIndex * 3] = ((PointLight) light).position.x;
					pointLightPositions[pointLightIndex * 3 + 1] = ((PointLight) light).position.y;
					pointLightPositions[pointLightIndex * 3 + 2] = ((PointLight) light).position.z;

					pointLightColors[pointLightIndex * 3] = light.color.r;
					pointLightColors[pointLightIndex * 3 + 1] = light.color.g;
					pointLightColors[pointLightIndex * 3 + 2] = light.color.b;

					pointLightIndex++;
				}

				if (light instanceof DirectionalLight) {
					dirLightDirections[dirLightIndex * 3] = ((DirectionalLight) light).direction.x;
					dirLightDirections[dirLightIndex * 3 + 1] = ((DirectionalLight) light).direction.y;
					dirLightDirections[dirLightIndex * 3 + 2] = ((DirectionalLight) light).direction.z;

					dirLightColors[dirLightIndex * 3] = light.color.r;
					dirLightColors[dirLightIndex * 3 + 1] = light.color.g;
					dirLightColors[dirLightIndex * 3 + 2] = light.color.b;

					dirLightIndex++;
				}
			}
			if (maxPointLights > 0) {
				GLES20.glUniform3fv(uniforms.get("pointLightPosition"), maxPointLights, pointLightPositions, 0);
				GLES20.glUniform3fv(uniforms.get("pointLightColor"), maxPointLights, pointLightColors, 0);
			}
			if (maxDirLights > 0) {
				GLES20.glUniform3fv(uniforms.get("dirLightDirection"), maxDirLights, dirLightDirections, 0);
				GLES20.glUniform3fv(uniforms.get("dirLightColor"), maxDirLights, dirLightColors, 0);
			}
		}
	}

	public void drawObject(Renderer3d renderer3d, GpuUploader gpuUploader, Object3d o3d, float[] perspectiveMatrix) {
		if (!Arrays.equals(this.perspectiveMatrix, perspectiveMatrix)) {
			GLES20.glUniformMatrix4fv(uniforms.get("perspectiveMatrix"), 1, false, perspectiveMatrix, 0);
			System.arraycopy(perspectiveMatrix, 0, this.perspectiveMatrix, 0, 16);
		}
		if (!Arrays.equals(modelViewMatrix, o3d.modelViewMatrix)) {
			GLES20.glUniformMatrix4fv(uniforms.get("modelViewMatrix"), 1, false, o3d.modelViewMatrix, 0);
			System.arraycopy(o3d.modelViewMatrix, 0, modelViewMatrix, 0, 16);
		}
		if (useNormals && o3d.normalMatrix != null && !Arrays.equals(normalMatrix, o3d.normalMatrix)) {
			GLES20.glUniformMatrix3fv(uniforms.get("normalMatrix"), 1, false, o3d.normalMatrix, 0);
			System.arraycopy(o3d.normalMatrix, 0, normalMatrix, 0, 9);
		}

		GeometryBuffers buffers = gpuUploader.upload(o3d.geometry3d);

		if (useMap) {
			gpuUploader.upload(renderer3d, o3d.material.map);
			if ((o3d.material.map.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (useEnvMap) {
			gpuUploader.upload(renderer3d, o3d.material.envMap);
			if ((o3d.material.envMap.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.vertexBufferId);
		GLES20.glVertexAttribPointer(attributes.get("vertexPosition"), 3, GLES20.GL_FLOAT, false, 0, 0);

		if (useNormals) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.normalsBufferId);
			GLES20.glVertexAttribPointer(attributes.get("vertexNormal"), 3, GLES20.GL_FLOAT, false, 0, 0);
		}

		if (useMap) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.uvsBufferId);
			GLES20.glVertexAttribPointer(attributes.get("textureCoord"), 2, GLES20.GL_FLOAT, false, 0, 0);
		}

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);

		if (!objectColor.equals(o3d.material.color)) {
			GLES20.glUniform4f(uniforms.get("objectColor"), o3d.material.color.r, o3d.material.color.g, o3d.material.color.b, o3d.material.color.a);
			objectColor.setAllFrom(o3d.material.color);
		}
		if (useMap) {
			Integer mapTextureId = gpuUploader.textures.get(o3d.material.map);
			if (renderer3d.mapTextureId != mapTextureId) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gpuUploader.textures.get(o3d.material.map));
				renderer3d.mapTextureId = mapTextureId;
			}
		}
		if (useEnvMap) {
			if (reflectivity != o3d.material.reflectivity) {
				GLES20.glUniform1f(uniforms.get("reflectivity"), o3d.material.reflectivity);
				reflectivity = o3d.material.reflectivity;
			}
			Integer envMapTextureId = gpuUploader.cubeMapTextures.get(o3d.material.envMap);
			if (renderer3d.envMapTextureId != envMapTextureId) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, envMapTextureId);
				renderer3d.envMapTextureId = envMapTextureId;
			}
		}

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, o3d.geometry3d.facesLength, GLES20.GL_UNSIGNED_SHORT, 0);
	}

	private int getShader(int type, String source) {
		int shader = GLES20.glCreateShader(type);

		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);

//		int[] linkStatus = new int[1];
//		GLES20.glGetProgramiv(webGLProgram, GLES20.GL_COMPILE_STATUS, linkStatus, 0);
//		if (linkStatus[0] != GLES20.GL_TRUE) {
//			Log.e(TAG, "Could not compile shaders: ");
//			Log.e(TAG, source);
//			GLES20.glDeleteProgram(webGLProgram);
//			throw new RuntimeException(GLES20.glGetShaderInfoLog(shader));
//		}

		return shader;
	}
}

package jmini3d.gwt;

import com.googlecode.gwtgl.array.Float32Array;
import com.googlecode.gwtgl.binding.WebGLProgram;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLShader;
import com.googlecode.gwtgl.binding.WebGLUniformLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.GpuObjectStatus;
import jmini3d.Material;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.Vector3;
import jmini3d.light.AmbientLight;
import jmini3d.light.DirectionalLight;
import jmini3d.light.Light;
import jmini3d.light.PointLight;

public class Program {
	static final String TAG = "Program";

	WebGLProgram webGLProgram;

	int maxPointLights;
	int maxDirLights;
	Float32Array pointLightPositions;
	Float32Array pointLightColors;
	Float32Array dirLightDirections;
	Float32Array dirLightColors;

	boolean useLighting = false;
	boolean useNormals = false;
	boolean useMap = false;
	boolean useEnvMap = false;

	HashMap<String, Integer> attributes = new HashMap<String, Integer>();
	HashMap<String, WebGLUniformLocation> uniforms = new HashMap<String, WebGLUniformLocation>();

	// *********************** BEGIN cached values to avoid setting uniforms two times
	static Texture mapTexture;
	static CubeMapTexture envMapTexture;
	int map = -1;
	int envMap = -1;
	float perspectiveMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	float modelViewMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	float normalMatrix[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	Color4 objectColor = new Color4(-1, -1, -1, -1);
	Color4 ambientColor = new Color4(0, 0, 0, 0);
	Vector3 cameraPosition = new Vector3(0, 0, 0);
	float reflectivity;
	// *********************** END

	private WebGLRenderingContext gl;

	public Program(WebGLRenderingContext gl) {
		this.gl = gl;
	}

	native void log(String message) /*-{
		console.log(message);
    }-*/;

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

	public void init(Scene scene, Material material) {
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

				pointLightPositions = Float32Array.create(maxPointLights * 3);
				pointLightColors = Float32Array.create(maxPointLights * 3);
			}

			if (maxDirLights > 0) {
				defines.add("USE_DIR_LIGHT");
				uniformsInit.add("dirLightDirection");
				uniformsInit.add("dirLightColor");

				dirLightDirections = Float32Array.create(maxDirLights * 3);
				dirLightColors = Float32Array.create(maxDirLights * 3);
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

		fragmentShaderStringBuffer.append(EngineResources.INSTANCE.fragmentShader().getText());
		vertexShaderStringBuffer.append(EngineResources.INSTANCE.vertexShader().getText());

		String vertexShaderString = vertexShaderStringBuffer.toString();
		String fragmentShaderString = fragmentShaderStringBuffer.toString();

//		log(vertexShaderStringBuffer.toString());
//		log(fragmentShaderStringBuffer.toString());

		WebGLShader vertexShader = getShader(WebGLRenderingContext.VERTEX_SHADER, vertexShaderString);
		WebGLShader fragmentShader = getShader(WebGLRenderingContext.FRAGMENT_SHADER, fragmentShaderString);

		webGLProgram = gl.createProgram();
		gl.attachShader(webGLProgram, vertexShader);
		gl.attachShader(webGLProgram, fragmentShader);
		gl.linkProgram(webGLProgram);

		if (!gl.getProgramParameterb(webGLProgram, WebGLRenderingContext.LINK_STATUS)) {
			throw new RuntimeException("Could not initialize shaders");
		}
		gl.useProgram(webGLProgram);

		for (String s : attributesInit) {
			Integer attribLocation = gl.getAttribLocation(webGLProgram, s);
//			log("attribute: " + s + " = " + attribLocation);
			gl.enableVertexAttribArray(attribLocation);
			attributes.put(s, attribLocation);
		}

		for (String s : uniformsInit) {
			uniforms.put(s, gl.getUniformLocation(webGLProgram, s));
//			log("uniform: " + s + " = " + gl.getUniformLocation(webGLProgram, s));
		}
		gl.deleteShader(vertexShader);
		gl.deleteShader(fragmentShader);
	}

	public void setSceneUniforms(Scene scene) {
		if (useMap && map != 0) {
			gl.uniform1i(uniforms.get("map"), 0);
			map = 0;
		}
		if (useEnvMap) {
			if (envMap != 1) {
				gl.uniform1i(uniforms.get("envMap"), 1);
				envMap = 1;
			}
			if (!cameraPosition.equals(scene.camera.position)) {
				gl.uniform3f(uniforms.get("cameraPosition"), scene.camera.position.x, scene.camera.position.y, scene.camera.position.z);
				cameraPosition.setAllFrom(scene.camera.position);
			}
		}

		if (useLighting) {
			int pointLightIndex = 0;
			int dirLightIndex = 0;

			for (Light light : scene.lights) {
				if (light instanceof AmbientLight) {
					if (!ambientColor.equals(light.color)) {
						gl.uniform3f(uniforms.get("ambientColor"), light.color.r, light.color.g, light.color.b);
						ambientColor.setAllFrom(light.color);
					}
				}

				if (light instanceof PointLight) {
					pointLightPositions.set(pointLightIndex * 3, ((PointLight) light).position.x);
					pointLightPositions.set(pointLightIndex * 3 + 1, ((PointLight) light).position.y);
					pointLightPositions.set(pointLightIndex * 3 + 2, ((PointLight) light).position.z);

					pointLightColors.set(pointLightIndex * 3, light.color.r);
					pointLightColors.set(pointLightIndex * 3 + 1, light.color.g);
					pointLightColors.set(pointLightIndex * 3 + 2, light.color.b);

					pointLightIndex++;
				}

				if (light instanceof DirectionalLight) {
					dirLightDirections.set(dirLightIndex * 3, ((DirectionalLight) light).direction.x);
					dirLightDirections.set(dirLightIndex * 3 + 1, ((DirectionalLight) light).direction.y);
					dirLightDirections.set(dirLightIndex * 3 + 2, ((DirectionalLight) light).direction.z);

					dirLightColors.set(dirLightIndex * 3, light.color.r);
					dirLightColors.set(dirLightIndex * 3 + 1, light.color.g);
					dirLightColors.set(dirLightIndex * 3 + 2, light.color.b);

					dirLightIndex++;
				}
			}
			if (maxPointLights > 0) {
				gl.uniform3fv(uniforms.get("pointLightPosition"), pointLightPositions);
				gl.uniform3fv(uniforms.get("pointLightColor"), pointLightColors);
			}
			if (maxDirLights > 0) {
				gl.uniform3fv(uniforms.get("dirLightDirection"), dirLightDirections);
				gl.uniform3fv(uniforms.get("dirLightColor"), dirLightColors);
			}
		}
	}

	public void drawObject(GpuUploader gpuUploader, Object3d o3d, float[] perspectiveMatrix) {
		if (!Arrays.equals(this.perspectiveMatrix, perspectiveMatrix)) {
			gl.uniformMatrix4fv(uniforms.get("perspectiveMatrix"), false, perspectiveMatrix);
			System.arraycopy(perspectiveMatrix, 0, this.perspectiveMatrix, 0, 16);
		}
		if (!Arrays.equals(modelViewMatrix, o3d.modelViewMatrix)) {
			gl.uniformMatrix4fv(uniforms.get("modelViewMatrix"), false, o3d.modelViewMatrix);
			System.arraycopy(o3d.modelViewMatrix, 0, modelViewMatrix, 0, 16);
		}
		if (useNormals && o3d.normalMatrix != null && !Arrays.equals(normalMatrix, o3d.normalMatrix)) {
			gl.uniformMatrix3fv(uniforms.get("normalMatrix"), false, o3d.normalMatrix);
			System.arraycopy(o3d.normalMatrix, 0, normalMatrix, 0, 9);
		}

		GeometryBuffers buffers = gpuUploader.upload(o3d.geometry3d);

		if (useMap) {
			gpuUploader.upload(o3d.material.map);
			if ((o3d.material.map.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}
		if (useEnvMap) {
			gpuUploader.upload(o3d.material.envMap);
			if ((o3d.material.envMap.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
				return;
			}
		}

		gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.vertexBufferId);
		gl.vertexAttribPointer(attributes.get("vertexPosition"), 3, WebGLRenderingContext.FLOAT, false, 0, 0);

		if (useNormals) {
			gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.normalsBufferId);
			gl.vertexAttribPointer(attributes.get("vertexNormal"), 3, WebGLRenderingContext.FLOAT, false, 0, 0);
		}

		if (useMap) {
			gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.uvsBufferId);
			gl.vertexAttribPointer(attributes.get("textureCoord"), 2, WebGLRenderingContext.FLOAT, false, 0, 0);
		}

		gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);

		if (!objectColor.equals(o3d.material.color)) {
			gl.uniform4f(uniforms.get("objectColor"), o3d.material.color.r, o3d.material.color.g, o3d.material.color.b, o3d.material.color.a);
			objectColor.setAllFrom(o3d.material.color);
		}
		if (useMap && mapTexture != o3d.material.map) {
			gl.activeTexture(WebGLRenderingContext.TEXTURE0);
			gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, gpuUploader.textures.get(o3d.material.map));
			mapTexture = o3d.material.map;
		}
		if (useEnvMap) {
			if (reflectivity != o3d.material.reflectivity) {
				gl.uniform1f(uniforms.get("reflectivity"), o3d.material.reflectivity);
				reflectivity = o3d.material.reflectivity;
			}
			if (envMapTexture != o3d.material.envMap) {
				gl.activeTexture(WebGLRenderingContext.TEXTURE1);
				gl.bindTexture(WebGLRenderingContext.TEXTURE_CUBE_MAP, gpuUploader.cubeMapTextures.get(o3d.material.envMap));
				envMapTexture = o3d.material.envMap;
			}
		}

		gl.drawElements(WebGLRenderingContext.TRIANGLES, o3d.geometry3d.facesLength, WebGLRenderingContext.UNSIGNED_SHORT, 0);
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
}

package jmini3d.gwt;

import com.googlecode.gwtgl.binding.WebGLProgram;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLShader;

import jmini3d.Color4;
import jmini3d.GpuObjectStatus;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.material.Material;

public class Program extends jmini3d.shader.Program {
	static final String TAG = Program.class.getName();

	WebGLProgram webGLProgram;

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

	public void init(final Scene scene, final Material material, final ResourceLoader resourceLoader, final GpuUploaderListener gpuUploaderListener) {
		prepareShader(scene, material);

		resourceLoader.loadShader(vertexShaderName, text -> {
			vertexShaderLoaded = text;
			if (vertexShaderLoaded != null && fragmentShaderLoaded != null) {
				finishShaderLoad(scene, material);
				if (gpuUploaderListener != null) {
					gpuUploaderListener.onGpuUploadFinish();
				}
			}
		});
		resourceLoader.loadShader(fragmentShaderName, text -> {
			fragmentShaderLoaded = text;
			if (vertexShaderLoaded != null && fragmentShaderLoaded != null) {
				finishShaderLoad(scene, material);
				if (gpuUploaderListener != null) {
					gpuUploaderListener.onGpuUploadFinish();
				}
			}
		});
	}

	public void finishShaderLoad(Scene scene, Material material) {
		StringBuilder vertexShaderStringBuffer = new StringBuilder();
		StringBuilder fragmentShaderStringBuffer = new StringBuilder();

		// TODO precision
		for (String k : shaderDefines.keySet()) {
			if (shaderDefines.get(k) == null) {
				vertexShaderStringBuffer.append("#define ").append(k).append("\n");
				fragmentShaderStringBuffer.append("#define ").append(k).append("\n");
			} else {
				vertexShaderStringBuffer.append("#define ").append(k).append(" ").append(shaderDefines.get(k)).append("\n");
				fragmentShaderStringBuffer.append("#define ").append(k).append(" ").append(shaderDefines.get(k)).append("\n");
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

		onShaderLoaded();

		GLES20.deleteShader(vertexShader);
		GLES20.deleteShader(fragmentShader);

		vertexShaderLoaded = null;
		fragmentShaderLoaded = null;
		shaderLoaded = true;
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

		if (useMap && mapLast != 0) {
			GLES20.uniform1i(mapUniform, 0);
			mapLast = 0;
		}
		if (useEnvMap && envMapLast != 1) {
			GLES20.uniform1i(envMapUniform, 1);
			envMapLast = 1;
		}
		if (useCameraPosition && !cameraPositionLast.equals(scene.camera.position)) {
			GLES20.uniform3f(cameraPositionUniform, scene.camera.position.x, scene.camera.position.y, scene.camera.position.z);
			cameraPositionLast.setAllFrom(scene.camera.position);
		}
		if (useNormalMap && normalMapLast != 2) {
			GLES20.uniform1i(normalMapUniform, 2);
			normalMapLast = 2;
		}
		if (sceneProgramPlugin != null) {
			sceneProgramPlugin.onSetSceneUniforms(scene);
		}
		if (materialProgramPlugin != null) {
			materialProgramPlugin.onSetSceneUniforms(scene);
		}
	}

	public void drawObject(Renderer3d renderer3d, GpuUploader gpuUploader, Object3d o3d, float[] projectionMatrix, float[] viewMatrix) {
		if (!shaderLoaded) {
			return;
		}

		setMatrix4UniformIfChanged(projectionMatrixUniform, projectionMatrix, this.projectionMatrixLast);
		setMatrix4UniformIfChanged(viewMatrixUniform, viewMatrix, this.viewMatrixLast);
		setMatrix4UniformIfChanged(modelMatrixUniform, o3d.modelMatrix, this.modelMatrixLast);
		if ((useNormals || useNormalMap) && o3d.normalMatrix != null) {
			setMatrix3UniformIfChanged(normalMatrixUniform, o3d.normalMatrix, this.normalMatrixLast);
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

		setColorUniformIfChanged(objectColorUniform, o3d.material.color, objectColorLast);

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
			if (reflectivityLast != o3d.material.reflectivity) {
				GLES20.uniform1f(reflectivityUniform, o3d.material.reflectivity);
				reflectivityLast = o3d.material.reflectivity;
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
	public int getAndEnableAttribLocation(String attribName) {
		int attribLocation = GLES20.getAttribLocation(webGLProgram, attribName);
		GLES20.enableVertexAttribArray(attribLocation);
		return attribLocation;
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

	private void setMatrix3UniformIfChanged(int matrixUniform, float[] newMatrix, float[] lastMatrix) {
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

	private void setMatrix4UniformIfChanged(int matrixUniform, float[] newMatrix, float[] lastMatrix) {
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

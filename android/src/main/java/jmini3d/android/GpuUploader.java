package jmini3d.android;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import jmini3d.CubeMapTexture;
import jmini3d.GpuObjectStatus;
import jmini3d.Material;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.geometry.Geometry;

public class GpuUploader {
	static final String TAG = "GpuUploader";

	// Use our Axis system
	static final int[] CUBE_MAP_SIDES = {GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, //
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, //
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y};

	ResourceLoader resourceLoader;

	HashMap<Geometry, GeometryBuffers> geometryBuffers = new HashMap<Geometry, GeometryBuffers>();
	HashMap<Texture, Integer> textures = new HashMap<Texture, Integer>();
	HashMap<CubeMapTexture, Integer> cubeMapTextures = new HashMap<CubeMapTexture, Integer>();
	ArrayList<Program> shaderPrograms = new ArrayList<Program>();

	public GpuUploader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Program getProgram(Scene scene, Material material) {
		if (scene.shaderKey == -1) {
			scene.shaderKey = Program.getSceneKey(scene);
		}
		if (material.shaderKey == -1) {
			material.shaderKey = Program.getMaterialKey(material);
		}
		int key = scene.shaderKey & material.shaderKey;
		Program program = null;
		// Use ArrayList instead HashMap to avoid Integer creation
		for (int i = 0; i < shaderPrograms.size(); i++) {
			if (key == shaderPrograms.get(i).key) {
				program = shaderPrograms.get(i);
			}
		}
		if (program == null) {
			program = new Program();
			program.key = key;
			program.init(scene, material, resourceLoader);
			shaderPrograms.add(program);
		}
		return program;
	}

	public GeometryBuffers upload(Geometry geometry3d) {
		GeometryBuffers buffers = geometryBuffers.get(geometry3d);
		if (buffers == null) {
			buffers = new GeometryBuffers();
			geometryBuffers.put(geometry3d, buffers);
		}

		if ((geometry3d.status & GpuObjectStatus.VERTICES_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.VERTICES_UPLOADED;
			float[] vertex = geometry3d.vertex();
			if (vertex != null) {
				if (buffers.vertexBufferId == null) {
					int[] vboId = new int[1];
					GLES20.glGenBuffers(1, vboId, 0);
					buffers.vertexBufferId = vboId[0];
				}
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.vertexBufferId);
				GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4, FloatBuffer.wrap(vertex), GLES20.GL_STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.NORMALS_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.NORMALS_UPLOADED;
			float[] normals = geometry3d.normals();
			if (normals != null) {
				if (buffers.normalsBufferId == null) {
					int[] vboId = new int[1];
					GLES20.glGenBuffers(1, vboId, 0);
					buffers.normalsBufferId = vboId[0];
				}
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.normalsBufferId);
				GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normals.length * 4, FloatBuffer.wrap(normals), GLES20.GL_STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.UVS_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.UVS_UPLOADED;
			float[] uvs = geometry3d.uvs();
			if (uvs != null) {
				if (buffers.uvsBufferId == null) {
					int[] vboId = new int[1];
					GLES20.glGenBuffers(1, vboId, 0);
					buffers.uvsBufferId = vboId[0];
				}
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers.uvsBufferId);
				GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, uvs.length * 4, FloatBuffer.wrap(uvs), GLES20.GL_STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.FACES_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.FACES_UPLOADED;
			short[] faces = geometry3d.faces();
			if (faces != null) {
				geometry3d.facesLength = faces.length;
				if (buffers.facesBufferId == null) {
					int[] vboId = new int[1];
					GLES20.glGenBuffers(1, vboId, 0);
					buffers.facesBufferId = vboId[0];
				}

				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
				GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, faces.length * 2, ShortBuffer.wrap(faces), GLES20.GL_STATIC_DRAW);
			}
		}

		return buffers;
	}

	public void upload(Renderer3d renderer3d, Texture texture) {
		if ((texture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
			texture.status |= GpuObjectStatus.TEXTURE_UPLOADED;

			Bitmap bitmap;
			try {
				bitmap = resourceLoader.getImage(texture.image);
			} catch (Exception e) {
				return;
			}
			Integer textureId = textures.get(texture);
			if (textureId == null) {
				int[] texturesIds = new int[1];
				GLES20.glGenTextures(1, texturesIds, 0);
				textureId = texturesIds[0];
				textures.put(texture, textureId);
			}

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
			renderer3d.mapTextureId = textureId;

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

			resourceLoader.freeBitmap(texture.image, bitmap);
		}
	}

	public void upload(Renderer3d renderer3d, CubeMapTexture cubeMapTexture) {
		if ((cubeMapTexture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
			cubeMapTexture.status |= GpuObjectStatus.TEXTURE_UPLOADED;

			Integer textureId = textures.get(cubeMapTexture);
			if (textureId == null) {
				int[] texturesIds = new int[1];
				GLES20.glGenTextures(1, texturesIds, 0);
				textureId = texturesIds[0];
				cubeMapTextures.put(cubeMapTexture, textureId);
			}
			cubeMapTextures.put(cubeMapTexture, textureId);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
			renderer3d.envMapTextureId = textureId;

			for (int i = 0; i < 6; i++) {
				Bitmap bitmap;
				try {
					bitmap = resourceLoader.getImage(cubeMapTexture.images[i]);
				} catch (Exception e) {
					Log.e(TAG, "Texture image not found in resources: " + cubeMapTexture.images[i]);
					return;
				}

				GLUtils.texImage2D(CUBE_MAP_SIDES[i], 0, bitmap, 0);

				resourceLoader.freeBitmap(null, bitmap);
			}
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
		}
	}

	public void unload(Object o) {
		if (o instanceof Geometry) {
			if (geometryBuffers.containsKey(o)) {
				((Geometry) o).status = 0;
				GeometryBuffers buffers = geometryBuffers.get(o);

				if (buffers.vertexBufferId != null) {
					GLES20.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.vertexBufferId}));
				}
				if (buffers.normalsBufferId != null) {
					GLES20.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.normalsBufferId}));
				}
				if (buffers.uvsBufferId != null) {
					GLES20.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.uvsBufferId}));
				}
				if (buffers.facesBufferId != null) {
					GLES20.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.facesBufferId}));
				}

				geometryBuffers.remove(o);
			}
		} else if (o instanceof Texture) {
			if (textures.containsKey(o)) {
				((Texture) o).status = 0;
				GLES20.glDeleteTextures(1, IntBuffer.wrap(new int[]{textures.get(o)}));
				textures.remove(o);
			}
		} else if (o instanceof CubeMapTexture) {
			if (cubeMapTextures.containsKey(o)) {
				((Texture) o).status = 0;
				GLES20.glDeleteTextures(1, IntBuffer.wrap(new int[]{cubeMapTextures.get(o)}));
				textures.remove(o);
			}
		}
	}

	public void reset() {
		// Now force re-upload of all objects
		for (Geometry geometry : geometryBuffers.keySet()) {
			geometry.status = 0;
		}
		for (Texture texture : textures.keySet()) {
			texture.status = 0;
		}
		for (CubeMapTexture texture : cubeMapTextures.keySet()) {
			texture.status = 0;
		}
		geometryBuffers.clear();
		textures.clear();
		cubeMapTextures.clear();
		shaderPrograms.clear();
	}
}
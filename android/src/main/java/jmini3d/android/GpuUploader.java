package jmini3d.android;

import android.graphics.Bitmap;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import jmini3d.Geometry3d;
import jmini3d.GpuObjectStatus;
import jmini3d.Texture;

public class GpuUploader {
	static final String TAG = "GpuUploader";

	// static final int[] CUBE_MAP_SIDES = {
	// GLES10.GL_TEXTURE_CUBE_MAP_POSITIVE_X, GLES10.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
	// GLES10.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GLES10.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
	// GLES10.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GLES10.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
	// };

	ResourceLoader resourceLoader;

	HashMap<Geometry3d, GeometryBuffers> geometryBuffers = new HashMap<Geometry3d, GeometryBuffers>();
	HashMap<Texture, Integer> textures = new HashMap<Texture, Integer>();

	// HashMap<Texture, Image> textureImages = new HashMap<Texture, Image>();
	// HashMap<CubeMapTexture, WebGLTexture> cubeMapTextures = new
	// HashMap<CubeMapTexture, WebGLTexture>();
	// HashMap<CubeMapTexture, Image[]> cubeMapImages = new
	// HashMap<CubeMapTexture, Image[]>();

	float openGlVersion = 1.0f;

	public GpuUploader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public GeometryBuffers upload(Geometry3d geometry3d) {
		GeometryBuffers buffers = geometryBuffers.get(geometry3d);
		if (buffers == null) {
			buffers = new GeometryBuffers();
			geometryBuffers.put(geometry3d, buffers);
		}

		if (openGlVersion <= 1.0) {
			if ((geometry3d.status & GpuObjectStatus.VERTICES_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.VERTICES_UPLOADED;
				float[] vertex = geometry3d.vertex();
				if (vertex != null) {
					if (buffers.vertexBuffer == null) {
						buffers.vertexBuffer = FloatBuffer.wrap(vertex);
					} else {
						buffers.vertexBuffer.position(0);
						buffers.vertexBuffer.put(vertex);
					}
				}
			}

			if ((geometry3d.status & GpuObjectStatus.NORMALS_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.NORMALS_UPLOADED;
				float[] normals = geometry3d.normals();
				if (normals != null) {
					if (buffers.normalsBuffer == null) {
						buffers.normalsBuffer = FloatBuffer.wrap(normals);
					} else {
						buffers.normalsBuffer.position(0);
						buffers.normalsBuffer.put(normals);
					}
				}
			}

			if ((geometry3d.status & GpuObjectStatus.UVS_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.UVS_UPLOADED;
				float[] uvs = geometry3d.uvs();
				if (uvs != null) {
					if (buffers.uvsBuffer == null) {
						buffers.uvsBuffer = FloatBuffer.wrap(uvs);
					} else {
						buffers.uvsBuffer.position(0);
						buffers.uvsBuffer.put(uvs);
					}
				}
			}

			if ((geometry3d.status & GpuObjectStatus.FACES_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.FACES_UPLOADED;
				short[] faces = geometry3d.faces();
				if (faces != null) {
					geometry3d.facesLength = faces.length;
					if (buffers.facesBuffer == null) {
						buffers.facesBuffer = ShortBuffer.wrap(faces);
					} else {
						buffers.facesBuffer.position(0);
						buffers.facesBuffer.put(faces);
					}
				}
			}

		} else {
			if ((geometry3d.status & GpuObjectStatus.VERTICES_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.VERTICES_UPLOADED;
				float[] vertex = geometry3d.vertex();
				if (vertex != null) {
					if (buffers.vertexBufferId == null) {
						int[] vboId = new int[1];
						GLES11.glGenBuffers(1, vboId, 0);
						buffers.vertexBufferId = vboId[0];
					}
					GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, buffers.vertexBufferId);
					GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, vertex.length * 4, FloatBuffer.wrap(vertex), GLES11.GL_STATIC_DRAW);
				}
			}

			if ((geometry3d.status & GpuObjectStatus.NORMALS_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.NORMALS_UPLOADED;
				float[] normals = geometry3d.normals();
				if (normals != null) {
					if (buffers.normalsBufferId == null) {
						int[] vboId = new int[1];
						GLES11.glGenBuffers(1, vboId, 0);
						buffers.normalsBufferId = vboId[0];
					}
					GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, buffers.normalsBufferId);
					GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, normals.length * 4, FloatBuffer.wrap(normals), GLES11.GL_STATIC_DRAW);
				}
			}

			if ((geometry3d.status & GpuObjectStatus.UVS_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.UVS_UPLOADED;
				float[] uvs = geometry3d.uvs();
				if (uvs != null) {
					if (buffers.uvsBufferId == null) {
						int[] vboId = new int[1];
						GLES11.glGenBuffers(1, vboId, 0);
						buffers.uvsBufferId = vboId[0];
					}
					GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, buffers.uvsBufferId);
					GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, uvs.length * 4, FloatBuffer.wrap(uvs), GLES11.GL_STATIC_DRAW);
				}
			}

			if ((geometry3d.status & GpuObjectStatus.FACES_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.FACES_UPLOADED;
				short[] faces = geometry3d.faces();
				if (faces != null) {
					geometry3d.facesLength = faces.length;
					if (buffers.facesBufferId == null) {
						int[] vboId = new int[1];
						GLES11.glGenBuffers(1, vboId, 0);
						buffers.facesBufferId = vboId[0];
					}

					GLES11.glBindBuffer(GLES11.GL_ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
					GLES11.glBufferData(GLES11.GL_ELEMENT_ARRAY_BUFFER, faces.length * 2, ShortBuffer.wrap(faces), GLES11.GL_STATIC_DRAW);
				}
			}
		}
		return buffers;
	}

	public void upload(Texture texture) {
		if ((texture.status & GpuObjectStatus.TEXTURE_UPLOADED) == 0) {
			texture.status |= GpuObjectStatus.TEXTURE_UPLOADED;
			Bitmap bitmap;
			try {
				bitmap = resourceLoader.getImage(texture.image);
			} catch (Exception e) {
				Log.e(TAG, "Texture image not found in resources: " + texture.image);
				return;
			}
			Integer textureId = textures.get(texture);
			if (textureId == null) {
				int[] texturesIds = new int[1];
				GLES10.glGenTextures(1, texturesIds, 0);
				textureId = texturesIds[0];
				textures.put(texture, textureId);
			}

			GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, textureId);
			// GLES10.pixelStorei(GLES10.UNPACK_FLIP_Y_WEBGL, 1);
			GLUtils.texImage2D(GLES10.GL_TEXTURE_2D, 0, bitmap, 0);
			// GLES10.glTexImage2D(GLES10.GL_TEXTURE_2D, 0, GLES10.GL_RGBA,
			// GLES10.GL_RGBA, GLES10.GL_UNSIGNED_BYTE,
			// imageResource);
			GLES10.glTexParameterx(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR);
			GLES10.glTexParameterx(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_LINEAR);
			GLES10.glTexParameterx(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S, GLES10.GL_CLAMP_TO_EDGE);
			GLES10.glTexParameterx(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T, GLES10.GL_CLAMP_TO_EDGE);
			// GLES10.generateMipmap(GLES10.TEXTURE_2D);
			GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, 0);

			Renderer.needsRedraw = true;

			resourceLoader.freeBitmap(texture.image, bitmap);
		}
	}

	// public void upload(final CubeMapTexture cubeMapTexture) {
	// if (cubeMapTexture.status != GpuObjectStatus.PENDING) {
	// return;
	// }
	// cubeMapTexture.status = GpuObjectStatus.UPLOADING;
	//
	// WebGLTexture texture = GLES10.createTexture();
	// cubeMapTextures.put(cubeMapTexture, texture);
	//
	// Image[] textureImages = new Image[6];
	// cubeMapImages.put(cubeMapTexture, textureImages);
	//
	// for (int i = 0; i < 6; i++) {
	// textureImages[i] = new
	// Image(resourceLoader.getImage(cubeMapTexture.images[i]).getSafeUri());
	// textureImages[i].addLoadHandler(new LoadHandler() {
	// @Override
	// public void onLoad(LoadEvent event) {
	// cubeTextureLoaded(cubeMapTexture);
	// }
	// });
	// RootPanel.get("preload").add(textureImages[i]);
	// }
	// }

	// public void textureLoaded(Texture texture) {
	//
	// }

	// public void cubeTextureLoaded(CubeMapTexture cubeMapTexture) {
	// cubeMapTexture.loadCount++;
	// if (cubeMapTexture.loadCount == 6) {
	// GLES10.bindTexture(GLES10.TEXTURE_CUBE_MAP,
	// cubeMapTextures.get(cubeMapTexture));
	// // GLES10.pixelStorei(GLES10.UNPACK_FLIP_Y_WEBGL, 1);
	//
	// for (int i = 0; i < 6; i++) {
	// GLES10.texImage2D(CUBE_MAP_SIDES[i], 0, GLES10.RGBA, GLES10.RGBA,
	// GLES10.UNSIGNED_BYTE,
	// cubeMapImages.get(cubeMapTexture)[i].getElement());
	// }
	//
	// GLES10.texParameteri(GLES11.GL_TEXTURE_CUBE_MAP, GLES10.TEXTURE_MAG_FILTER,
	// GLES10.LINEAR);
	// GLES10.texParameteri(GLES11.GL_TEXTURE_CUBE_MAP, GLES10.TEXTURE_MIN_FILTER,
	// GLES10.LINEAR);
	// GLES10.texParameteri(GLES11.GL_TEXTURE_CUBE_MAP, GLES10.TEXTURE_WRAP_S,
	// GLES10.CLAMP_TO_EDGE);
	// GLES10.texParameteri(GLES11.GL_TEXTURE_CUBE_MAP, GLES10.TEXTURE_WRAP_T,
	// GLES10.CLAMP_TO_EDGE);
	// // GLES10.generateMipmap(GLES10.TEXTURE_CUBE_MAP);
	// GLES10.bindTexture(GLES10.TEXTURE_CUBE_MAP, null);
	//
	// cubeMapTexture.status = GpuObjectStatus.UPLOADED;
	// Renderer.needsRedraw = true;
	// }
	// }

	public void unload(Object o) {
		if (o instanceof Geometry3d) {
			if (geometryBuffers.containsKey(o)) {
				((Geometry3d) o).status = 0;
				GeometryBuffers buffers = geometryBuffers.get(o);

				if (openGlVersion <= 1.0) {
					if (buffers.vertexBuffer != null) {
						buffers.vertexBuffer.clear();
					}
					if (buffers.normalsBuffer != null) {
						buffers.normalsBuffer.clear();
					}
					if (buffers.uvsBuffer != null) {
						buffers.uvsBuffer.clear();
					}
					if (buffers.facesBuffer != null) {
						buffers.facesBuffer.clear();
					}
				} else {
					if (buffers.vertexBufferId != null) {
						GLES11.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.vertexBufferId}));
					}
					if (buffers.normalsBufferId != null) {
						GLES11.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.normalsBufferId}));
					}
					if (buffers.uvsBufferId != null) {
						GLES11.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.uvsBufferId}));
					}
					if (buffers.facesBufferId != null) {
						GLES11.glDeleteBuffers(1, IntBuffer.wrap(new int[]{buffers.facesBufferId}));
					}
				}

				geometryBuffers.remove(o);
			}
		} else if (o instanceof Texture) {
			if (textures.containsKey(o)) {
				((Texture) o).status = 0;
				GLES10.glDeleteTextures(1, IntBuffer.wrap(new int[]{textures.get(o)}));
				textures.remove(o);
			}
		}
	}

	public void reset() {
		// Now force re-upload of all objects
		for (Geometry3d geometry : geometryBuffers.keySet()) {
			geometry.status = 0;
		}
		for (Texture texture : textures.keySet()) {
			texture.status = 0;
		}
		geometryBuffers.clear();
		textures.clear();
	}

	public void setGl(GL10 gl) {
		// Detect OpenGL ES version
		if (gl instanceof GL11) {
			openGlVersion = 1.1f;
		} else {
			openGlVersion = 1.0f;
		}
	}
}
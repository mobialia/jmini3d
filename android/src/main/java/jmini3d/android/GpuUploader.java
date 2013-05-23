package jmini3d.android;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import jmini3d.Geometry3d;
import jmini3d.GpuObjectStatus;
import jmini3d.Texture;
import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

public class GpuUploader {
	static final String TAG = "GpuUploader";

	// static final int[] CUBE_MAP_SIDES = {
	// GL10.GL_TEXTURE_CUBE_MAP_POSITIVE_X, GL10.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
	// GL10.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GL10.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
	// GL10.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GL10.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
	// };

	GL10 gl;
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
			GL11 gl11 = (GL11) gl;

			if ((geometry3d.status & GpuObjectStatus.VERTICES_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.VERTICES_UPLOADED;
				float[] vertex = geometry3d.vertex();
				if (vertex != null) {
					if (buffers.vertexBufferId == null) {
						int[] vboId = new int[1];
						((GL11) gl).glGenBuffers(1, vboId, 0);
						buffers.vertexBufferId = vboId[0];
					}
					gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffers.vertexBufferId);
					gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertex.length * 4, FloatBuffer.wrap(vertex), GL11.GL_STATIC_DRAW);
				}
			}

			if ((geometry3d.status & GpuObjectStatus.NORMALS_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.NORMALS_UPLOADED;
				float[] normals = geometry3d.normals();
				if (normals != null) {
					if (buffers.normalsBufferId == null) {
						int[] vboId = new int[1];
						((GL11) gl).glGenBuffers(1, vboId, 0);
						buffers.normalsBufferId = vboId[0];
					}
					gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffers.normalsBufferId);
					gl11.glBufferData(GL11.GL_ARRAY_BUFFER, normals.length * 4, FloatBuffer.wrap(normals), GL11.GL_STATIC_DRAW);
				}
			}

			if ((geometry3d.status & GpuObjectStatus.UVS_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.UVS_UPLOADED;
				float[] uvs = geometry3d.uvs();
				if (uvs != null) {
					if (buffers.uvsBufferId == null) {
						int[] vboId = new int[1];
						((GL11) gl).glGenBuffers(1, vboId, 0);
						buffers.uvsBufferId = vboId[0];
					}
					gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffers.uvsBufferId);
					gl11.glBufferData(GL11.GL_ARRAY_BUFFER, uvs.length * 4, FloatBuffer.wrap(uvs), GL11.GL_STATIC_DRAW);
				}
			}

			if ((geometry3d.status & GpuObjectStatus.FACES_UPLOADED) == 0) {
				geometry3d.status |= GpuObjectStatus.FACES_UPLOADED;
				short[] faces = geometry3d.faces();
				if (faces != null) {
					geometry3d.facesLength = faces.length;
					if (buffers.facesBufferId == null) {
						int[] vboId = new int[1];
						((GL11) gl).glGenBuffers(1, vboId, 0);
						buffers.facesBufferId = vboId[0];
					}

					gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
					gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, faces.length * 2, ShortBuffer.wrap(faces), GL11.GL_STATIC_DRAW);
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
				gl.glGenTextures(1, texturesIds, 0);
				textureId = texturesIds[0];
				textures.put(texture, textureId);
			}

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
			// gl.pixelStorei(GL10.UNPACK_FLIP_Y_WEBGL, 1);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			// gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA,
			// GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE,
			// imageResource);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
			// gl.generateMipmap(GL10.TEXTURE_2D);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

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
	// WebGLTexture texture = gl.createTexture();
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
	// gl.bindTexture(GL10.TEXTURE_CUBE_MAP,
	// cubeMapTextures.get(cubeMapTexture));
	// // gl.pixelStorei(GL10.UNPACK_FLIP_Y_WEBGL, 1);
	//
	// for (int i = 0; i < 6; i++) {
	// gl.texImage2D(CUBE_MAP_SIDES[i], 0, GL10.RGBA, GL10.RGBA,
	// GL10.UNSIGNED_BYTE,
	// cubeMapImages.get(cubeMapTexture)[i].getElement());
	// }
	//
	// gl.texParameteri(GL11.GL_TEXTURE_CUBE_MAP, GL10.TEXTURE_MAG_FILTER,
	// GL10.LINEAR);
	// gl.texParameteri(GL11.GL_TEXTURE_CUBE_MAP, GL10.TEXTURE_MIN_FILTER,
	// GL10.LINEAR);
	// gl.texParameteri(GL11.GL_TEXTURE_CUBE_MAP, GL10.TEXTURE_WRAP_S,
	// GL10.CLAMP_TO_EDGE);
	// gl.texParameteri(GL11.GL_TEXTURE_CUBE_MAP, GL10.TEXTURE_WRAP_T,
	// GL10.CLAMP_TO_EDGE);
	// // gl.generateMipmap(GL10.TEXTURE_CUBE_MAP);
	// gl.bindTexture(GL10.TEXTURE_CUBE_MAP, null);
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
					GL11 gl11 = (GL11) gl;

					if (buffers.vertexBufferId != null) {
						gl11.glDeleteBuffers(1, IntBuffer.wrap(new int[] { buffers.vertexBufferId }));
					}
					if (buffers.normalsBufferId != null) {
						gl11.glDeleteBuffers(1, IntBuffer.wrap(new int[] { buffers.normalsBufferId }));
					}
					if (buffers.uvsBufferId != null) {
						gl11.glDeleteBuffers(1, IntBuffer.wrap(new int[] { buffers.uvsBufferId }));
					}
					if (buffers.facesBufferId != null) {
						gl11.glDeleteBuffers(1, IntBuffer.wrap(new int[] { buffers.facesBufferId }));
					}
				}

				geometryBuffers.remove(o);
			}
		} else if (o instanceof Texture) {
			if (textures.containsKey(o)) {
				((Texture) o).status = 0;
				gl.glDeleteTextures(1, IntBuffer.wrap(new int[] { textures.get(o) }));
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
		this.gl = gl;

		// OpenGL ES version
		if (gl instanceof GL11) {
			openGlVersion = 1.1f;
		} else {
			openGlVersion = 1.0f;
		}
	}
}
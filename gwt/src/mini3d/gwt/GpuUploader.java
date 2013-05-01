package mini3d.gwt;

import java.util.HashMap;

import mini3d.CubeMapTexture;
import mini3d.Geometry3d;
import mini3d.GpuObjectStatus;
import mini3d.Texture;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.googlecode.gwtgl.array.Float32Array;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLTexture;

public class GpuUploader {
	static final int[] CUBE_MAP_SIDES = { WebGLRenderingContext.TEXTURE_CUBE_MAP_POSITIVE_X, WebGLRenderingContext.TEXTURE_CUBE_MAP_NEGATIVE_X,
			WebGLRenderingContext.TEXTURE_CUBE_MAP_POSITIVE_Y, WebGLRenderingContext.TEXTURE_CUBE_MAP_NEGATIVE_Y,
			WebGLRenderingContext.TEXTURE_CUBE_MAP_POSITIVE_Z, WebGLRenderingContext.TEXTURE_CUBE_MAP_NEGATIVE_Z };

	WebGLRenderingContext gl;
	ResourceLoader resourceLoader;

	HashMap<Geometry3d, GeometryBuffers> geometryBuffers = new HashMap<Geometry3d, GeometryBuffers>();
	HashMap<Texture, WebGLTexture> textures = new HashMap<Texture, WebGLTexture>();
	HashMap<Texture, ImageElement> textureImages = new HashMap<Texture, ImageElement>();
	HashMap<CubeMapTexture, WebGLTexture> cubeMapTextures = new HashMap<CubeMapTexture, WebGLTexture>();
	HashMap<CubeMapTexture, ImageElement[]> cubeMapImages = new HashMap<CubeMapTexture, ImageElement[]>();

	public GpuUploader(WebGLRenderingContext gl, ResourceLoader resourceLoader) {
		this.gl = gl;
		this.resourceLoader = resourceLoader;
	}

	public GeometryBuffers upload(Geometry3d geometry3d) {
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
					buffers.vertexBufferId = gl.createBuffer();
				}
				gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.vertexBufferId);
				gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, Float32Array.create(vertex), WebGLRenderingContext.STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.NORMALS_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.NORMALS_UPLOADED;
			float[] normals = geometry3d.normals();
			if (normals != null) {
				if (buffers.normalsBufferId == null) {
					buffers.normalsBufferId = gl.createBuffer();
				}
				gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.normalsBufferId);
				gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, Float32Array.create(normals), WebGLRenderingContext.STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.UVS_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.UVS_UPLOADED;
			float[] uvs = geometry3d.uvs();
			if (uvs != null) {
				if (buffers.uvsBufferId == null) {
					buffers.uvsBufferId = gl.createBuffer();
				}
				gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffers.uvsBufferId);
				gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, Float32Array.create(uvs), WebGLRenderingContext.STATIC_DRAW);
			}
		}

		if ((geometry3d.status & GpuObjectStatus.FACES_UPLOADED) == 0) {
			geometry3d.status |= GpuObjectStatus.FACES_UPLOADED;
			short[] faces = geometry3d.faces();
			if (faces != null) {
				geometry3d.facesLength = faces.length;
				if (buffers.facesBufferId == null) {
					buffers.facesBufferId = gl.createBuffer();
				}
				gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, buffers.facesBufferId);
				gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, MyInt16Array.create(faces), WebGLRenderingContext.STATIC_DRAW);
			}
		}
		return buffers;
	}

	public void upload(final Texture texture) {
		if ((texture.status & GpuObjectStatus.TEXTURE_UPLOADING) == 0) {
			texture.status |= GpuObjectStatus.TEXTURE_UPLOADING;

			WebGLTexture webGlTexture = gl.createTexture();
			textures.put(texture, webGlTexture);

			ImageElement textureImage = resourceLoader.getImage(texture.image);
			if (textureImage == null) {
				Window.alert("Texture image not found in resources: " + texture.image);
			} else {
				textureImages.put(texture, textureImage);

				Event.setEventListener(textureImage, new EventListener() {
					@Override
					public void onBrowserEvent(Event event) {
						textureLoaded(texture);
					}
				});
				Event.sinkEvents(textureImage, Event.ONLOAD);
			}
		}
	}

	public void upload(final CubeMapTexture cubeMapTexture) {
		if ((cubeMapTexture.status & GpuObjectStatus.TEXTURE_UPLOADING) == 0) {
			cubeMapTexture.status |= GpuObjectStatus.TEXTURE_UPLOADING;

			WebGLTexture texture = gl.createTexture();
			cubeMapTextures.put(cubeMapTexture, texture);

			ImageElement[] textureImages = new ImageElement[6];
			cubeMapImages.put(cubeMapTexture, textureImages);

			for (int i = 0; i < 6; i++) {
				textureImages[i] = resourceLoader.getImage(cubeMapTexture.images[i]);

				Event.setEventListener(textureImages[i], new EventListener() {
					@Override
					public void onBrowserEvent(Event event) {
						cubeTextureLoaded(cubeMapTexture);
					}
				});
				Event.sinkEvents(textureImages[i], Event.ONLOAD);
			}
		}
	}

	public void textureLoaded(Texture texture) {
		gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, textures.get(texture));
		// gl.pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
		gl.texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE,
				textureImages.get(texture));
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR_MIPMAP_LINEAR);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);
		gl.generateMipmap(WebGLRenderingContext.TEXTURE_2D);
		gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, null);

		texture.status |= GpuObjectStatus.TEXTURE_UPLOADED;
		Renderer.needsRedraw = true;
	}

	public void cubeTextureLoaded(CubeMapTexture cubeMapTexture) {
		cubeMapTexture.loadCount++;
		if (cubeMapTexture.loadCount == 6) {
			gl.bindTexture(WebGLRenderingContext.TEXTURE_CUBE_MAP, cubeMapTextures.get(cubeMapTexture));
			// gl.pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);

			for (int i = 0; i < 6; i++) {
				gl.texImage2D(CUBE_MAP_SIDES[i], 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE,
						cubeMapImages.get(cubeMapTexture)[i]);
			}

			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);
			// gl.generateMipmap(WebGLRenderingContext.TEXTURE_CUBE_MAP);
			gl.bindTexture(WebGLRenderingContext.TEXTURE_CUBE_MAP, null);

			cubeMapTexture.status |= GpuObjectStatus.TEXTURE_UPLOADED;
			Renderer.needsRedraw = true;
		}
	}

	public void unload(Object o) {
		if (o instanceof Geometry3d) {
			if (geometryBuffers.containsKey(o)) {
				((Geometry3d) o).status = 0;
				GeometryBuffers buffers = geometryBuffers.get(o);

				if (buffers.vertexBufferId != null) {
					gl.deleteBuffer(buffers.vertexBufferId);
				}
				if (buffers.normalsBufferId != null) {
					gl.deleteBuffer(buffers.normalsBufferId);
				}
				if (buffers.uvsBufferId != null) {
					gl.deleteBuffer(buffers.uvsBufferId);
				}
				if (buffers.facesBufferId != null) {
					gl.deleteBuffer(buffers.facesBufferId);
				}
				geometryBuffers.remove(o);
			}
		} else if (o instanceof Texture) {
			if (textures.containsKey(o)) {
				((Texture) o).status = 0;
				gl.deleteTexture(textures.get(o));
				textures.remove(o);
			}
		} else if (o instanceof CubeMapTexture) {
			if (cubeMapTextures.containsKey(o)) {
				((Texture) o).status = 0;
				gl.deleteTexture(cubeMapTextures.get(o));
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
}
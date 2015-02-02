package jmini3d.gwt;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.googlecode.gwtgl.array.Float32Array;
import com.googlecode.gwtgl.binding.WebGLBuffer;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLTexture;

import java.util.ArrayList;
import java.util.HashMap;

import jmini3d.CubeMapTexture;
import jmini3d.GpuObjectStatus;
import jmini3d.Object3d;
import jmini3d.Scene;
import jmini3d.Texture;
import jmini3d.geometry.Geometry;
import jmini3d.material.Material;

public class GpuUploader {
	// Use our Axis system
	static final int[] CUBE_MAP_SIDES = {WebGLRenderingContext.TEXTURE_CUBE_MAP_POSITIVE_X, WebGLRenderingContext.TEXTURE_CUBE_MAP_NEGATIVE_X,
			WebGLRenderingContext.TEXTURE_CUBE_MAP_POSITIVE_Z, WebGLRenderingContext.TEXTURE_CUBE_MAP_NEGATIVE_Z,
			WebGLRenderingContext.TEXTURE_CUBE_MAP_POSITIVE_Y, WebGLRenderingContext.TEXTURE_CUBE_MAP_NEGATIVE_Y};

	WebGLRenderingContext gl;
	ResourceLoader resourceLoader;

	HashMap<Geometry, GeometryBuffers> geometryBuffers = new HashMap<Geometry, GeometryBuffers>();
    HashMap<Object3d, WebGLBuffer> objectBuffers = new HashMap<Object3d, WebGLBuffer>();
    HashMap<Texture, WebGLTexture> textures = new HashMap<Texture, WebGLTexture>();
	HashMap<Texture, ImageElement> textureImages = new HashMap<Texture, ImageElement>();
	HashMap<CubeMapTexture, WebGLTexture> cubeMapTextures = new HashMap<CubeMapTexture, WebGLTexture>();
	HashMap<CubeMapTexture, ImageElement[]> cubeMapImages = new HashMap<CubeMapTexture, ImageElement[]>();
	ArrayList<Program> shaderPrograms = new ArrayList<Program>();

	TextureLoadedListener textureLoadedListener;

	public GpuUploader(WebGLRenderingContext gl, ResourceLoader resourceLoader, TextureLoadedListener textureLoadedListener) {
		this.gl = gl;
		this.resourceLoader = resourceLoader;
		this.textureLoadedListener = textureLoadedListener;
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
			program = new Program(gl);
			program.key = key;
			program.init(scene, material);
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

    public WebGLBuffer uploadVertexColors(Object3d object) {
        WebGLBuffer bufferId = objectBuffers.get(object);
        if (object.isVertexColorsDirty()) {
            object.setVertexColorsDirty(false);

            float[] colors = object.getVertexColors();

            if (colors != null) {
                if (bufferId == null) {
                    bufferId = gl.createBuffer();
                    objectBuffers.put(object, bufferId);
                }
                gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, bufferId);
                gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, Float32Array.create(colors), WebGLRenderingContext.STATIC_DRAW);
            }
        }

        return bufferId;
    }


    public void upload(final Renderer3d renderer3d, final Texture texture, final int activeTexture) {
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
						textureLoaded(renderer3d, texture, activeTexture);
					}
				});
				Event.sinkEvents(textureImage, Event.ONLOAD);
			}
		}
	}

	public void upload(final Renderer3d renderer3d, final CubeMapTexture cubeMapTexture, final int activeTexture) {
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
						cubeTextureLoaded(renderer3d, cubeMapTexture, activeTexture);
					}
				});
				Event.sinkEvents(textureImages[i], Event.ONLOAD);
			}
		}
	}

	public void textureLoaded(Renderer3d renderer3d, Texture texture, int activeTexture) {
		WebGLTexture mapTextureId = textures.get(texture);

		if (renderer3d.activeTexture != activeTexture) {
			gl.activeTexture(activeTexture);
			renderer3d.activeTexture = activeTexture;
		}
		gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, mapTextureId);
		renderer3d.mapTextureId = mapTextureId;

		gl.texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE,
				textureImages.get(texture));
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
		gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);

		texture.status |= GpuObjectStatus.TEXTURE_UPLOADED;

		if (textureLoadedListener != null) {
			textureLoadedListener.onTextureLoaded();
		}
	}

	public void cubeTextureLoaded(Renderer3d renderer3d, CubeMapTexture cubeMapTexture, int activeTexture) {
		cubeMapTexture.loadCount++;
		if (cubeMapTexture.loadCount == 6) {
			WebGLTexture envMapTextureId = cubeMapTextures.get(cubeMapTexture);

			if (renderer3d.activeTexture != activeTexture) {
				gl.activeTexture(activeTexture);
				renderer3d.activeTexture = activeTexture;
			}
			gl.bindTexture(WebGLRenderingContext.TEXTURE_CUBE_MAP, envMapTextureId);
			renderer3d.envMapTextureId = envMapTextureId;

			for (int i = 0; i < 6; i++) {
				gl.texImage2D(CUBE_MAP_SIDES[i], 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE,
						cubeMapImages.get(cubeMapTexture)[i]);
			}

			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
			gl.texParameteri(WebGLRenderingContext.TEXTURE_CUBE_MAP, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);

			cubeMapTexture.status |= GpuObjectStatus.TEXTURE_UPLOADED;

			if (textureLoadedListener != null) {
				textureLoadedListener.onTextureLoaded();
			}
		}
	}

	public void unload(Object o) {
		if (o instanceof Geometry) {
			if (geometryBuffers.containsKey(o)) {
				((Geometry) o).status = 0;
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
                cubeMapTextures.remove(o);
			}
        } else if (o instanceof Object3d) {
            WebGLBuffer bufferId = objectBuffers.remove(o);
            if (bufferId != null) {
                gl.deleteBuffer(bufferId);
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
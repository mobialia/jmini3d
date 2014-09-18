package jmini3d.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface EngineResources extends ClientBundle {
	public static EngineResources INSTANCE = GWT.create(EngineResources.class);

	@Source(value = {"fragment_shader.glsl"})
	TextResource fragmentShader();

	@Source(value = {"vertex_shader.glsl"})
	TextResource vertexShader();
}
package jmini3d.shader;

/**
 * To be used from the Shader plugins
 */
public interface UniformSetter {

	public abstract void setUniform(String uniformName, float value);
}

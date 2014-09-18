package jmini3d.geometry;

public abstract class Geometry {

	public int facesLength = 0;
	public int status = 0;

	public abstract float[] vertex();

	public abstract float[] normals();

	public abstract float[] uvs();

	public abstract short[] faces();
}
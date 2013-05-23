package jmini3d;

public class Color4 {

	public float r;
	public float g;
	public float b;
	public float a;

	public Color4(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public void setAll(long argb32) {
		a = ((argb32 >> 24) & 0x000000FF) / 255f;
		r = ((argb32 >> 16) & 0x000000FF) / 255f;
		g = ((argb32 >> 8) & 0x000000FF) / 255f;
		b = ((argb32) & 0x000000FF) / 255f;
	}

	public void setAll(int r, int g, int b, int a) {
		this.r = r / 255f;
		this.g = g / 255f;
		this.b = b / 255f;
		this.a = a / 255f;
	}
}
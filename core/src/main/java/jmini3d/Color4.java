package jmini3d;

public class Color4 {

	public float r;
	public float g;
	public float b;
	public float a;

	public static Color4 fromFloat(float r, float g, float b, float a) {
		Color4 color4 = new Color4();
		color4.r = r;
		color4.g = g;
		color4.b = b;
		color4.a = a;
		return color4;
	}

	public Color4() {
	}

	public Color4(int r, int g, int b) {
		this.r = r / 255f;
		this.g = g / 255f;
		this.b = b / 255f;
		this.a = 1;
	}

	public Color4(int r, int g, int b, int a) {
		this.r = r / 255f;
		this.g = g / 255f;
		this.b = b / 255f;
		this.a = a / 255f;
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

	public void setAll(float[] array, int offset) {
		r = array[offset];
		g = array[offset + 1];
		b = array[offset + 2];
		a = array[offset + 3];
	}

	public void setAllFrom(Color4 color) {
		r = color.r;
		g = color.g;
		b = color.b;
		a = color.a;
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Color4)) {
			return false;
		}
		return (r == ((Color4) o).r) && (g == ((Color4) o).g) && (b == ((Color4) o).b) && (a == ((Color4) o).a);
	}
}
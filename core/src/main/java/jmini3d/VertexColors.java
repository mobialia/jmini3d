package jmini3d;

public class VertexColors {
	public float[] vertexColors;
	public int status;

	public VertexColors(int colorCount) {
		vertexColors = new float[colorCount << 2];
	}

	public void setColor(int index, float r, float g, float b, float a) {
		vertexColors[(index << 2)] = r;
		vertexColors[(index << 2) + 1] = g;
		vertexColors[(index << 2) + 2] = b;
		vertexColors[(index << 2) + 3] = a;
	}

	public void setColor(int index, Color4 color4) {
		vertexColors[(index << 2)] = color4.r;
		vertexColors[(index << 2) + 1] = color4.g;
		vertexColors[(index << 2) + 2] = color4.b;
		vertexColors[(index << 2) + 3] = color4.a;
	}

	public float[] getVertexColors() {
		return vertexColors;
	}
}

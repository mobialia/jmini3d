package jmini3d.geometry;

import jmini3d.GpuObjectStatus;
import jmini3d.Vector3;

public class VariableGeometry extends Geometry {
	float[] vertex;
	float[] normals;
	float[] uvs;
	short[] faces;

	int vertexPointer = 0;
	int facePointer = 0;

	public VariableGeometry(int vertexCount, int faceCount) {
		vertex = new float[3 * vertexCount];
		normals = new float[3 * vertexCount];
		uvs = new float[2 * vertexCount];
		faces = new short[3 * faceCount];
	}

	public VariableGeometry(float[] vertex, float[] normals, float[] uvs, short[] faces) {
		this.vertex = vertex;
		this.normals = normals;
		this.uvs = uvs;
		this.faces = faces;
	}

	public short addVertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
		vertex[vertexPointer * 3] = x;
		vertex[vertexPointer * 3 + 1] = y;
		vertex[vertexPointer * 3 + 2] = z;

		normals[vertexPointer * 3] = nx;
		normals[vertexPointer * 3 + 1] = ny;
		normals[vertexPointer * 3 + 2] = nz;

		uvs[vertexPointer * 2] = u;
		uvs[vertexPointer * 2 + 1] = v;

		vertexPointer++;
		return (short) (vertexPointer - 1);
	}

	public void addFace(short a, short b, short c) {
		faces[facePointer * 3] = a;
		faces[facePointer * 3 + 1] = b;
		faces[facePointer * 3 + 2] = c;

		facePointer++;
	}

	public void addQuad(Vector3 leftBack, Vector3 rightBack, Vector3 leftFront, Vector3 rightFront) {
		addQuadUv(leftBack, rightBack, leftFront, rightFront, 0, 0, 1, 1);
	}

	public void addQuadUv(Vector3 leftBack, Vector3 rightBack, Vector3 leftFront, Vector3 rightFront, float x1, float y1, float x2, float y2) {
		Vector3 normal = calcNormal(rightBack, leftFront, leftBack);

		short _leftBack = addVertex(leftBack.x, leftBack.y, leftBack.z, x1, y1, normal.x, normal.y, normal.z);
		short _rightBack = addVertex(rightBack.x, rightBack.y, rightBack.z, x2, y1, normal.x, normal.y, normal.z);
		short _leftFront = addVertex(leftFront.x, leftFront.y, leftFront.z, x1, y2, normal.x, normal.y, normal.z);
		short _rightFront = addVertex(rightFront.x, rightFront.y, rightFront.z, x2, y2, normal.x, normal.y, normal.z);

		addFace(_leftBack, _leftFront, _rightBack);
		addFace(_rightBack, _leftFront, _rightFront);
	}

	public void addBox(Vector3 upperLeftFront, Vector3 upperRightFront, Vector3 lowerLeftFront, Vector3 lowerRightFront, Vector3 upperLeftBack,
			Vector3 upperRightBack, Vector3 lowerLeftBack, Vector3 lowerRightBack) {
		// Front
		addQuad(upperLeftFront, upperRightFront, lowerLeftFront, lowerRightFront);
		// Back
		addQuad(upperRightBack, upperLeftBack, lowerRightBack, lowerLeftBack);
		// Upper
		addQuad(upperLeftBack, upperRightBack, upperLeftFront, upperRightFront);
		// Lower
		addQuad(lowerLeftFront, lowerRightFront, lowerLeftBack, lowerRightBack);
		// Left
		addQuad(upperLeftBack, upperLeftFront, lowerLeftBack, lowerLeftFront);
		// Right
		addQuad(upperRightFront, upperRightBack, lowerRightFront, lowerRightBack);
	}

	private Vector3 calcNormal(Vector3 p0, Vector3 p1, Vector3 p2) {
		Vector3 temp = Vector3.cross(Vector3.subtract(p1, p0), Vector3.subtract(p2, p0));
		temp.normalize();
		return temp;
	}

	public void setUv(int index, float u, float v) {
		uvs[index << 1] = u;
		uvs[(index << 1) + 1] = v;

		status &= ~GpuObjectStatus.UVS_UPLOADED;
	}

	public float[] vertex() {
		return vertex;
	}

	public float[] normals() {
		return normals;
	}

	public float[] uvs() {
		return uvs;
	}

	public short[] faces() {
		return faces;
	}
}
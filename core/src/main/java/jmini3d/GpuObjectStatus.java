package jmini3d;

public class GpuObjectStatus {
	public static final int TEXTURE_UPLOADING = 0x1;
	public static final int TEXTURE_UPLOADED = 0x2;
	public static final int VERTICES_UPLOADED = 0x4;
	public static final int NORMALS_UPLOADED = 0x8;
	public static final int UVS_UPLOADED = 0x10;
	public static final int FACES_UPLOADED = 0x20;
	public static final int VERTEX_COLORS_UPLOADED = 0x100;
}

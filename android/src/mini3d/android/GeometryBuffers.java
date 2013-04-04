package mini3d.android;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GeometryBuffers {
	Integer vertexBufferId;
	Integer normalsBufferId;
	Integer uvsBufferId;
	Integer facesBufferId;

	FloatBuffer vertexBuffer;
	FloatBuffer normalsBuffer;
	FloatBuffer uvsBuffer;
	ShortBuffer facesBuffer;
}

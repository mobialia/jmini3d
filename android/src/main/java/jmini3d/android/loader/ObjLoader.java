package jmini3d.android.loader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import jmini3d.geometry.VariableGeometry;

/**
 * Loads dynamically a Wavefront OBJ file creating a new Geometry
 *
 * This support bigger files than the conversion-to-a-class method
 */
public class ObjLoader {
	ArrayList<Float> vertexList = new ArrayList<>();
	ArrayList<Float> normalsList = new ArrayList<>();
	ArrayList<Float> uvsList = new ArrayList<>();
	HashMap<String, Integer> vertexMap = new HashMap<>();
	Integer vertexMapIndex = 0;

	ArrayList<Float> vertex = new ArrayList<>();
	ArrayList<Float> normals = new ArrayList<>();
	ArrayList<Float> uvs = new ArrayList<>();
	ArrayList<Short> faces = new ArrayList<>();

	public VariableGeometry load(InputStream is) {
		VariableGeometry geometry = null;

		System.out.println("Loading model...");

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line;

			int lineCount = 0;

			// First load all the file into the xxxList arrays
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\\s|/");

				lineCount++;
				if ((lineCount % 1000) == 0) {
					System.out.println("OBJ lines processed " + lineCount);
				}

				if ("v".equals(tokens[0])) {
					vertexList.add(Float.valueOf(tokens[1]));
					vertexList.add(Float.valueOf(tokens[2]));
					vertexList.add(Float.valueOf(tokens[3]));
				} else if ("vn".equals(tokens[0])) {
					normalsList.add(Float.valueOf(tokens[1]));
					normalsList.add(Float.valueOf(tokens[2]));
					normalsList.add(Float.valueOf(tokens[3]));
				} else if ("vt".equals(tokens[0])) {
					uvsList.add(Float.valueOf(tokens[1]));
					uvsList.add(1f - Float.parseFloat(tokens[2]));
				} else if ("f".equals(tokens[0])) {
					faces.add((short) addVertexNormalUv(tokens[1], tokens[2], tokens[3]));
					faces.add((short) addVertexNormalUv(tokens[4], tokens[5], tokens[6]));
					faces.add((short) addVertexNormalUv(tokens[7], tokens[8], tokens[9]));
				}
			}

			br.close();
			System.out.println("Vertex size=" + vertexList.size());
			System.out.println("Normals size=" + normalsList.size());
			System.out.println("Uvs size=" + uvsList.size());
			System.out.println("FaceList (vertex+normals+uvs) size=" + vertexMap.size());

			System.out.println("Converting to geometry");

			geometry = new VariableGeometry(convertFloatArray(vertex),
					convertFloatArray(normals),
					convertFloatArray(uvs),
					convertShortArray(faces));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return geometry;
	}

	public float[] convertFloatArray(ArrayList<Float> in) {
		float[] out = new float[in.size()];
		for (int i = 0; i < in.size(); i++) {
			out[i] = in.get(i);
		}
		return out;
	}

	public short[] convertShortArray(ArrayList<Short> in) {
		short[] out = new short[in.size()];
		for (int i = 0; i < in.size(); i++) {
			out[i] = in.get(i);
		}
		return out;
	}

	public int addVertexNormalUv(String vertexIndex, String uvIndex, String normalIndex) {
		String key = vertexIndex + "/" + normalIndex + "/" + uvIndex;

		int vertexInt = (Integer.parseInt(vertexIndex) - 1) * 3;
		int uvInt = "".equals(uvIndex) ? -1 : (Integer.parseInt(uvIndex) - 1) * 2;
		int normalInt = (Integer.parseInt(normalIndex) - 1) * 3;

		Integer index = vertexMap.get(key);
		if (index == null) {
			vertex.add(vertexList.get(vertexInt));
			vertex.add(vertexList.get(vertexInt + 1));
			vertex.add(vertexList.get(vertexInt + 2));

			normals.add(normalsList.get(normalInt));
			normals.add(normalsList.get(normalInt + 1));
			normals.add(normalsList.get(normalInt + 2));

			if (uvInt != -1) {
				uvs.add(uvsList.get(uvInt));
				uvs.add(uvsList.get(uvInt + 1));
			}

			index = vertexMapIndex++;
			vertexMap.put(key, index);
		}

		return index;
	}
}
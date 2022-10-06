package jmini3d.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Obj2Class {
	ArrayList<String> vertexList = new ArrayList<>();
	ArrayList<String> normalsList = new ArrayList<>();
	ArrayList<String> uvsList = new ArrayList<>();
	HashMap<String, Integer> vertexMap = new HashMap<>();
	Integer vertexMapIndex = 0;

	StringBuilder vertexSB = new StringBuilder();
	StringBuilder normalsSB = new StringBuilder();
	StringBuilder uvsSB = new StringBuilder();
	StringBuilder facesSB = new StringBuilder();

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: Obj2Class file.obj file.java com.package.name");
			System.out.println("Converts an OBJ file to a jmini3d geometry in a Java file");
			return;
		}
		Obj2Class obj2Class = new Obj2Class();

		obj2Class.process(args[0], args[1], args[2]);
	}

	public void process(String inFile, String outFile, String packageName) {
		File file = new File(inFile);
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String line;

			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\\s|/");

				if ("v".equals(tokens[0])) {
					vertexList.add(tokens[1] + "f, " + tokens[2] + "f, " + tokens[3] + "f");
				} else if ("vn".equals(tokens[0])) {
					normalsList.add(tokens[1] + "f, " + tokens[2] + "f, " + tokens[3] + "f");
				} else if ("vt".equals(tokens[0])) {
					uvsList.add(tokens[1] + "f, " + (1f - Float.parseFloat(tokens[2])) + "f");
				} else if ("f".equals(tokens[0])) {
					if (facesSB.length() > 0) {
						facesSB.append(", ");
					}

					facesSB.append(addVertexNormalUv(tokens[1], tokens[2], tokens[3]));
					facesSB.append(", ");
					facesSB.append(addVertexNormalUv(tokens[4], tokens[5], tokens[6]));
					facesSB.append(", ");
					facesSB.append(addVertexNormalUv(tokens[7], tokens[8], tokens[9]));
				}
			}

			br.close();
			System.out.println("Vertex size=" + vertexList.size());
			System.out.println("Normals size=" + normalsList.size());
			System.out.println("Uvs size=" + uvsList.size());
			System.out.println("FaceList (vertex+normals+uvs) size=" + vertexMap.size());

			writeFile(packageName, outFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int addVertexNormalUv(String vertexIndex, String uvIndex, String normalIndex) {
		String key = vertexIndex + "/" + normalIndex + "/" + uvIndex;

		int vertexInt = Integer.parseInt(vertexIndex);
		int uvInt = "".equals(uvIndex) ? -1 : Integer.parseInt(uvIndex);
		int normalInt = Integer.parseInt(normalIndex);

		Integer index = vertexMap.get(key);
		if (index == null) {
			if (vertexSB.length() > 0) {
				vertexSB.append(", ");
				normalsSB.append(", ");
				if (uvInt != -1) {
					uvsSB.append(", ");
				}
			}
			vertexSB.append(vertexList.get(vertexInt - 1));
			normalsSB.append(normalsList.get(normalInt - 1));
			if (uvInt != -1) {
				uvsSB.append(uvsList.get(uvInt - 1));
			}

			index = vertexMapIndex++;
			vertexMap.put(key, index);
		}

		return index;
	}


	public void writeFile(String packageName, String fileName) {
		StringBuilder sb = new StringBuilder();

		String className = fileName.substring(fileName.lastIndexOf("/") + 1).replace(".java", "");

		System.out.println(className);

		sb.append("package ").append(packageName).append(";\n");
		sb.append("import jmini3d.geometry.Geometry;");

		sb.append("\n");
		sb.append("public class ").append(className).append(" extends Geometry {\n");
		sb.append("\n");

		sb.append("public float[] vertex() {\n");
		sb.append("    final float vertex[] = {\n");
		sb.append(vertexSB);
		sb.append("    };\n");
		sb.append("    return vertex;\n");
		sb.append("}\n");

		sb.append("public float[] normals() {\n");
		sb.append("    final float normals[] = {\n");
		sb.append(normalsSB);
		sb.append("    };\n");
		sb.append("    return normals;\n");
		sb.append("}\n");

		if (uvsList.size() > 0) {
			sb.append("public float[] uvs() {\n");
			sb.append("    final float uvs[] = {\n");
			sb.append(uvsSB);
			sb.append("    };\n");
			sb.append("    return uvs;\n");
			sb.append("}\n");
		} else {
			sb.append("public float[] uvs() {\n");
			sb.append("    return null;\n");
			sb.append("}\n");
		}

		sb.append("public short[] faces() {\n");
		sb.append("    final short faces[] = {\n");
		sb.append(facesSB);
		sb.append("    };\n");
		sb.append("    return faces;\n");
		sb.append("}\n");
		sb.append("}\n");

		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			FileWriter fos = new FileWriter(file);
			fos.append(sb.toString());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
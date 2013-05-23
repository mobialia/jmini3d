package jmini3d.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Obj2Class {
	
	ArrayList<String> vertexList = new ArrayList<String>();
	ArrayList<String> normalsList = new ArrayList<String>();
	ArrayList<String> uvsList = new ArrayList<String>();
	ArrayList<String> facesList = new ArrayList<String>();
	
	StringBuffer vertexSB = new StringBuffer();
	StringBuffer normalsSB = new StringBuffer();
	StringBuffer uvsSB = new StringBuffer();
	StringBuffer facesSB = new StringBuffer();
	

	public static void main(String args[]) {
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
			
				String tokens[] = line.split("\\s|/");
				
//				System.out.println("line = " + line);
//				System.out.println("token = " + tokens[0]);
				
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
					
					facesSB.append(addVertexNormalUv(Integer.valueOf(tokens[1]), Integer.valueOf(tokens[2]), Integer.valueOf(tokens[3])));
					facesSB.append(", ");
					facesSB.append(addVertexNormalUv(Integer.valueOf(tokens[4]), Integer.valueOf(tokens[5]), Integer.valueOf(tokens[6])));
					facesSB.append(", ");
					facesSB.append(addVertexNormalUv(Integer.valueOf(tokens[7]), Integer.valueOf(tokens[8]), Integer.valueOf(tokens[9])));
					
				}
			}
			
			br.close();
			System.out.println("Vertex size=" + vertexList.size());
			System.out.println("Normals size=" + normalsList.size());
			System.out.println("Uvs size=" + uvsList.size());
			System.out.println("FaceList (vertex+normals+uvs) size=" + facesList.size());
			System.out.println(vertexSB.toString());
			System.out.println(normalsSB.toString());
			System.out.println(uvsSB.toString());
			System.out.println(facesSB.toString());
			
			writeFile(packageName, outFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int addVertexNormalUv(int vertexIndex, int uvIndex, int normalIndex) {
		String key = vertexIndex + "/" + normalIndex + "/" + uvIndex;
		//String key = vertexList.get(vertexIndex-1) + "/" + normalsList.get(normalIndex-1) + "/" + uvsList.get(uvIndex-1);
		
		if (!facesList.contains(key)) {
			if (vertexSB.length() > 0) {
				vertexSB.append(", ");
				normalsSB.append(", ");
				uvsSB.append(", ");
			}		
			vertexSB.append(vertexList.get(vertexIndex - 1));
			normalsSB.append(normalsList.get(normalIndex - 1));
			uvsSB.append(uvsList.get(uvIndex - 1));
			
			facesList.add(key);
		}
		
		return facesList.indexOf(key);
	}
	
	
	public void writeFile(String packageName, String fileName) {
		StringBuffer sb = new StringBuffer();

		String className = fileName.substring(fileName.lastIndexOf("/") + 1).replace(".java", "");
		
		System.out.println(className);
		
		sb.append("package " + packageName + ";\n");
		sb.append("import com.mobialia.jmini3d.Geometry3d;");

		sb.append("\n");
		sb.append("public class " + className + " extends Geometry3d {\n");
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

		sb.append("public float[] uvs() {\n");
		sb.append("    final float uvs[] = {\n");
		sb.append(uvsSB);
		sb.append("    };\n");
		sb.append("    return uvs;\n");
		sb.append("}\n");

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

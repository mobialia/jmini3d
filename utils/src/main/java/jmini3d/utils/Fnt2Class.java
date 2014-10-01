package jmini3d.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Fnt2Class {

	StringBuffer constructorSB = new StringBuffer();

	public static void main(String args[]) {
		if (args.length < 3) {
			System.out.println("Usage: Fnt2Class file.fnt file.java com.package.name");
			System.out.println("Converts an FNT file to a jmini3d font in a Java file");
			return;
		}
		Fnt2Class fnt2Class = new Fnt2Class();

		fnt2Class.process(args[0], args[1], args[2]);
	}

	public void process(String inFile, String outFile, String packageName) {

		File file = new File(inFile);
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String line;

			while ((line = br.readLine()) != null) {
				addFntLine(line);
			}

			br.close();

			writeFile(packageName, outFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Integer getInt(String splits[], int index) {
		if (index > splits.length) {
			return 0;
		}
		return Integer.valueOf(splits[index]);
	}

	public void addFntLine(String line) {
		String splits[] = line.split("[ =\r\n]+");
		if (line.startsWith("common ")) {
			// common lineHeight=32 base=26 scaleW=256 scaleH=256 pages=1 packed=0 alphaChnl=1 redChnl=0 greenChnl=0 blueChnl=0
			constructorSB.append("\t\tsetCommon(" + getInt(splits, 2) + ", " + getInt(splits, 4) + ", " + getInt(splits, 6)  //
					+ ", " + getInt(splits, 8) + ", " + getInt(splits, 10) + ", " + getInt(splits, 12)  //
					+ ", " + getInt(splits, 14) + ", " + getInt(splits, 16) + ", " + getInt(splits, 18)  //
					+ ", " + getInt(splits, 20) + ");\n");

		} else if (line.startsWith("page ")) {
			//page id=0 file="arial_0.png"
			constructorSB.append("\t\taddPage(" + getInt(splits, 2) + ", " + splits[4] + ");\n");

		} else if (line.startsWith("char ")) {
			// char id=32   x=254   y=0     width=0     height=1     xoffset=0     yoffset=31    xadvance=8     page=0  chnl=15
			constructorSB.append("\t\taddChar(" + getInt(splits, 2) + ", " + getInt(splits, 4) + ", " + getInt(splits, 6)  //
					+ ", " + getInt(splits, 8) + ", " + getInt(splits, 10) + ", " + getInt(splits, 12)  //
					+ ", " + getInt(splits, 14) + ", " + getInt(splits, 16) + ", " + getInt(splits, 18)  //
					+ ", " + getInt(splits, 20) + ");\n");

		} else if (line.startsWith("kerning ")) {
			// kerning first=32  second=65  amount=-2
			constructorSB.append("\t\taddKerning(" + getInt(splits, 2) + ", " + getInt(splits, 4) + ", " + getInt(splits, 6) + ");\n");
		}
	}

	public void writeFile(String packageName, String fileName) {
		StringBuilder sb = new StringBuilder();

		String className = fileName.substring(fileName.lastIndexOf("/") + 1).replace(".java", "");

		System.out.println(className);

		sb.append("package ").append(packageName).append(";\n");
		sb.append("import jmini3d.Font;");

		sb.append("\n");
		sb.append("public class ").append(className).append(" extends Font {\n");
		sb.append("\n");

		sb.append("\tpublic ").append(className).append("() {\n");
		sb.append(constructorSB);
		sb.append("\t}\n");
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

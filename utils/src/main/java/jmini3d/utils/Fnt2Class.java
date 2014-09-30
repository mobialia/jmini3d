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

	public void addFntLine(String line) {
		String splits[] = line.split("[ =\r\n]+");
		if (line.startsWith("common ")) {
			// common lineHeight=32 base=26 scaleW=256 scaleH=256 pages=1 packed=0 alphaChnl=1 redChnl=0 greenChnl=0 blueChnl=0
			constructorSB.append("\t\tsetCommon(" + Integer.valueOf(splits[2]) + ", " + Integer.valueOf(splits[4]) + ", " + Integer.valueOf(splits[6]) //
					+ ", " + Integer.valueOf(splits[8]) + ", " + Integer.valueOf(splits[10]) + ", " + Integer.valueOf(splits[12]) //
					+ ", " + Integer.valueOf(splits[14]) + ", " + Integer.valueOf(splits[16]) + ", " + Integer.valueOf(splits[18]) //
					+ ", " + Integer.valueOf(splits[20]) + ");\n");

		} else if (line.startsWith("page ")) {
			//page id=0 file="arial_0.png"
			constructorSB.append("\t\taddPage(" + Integer.valueOf(splits[2]) + ", " + splits[4] + ");\n");

		} else if (line.startsWith("char ")) {
			// char id=32   x=254   y=0     width=0     height=1     xoffset=0     yoffset=31    xadvance=8     page=0  chnl=15
			constructorSB.append("\t\taddChar(" + Integer.valueOf(splits[2]) + ", " + Integer.valueOf(splits[4]) + ", " + Integer.valueOf(splits[6]) //
					+ ", " + Integer.valueOf(splits[8]) + ", " + Integer.valueOf(splits[10]) + ", " + Integer.valueOf(splits[12]) //
					+ ", " + Integer.valueOf(splits[14]) + ", " + Integer.valueOf(splits[16]) + ", " + Integer.valueOf(splits[18]) //
					+ ", " + Integer.valueOf(splits[20]) + ");\n");

		} else if (line.startsWith("kerning ")) {
			// kerning first=32  second=65  amount=-2
			constructorSB.append("\t\taddKerning(" + Integer.valueOf(splits[2]) + ", " + Integer.valueOf(splits[4]) + ", " + Integer.valueOf(splits[6]) + ");\n");
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

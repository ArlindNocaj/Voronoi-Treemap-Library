/*******************************************************************************
 * Copyright (c) 2013 Arlind Nocaj, University of Konstanz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * For distributors of proprietary software, other licensing is possible on request: arlind.nocaj@gmail.com
 * 
 * This work is based on the publication below, please cite on usage, e.g.,  when publishing an article.
 * Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864
 ******************************************************************************/
package kn.uni.voronoitreemap.interfaces;

import java.io.File;

import kn.uni.voronoitreemap.IO.PDFStatusObject;
import kn.uni.voronoitreemap.IO.PNGStatusObject;
import kn.uni.voronoitreemap.IO.WriteStatusObject;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.treemap.FileSystemReader;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;

public class MainClass {

	public static String getFileName(String name, String extension) {
		return getFileName(name, extension, "");
	}

	/**
	 * Returns a file name
	 * 
	 * @param name
	 * @param extension
	 * @return
	 */
	public static String getFileName(String name, String extension,
			String suffix) {
		if (name.contains("."))
			name = name.substring(0, name.lastIndexOf("."));// remove extension
															// if there is one

		name += suffix;
		String fileName = name;

		File file = new File(fileName + "." + extension);
		int i = 1;
		while (file.exists()) {
			String num = String.format("%03d", i);
			fileName = name + num;
			file = new File(fileName + "." + extension);
			i++;
		}
		return fileName;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out
					.println("JVoroTreemap - Java Voronoi Treemap Library, (c) 2015 Arlind Nocaj, University of Konstanz\n  usage: VoronoiTreemap  [OPTIONS] FILE \n  "
							+ "Computes a Voronoi Treemap for the given input file and stores the result in a link list format ing visu as a PNG and as link"
							+ "options \n   "							
							+ "-d uses FILE as a directory and extracts the hierarchical structure from this directory as basis for the treemap \n"
							+ "-pdf generates a pdf version of the Voronoi treemap (very slow) \n"
							
							+ "Examples:"
							+ "java -jar JVoroTreemap.jar data/Octagon.txt \n"
							+ "java -jar JVoroTreemap.jar -d ~/Desktop");
			return;
		}

		String filePath = "";
		boolean writePDF = false;
		boolean pathIsDirectory = false;
		boolean uniformWeight=true;
		
		for (int i = 0; i < args.length; i++) {
			if (!args[i].startsWith("-")) {
				filePath = args[i];
				continue;
			}
			String option = args[i].substring(1);

			if (option.equals("d")) {
				pathIsDirectory = true;
			}
			if (option.equals("pdf")) {
				writePDF = true;
			}
			if(option.equals("w")){
				uniformWeight=false;
			}
			
		}

		if (pathIsDirectory) {
			// FileSystemReader reader = new
			// FileSystemReader("/home/nocaj/git/linux");
			
			FileSystemReader reader = new FileSystemReader(filePath);
			reader.createTreeFile();
			filePath = reader.getExportFile();
			System.out.println("Exported recursive folder structure to: " + filePath);
		}

		String filename = filePath;
		String name = new File(filename).getName();

		PolygonSimple rootPolygon = new PolygonSimple();
		int width = (int) (650 * 1.95);
		int height = (int) (400 * 1.95);

		// width=600*2;
		// height=400*2;
		//
		int numPoints = 20;
		for (int j = 0; j < numPoints; j++) {
			double angle = 2.0 * Math.PI * (j * 1.0 / numPoints);
			double rotate = 2.0 * Math.PI / numPoints / 2;
			double y = Math.sin(angle + rotate) * height + height;
			double x = Math.cos(angle + rotate) * width + width;
			rootPolygon.add(x, y);
		}

		// rootPolygon.add(0, 0);
		// rootPolygon.add(width, 0);
		// rootPolygon.add(width, height);
		// rootPolygon.add(0, height);

		// VoronoiCore.setDebugMode();
		VoronoiTreemap treemap = new VoronoiTreemap();
		treemap.setRootPolygon(rootPolygon);
		treemap.readEdgeList(filename);
		treemap.setCancelOnMaxIteration(true);
		treemap.setNumberMaxIterations(1500);
		treemap.setCancelOnThreshold(true);
		treemap.setErrorAreaThreshold(0.08);
		treemap.setUniformWeights(uniformWeight);
		treemap.setNumberThreads(8);

		treemap.setStatusObject(new WriteStatusObject(getFileName(name, "txt",
				"-finished"), treemap));
		treemap.setStatusObject(new PNGStatusObject(getFileName(name, "png"),
				treemap));
		if(writePDF){
		 treemap.setStatusObject(new
		 PDFStatusObject(getFileName(name, "pdf"), treemap));
		}
		treemap.computeLocked();
	}

}

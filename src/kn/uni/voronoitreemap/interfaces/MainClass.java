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
import java.util.ArrayList;

import kn.uni.voronoitreemap.IO.IO;
import kn.uni.voronoitreemap.IO.PDFStatusObject;
import kn.uni.voronoitreemap.IO.PNGStatusObject;
import kn.uni.voronoitreemap.IO.WriteStatusObject;
import kn.uni.voronoitreemap.core.VoronoiCore;
import kn.uni.voronoitreemap.interfaces.data.TreeData;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;



public class MainClass {		
	
	/**
	 * Returns a file name
	 * @param name
	 * @param extension
	 * @return
	 */
	public static String getFileName(String name, String extension){
		
		String fileName=name;
		File file=new File(fileName+"."+extension);
		int i=1;
		while(file.exists()){
			String num=String.format("%03d", i);
			fileName=name+num;
			file=new File(fileName+"."+extension);
			i++;
		}
		return fileName;		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
		      System.out.println("VoroTreemap v0.2 - Voronoi Treemap library for Java   (c) 2015 University of Konstanz\n  usage: VoroTreemap  [options] infile        see manual for more details\n  options \n   -iF Filename \n");
		      return;
		    }
		    try {
		    	
		    	for (int i = 0; i < args.length; i++) {
		    		if (!args[i].startsWith("-")) {
//		    	        throw new Exception("parsing error, options must start with a minus: \"" + args[i] + "\"");
		    			continue;
		    	      }
		    		 String option = args[i].substring(1);
		    		 
		    		 if (option.startsWith("i")){
		    			for (int k = i+1; k < args.length; k++) {
		    			if(args[k].startsWith("-")) continue;
		    			String filename=args[k];
		    			String name=new File(filename).getName();				    			 	 		    			 
		    					    			 
		    			PolygonSimple rootPolygon = new PolygonSimple();
		    				int width = (int) (400*1.9);
		    				int height = (int)(600*1.9);
		    				
		    				int numPoints=12;
//		    			for (int j = 0; j < numPoints; j++) {
//								double angle=2.0*Math.PI*(j*1.0/numPoints);
//								double rotate=2.0*Math.PI/numPoints/2;
//		    					double y=Math.sin(angle+rotate)*height+height;
//		    					double x=Math.cos(angle+rotate)*width+width;
//		    					rootPolygon.add(x,y);
//							}
		
	
		    				
		    				rootPolygon.add(0, 0);
		    				rootPolygon.add(width, 0);
		    				rootPolygon.add(width, height);
		    				rootPolygon.add(0, height);

		    				
		    				
//			    			VoronoiCore.setDebugMode(); 
		    				VoronoiTreemap treemap = new VoronoiTreemap();
		    			   	treemap.setRootPolygon(rootPolygon);
		    			   	treemap.readEdgeList(filename);			    			    					    			
			    			treemap.setCancelOnMaxIteration(true);
			    			treemap.setNumberMaxIterations(2000);
			    			treemap.setCancelOnThreshold(true);
			    			treemap.setErrorAreaThreshold(0.05);
			    			treemap.setUniformWeights(true);
			    			
			    			treemap.setNumberThreads(8);			    						    			
			    			
//			    			treemap.setStatusObject(new WriteStatusObject(getFileName(name, "txt")));
			    			treemap.setStatusObject(new PNGStatusObject(getFileName(name, "png"), treemap));
//			    			treemap.setStatusObject(new PDFStatusObject(getFileName(name, "pdf"), treemap));
			    			treemap.computeLocked();		    			
		    		 }
		    		 }
		    		 
		    	}

		    }catch(Exception e){
		    	e.printStackTrace();
		    }
	}
	
	

}

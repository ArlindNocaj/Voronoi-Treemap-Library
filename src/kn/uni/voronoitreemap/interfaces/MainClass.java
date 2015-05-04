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
import kn.uni.voronoitreemap.IO.PNGStatusObject;
import kn.uni.voronoitreemap.IO.WriteStatusObject;
import kn.uni.voronoitreemap.core.VoronoiCore;
import kn.uni.voronoitreemap.interfaces.data.TreeData;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;


/**
 * Started implementing for command line use. But not finished yet...<br>
 * Example to use the VoronoiTreemapLibrary: <br>
 * > Create an implementation of the StatusObject-interface.<br>
 * > Create the root polygon:<br>
 * PolygonSimple root = new PolygonSimple(4);<br>
 * root.add(0,0); root.add(800,0); root.add(800,800); root.add(0,800);<br>
 * > Create a StatusObject of your implementation<br>
 * StatusObjectImpl statusObject = new StatusObjectImpl();<br>
 * > Get VoronoiTreemapInterface instance:<br>
 * VoronoiTreemapInterface voronoiTreemap = MainClass.getInstance(statusObject,true);<br>
 * > Insert the tree structure <br>
 * List<List<Integer>> treeStructure = ...; <br>
 * voronoiTreemap.setTreeAndWeights(root, treeStructure, null, null);<br>
 * > Compute the voronoi treemap<br>
 * voronoiTreemap.compute();<br>
 * > Handle the results in statusObject.finished() or respectively in statusObject.finishedNode(int node, int layer, int[] children, PolygonSimple[] polygons) <br>
 * @author nocaj
 *
 *
 */
public class MainClass {

	
	public static VoronoiTreemapInterface getInstance(StatusObject object){
		return new VoronoiTreemap(object);
	}
	
	public static VoronoiTreemapInterface getInstance(){
		return new VoronoiTreemap();
	}
	
	/**
	 * 
	 * Returning an instance of the implementation of the VoronoiTreemapInterface
	 * @param object 
	 * 			On the StatusObject is finished() and finishedNode(...) called after the computation
	 * @param multiThreaded
	 * 			Whether multithreading is used or not.
	 * @return
	 * 		returns an instance of an implementation of the VoronoiTreemapInterface
	 */
	
	public static VoronoiTreemapInterface getInstance(StatusObject object,boolean multiThreaded){
		return new VoronoiTreemap(object, multiThreaded);
	}
	
	public static String getFileName(String name, String extension){
		
		String fileName=name+"."+extension;
		File file=new File(fileName);
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
		    	VoronoiTreemapInterface treemap=null;
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
		    			 String name="VT"+new File(filename).getName();		
		    			 
		    			 TreeData treeData = IO.readEdgeList(filename);
		    			 
//		    			 ArrayList<ArrayList<Integer>> tree = IO.readTree(filename);		    			 
		    			     			 		    			 
		    					    			 
		    			 PolygonSimple rootPolygon = new PolygonSimple();
		    				int width = 500;
		    				int height = 500;
		    				
		    				int numPoints=8;
		    				for (int j = 0; j < numPoints; j++) {
								double angle=2.0*Math.PI*(j*1.0/numPoints);
		    					double y=Math.sin(angle)*height+height-200;
		    					double x=Math.cos(angle)*width+width-200;
		    					rootPolygon.add(x,y);
							}
		
//		    			VoronoiCore.setDebugMode();	
		    				
//		    				rootPolygon.add(0, 0);
//		    				rootPolygon.add(width, 0);
//		    				rootPolygon.add(width, height);
//		    				rootPolygon.add(0, height);

		    			
		    			   treemap=new VoronoiTreemap();
			    		    ((VoronoiTreemap)treemap).setRootIndex(treeData.rootIndex);
			    		    
			    		    ((VoronoiTreemap)treemap).setTreeData(treeData);
			    			treemap.setTreeAndWeights(rootPolygon, treeData.tree, null, null);		    					    			
			    			treemap.setCancelOnMaxIteration(true);
			    			treemap.setNumberThreads(1);
			    			treemap.setCancelOnThreshold(true);
			    			treemap.setNumberMaxIterations(3000);
			    			treemap.setErrorAreaThreshold(0.05);
//			    			treemap.setStatusObject(new WriteStatusObject(getFileName(name, "txt")));
			    			treemap.setStatusObject(new PNGStatusObject(getFileName(name, "png"), (VoronoiTreemap) treemap));
			    			treemap.compute();
		    			
		    		 }
		    		 }
		    		 
		    	}
		    	 
		    	if (args.length==1){
		    		
		    	}
		    	// treemap.setNumberThreads(1);
		    	// VoronoiCore.setDebugMode();
		    
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
	}
	
	

}

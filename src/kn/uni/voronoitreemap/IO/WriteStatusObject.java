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
package kn.uni.voronoitreemap.IO;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import kn.uni.voronoitreemap.interfaces.StatusObject;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;
import kn.uni.voronoitreemap.treemap.VoroNode;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;

/**
 * Class to write the result of a Voronoi Treemap computation as an edge list, including polygons
 * "nodeId;parentID;name;weight;hierarchyLevel;sitePosX;sitePosY;siteWeight;polygonPoints x1,y2,x2,y2\n";
 * @author Arlind Nocaj
 *
 */
public class WriteStatusObject implements StatusObject {

	private String filename;
	
	BufferedWriter writer;

	private VoronoiTreemap treemap;
	
	public WriteStatusObject(String outputFile, VoronoiTreemap treemap) {
		this.filename=outputFile+".txt";
		this.treemap=treemap;
		
		try {
			writer=new BufferedWriter(new FileWriter(filename));
			String header="nodeId;parentID;name;weight;hierarchyLevel;sitePosX;sitePosY;siteWeight;polygonPoints x1,y2,x2,y2\n";
		writer.write(header);			
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}
	@Override
	public void finished() {
		
		writeTreemap();
		
		try{			
		writer.flush();
		writer.close();
		}catch(Exception e){
//			e.printStackTrace();
		}
		
	}
	

	private void writeTreemap(){
		if (treemap==null)
			return;
		int wrote=0;
		for(VoroNode voroNode:treemap.getIdToNode().values()){

			if(voroNode.getParent()==null) continue;
			VoroNode parent = voroNode.getParent();
			Site site=voroNode.getSite();
			if(site==null ) {
				System.out.println("site null: "+voroNode.getName()+"\t level: "+voroNode.getHeight()+"\t parent: "+parent.getName());
				continue;
			}
			wrote++;
			StringBuilder builder=new StringBuilder();
			builder.append(voroNode.getNodeID()+";"+parent.getNodeID()+";"+voroNode.name+";"+voroNode.getWeight()+";"+voroNode.getHeight()+";"+site.x+";"+site.y+";"+site.getWeight()+";");
			
			PolygonSimple polygon=voroNode.getPolygon();
			if(polygon!=null){
			double[] xPoints = polygon.getXPoints();
			double[] yPoints = polygon.getYPoints();
			
			for (int j=0;j<polygon.length;j++){
				String first=(j==0)?"":",";
				builder.append(first+xPoints[j]+","+yPoints[j]);
			}
			}else
				builder.append("0,0");
			
			try {
				writer.write(builder.toString()+"\n");
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Wrote elements # "+wrote);
		
	
		
	}

	@Override
	synchronized public void finishedNode(int Node, int layer, int[] children,
			PolygonSimple[] polygons) {

	}

}

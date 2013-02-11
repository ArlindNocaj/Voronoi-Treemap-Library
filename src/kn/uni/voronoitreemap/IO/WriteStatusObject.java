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

/**
 * Class to write the result of a Voronoi Treemap computation as list of nodes together with the polygon:
 * parentID;hierarchyLevel;childID;polygonPoints x1;y2;x2;y2,...\n
 * Useful for command line.
 * @author Arlind Nocaj
 *
 */
public class WriteStatusObject implements StatusObject {

	private String filename;
	
	BufferedWriter writer;
	public WriteStatusObject(String outputFile) {
		this.filename=outputFile;
		try {
			writer=new BufferedWriter(new FileWriter(filename));
		writer.write("parentID;hierarchyLevel;childID;polygonPoints x1;y2;x2;y2,...\n");
			
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	@Override
	public void finished() {
		try{
		writer.flush();
		writer.close();
		}catch(Exception e){
			System.out.println(e);
		}
		
	}

	@Override
	public void finishedNode(int Node, int layer, int[] children,
			PolygonSimple[] polygons) {
		if (children==null){
			return;
		}
		
		for (int i=0;i<children.length;i++){
				StringBuilder builder=new StringBuilder();
				builder.append(Node+";"+layer+";"+children[i]);
				PolygonSimple polygon=polygons[i];
				double[] xPoints = polygon.getXPoints();
				double[] yPoints = polygon.getYPoints();
				for (int j=0;j<polygon.length;j++){
					builder.append(";"+xPoints[j]+";"+yPoints[j]);
				}
				
				try {
					writer.write(builder.toString()+"\n");
				} catch (IOException e) {
					System.out.println(e);
				}
			}
	}

}

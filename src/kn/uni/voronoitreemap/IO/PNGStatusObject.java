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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import kn.uni.voronoitreemap.interfaces.StatusObject;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.renderer.VoroRenderer;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;

/**
 * Class to write the result out as PNG file, e.g. when used from command line.
 * @author Arlind Nocaj
 *
 */
public class PNGStatusObject implements StatusObject {

	int border=10;
	private String filename;
	private VoronoiTreemap treemap;
	private BufferedImage bufferImage;

	public PNGStatusObject(String filename, VoronoiTreemap treemap){
		this.filename=filename;
		this.treemap=treemap;
		
	}
	@Override
	public void finished() {
		PolygonSimple rootPolygon = treemap.getRootPolygon();
		Rectangle rootRect = rootPolygon.getBounds();
		Graphics2D g;
		
		this.bufferImage = new BufferedImage(rootRect.width+2*border, rootRect.height+2*border,
					BufferedImage.TYPE_INT_ARGB);
		
		g = bufferImage.createGraphics();
		VoroRenderer renderer=new VoroRenderer();
		renderer.setTreemap(treemap);
		g.translate(border, border);
		renderer.setGraphics2D(g);		
		renderer.renderTreemap(null);
		
		
			try {
				ImageIO.write(bufferImage, "png", new File(filename + ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	
	}

	@Override
	public void finishedNode(int Node, int layer, int[] children,
			PolygonSimple[] polygons) {
		// TODO Auto-generated method stub

	}

}

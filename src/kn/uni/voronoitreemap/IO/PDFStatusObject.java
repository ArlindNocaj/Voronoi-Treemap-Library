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

import java.awt.Rectangle;
import java.io.FileOutputStream;

import kn.uni.voronoitreemap.interfaces.StatusObject;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.renderer.VoroRenderer;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;
import de.erichseifert.vectorgraphics2d.PDFGraphics2D;

/**
 * Class to write the result out as PNG file, e.g. when used from command line.
 * 
 * @author Arlind Nocaj
 * 
 */
public class PDFStatusObject implements StatusObject {
	int border=10;
	private String filename;
	private VoronoiTreemap treemap;

	public PDFStatusObject(String filename, VoronoiTreemap treemap) {
		this.filename = filename;
		this.treemap = treemap;

	}

	@Override
	public void finished() {
		
		PolygonSimple rootPolygon = treemap.getRootPolygon();
		Rectangle rootRect = rootPolygon.getBounds();
		PDFGraphics2D g;
		
		// Create a new PDF document with a width of 210 and a height of 297
		g = new PDFGraphics2D(0.0, 0.0, rootRect.getWidth()+2*border,
				rootRect.getHeight()+2*border);

		VoroRenderer renderer = new VoroRenderer();
		renderer.setRenderText(false);
		renderer.setTreemap(treemap);
		g.translate(border, border);
		renderer.setGraphics2D(g);
		renderer.renderTreemap(null);

		// Write the PDF output to a file
		FileOutputStream file = null;		
		try {
			file = new FileOutputStream(filename + ".pdf");
			
			file.write(g.getBytes());
			file.close();
		} catch (Exception e) {
//			e.printStackTrace();
		}

	}

	@Override
	public void finishedNode(int Node, int layer, int[] children,
			PolygonSimple[] polygons) {
		// TODO Auto-generated method stub

	}

}

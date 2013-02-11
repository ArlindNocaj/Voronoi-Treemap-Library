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

import kn.uni.voronoitreemap.interfaces.StatusObject;
import kn.uni.voronoitreemap.interfaces.VoronoiTreemapInterface;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.renderer.VoroRenderer;

/**
 * Class to write the result out as PNG file, e.g. when used from command line.
 * @author Arlind Nocaj
 *
 */
public class PNGStatusObject implements StatusObject {

	
	private String filename;
	private VoronoiTreemapInterface treemap;

	public PNGStatusObject(String filename, VoronoiTreemapInterface treemap){
		this.filename=filename;
		this.treemap=treemap;
		
	}
	@Override
	public void finished() {
		VoroRenderer renderer=new VoroRenderer();
		renderer.setTreemap(treemap);
		renderer.renderTreemap(filename);
	}

	@Override
	public void finishedNode(int Node, int layer, int[] children,
			PolygonSimple[] polygons) {
		// TODO Auto-generated method stub

	}

}

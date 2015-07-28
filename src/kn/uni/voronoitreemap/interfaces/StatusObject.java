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

import kn.uni.voronoitreemap.j2d.PolygonSimple;

/**
 * This object is used to notify certain steps to outer objects, e.g. when a node is finished.
 * It gives the corresponding child ids and polygons of the finished node.
 * 
 * @author Arlind Nocaj
 *
 */
public interface StatusObject {
	
	public void finishedNode(int Node, int layer, int[] children, PolygonSimple[] polygons);
	
	public void finished();
}

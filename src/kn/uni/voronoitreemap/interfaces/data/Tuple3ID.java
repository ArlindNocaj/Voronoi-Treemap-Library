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
package kn.uni.voronoitreemap.interfaces.data;

/**
 * Stores a triple where the first entry is an integer (e.g. node id), the second and third a double value (e.g. x and y coordinates)
 * @author Arlind Nocaj
 *
 */
public class Tuple3ID {

	public int id;
	public double valueX;
	public double valueY;
	
	public Tuple3ID(int id, double valueX, double valueY) {
		this.id=id;
		this.valueX=valueX;
		this.valueY=valueY;
	}
}

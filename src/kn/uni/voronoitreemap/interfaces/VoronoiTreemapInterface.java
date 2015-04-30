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


import java.awt.Graphics2D;
import java.util.ArrayList;

import kn.uni.voronoitreemap.interfaces.data.Tuple2ID;
import kn.uni.voronoitreemap.interfaces.data.Tuple3ID;
import kn.uni.voronoitreemap.j2d.PolygonSimple;


/**
 * Main interface to use the library.
 * First the hierarchy (tree) with weights and root polygon has to be set.
* Every functionality should go through this interface.
 * @author Arlind Nocaj
 *
 */
public interface VoronoiTreemapInterface {
	/**
	 * See {@link #compute()}
	 * 
	 * Caller gets locked until the computation is finished. This method further does not return until the callback on finished() in the StatusObject is completed. 
	 */
	public abstract void computeLocked();
	
	
	/**
	 *
	 * After setting at least the rootPolygon and the treeStructure(see {@link #setTreeAndWeights(PolygonSimple, ArrayList, ArrayList, ArrayList)}), the voronoi Treemap can be computed.
	 * For every computed Node finishedNode(...) will be called in the StatusObject. If the computation is completed finished() will be called in the Statusobject.
	 * 
	 */
	public abstract void compute();
	
	
	/**
	 * Sets the seed for the random position generator which is used to initialize the sites.
	 * @param seed
	 */
	public abstract void setRandomSeed(long seed);
	
	/**
	 * Seed which is used for random initialization of the sites.
	 * @return
	 */
	public abstract long getRandomSeed();
	
	/**
	 * All necessary things are set here such that a Voronoi Treemap can be
	 * computed. AreaGoals and refereceMapPositions can be null.
	 * 
	 * @link {@link #setRootPolygon(PolygonSimple)}
	 * @link {@link #setTree(ArrayList)}
	 * @link {@link #setAreaGoals(ArrayList)}
	 * @link {@link #setReferenceMap(ArrayList)}
	 * 	
	 * @param rootPolygon
	 * 			  the root Polygon to be set (must be convex currently)
	 * @param treeStructure
	 *            defines a tree by an adjacency list, node a has children b,c,d
	 *            (list(a,b,c,d))
	 * @param areaGoals
	 * 			  Tupel2ID with (NodeId, weight), can be null
	 * @param referenceMapPositions
	 * 			 Tupel3ID with (NodeId, relative x, relative y), x,y in range [0,1], can be null
	 */
	public abstract void setTreeAndWeights(PolygonSimple rootPolygon,
			final ArrayList<ArrayList<Integer>> treeStructure,
			final ArrayList<Tuple2ID> areaGoals,
			ArrayList<Tuple3ID> referenceMapPositions);

	/**
	 * Sets the tree.
	 * @param treeStructure
	 *            defines a tree by an adjacency list, node a has children b,c,d
	 *            (list(a,b,c,d))
	 */
	public abstract void setTree(final ArrayList<ArrayList<Integer>> treeStructure);
	
	/**
	 * Sets a weighting for each nodes which influences the final area the node polygon has.
	 * the weightings are normalized so that the sum over the weightings of descendant nodes equals to one. 
	 * this function must be called after the tree structure is defined (setTree(...)).
	 * @param areaGoals
	 * 				Tupel2ID with (NodeId, weight)
	 */
	public abstract void setAreaGoals(final ArrayList<Tuple2ID> areaGoals);
	
	/**
	 * ReferenceMap, which consists of a relative x,y position for each node: each coordinate must be in the range between [0...1]  
	 * @param relativePositions
	 * 					Tupel3ID with (NodeId, relative x, relative y), x,y in range [0,1]
	 */
	public abstract void setReferenceMap(ArrayList<Tuple3ID> relativePositions);
	
	
	public abstract boolean getShowLeafs();

	/**
	 * implementation not finished yet.
	 * @param g
	 */
	public abstract void drawTreemap(Graphics2D g);

	/**
	 * Sets the number of maximal iterations differently for each level, e.g.
	 * more iterations in the first levels, and less for the lowest levels.
	 * 
	 * @param levels
	 *            array where in the i-th position describes the maximal number
	 *            of iterations of level i
	 */
//	public abstract void setNumberIterationsLevel(int[] levelsMaxIteration);
	
	/**
	 * Sets the factor which scales every iteration, so 1-shrinkPercentage is equally the border percentage.
	 * @param shrinkPercentage
	 * 							preferred between 0 < shrinkPercentage <= 1
	 */
	public abstract void setShrinkPercentage(double shrinkPercentage);

	/**
	 * {@link #setShrinkPercentage(double)}
	 * @return
	 */
	public abstract double getShrinkPercentage();
	/**
	 * Sets whether there borders are added for every iteration level or not.
	 * @param useBorder
	 */
	public abstract void setUseBorder(boolean useBorder);
	/**
	 * {@link #setUseBorder(boolean)}
	 * @return
	 */
	public abstract boolean getUseBorder();

	/**
	 * Sets the default maximal number of iterations done in a single level
	 * computation.
	 * 
	 * @param numberMaxIterations
	 */
	public abstract void setNumberMaxIterations(int numberMaxIterations);

	
	/**
	 * the maximal number of iterations.
	 * @return cancelOnMaxIterat
	 */
	public abstract int getNumberMaxIterations();


	/**
	 * true: the optimization process is canceled when the area error is below the given threshold.
	 * @param cancelOnThreshold
	 *            the cancelOnThreshold to set
	 */
	public abstract void setCancelOnThreshold(boolean cancelOnThreshold);

	/**
	 * {@link #setCancelOnThreshold(boolean)}
	 * @return the cancelOnThreshold
	 */
	public abstract boolean getCancelOnThreshold();
	

	/**
	 * true: The optimization process is canceled when the maximal iteration number is reached.
	 * @param cancelOnMaxIterat
	 *            the cancelOnMaxIterat to set
	 */
	public abstract void setCancelOnMaxIteration(boolean cancelOnMaxIterat);

	/**
	 * @link #setCancelOnMaxIteration(boolean)
	 * @return the cancelOnMaxIterat
	 */
	public abstract boolean getCancelOnMaxIteration();

	/**
	 * Sets the convex root polygon in which the treemap is computed. The result will always be included into the root polygon.
	 * Currently the root polygon has to be convex, if you have a fast implementation for the intersection of non convex polygons, I could easily extend the library to simple polygons.
	 * 
	 * 
	 * @param rootPolygon
	 *            the root Polygon to be set (must be convex currently)
	 */
	public abstract void setRootPolygon(PolygonSimple rootPolygon);

	/**
	 * Sets the a rectangle (as root polygon) in which the treemap is computed.
	 * 
	 * @param x x-coordinate of the upper left point of the rectangle
	 * @param y y-coordinate of the upper left point of the rectangle
	 * @param width width of the rectangle
	 * @param height height of the rectangle
	 */
	public abstract void setRootRectangle(double x, double y, double width,
			double height);

	/**
	 * Returns the root polygon.
	 * @return the rootPolygon
	 */
	public abstract PolygonSimple getRootPolygon();

	
	
	/**
	 * If possible the given number of threads will be started to speed up the optimization process. 
	 * Usually this is possible after the computation of the first hierarchy level has been finished.
	 * @param numberThreads
	 *            the numberThreads to set
	 */
	public abstract void setNumberThreads(int numberThreads);

	/**
	 * This status object is notified when the children of a node are processed and when the whole computation has finished.
	 * @param tellTheEndObject
	 *            the tellTheEndObject to set
	 */
	public abstract void setStatusObject(StatusObject statusObject);

	/**
	 * {@link #setStatusObject(StatusObject)}
	 * @return
	 */
	public abstract StatusObject getStatusObject();
	
	
	/**
	 * Clears the used datastructures.
	 */
	public abstract void clear();


	public abstract void setErrorAreaThreshold(double d);
}

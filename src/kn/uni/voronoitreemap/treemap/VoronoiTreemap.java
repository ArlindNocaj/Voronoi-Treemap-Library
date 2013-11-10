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
package kn.uni.voronoitreemap.treemap;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.swing.JLayeredPane;

import kn.uni.voronoitreemap.core.VoronoiCore;
import kn.uni.voronoitreemap.debug.ImageFrame;
import kn.uni.voronoitreemap.gui.JPolygon;
import kn.uni.voronoitreemap.helper.InterpolColor;
import kn.uni.voronoitreemap.interfaces.StatusObject;
import kn.uni.voronoitreemap.interfaces.VoronoiTreemapInterface;
import kn.uni.voronoitreemap.interfaces.data.Tuple2ID;
import kn.uni.voronoitreemap.interfaces.data.Tuple3ID;
import kn.uni.voronoitreemap.j2d.Point2D;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;

/**
 * Main Voronoi Treemap class implementing the Voronoi Treemap interface and maintaining the settings.
 * @author nocaj
 *
 */
public class VoronoiTreemap implements Iterable<VoroNode>, StatusObject,
		VoronoiTreemapInterface {
	/**
	 * DEBUGGING
	 */
	// TODO remove debug Mode
	public static boolean debugMode = false;
	public static ImageFrame frame;
	public static Graphics2D graphics;
	// geVoroRenderer renderer;

	/**
	 * DEBUGGING
	 */

	private boolean initialized = false;

	private boolean useBorder = false;
	private double shrinkPercentage = 1;
	private boolean showLeafs = false;

	private int numberThreads = 1;
	protected VoroNode root;
	private PolygonSimple rootPolygon;
	public static InterpolColor interpolColor = new InterpolColor(0, 1, 0.0,
			0.73, 0.58, 224.0 / 225.0, 0.73, 0.58);
	int amountAllNodes = 0;
	int alreadyDoneNodes = 0;

	long timeStart;
	long timeEnd;
	private Semaphore lock = new Semaphore(1);

	/**
	 * Settings for the Core
	 */
	private int numberMaxIterations = 200;
	/**
	 * A site which wants to reach more then preflowPercentage is considered for
	 * preflow extrapolation. default value: 0.08
	 */
	private double preflowPercentage = 0.08;
	/**
	 * If a region wants to increase its area by the factor preflowIncrease it
	 * is considered for preflow extrapolation default value is 1.5 or 1.6
	 */
	private double preflowIncrease = 1.3;
	private boolean useNegativeWeights = true;
	private boolean useExtrapolation = false;
	private boolean cancelOnThreshold = false;
	private boolean cancelOnMaxIterat = true;
	protected double errorAreaThreshold = 0.001;
	protected boolean preflowFinished = false;
	private boolean guaranteeValidCells = false;

	
	private boolean aggressiveMode=false;
	
	/**
	 * This queue handles the voronoi cells which have to be calculated
	 */
	BlockingQueue<VoroNode> cellQueue = new LinkedBlockingQueue<VoroNode>();
	private int[] levelsMaxIteration;
	private StatusObject statusObject;

	/**
	 * used for, e.g., random positioning of points
	 */
	long randomSeed=1985;
	Random rand = new Random(randomSeed);
	private HashMap<Integer, VoroNode> idToNode;

	/** when a node is finished the status object is notified. **/

	public VoronoiTreemap(StatusObject statusObject) {
		this();
		this.statusObject = statusObject;
	}

	public VoronoiTreemap(StatusObject statusObject, boolean multiThreaded) {
		this();
		this.statusObject = statusObject;

		if (multiThreaded) {
			setNumberThreads(Runtime.getRuntime().availableProcessors());
		}
	}

	public VoronoiTreemap() {
		init();
	}

	protected void recalculatePercentage() {
		amountAllNodes = 0;
		alreadyDoneNodes = 0;

		root.calculateWeights();
	}

	protected void setRootCell(VoroNode cell) {
		this.root = cell;
		root.setHeight(1);
		root.setWantedPercentage(0);
	}

	protected VoroNode getRootCell() {
		return root;
	}

	protected void init() {

		initialized = false;

		useBorder = false;
		shrinkPercentage = 1;
		showLeafs = false;
		
		numberThreads = 1;
		root = null;
		rootPolygon = null;

		numberMaxIterations = 200;
		preflowPercentage = 0.08;

		preflowIncrease = 1.3;
		useNegativeWeights = true;
		useExtrapolation = false;
		cancelOnThreshold = false;
		cancelOnMaxIterat = true;
		errorAreaThreshold = 0.001;
		preflowFinished = false;
		guaranteeValidCells = false;

		if (cellQueue!=null)
			cellQueue.clear();

		statusObject = null;
		rand = new Random(1985);
		if (idToNode!=null)
			idToNode.clear();
		lock = new Semaphore(1);

	}

	protected void initVoroNodes() {
		if (!initialized && root != null) {
			initialized = true;
			cellQueue.clear();
			root.calculateWeights();
		}
	}

	private void startComputeThreads() {
		// start as much VoroCPUs as there are CPUS available
		//System.out.println("numberThreads:" + getNumberThreads());
		for (int i = 0; i < getNumberThreads(); i++) {
			new VoroCPU(cellQueue, this).start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#compute()
	 */
	public void compute() {
		if (rootPolygon == null)
			throw new RuntimeException("Root Polygon not set.");
		timeStart = System.currentTimeMillis();
		initVoroNodes();
		cellQueue.add(root);
		startComputeThreads();
	}
	
	public void computeLocked(){
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		compute();
		try {
			lock.acquire();
			lock.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void setSettingsToVoroNode(VoroNode node) {
		node.setTreemap(this);
	}

	/**
	 * Generate ExamplePicture
	 * 
	 * @param args
	 */
	// public static void main(String[] args) {
	// VoronoiCore.setDebugMode();
	// VoronoiTreemap voronoiTreemap = new VoronoiTreemap(null);
	// voronoiTreemap.setUpToLastLeaf(true);
	// voronoiTreemap.setUseBorder(false);
	// voronoiTreemap.setShrinkPercentage(0.96);
	//
	// NPoly rootPolygon = new NPoly();
	// int width=200;
	// int height=400;
	// rootPolygon.add(0, 0);
	// rootPolygon.add(width, 0);
	// rootPolygon.add(width, height);
	// rootPolygon.add(0, height);
	//
	// /**
	// * Create the hierarchy
	// */
	//
	//
	//
	// VoroNode r = getSmallTreemapExample(voronoiTreemap, rootPolygon);
	//
	// /**
	// // * ImageFrame
	// // */
	// BufferedImage image = new BufferedImage(8000, 8000,
	// BufferedImage.TYPE_INT_RGB);
	//
	// Graphics2D graphic = image.createGraphics();
	// voronoiTreemap.setGraphics(graphic);
	// voronoiTreemap.iterate();
	// }

	public static void main(String[] args) {
		VoronoiCore.setDebugMode();
		// VoronoiCore.graphics.translate(-178,-483);
		// VoronoiCore.graphics.scale(5, 5);
		VoronoiTreemapInterface voronoiTreemap = new VoronoiTreemap();
		voronoiTreemap.setUseBorder(true);
		voronoiTreemap.setShrinkPercentage(0.95);

		PolygonSimple rootPolygon = new PolygonSimple();
		int width = 500;
		int height = 500;
		rootPolygon.add(0, 0);
		rootPolygon.add(width, 0);
		rootPolygon.add(width, height);
		rootPolygon.add(0, height);

		voronoiTreemap.setRootPolygon(rootPolygon);

		// array with relative positions
		Random rand = new Random();
		double[][] relativePositions = new double[8][2];
		for (int i = 0; i < 8; i++) {
			relativePositions[i][0] = rand.nextDouble();
			relativePositions[i][1] = rand.nextDouble();
		}
		// array with goal area weights, can also be not normalized
		double[] areaGoals = new double[8];
		for (int i = 0; i < areaGoals.length; i++) {
			areaGoals[i] = rand.nextInt(100);
		}

		// voronoiTreemap.setEdgeListAndWeights(treeStructure, areaGoals,
		// relativePositions);
		// voronoiTreemap.setTreeAndWeights(treeStructure,areaGoals,relativePositions);
		/**
		 * Things which have to be set:
		 */
		/**
		 * sites: x,y,initialWeights
		 * 
		 */

		// /**
		// * ImageFrame
		// */
		// BufferedImage image = new BufferedImage(800, 800,
		// BufferedImage.TYPE_INT_RGB);
		//
		// ImageFrame frame = new ImageFrame(image);
		// frame.setVisible(true);
		// frame.setBounds(20, 20, 1600, 800);
		// Graphics2D graphic = image.createGraphics();
		// voronoiTreemap.setGraphics(graphic);

		/**
		 * Panel and JFrame;
		 */
		// JFrame frame = new JFrame();
		// frame.setBounds(20, 20, 1400, 1400);
		// frame.setVisible(true);
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(null);
		// frame.getContentPane().add(layeredPane);

		voronoiTreemap.compute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setTreeAndWeights(j2d.PolygonSimple,
	 * java.util.ArrayList, java.util.ArrayList, java.util.ArrayList)
	 */
	public void setTreeAndWeights(PolygonSimple rootPolygon,
			final ArrayList<ArrayList<Integer>> treeStructure,
			final ArrayList<Tuple2ID> areaGoals,
			ArrayList<Tuple3ID> relativePositions) {
		this.rootPolygon = rootPolygon;

		setTree(treeStructure);
		setAreaGoals(areaGoals);
		
		if (relativePositions == null) {

			for (VoroNode voroNode : idToNode.values()) {
				double x = rand.nextDouble();
				double y = rand.nextDouble();
				voroNode.setRelativeVector(new Point2D(x, y));
			}
		} else {
			setReferenceMap(relativePositions);
		}

		root.setVoroPolygon(rootPolygon);

	}

	protected final VoroNode createVoroNode(
			HashMap<Integer, VoroNode> idToNode,
			final ArrayList<Integer> adjacencyLine) {
		ArrayList<Integer> childList = adjacencyLine;
		if (adjacencyLine == null) {
			return null;
		}
		Integer parentId = childList.get(0);

		int numberChildren = 0;

		numberChildren = childList.size() - 1;

		VoroNode voroNode = null;
		voroNode = idToNode.get(parentId);
		if (voroNode == null) {
			voroNode = new VoroNode(parentId, numberChildren);
		}

		voroNode.setTreemap(this);
		setSettingsToVoroNode(voroNode);

		// create child nodes
		if (numberChildren >= 1) {
			for (int i = 1; i < (numberChildren + 1); i++) {
				Integer childId = childList.get(i);
				VoroNode voroChild = new VoroNode(childId);
				idToNode.put(childId, voroChild);
				voroNode.addChild(voroChild);
				voroChild.setParent(voroNode);
				voroNode.setTreemap(this);
				setSettingsToVoroNode(voroNode);
			}
		}

		return voroNode;

	}

	void setShowLeafs(boolean showLeafs) {
		this.showLeafs = showLeafs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getShowLeafs()
	 */
	public boolean getShowLeafs() {
		return showLeafs;
	}

	/**
	 * Iterator for going over the VoroNodes of this Treemap
	 * 
	 * @author nocaj
	 * 
	 */
	private class NodeIterator implements Iterator<VoroNode> {
		Stack<VoroNode> stack;

		public NodeIterator(VoroNode root) {
			stack = new Stack<VoroNode>();
			stack.addAll(root.getChildren());

		}

		@Override
		public boolean hasNext() {
			if (stack.size() > 0) {
				return true;
			} else
				return false;
		}

		@Override
		public VoroNode next() {
			VoroNode t = stack.pop();
			if (t != null && t.getChildren() != null
					&& t.getChildren().size() > 0) {
				stack.addAll(t.getChildren());
			}
			return t;
		}

		@Override
		public void remove() {

		}

	}

	@Override
	public Iterator<VoroNode> iterator() {
		return new NodeIterator(root);
	}

	// /**
	// * Searches for the Least Common Ancestor of two Nodes and return the
	// Points
	// * on the way to the Least Common Ancestor. Algorithm: Go the node with
	// the
	// * higher Level upwards until your reach a common node from both sides.
	// *
	// * @return Points on the way to the Least Common Ancestor
	// */
	// public ArrayList<Point2D> getSplinePoints(Integer nodeID1, Integer
	// nodeID2,
	// boolean removeLCA) {
	// VoroNode node1 = nodeMap.get(nodeID1);
	// VoroNode node2 = nodeMap.get(nodeID2);
	// Point2D p1 = null;
	// Point2D p2 = null;
	// if (node1.getPolygon() == null) {
	// p1 = node1.getParent().getPolygon().getInnerPoint();
	// } else {
	// p1 = node1.getPolygon().getCentroid();
	// }
	//
	// if (node2.getPolygon() == null) {
	// p2 = node2.getParent().getPolygon().getInnerPoint();
	// } else {
	// p2 = node2.getPolygon().getInnerPoint();
	// }
	//
	// ArrayList<Point2D> list1 = new ArrayList<Point2D>();
	// LinkedList<Point2D> list2 = new LinkedList<Point2D>();
	//
	// if (node1 == null || node2 == null) {
	// throw new RuntimeException("No nodes found for given nodeID");
	// }
	//
	// list1.add(p1);
	// list2.add(p2);
	// while (!node1.equals(node2)) {
	// if (node1.getHeight() > node2.getHeight()) {
	// node1 = node1.getParent();
	// list1.add(node1.getPolygon().getCentroid());
	// } else {
	// node2 = node2.getParent();
	// list2.addFirst(node2.getPolygon().getCentroid());
	// }
	// }
	// // remove the least common ancestor point because he is two times in the
	// // lists if (areaGoals!=null){
	// list2.removeFirst();
	// if (removeLCA) {
	// list1.remove(list1.size() - 1);
	// }
	// list1.addAll(list2);
	// return list1;
	// }

	@Override
	public synchronized void finished() {

		timeEnd = System.currentTimeMillis();
		// System.out.println("AmountSites:" + VoronoiCore.amountSites);
		double diff = timeEnd - timeStart;
		// System.out.println("Nodes done:" + this.alreadyDoneNodes);
		//
		// System.out.println("ComputationTime seconds:" + diff / 1000.0);

		// drawTreemap(graphics);
		// drawTreemapWithComponents(graphics);
		/**
		 * Using the JPolygon components
		 */
		// for (VoroNode node : this) {
		// int height = node.getHeight();
		//
		// Site site = node.getSite();
		// if (site != null) {
		// NPoly poly = site.getPolygon();
		// if (poly != null) {
		// if (site.cellObject != null) {
		// site.cellObject.setVoroPolygon(poly);
		// site.cellObject.doFinalWork();
		// }
		// }
		// }
		// JPolygon component = node.getPolygonComponent();
		// if (component != null) { if (areaGoals!=null){
		// this.getLayeredPane().add(component, new Integer(height));
		// }
		// }
		
		if (statusObject != null)
			statusObject.finished();
		lock.release();

	}

	protected void setAmountNodes(int amountNodes) {
		this.amountAllNodes = amountNodes;
	}

	protected int getAmountNodes() {
		return amountAllNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#drawTreemap(java.awt.Graphics2D)
	 */
	public void drawTreemap(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int lastNodes = 0;
		int amountPolygons = 0;
		for (VoroNode node : this) {
			int height = node.getHeight();
			if (node.getChildren() == null) {
				lastNodes++;
			}
			Site site = node.getSite();
			if (site != null) {

				PolygonSimple poly = site.getPolygon();
				if (poly != null) {
					// poly.shrinkForBorder(0.95);
					amountPolygons++;
					g.draw(poly);
				}
			}

		}
		// System.out.println("LeafNodes:" + lastNodes);
		// System.out.println("AmountPolygons:" + amountPolygons);

		// VoroRenderer renderer = new VoroRenderer();
		// renderer.setTreemap(this);
		// renderer.renderTreemap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setNumberIterationsLevel(int[])
	 */
	public void setNumberIterationsLevel(int[] levelsMaxIteration) {
		this.levelsMaxIteration = levelsMaxIteration;
	}

	protected void drawTreemapWithComponents(Graphics2D g) {
		for (VoroNode child : this) {
			JPolygon jp = new JPolygon(child.getNodeID(), new Integer(
					child.getNodeID()).toString());
		}
	}

	void setGraphics(Graphics2D graphics) {
		this.graphics = graphics;
	}

	protected Graphics2D getGraphics() {
		return graphics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setShrinkPercentage(double)
	 */
	public void setShrinkPercentage(double shrinkPercentage) {
		this.shrinkPercentage = shrinkPercentage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getShrinkPercentage()
	 */
	public double getShrinkPercentage() {
		return shrinkPercentage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setUseBorder(boolean)
	 */
	public void setUseBorder(boolean useBorder) {
		this.useBorder = useBorder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getUseBorder()
	 */
	public boolean getUseBorder() {
		return useBorder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setNumberMaxIterations(int)
	 */
	public void setNumberMaxIterations(int numberMaxIterations) {
		this.numberMaxIterations = numberMaxIterations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getNumberMaxIterations()
	 */
	public int getNumberMaxIterations() {
		return numberMaxIterations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setPreflowPercentage(double)
	 */
	public void setPreflowPercentage(double preflowPercentage) {
		this.preflowPercentage = preflowPercentage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getPreflowPercentage()
	 */
	public double getPreflowPercentage() {
		return preflowPercentage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setPreflowIncrease(double)
	 */
	public void setPreflowIncrease(double preflowIncrease) {
		this.preflowIncrease = preflowIncrease;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getPreflowIncrease()
	 */
	public double getPreflowIncrease() {
		return preflowIncrease;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setUseExtrapolation(boolean)
	 */
	public void setUseExtrapolation(boolean useExtrapolation) {
		this.useExtrapolation = useExtrapolation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getUseExtrapolation()
	 */
	public boolean getUseExtrapolation() {
		return useExtrapolation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setCancelOnThreshold(boolean)
	 */
	public void setCancelOnThreshold(boolean cancelOnThreshold) {
		this.cancelOnThreshold = cancelOnThreshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getCancelOnThreshold()
	 */
	public boolean getCancelOnThreshold() {
		return cancelOnThreshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setCancelOnMaxIteration(boolean)
	 */
	public void setCancelOnMaxIteration(boolean cancelOnMaxIterat) {
		this.cancelOnMaxIterat = cancelOnMaxIterat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getCancelOnMaxIteration()
	 */
	public boolean getCancelOnMaxIteration() {
		return cancelOnMaxIterat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setRootPolygon(j2d.PolygonSimple)
	 */
	public void setRootPolygon(PolygonSimple rootPolygon) {
		this.rootPolygon = rootPolygon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setRootRectangle(double, double,
	 * double, double)
	 */
	public void setRootRectangle(double x, double y, double width, double height) {
		rootPolygon = new PolygonSimple();
		rootPolygon.add(x, y);
		rootPolygon.add(x + width, y);
		rootPolygon.add(x + width, y + height);
		rootPolygon.add(x, y + height);
	}

	/**
	 * Sets the root rectangle in which the treemap is computed.
	 * 
	 * @param rectangle
	 */
	public void setRootRectangle(Rectangle2D.Double rectangle) {
		setRootRectangle(rectangle.getX(), rectangle.getY(),
				rectangle.getWidth(), rectangle.getHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getRootPolygon()
	 */
	public PolygonSimple getRootPolygon() {
		return rootPolygon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setGuaranteeValidCells(boolean)
	 */
	public void setGuaranteeValidCells(boolean guaranteeInvariant) {
		this.guaranteeValidCells = guaranteeInvariant;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getGuaranteeValidCells()
	 */
	public boolean getGuaranteeValidCells() {
		return guaranteeValidCells;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setNumberThreads(int)
	 */
	public void setNumberThreads(int numberThreads) {
		if (numberThreads >= 1) {
			this.numberThreads = numberThreads;
		} else {
			this.numberThreads = 1;
		}

	}

	/**
	 * @return the numberThreads
	 */
	int getNumberThreads() {
		return numberThreads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * treemap.voronoiTreemapInterface#setStatusObject(libinterfaces.IStatusObject
	 * )
	 */
	public void setStatusObject(StatusObject statusObject) {
		this.statusObject = statusObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getStatusObject()
	 */
	public StatusObject getStatusObject() {
		return statusObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#finishedNode(int, int, int[],
	 * j2d.PolygonSimple[])
	 */
	@Override
	public void finishedNode(int Node, int layer, int[] children,
			PolygonSimple[] polygons) {
		if (statusObject != null)
			statusObject.finishedNode(Node, layer, children, polygons);
	}

	@Override
	public void setAreaGoals(ArrayList<Tuple2ID> areaGoals) {
		if (areaGoals != null) {
			for (Tuple2ID tuple : areaGoals) {
				VoroNode voroNode = null;
				voroNode = idToNode.get(tuple.id);
				if (voroNode != null) {
					voroNode.setWeight(tuple.value);
					
				}else if(tuple.id==root.getNodeID()){
					//we do not care, we don't need the weighting of the root
					
				
				} else{
					System.out.println("id: "+tuple.id);
					throw new RuntimeException(
							"There is no node in the tree structure with this ID.");
			}
		}
		}
	}

	@Override
	public void setReferenceMap(ArrayList<Tuple3ID> relativePositions) {
		for (Tuple3ID tuple : relativePositions) {
			VoroNode voroNode = null;
			voroNode = idToNode.get(tuple.id);
			if (voroNode != null) {

				voroNode.setRelativeVector(new Point2D(tuple.valueX,
						tuple.valueY));
			} else if(tuple.id == root.getNodeID()){
				//we do not care, we can't set a relative position for the root	
			} else
				throw new RuntimeException(
						"ReferencePosition for ID without node in the tree structure.");
		}
	}

	@Override
	public void setTree(ArrayList<ArrayList<Integer>> treeStructure) {
		idToNode = new HashMap<Integer, VoroNode>();

		ArrayList<Integer> line = treeStructure.get(0);
		int numberLines = treeStructure.size();
		root = createVoroNode(idToNode, line);

		for (int i = 1; i < numberLines; i++) {
			createVoroNode(idToNode, treeStructure.get(i));
		}

		for (VoroNode voroNode : idToNode.values()) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			voroNode.setRelativeVector(new Point2D(x, y));
		}

		root.setVoroPolygon(rootPolygon);
	}

	@Override
	public boolean isUseNegativeWeights() {
		return useNegativeWeights;
	}

	@Override
	public void setUseNegativeWeights(boolean use) {
		useNegativeWeights = use;
	}

	@Override
	public void clear() {
		init();
	}

	@Override
	public void setAggressiveMode(boolean mode) {
		aggressiveMode=mode;
	}

	@Override
	public boolean getAggressiveMode() {
	return aggressiveMode;
	}

	@Override
	public void setRandomSeed(long seed) {
		randomSeed=seed;
		rand.setSeed(seed);
	}

	@Override
	public long getRandomSeed() {
		return randomSeed;
	}

	@Override
	public double getCancelErrorThreshold() {
		return errorAreaThreshold;
	}

}

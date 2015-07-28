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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.swing.JLayeredPane;

import kn.uni.voronoitreemap.IO.IO;
import kn.uni.voronoitreemap.IO.PDFStatusObject;
import kn.uni.voronoitreemap.IO.PNGStatusObject;
import kn.uni.voronoitreemap.IO.WriteStatusObject;
import kn.uni.voronoitreemap.core.VoroSettings;
import kn.uni.voronoitreemap.core.VoronoiCore;
import kn.uni.voronoitreemap.debug.ImageFrame;
import kn.uni.voronoitreemap.gui.JPolygon;
import kn.uni.voronoitreemap.interfaces.StatusObject;
import kn.uni.voronoitreemap.interfaces.data.TreeData;
import kn.uni.voronoitreemap.interfaces.data.TreeData.Node;
import kn.uni.voronoitreemap.interfaces.data.Tuple2ID;
import kn.uni.voronoitreemap.interfaces.data.Tuple3ID;
import kn.uni.voronoitreemap.j2d.Point2D;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;

/**
 * Main Voronoi Treemap class implementing the Voronoi Treemap interface and
 * maintaining the settings.
 * 
 * @author nocaj
 * 
 */
public class VoronoiTreemap implements Iterable<VoroNode>, StatusObject {
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
	private boolean uniformWeights = false;
	private double shrinkPercentage = 1;
	private boolean showLeafs = false;

	private int numberThreads = 1;
	protected VoroNode root;
	private PolygonSimple rootPolygon;

	int amountAllNodes = 0;
	int alreadyDoneNodes = 0;

	long timeStart;
	long timeEnd;
	private Semaphore lock = new Semaphore(1);

	/**
	 * This queue handles the voronoi cells which have to be calculated.
	 */
	BlockingQueue<VoroNode> cellQueue = new LinkedBlockingQueue<VoroNode>();
	private List<StatusObject> statusObject;

	// used for randomization
	long randomSeed = 21;
	Random rand = new Random(randomSeed);

	private HashMap<Integer, VoroNode> idToNode;
	private ArrayList<Tuple3ID> relativePositions;

	VoroSettings coreSettings = new VoroSettings();
	private int[] levelsMaxIteration;
	private Set<VoroCPU> runningThreads;
	private int rootIndex;
	private TreeData treeData;

	/** when a node is finished the status object is notified. **/

	public HashMap<Integer, VoroNode> getIdToNode() {
		return idToNode;
	}

	public VoronoiTreemap(StatusObject statusObject) {
		this();
		this.statusObject.add(statusObject);
	}

	public VoronoiTreemap(StatusObject statusObject, boolean multiThreaded) {
		this(statusObject);

		if (multiThreaded)
			setNumberThreads(Runtime.getRuntime().availableProcessors());
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
		if (cellQueue != null)
			cellQueue.clear();
		statusObject = new ArrayList<StatusObject>();
		rand = new Random(randomSeed);
		if (idToNode != null)
			idToNode.clear();
		lock = new Semaphore(1);
	}

	protected void initVoroNodes() {
		if (!initialized && root != null) {
			initialized = true;
			cellQueue.clear();
			root.calculateWeights();
			setRelativePositions(relativePositions);
		}
	}

	private void startComputeThreads() {
		this.runningThreads = Collections
				.newSetFromMap(new ConcurrentHashMap<VoroCPU, Boolean>());
		for (int i = 0; i < getNumberThreads(); i++)
			new VoroCPU(cellQueue, this, runningThreads).start();
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

	public void computeLocked() {
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

	public static void main(String[] args) {
		
		
		VoronoiTreemap voronoiTreemap = new VoronoiTreemap();				

		//create a convex root polygon
		PolygonSimple rootPolygon = new PolygonSimple();
		int width = 300;
		int height = 500;
		int numPoints = 8;
		for (int j = 0; j < numPoints; j++) {
			double angle = 2.0 * Math.PI * (j * 1.0 / numPoints);
			double rotate = 2.0 * Math.PI / numPoints / 2;
			double y = Math.sin(angle + rotate) * height + height;
			double x = Math.cos(angle + rotate) * width + width;
			rootPolygon.add(x, y);
		}
		
		//create hierarchy structure (first parent will be root element)
		TreeData data=new TreeData();
		data.addLink("README.md", "project");
		data.addLink("file001", "project");
		
		data.addLink("folder1", "project");
		data.addLink("file011", "folder1");
		data.addLink("file012", "folder1");
		
		data.addLink("subfolder1", "folder1");
		data.addLink("file111", "subfolder1");
		data.addLink("file112", "subfolder1");
		data.addLink("...", "subfolder1");
		
		data.addLink("folder2","folder1");
		data.addLink("file021","folder2");
		data.addLink("file022","folder2");	
		
		data.addLink("folder3", "project");
		data.addLink("file031","folder3");
		data.addLink("file032","folder3");
		data.addLink("file033","folder3");
		data.addLink("file034","folder3");
		data.addLink("file035","folder3");
		data.addLink("file036","folder3");
	
//		VoronoiCore.setDebugMode();
		VoronoiTreemap treemap = new VoronoiTreemap();
		treemap.setRootPolygon(rootPolygon);
		treemap.setTreeData(data);
		treemap.setCancelOnMaxIteration(true);
		treemap.setNumberMaxIterations(1500);
		treemap.setCancelOnThreshold(true);
		treemap.setErrorAreaThreshold(0.01);
		treemap.setUniformWeights(true);
		treemap.setNumberThreads(1);

		treemap.setStatusObject(new WriteStatusObject("miniHierarchy-result", treemap));
		treemap.setStatusObject(new PNGStatusObject("miniHierarchy",
				treemap));
		treemap.setStatusObject(new
				 PDFStatusObject("miniHierarchy", treemap));
		treemap.computeLocked();				
	}

	public void setRootIndex(int rootIndex) {
		this.rootIndex = rootIndex;
	}

	private void setRelativePositions(ArrayList<Tuple3ID> relativePositions) {
		if (relativePositions == null) {
			for (VoroNode voroNode : idToNode.values()) {
				double x = rand.nextDouble();
				double y = rand.nextDouble();
				voroNode.setRelativeVector(new Point2D(x, y));
			}
			return;
		}

		setReferenceMap(relativePositions);

	}

	public void setReferenceMap(ArrayList<Tuple3ID> relativePositions) {
		for (Tuple3ID tuple : relativePositions) {
			VoroNode voroNode = null;
			voroNode = idToNode.get(tuple.id);
			if (voroNode != null) {

				voroNode.setRelativeVector(new Point2D(tuple.valueX,
						tuple.valueY));
			} else
				System.out
						.println("node id could not be found for setting reference position: "
								+ tuple.id);
		}
	}

	protected final void addChildren(HashMap<Integer, VoroNode> idToNode,
			final ArrayList<ArrayList<Integer>> adjLists, int currentPos) {
		ArrayList<Integer> childList = adjLists.get(currentPos);
		if (childList == null || childList.size() == 1)
			return;

		Integer parentId = childList.get(0);
		VoroNode voroParent = idToNode.get(parentId);

		// add children to parent

		for (int i = 1; i < childList.size(); i++) {
			Integer childId = childList.get(i);
			VoroNode voroChild = idToNode.get(childId);
			voroParent.addChild(voroChild);
			voroChild.setParent(voroParent);
		}
		
		for (int i = 1; i < childList.size(); i++) {
			Integer childId = childList.get(i);
			addChildren(idToNode, adjLists, childId);
		}

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
		for (StatusObject statusObject : this.statusObject)
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
		coreSettings.maxIterat = numberMaxIterations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getNumberMaxIterations()
	 */
	public int getNumberMaxIterations() {
		return coreSettings.maxIterat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setCancelOnThreshold(boolean)
	 */
	public void setCancelOnThreshold(boolean cancelOnThreshold) {
		this.coreSettings.cancelAreaError = cancelOnThreshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getCancelOnThreshold()
	 */
	public boolean getCancelOnThreshold() {
		return coreSettings.cancelAreaError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setCancelOnMaxIteration(boolean)
	 */
	public void setCancelOnMaxIteration(boolean cancelOnMaxIterat) {
		coreSettings.cancelMaxIterat = cancelOnMaxIterat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getCancelOnMaxIteration()
	 */
	public boolean getCancelOnMaxIteration() {
		return coreSettings.cancelMaxIterat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#setRootPolygon(j2d.PolygonSimple)
	 */
	public void setRootPolygon(PolygonSimple rootPolygon) {
		this.rootPolygon = rootPolygon;
		if (root != null)
			root.setPolygon(rootPolygon);
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
		root.setPolygon(rootPolygon);
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
		this.statusObject.add(statusObject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see treemap.voronoiTreemapInterface#getStatusObject()
	 */
	public StatusObject getStatusObject() {
		return statusObject.get(0);
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
		for (StatusObject statusObject : this.statusObject)
			statusObject.finishedNode(Node, layer, children, polygons);
	}

	public void setTree(ArrayList<ArrayList<Integer>> treeStructure) {
		idToNode = new HashMap<Integer, VoroNode>();

		for (int i = 0; i < treeStructure.size(); i++) {
			ArrayList<Integer> adj = treeStructure.get(i);
			VoroNode node = new VoroNode(i, adj.size() - 1);
			idToNode.put(i, node);
			node.setTreemap(this);
		}

		root = idToNode.get(rootIndex);

		addChildren(idToNode, treeStructure, rootIndex);

		for (VoroNode voroNode : idToNode.values()) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			voroNode.setRelativeVector(new Point2D(x, y));
		}

		root.setVoroPolygon(rootPolygon);
	}

	public void clear() {
		init();
	}

	public void setRandomSeed(long seed) {
		randomSeed = seed;
		rand.setSeed(seed);
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setErrorAreaThreshold(double d) {
		coreSettings.errorThreshold = d;

	}

	public void setTreeData(TreeData treeData) {
		this.treeData = treeData;
		rootIndex = treeData.rootIndex;

		setTree(treeData.tree);
		root.setVoroPolygon(rootPolygon);

		// set names and weights
		if (treeData != null && treeData.nodeAtt != null) {
			for (Integer id : idToNode.keySet()) {
				VoroNode voroNode = idToNode.get(id);
				Node node = treeData.nodeAtt.get(id);
				if(node==null) continue;
				if (!getUniFormWeights())
					voroNode.setWeight(node.weight);
				voroNode.setName(node.name);
			}
		}

	}

	public void readEdgeList(String file) {
		try {
			TreeData data = IO.readEdgeList(file);
			setTreeData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getUniFormWeights() {
		return uniformWeights;
	}

	public void setUniformWeights(boolean considerWeights) {
		this.uniformWeights = considerWeights;
		if (uniformWeights)
			for (VoroNode node : idToNode.values())
				node.setWeight(1.0);
	}
}

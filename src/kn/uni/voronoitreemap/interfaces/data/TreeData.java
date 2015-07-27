package kn.uni.voronoitreemap.interfaces.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Class for storing tree data (or hierarchy) with a root.
 * @author Arlind Nocaj
 *
 */
public class TreeData {

	public ArrayList<ArrayList<Integer>> tree;	
	public Integer rootIndex;
		
	//map pointing to the node attributes of a specific id
	public HashMap<Integer, Node> nodeAtt;	
	
	
	public static class Node{
		
		public Node(){			
		}
		
		// id of the parent node
		public Integer parentId;
		// id of this node
		public Integer nodeId;
		// level of this node
		public Integer level;
		//name of this node
		public String name;
		// weight of this node
		public double weight;
		
		// relative coordinate of this node
		public Point2D.Double relativeCoord;
	}
	
	
	
}

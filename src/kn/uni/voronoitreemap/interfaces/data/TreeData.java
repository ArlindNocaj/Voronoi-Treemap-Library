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

	private void init(){
		tree=new ArrayList<ArrayList<Integer>>();
		rootIndex=0;
		nodeAtt=new HashMap<Integer, TreeData.Node>();
		nodeNameToId=new HashMap<String, Integer>();
	}
	
	//adjacency list of the tree structure
	public ArrayList<ArrayList<Integer>> tree;
	// index of the root node
	public Integer rootIndex;
		
	//map pointing to the node attributes of a specific id
	public HashMap<Integer, Node> nodeAtt;	
	
	private HashMap<String, Integer> nodeNameToId;
	
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
		public double weight=1;
		
		// relative coordinate of this node
		public Point2D.Double relativeCoord;
	}
	
	public void setRoot(String name){
		if(!nodeNameToId.containsKey(name)) return;
		rootIndex=nodeNameToId.get(name);
	}
	
	public void setWeight(String name,double weight){
		if(weight<=0) return;
		if(!nodeNameToId.containsKey(name)) return;		
		nodeAtt.get(nodeNameToId.get(name)).weight=weight;
	}
	
	public void addLink(String childName, String parentName){
		if(nodeNameToId==null)
			init();			
				
		Node childNode=null;
		Node parentNode=null;
		
		if(!nodeNameToId.containsKey(parentName))
			createNode(parentName);
		
		if(!nodeNameToId.containsKey(childName))
			createNode(childName);			
								
		childNode=nodeAtt.get(nodeNameToId.get(childName));
		parentNode=nodeAtt.get(nodeNameToId.get(parentName));
		
		childNode.parentId=parentNode.nodeId;
		tree.get(parentNode.nodeId).add(childNode.nodeId);
	}


	private Node createNode(String name) {
		int index=tree.size();
		Node node=new TreeData.Node();
		node.nodeId=index;	
		node.parentId=index;
		node.name=name;
		ArrayList<Integer> adjList=new ArrayList<Integer>();
		adjList.add(index);
		tree.add(adjList);
		nodeAtt.put(index, node);
		nodeNameToId.put(name, index);
		return node;
	}
	
}

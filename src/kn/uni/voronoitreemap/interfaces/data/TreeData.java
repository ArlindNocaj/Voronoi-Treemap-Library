package kn.uni.voronoitreemap.interfaces.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;


public class TreeData {

	public ArrayList<ArrayList<Integer>> tree;	
	public Integer rootIndex;
		
	public HashMap<Integer, Node> nodeAtt;	
	
	
	public static class Node{
		
		public Node(){
			
		}
		public Integer parentId;
		public Integer nodeId;
		public Integer level;
		public String name;
		public double weight;
		
		public Point2D.Double relativeCoord;
	}
	
	
	
}

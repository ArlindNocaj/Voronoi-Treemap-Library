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


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kn.uni.voronoitreemap.interfaces.data.TreeData;
public class IO {		

	private static int countLines(String filename)
			throws FileNotFoundException, IOException {
		BufferedReader reader =new BufferedReader(new FileReader(filename));
		int n=0;
		String line="";
		
		while (reader.ready()){
			line=reader.readLine();
			if (line==null) break;
			if (!(line.startsWith("*"))){
				n++;	
			}
		}
		reader.close();
		return n;
	}
	
	
	public static void main(String[] args){
		String file="examples/fujaba.txt";
		try {
			readEdgeList(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads an edge list format with additional attributes.
	 * Tree (hierachy format): "nodeId;parentId;name;weight";
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static TreeData readEdgeList(String filename) throws Exception {
			int numLines=countLines(filename);
			ArrayList<ArrayList<Integer>> treeAdj=new ArrayList<>(numLines);			
			
			BufferedReader reader= new BufferedReader(new FileReader(filename));									
			
			HashMap<String, Integer> nameToId=new HashMap<String, Integer>(numLines);
			HashMap<Integer, String[]> nodeEntry=new HashMap<Integer, String[]>(numLines);
			HashMap<Integer,Integer> parent=new HashMap<Integer, Integer>();
			
			HashMap<Integer,TreeData.Node> nodeAtt=new HashMap<>();
			
			String firstLine= reader.readLine();
			String[] columnHeader = firstLine.split(";");
//			int numCol=columnHeader.length;
			
			String weightCol="weight";
			
			
			String line;
			int id=0;
			Set<Integer> childSet=new HashSet<Integer>();
			while(reader.ready()){
				line=reader.readLine();
				if (line==null) break;
				if (line.startsWith("*")) continue;
				if (line.startsWith("#")) continue;
	
				String[] strings = line.split(";");
				String nodeName=strings[0];

				
				String parentName=strings[1];

				Integer nodeId=nameToId.get(nodeName);				
				if(nodeId==null){
					nodeId=id++;
					nameToId.put(nodeName, nodeId);
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(nodeId);
					treeAdj.add(adj);
					
				}
				Integer parentId = nameToId.get(parentName);
				if(parentId==null){
					parentId=id++;
					nameToId.put(parentName, parentId);
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(parentId);
					treeAdj.add(adj);
				}
				
				childSet.add(nodeId);									
				treeAdj.get(parentId).add(nodeId);
				parent.put(nodeId, parentId);
				nodeEntry.put(nodeId, strings);
				
			}
			reader.close();
			
			
			for(Integer key:nodeEntry.keySet()){
				if(!nodeAtt.containsKey(key)){
					TreeData.Node node=new TreeData.Node();
					node.parentId=parent.get(key);
					node.nodeId=key;					
					nodeAtt.put(key, node);
				}
				
			}
			
			int weightIndex=-1;
			for (int i = 0; i < columnHeader.length; i++) 
				if(columnHeader[i].equals(weightCol))
					weightIndex=i;
			
			for(Integer key:nodeEntry.keySet()){
				TreeData.Node node = nodeAtt.get(key);
				String[] strings=nodeEntry.get(key);
				double weight=1.0;
				if(weightIndex>=0){
				String w = strings[weightIndex];
				weight=Double.parseDouble(w);
				}
				node.weight=weight;				
			}
			
			Integer root=0;
			
			while(parent.get(root)!=null){
				root=parent.get(root);
			}						
			
			
			//set name for each node
			String nodeName="name";			
			int nameIndex=-1;
			for (int i = 0; i < columnHeader.length; i++) 
				if(columnHeader[i].equals(nodeName))
					nameIndex=i;
			for (Integer key: nodeEntry.keySet()) {
			String[] strings=nodeEntry.get(key);	
			String name="";
				if(nameIndex>=0 && strings!=null){
				name = strings[nameIndex];				
				}
				TreeData.Node node = nodeAtt.get(key);
				node.name=name;
			}
			
			System.out.println("Read nodes: # "+nodeAtt.keySet().size());
			
			TreeData data=new TreeData();
			data.tree=treeAdj;
			data.nodeAtt=nodeAtt;
			
			data.rootIndex=root;
			return data;
	}

}

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
import java.io.FileReader;
import java.util.ArrayList;

import kn.uni.voronoitreemap.interfaces.data.Tuple2ID;
import kn.uni.voronoitreemap.interfaces.data.Tuple3ID;


public class IO {

	/**
	 * Reads a list of two tuples, the first entry corresponds to an integer (id of the node) and the second corresponds to the value, e.g. desired area weighting of the node
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Tuple2ID> readWeights(String filename) throws Exception{
		
		try{
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
			
			ArrayList<Tuple2ID> data=new ArrayList<Tuple2ID>(n);
			reader=new BufferedReader(new FileReader(filename));
			
			
			while(reader.ready()){
				line=reader.readLine();
				if (line==null) break;
				if (line.startsWith("*")) continue;
	
				String[] strings = line.split(";");
				
				int id=Integer.parseInt(strings[0].trim());
				double value= Double.parseDouble(strings[1].trim());
				
				Tuple2ID tuple = new Tuple2ID(id, value);
				data.add(tuple);
			
			}
			reader.close();
			
			return data;
		} catch (Exception e){
			throw e;
		}
	
		
	}
	
	/**
	 * Reads an id and two values per line
	 * @param filename
	 * @return
	 * @throws Exception
	 */
public static ArrayList<Tuple3ID> readRelativeVector(String filename) throws Exception{
		
		try{
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
			
			ArrayList<Tuple3ID> data=new ArrayList<Tuple3ID>(n);
			reader=new BufferedReader(new FileReader(filename));
			
			
			while(reader.ready()){
				line=reader.readLine();
				if (line==null) break;
				if (line.startsWith("*")) continue;
	
				String[] strings = line.split(";");
				
				int id=Integer.parseInt(strings[0].trim());
				double value1= Double.parseDouble(strings[1].trim());
				double value2= Double.parseDouble(strings[2].trim());
				Tuple3ID tuple = new Tuple3ID(id, value1,value2);
				data.add(tuple);
			
			}
			reader.close();
			
			return data;
		} catch (Exception e){
			throw e;
		}
	
		
	}
	
	/**
	 * Reads an adjacency list and stores it by using ArrayLists.
	 * e.g. 
	 * 0;1;2;3
	 * 2;5;6
	 * 
	 * means that node 0 as children 1,2,3 and node 2 has children 5,6
	 *
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<ArrayList<Integer>> readTree(String filename) throws Exception {
		try{
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
			
			ArrayList<ArrayList<Integer>> data=new ArrayList<ArrayList<Integer>>(n);
			reader=new BufferedReader(new FileReader(filename));
			
			
			while(reader.ready()){
				line=reader.readLine();
				if (line==null) break;
				if (line.startsWith("*")) continue;
	
				String[] strings = line.split(";");
				
				ArrayList<Integer> list = new ArrayList<Integer>(strings.length);
				for (int i=0;i<strings.length;i++){
					int value=Integer.parseInt(strings[i].trim());
					list.add(value);
				}
				data.add(list);
			}
			reader.close();
			
			return data;
		} catch (Exception e){
			throw e;
		}
		
	}
}

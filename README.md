Java Voronoi Treemap Library
=====================

*Voronoi Treemap Library* is a fast standalone java library which computes Voronoi Treemaps.

The following article contains most important references related to this implementation.


* Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864

Note that the implementation used for the article is a different one, but the runtime should be approx. the same.


Usage
-------------

First, you need a StatusObject which is able to handle the results when they are available.
```java
public class StatusObjectImpl implements kn.uni.voronoitreemap.interfaces.StatusObject{
  public void finishedNode(int node, int layer, int[] children, PolygonSimple[] polygons){
    System.out.println("finished node No. " + node + " in layer: " + layer + " with children: " + Arrays.toString(children));
    for(PolygonSimple poly : polygons){
      System.out.println(poly.getArea());
    }
  }
  public void finished(){
    System.out.println("Computation is finished!");
  }
}
```

The second step is to get an instance of an Object implementing the Voronoi Treemap Interface and to set the hierarchy data and the StatusObject to handle the results.

```Java
//Create the root polygon(it has to be convex):
PolygonSimple root = new PolygonSimple(4);
root.add(0,0);
root.add(800,0);
root.add(800,800);
root.add(0,800);

//Create a StatusObject of your implementation
StatusObjectImpl statusOjbect = new StatusObjectImpl();

//Get the voronoi treemap with the your status object as parameter and whether you want to use multithreaded computation
VoronoiTreemapInterface voronoiTreemap = MainClass.getInstance(statusObject,true);
 
//Set root polygon
voronoiTreemap.setRootPolygon(root);

//Insert the tree structure with the format List<List<Integer>> as an adjacency list:
//E.g. if a node 1 has children 3,7 and 9 the List must have an entry (1,3,7,9)


List<List<Integer> treeList = new ArrayList<List<Integer>>();
treeList.add(new ArrayList<Integer> (Arrays.asList(1,2,3)));
treeList.add(new ArrayList<Integer> (Arrays.asList(2,4,5)));
treeList.add(new ArrayList<Integer> (Arrays.asList(3,6,7,8)));
voronoiTreemap.setTree(treeList);

//Optional set AreaGoals (=weighting for each node which influences the final area the polygon of a cell will have)
//(can only be set after the tree structure is defined (setTree(...)))
//Format: Tuple2ID with (NodeId, weight) e.g. Node 1 (1,0.55)
List<Tuple2ID> areaGoals = new ArrayList<Tuple2ID>();
areaGoals.add(new Tuple2ID(1,1.0));
areaGoals.add(new Tuple2ID(2,4.0));
areaGoals.add(new Tuple2ID(3,1.0));
areaGoals.add(new Tuple2ID(4,9));
areaGoals.add(new Tuple2ID(5,1));
areaGoals.add(new Tuple2ID(6,0.05));
areaGoals.add(new Tuple2ID(7,0.10));
areaGoals.add(new Tuple2ID(8,0.85));
voronoiTreemap.setAreaGoals(areaGoals);
 
//Now compute the voronoi treemap
voronoiTreemap.compute();

//Handle the results in statusObject.finished() or respectively in statusObject.finishedNode(int node, int layer, int[] children, PolygonSimple[] polygons)
```

If you want to lock the caller until the computation is finished use **voronoiTreemap.computeLocked()** instead of **voronoiTreemap.compute()**

The tree list structure above looks as follows:
\`*1*
\`|-- *2*
\`|    |-- *4* 
\`|    |-- *5*
\`| 
\`|-- *3*
\`		 |-- *6*
\`		 |-- *7*
\`		 |-- *8*

License
------------------------

Copyright (c) 2013 Arlind Nocaj, University of Konstanz.

All rights reserved. This program and the accompanying materials are made available under the terms of the GNU Public License v3.0 which accompanies this distribution, and is available at http://www.gnu.org/licenses/gpl.html

For distributors of proprietary software, other licensing is possible on request (with University of Konstanz): <arlind.nocaj@gmail.com>


Citation
-----------------

This work is based on the publication below, please cite on usage:

* Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864

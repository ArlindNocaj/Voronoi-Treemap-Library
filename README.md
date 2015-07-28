Java Voronoi Treemap Library
=====================

*JVoroTreemap* is a fast standalone java library which computes Voronoi treemaps.

The following article contains most important references related to this implementation.


* Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864

Note that the implementation used for the article is a different one, but the runtime should be approx. the same.

There are two possibilities to use this library:

1. Minimalistic command line interface using JVoroTreemap.jar.
2. Direct usage over the source code.


How to build a Voronoi Treemap Jar:
----
Clone a copy of the git repo by running:

```
git clone --recursive https://github.com/ArlindNocaj/Voronoi-Treemap-Library.git
```

Make sure you have Java and Gradle installed (tested with Java 7 and Gradle 2.4):
``
java -version
``
and
``
gradle -v
``

Change to the repo folder and use gradle for building:

```
cd Voronoi-Treemap-Library
gradle build
```
The result will be: ``` build/libs/JVoroTreemap.jar ```



How to use with command line
----
The final Jar can be used to compute Voronoi treemaps from an input file using a rudimentary command line interface:

```
java -jar build/libs/JVoroTreemap.jar examples/fujaba.txt
```
The result will be a PNG image (``fujaba.png``) of the resulting Voronoi treemap visualization together with an output file (``fujaba-finished.txt``) containing the same tree structure as the input file, but in addition to that the polygon and site coordinates of the Voronoi treemap.

PDF file generation is also supported but should be used with caution as the resulting files can be very large:

```
java -jar build/libs/JVoroTreemap.jar -pdf examples/fujaba.txt
```

The folder structure of a file system directory can be extracted and used for Voronoi treemap generation with the `` -d`` option:

```
java -jar build/libs/JVoroTreemap.jar -d ~/Desktop
```


How to use the library
-------------
For simple hierarchy as the following:

(same structure is given in ``examples/miniHierarchy.txt``)

```
 project
│   README.md
│   file001
│
│───folder1
│    │   file011
│    │   file012
│    │
│    ├───subfolder1
│    │   │   file111
│    │   │   file112
│    │   │   ...
│    │
│    └───folder2
│    │   file021
│    │   file022
│
└───folder3
│    │   file031
│    │   file032
│    │   file033
│    │   file034
│    │   file035
│    │   file036
```
we get

![mini hierarchy](https://github.com/ArlindNocaj/Voronoi-Treemap-Library/raw/master/examples/miniHierarchy.png)

when using the following code (see ``kn.uni.voronoitreemap.interfaces.Sample1.java``):
```java
    // create a convex root polygon
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

    // create hierarchical structure
    TreeData data = new TreeData();
    data.addLink("README.md", "project");
    data.addLink("file001", "project");
    data.setRoot("project");

    data.addLink("folder1", "project");
    data.addLink("file011", "folder1");
    data.addLink("file012", "folder1");

    data.addLink("subfolder1", "folder1");
    data.addLink("file111", "subfolder1");
    data.addLink("file112", "subfolder1");
    data.addLink("...", "subfolder1");

    data.addLink("folder2", "folder1");
    data.addLink("file021", "folder2");
    data.addLink("file022", "folder2");

    data.addLink("folder3", "project");
    data.addLink("file031", "folder3");
    data.addLink("file032", "folder3");
    data.addLink("file033", "folder3");
    data.addLink("file034", "folder3");
    data.addLink("file035", "folder3");
    data.addLink("file036", "folder3");


    // increases the size of the corresponding cell
    // data.setWeight("file036", 4);

    // VoronoiCore.setDebugMode(); //shows iteration process
    VoronoiTreemap treemap = new VoronoiTreemap();
    treemap.setRootPolygon(rootPolygon);
    treemap.setTreeData(data);
    treemap.setCancelOnMaxIteration(true);
    treemap.setNumberMaxIterations(1500);
    treemap.setCancelOnThreshold(true);
    treemap.setErrorAreaThreshold(0.01);
    // treemap.setUniformWeights(true);
    treemap.setNumberThreads(1);

    //add result handler
    treemap.setStatusObject(new PNGStatusObject("miniHierarchy", treemap));
    treemap.setStatusObject(new PDFStatusObject("miniHierarchy", treemap));
    treemap.computeLocked();
```

License
------------------------

Copyright (c) 2015 Arlind Nocaj, University of Konstanz.

All rights reserved. This program and the accompanying materials are made available under the terms of the GNU Public License v3.0 which accompanies this distribution, and is available at http://www.gnu.org/licenses/gpl.html

For distributors of proprietary software, other licensing is possible on request (with University of Konstanz): <arlind.nocaj@gmail.com>


Citation
-----------------

This work is based on the publication below, please cite on usage:

* Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864

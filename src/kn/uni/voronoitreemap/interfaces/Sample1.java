package kn.uni.voronoitreemap.interfaces;

import kn.uni.voronoitreemap.IO.PDFStatusObject;
import kn.uni.voronoitreemap.IO.PNGStatusObject;
import kn.uni.voronoitreemap.IO.WriteStatusObject;
import kn.uni.voronoitreemap.interfaces.data.TreeData;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;

public class Sample1 {

	public static void main(String[] args) {
		// create a convex root polygon
		PolygonSimple rootPolygon = new PolygonSimple();
		int width = 500;
		int height = 200;
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

	}

}

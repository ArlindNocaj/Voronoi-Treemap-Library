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
package kn.uni.voronoitreemap.renderer;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;

import kn.uni.voronoitreemap.gui.Colors;
import kn.uni.voronoitreemap.gui.JPolygon;
import kn.uni.voronoitreemap.interfaces.VoronoiTreemapInterface;
import kn.uni.voronoitreemap.j2d.Point2D;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.treemap.VoroNode;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;


/**
 * Renderer which should draw the polygons of a Voronoi Treemap into a Graphics2D.
 * Mainly used for debugging and testing.
 * @author nocaj
 *
 */

public class VoroRenderer {
	Graphics2D g;

	protected VoronoiTreemapInterface treemap;

	private JLayeredPane layeredPane;

	private BufferedImage bufferImage;

	public VoroRenderer() {
		init();
	}

	private void init() {
		layeredPane = new JLayeredPane();
	}

	public void setTreemap(VoronoiTreemapInterface treemap) {
		this.treemap = treemap;
	}

	public VoronoiTreemapInterface getTreemap() {
		return treemap; 
	}
	public void setGraphics2D(Graphics2D graphics){
		this.g=graphics;
	}
	public void renderTreemap(String filename) {
		PolygonSimple rootPolygon = treemap.getRootPolygon();
		Rectangle rootRect = rootPolygon.getBounds();
		if (g==null){
			bufferImage = new BufferedImage(rootRect.width, rootRect.height, BufferedImage.TYPE_INT_ARGB);
			g=bufferImage.createGraphics();
		}
		double translateX=-rootRect.getMinX();
		double translateY=-rootRect.getMinY();
		g.translate(translateX, translateY);
		
		
		
//		JFrame frame = new JFrame();
//		frame.setVisible(true);
//		frame.setSize(1500, 1000);
//		frame.getContentPane().add(layeredPane);
//		layeredPane.setVisible(true);
		// Ask the test to render into the SVG Graphics2D implementation.
		LinkedList<VoroNode> nodeList = new LinkedList<VoroNode>();
		for (VoroNode child : (VoronoiTreemap)treemap) {
//			JPolygon2 jp = new JPolygon2(child.getNodeID(), new Integer(child.getNodeID()).toString());
//			layeredPane.add(jp, -child.getHeight());
//
//			jp.setVisible(true);
			nodeList.add(child);
		}
		System.out.println("Elements:"+nodeList.size());
		// svgGraphics.setBackground(Color.black);
		// svgGraphics.clearRect(bb.x,bb.y,bb.width,bb.height);
		//svgGraphics.setColor(Color.black);
//		svgGraphics.setColor(new Color(245,87,0)); // orange
		g.setColor(new Color(90,180,172)); // orange
		g.setColor(Colors.getColors().get(0));
		g.fill(rootPolygon);
//		layeredPane.setSize(5000, 5000);
		layeredPane.setVisible(true);
		layeredPane.paintAll(g);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	
		
		for(VoroNode child:nodeList){
			PolygonSimple poly = child.getPolygon();
			if(poly==null) {
				System.out.println("no poly "+child.getNodeID());
				continue;
			}
			Color fillColor=Colors.getColors().get(Math.min(child.getHeight(),Colors.getColors().size()-1));
			g.setColor(fillColor);
			g.drawPolygon(poly.getXpointsClosed(), poly.getYpointsClosed(), poly.length+1);			
		}
		
//		for(VoroNode child:nodeList){			
//			PolygonSimple poly = child.getPolygon();
//			if(poly==null) {
//				System.out.println("no poly "+child.getNodeID());
//				continue;
//			}
//			Point2D center = poly.getCentroid();
//			
//			g.setColor(Color.black);
//			g.drawString(new Integer(child.getHeight()).toString(), (int)center.x, (int)center.y);
//		}
				
		try {
			ImageIO.write(bufferImage, "png", new File(filename+".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

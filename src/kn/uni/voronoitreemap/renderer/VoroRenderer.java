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
	Graphics2D graphics;

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
		this.graphics=graphics;
	}
	public void renderTreemap(String filename) {
		PolygonSimple rootPolygon = treemap.getRootPolygon();
		Rectangle rootRect = rootPolygon.getBounds();
		if (graphics==null){

			bufferImage = new BufferedImage(rootRect.width, rootRect.height, BufferedImage.TYPE_INT_ARGB);
			graphics=bufferImage.createGraphics();
		}
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
		graphics.setColor(new Color(90,180,172)); // orange
		graphics.setColor(Colors.getColors().get(0));
		graphics.fill(rootPolygon);
//		layeredPane.setSize(5000, 5000);
		layeredPane.setVisible(true);
		layeredPane.paintAll(graphics);

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	
		// paint the borders in the different order
		while (nodeList.size() > 0) {
			VoroNode child = nodeList.removeFirst();
			// draw border
			JPolygon jpolygon = new JPolygon(child.getPolygon(), child.getNodeID());
			PolygonSimple polygonNew = child.getPolygon();
			PolygonSimple oldPolygon = polygonNew.getOriginalPolygon();

			if (oldPolygon==null){
				oldPolygon=polygonNew;
			}
			
			//svgGraphics.draw(oldPolygon);
			//draw border of polygon
			int length = oldPolygon.getNumPoints();
			double[] x = oldPolygon.getXPoints();
			double[] y = oldPolygon.getYPoints();
			int tx = jpolygon.getLocation().x;
			int ty = jpolygon.getLocation().y;
			
			tx=0;
			ty=0;
			
			/**
			 * Fill Polygon
			 */
			int[] x2=new int[length+1];
			int[] y2=new int[length+1];
			for (int i = 0; i <= length; i++){
				x2[i]=(int) x[i]-tx;
				y2[i]=(int) y[i]-ty;
			}
			Color fillColor = jpolygon.getFillColor().darker();
			
			fillColor=Colors.getColors().get(Math.min(child.getHeight(),Colors.getColors().size()-1));
			graphics.setColor(fillColor);
			graphics.drawPolygon(x2, y2, length);
			
			if (child.getHeight()==2){
//				fillColor=fillColor.darker();
//				fillColor=fillColor.darker();
			}
			fillColor=fillColor.darker();
//			g.setColor(Colors.getColors().get(0));
//			g.setColor(new Color(fillColor.getRed(),fillColor.getGreen(),fillColor.getBlue(),80));			
			graphics.setColor(new Color(fillColor.getRed(),fillColor.getGreen(),fillColor.getBlue(),255));
			
			float strokeWidth=15;
			
			if (child.getHeight()==2){
				strokeWidth=11;
			}else{
			
			strokeWidth=(float) (strokeWidth* Math.pow(0.37, child.getHeight()-1));
			}
//			//draw border of polygon
//			graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
//			for (int i = 0; i < (length-1); i++){
//				graphics.drawLine(x2[i], y2[i], x2[i + 1], y2[i + 1]);
//			}
//			graphics.drawLine(x2[length-1], y2[length-1], x2[0], y2[0]);
			
			
		}
		try {
			ImageIO.write(bufferImage, "png", new File(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

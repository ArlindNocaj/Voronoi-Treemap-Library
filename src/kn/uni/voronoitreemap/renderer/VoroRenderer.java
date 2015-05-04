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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import kn.uni.voronoitreemap.gui.Colors;
import kn.uni.voronoitreemap.gui.JPolygon;
import kn.uni.voronoitreemap.helper.InterpolColor;
import kn.uni.voronoitreemap.interfaces.VoronoiTreemapInterface;
import kn.uni.voronoitreemap.j2d.Point2D;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.treemap.VoroNode;
import kn.uni.voronoitreemap.treemap.VoronoiTreemap;

/**
 * Renderer which should draw the polygons of a Voronoi Treemap into a
 * Graphics2D. Mainly used for debugging and testing.
 * 
 * @author nocaj
 * 
 */

public class VoroRenderer {

	boolean drawNames = true;
	Graphics2D g;

	protected VoronoiTreemap treemap;

	private JLayeredPane layeredPane;

	private BufferedImage bufferImage;

	public VoroRenderer() {
		init();
	}

	private void init() {
		layeredPane = new JLayeredPane();
	}

	public void setTreemap(VoronoiTreemap treemap) {
		this.treemap = treemap;
	}

	public VoronoiTreemapInterface getTreemap() {
		return treemap;
	}

	public void setGraphics2D(Graphics2D graphics) {
		this.g = graphics;
	}

	public void renderTreemap(String filename) {
		PolygonSimple rootPolygon = treemap.getRootPolygon();
		Rectangle rootRect = rootPolygon.getBounds();

		if (g == null) {
			bufferImage = new BufferedImage(rootRect.width, rootRect.height,
					BufferedImage.TYPE_INT_ARGB);
			g = bufferImage.createGraphics();
		}
		double translateX = -rootRect.getMinX();
		double translateY = -rootRect.getMinY();
		g.translate(translateX, translateY);

		// JFrame frame = new JFrame();
		// frame.setVisible(true);
		// frame.setSize(1500, 1000);
		// frame.getContentPane().add(layeredPane);
		// layeredPane.setVisible(true);
		// Ask the test to render into the SVG Graphics2D implementation.
		int maxHeight = 0;
		LinkedList<VoroNode> nodeList = new LinkedList<VoroNode>();
		LinkedList<VoroNode> nodeListReverse = new LinkedList<VoroNode>();
		for (VoroNode child : (VoronoiTreemap) treemap) {
			// JPolygon2 jp = new JPolygon2(child.getNodeID(), new
			// Integer(child.getNodeID()).toString());
			// layeredPane.add(jp, -child.getHeight());
			//
			// jp.setVisible(true);
			if (child.getPolygon() != null) {
				nodeList.add(child);
				nodeListReverse.addFirst(child);
				if (child.getHeight() > maxHeight) {
					maxHeight = child.getHeight();
				}
			}
		}

		System.out.println("Elements:" + nodeList.size());
		// svgGraphics.setBackground(Color.black);
		// svgGraphics.clearRect(bb.x,bb.y,bb.width,bb.height);
		// svgGraphics.setColor(Color.black);
		// svgGraphics.setColor(new Color(245,87,0)); // orange
		g.setColor(new Color(90, 180, 172)); // orange
		g.setColor(Colors.getColors().get(0));
		g.fill(rootPolygon);
		// layeredPane.setSize(5000, 5000);
		layeredPane.setVisible(true);
		layeredPane.paintAll(g);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// draw polygon border
		InterpolColor grayGetDarker = new InterpolColor(2, maxHeight, 0, 0, 0.5, 0,
				0, 1.0);

		InterpolColor grayGetBrighter = new InterpolColor(2, maxHeight, 0, 0, 1.0, 0,
				0, 0.5);
		
		Random rand=new Random(1);
		
		// fill polygon
		for (VoroNode child : nodeList) {
			PolygonSimple poly = child.getPolygon();
			Color fillColor = Colors.getColors().get(
					Math.min(child.getHeight(), Colors.getColors().size() - 1));

			g.setColor(fillColor);
			g.fillPolygon(poly.getXpointsClosed(), poly.getYpointsClosed(),
					poly.length + 1);						
			
		}

	//draw border in reverse order
		for (VoroNode child : nodeListReverse) {
			PolygonSimple poly = child.getPolygon();
//			Color col = grayScale.getColorLinear(child.getHeight());
			Color col = grayGetBrighter.getColorLinear(child.getHeight());
			double width = 5 * (1.0 / child.getHeight());
			g.setStroke(new BasicStroke((int) width));
			g.setColor(col);
			g.drawPolygon(poly.getXpointsClosed(), poly.getYpointsClosed(),
					poly.length + 1);
		}

		// draw text
		for (VoroNode child : nodeList) {
			if(child.getHeight()<=1) continue;
			if(child.getHeight()>3) continue;
			if(child.getHeight()==3 && rand.nextDouble()<0.50) continue;
						
			 g.setColor(grayGetDarker.getColorLinear(child.getHeight(),150));
			drawName(child,g);
	
		}

		
if(filename!=null){
		try {
			ImageIO.write(bufferImage, "png", new File(filename + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

	}

	private void drawName(VoroNode child, Graphics2D g) {
		// draw name
		PolygonSimple poly = child.getPolygon();
		if(poly==null) return;
		
		Point2D center = poly.getCentroid();
		String name = child.getName();
		int maxChar=10;
		if(name.length()>maxChar)
			name=name.substring(0, 10);
		
		Font res = scaleFont(name, poly, g, g.getFont());
		if(res==null) return;
		g.setFont(res);
		FontMetrics fm = g.getFontMetrics(res);
		Rectangle2D bounds = fm.getStringBounds(name, g);
		
		g.drawString(name, (int) (center.x - bounds.getWidth() / 2.0), (int) (center.y));
	}

	// public Font scaleFont(String text, Rectangle rect, Graphics2D g, Font
	// pFont) {
	// float fontSize = 20.0f;
	// Font font = pFont;
	//
	// font = g.getFont().deriveFont(fontSize);
	// int width = g.getFontMetrics(font).stringWidth(text);
	// fontSize = (rect.width / width ) * fontSize;
	// return g.getFont().deriveFont(fontSize);
	// }

	public Font scaleFont(String text, Rectangle rect, Graphics2D g, Font pFont) {
		float nextTry = 100.0f;
		Font font = pFont;

		while (true) {
			font = g.getFont().deriveFont(nextTry);
			FontMetrics fm = g.getFontMetrics(font);
			int width = fm.stringWidth(text);
			if (width <= rect.width)
				return font;
			nextTry *= .8;
		}
		// return font;
	}

	public Font scaleFont(String text, PolygonSimple poly, Graphics2D g,
			Font pFont) {
		float nextTry = 100.0f;
		Font font = pFont;
		Point2D center = poly.getCentroid();
//		int x=0;
		while (true) {
			font = g.getFont().deriveFont(nextTry);
			FontMetrics fm = g.getFontMetrics(font);
			Rectangle2D bounds = fm.getStringBounds(text, g);
			// int width=fm.stringWidth(text);
			double cx = center.x - bounds.getWidth() * 0.5;
			double cy = center.y - bounds.getHeight() * 0.5;
			Rectangle2D.Double rect = new Rectangle2D.Double(cx, cy,
					bounds.getWidth(), bounds.getHeight());
			if (poly.contains(rect))
				// if(width <= rect.width)
				return font;
			nextTry *= .8;
		}
//		return null;
	}

}

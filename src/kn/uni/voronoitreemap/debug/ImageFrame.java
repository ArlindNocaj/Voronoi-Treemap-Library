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
package kn.uni.voronoitreemap.debug;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import kn.uni.voronoitreemap.core.VoronoiCore;

/**
 * JFrame with a buffered image.
 * @author Arlind Nocaj
 *
 */

public class ImageFrame extends JFrame{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private ConcurrentLinkedQueue<VoronoiCore> coreList;


	public ImageFrame(BufferedImage image){
		super();
		this.image=image;
		
		

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(800, 600);

        setVisible(true); 
		   
	}
	
	 class DrawPane extends JPanel{		 
		private ConcurrentLinkedQueue<VoronoiCore> list;
		public DrawPane(ConcurrentLinkedQueue<VoronoiCore> coreList){
			this.list=coreList;
		 }
	        public void paintComponent(Graphics g2){	        	
	        	Graphics2D g = (Graphics2D)g2;
	        	if(list==null) return;
	        	
	        	g.clearRect(-2000, -2000, 5000, 5000);
	    		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	    				RenderingHints.VALUE_ANTIALIAS_ON);
	        	for(VoronoiCore core:list){	        		
	          //draw on g here e.g.	       	        		
	    			core.drawState((Graphics2D)g, false);
	        	}
	         }
	     }


	
//	@Override
//	public void paint(Graphics g) {
//		super.paint(g);
////		g.drawImage(image,0,0,this);
//	}
	
	public synchronized void setVoroCore(VoronoiCore core){
		boolean first=false;
					
		this.getContentPane().validate();
		this.getContentPane().repaint();
		if(coreList==null){
		coreList=new ConcurrentLinkedQueue<VoronoiCore>();
        setContentPane(new DrawPane(coreList));
		Rectangle bounds = core.getClipPolyogon().getBounds();
		setSize((int)bounds.getWidth(), (int)bounds.getHeight());
		setVisible(true); 
		}
		coreList.clear();
		coreList.add(core);
	    
	}



	public void repaintWithWait(int i) {
	
		this.getContentPane().validate();
		this.getContentPane().repaint();
//		try {
//			Thread.sleep(i);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
	 
	}
	

	
	
}

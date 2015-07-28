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
package kn.uni.voronoitreemap.treemap;


import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import kn.uni.voronoitreemap.interfaces.StatusObject;



/**
 * Uses a CPU completely(all kernels) for calculation of the voronoi cells. As soon as the
 * queue is not empty it starts calculating.
 * 
 * @author Arlind Nocaj
 * 
 */
public class VoroCPU extends Thread {
	public Set<VoroCPU> runningThreads;

	protected int allNodes=0;
	
	
	public synchronized void stopThreads() {
		for (VoroCPU v : runningThreads) {
			v.interrupt();
		}
	}

	private BlockingQueue<VoroNode> cellQueue;
	private StatusObject tellEnd;

	VoroCPU(BlockingQueue<VoroNode> queue, StatusObject tellEnd, Set<VoroCPU> runningThreads) {
		this.tellEnd = tellEnd;
		this.cellQueue = queue;
		this.runningThreads=runningThreads;
	}

	@Override
	public void run() {
		while (true) {
			try {
				VoroNode voroNode = cellQueue.poll();
				if (voroNode == null) {
					// notify that we are finished
					if (runningThreads.size() == 0 && cellQueue.size() == 0) {
					break;
					}else{
						voroNode = cellQueue.poll(100, TimeUnit.MILLISECONDS);
						if (voroNode == null){
							continue;
						}
					}
				}
				runningThreads.add(this);				 
				voroNode.iterate();				
				tellEnd.finishedNode(voroNode.getNodeID(), voroNode.getHeight(),voroNode.getChildrenIDs(),voroNode.getChildrenPolygons());
				ArrayList<VoroNode> children = voroNode.getChildren();
				if (children!=null){
				for (VoroNode node : children) {
						cellQueue.add(node);
					}
				}
				runningThreads.remove(this);
				if (runningThreads.size() == 0 && cellQueue.size() == 0 && (voroNode.getChildren()==null || voroNode.getChildren().size()==0 )) {
					tellEnd.finished();
				}
			} catch (Exception e) {
				e.printStackTrace();
				cellQueue.clear();
				runningThreads.remove(this);
				System.out.println("VoroCPU is stopped.");;
			}
		}
	}

}

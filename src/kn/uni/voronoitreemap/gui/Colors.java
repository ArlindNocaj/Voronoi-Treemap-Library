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
package kn.uni.voronoitreemap.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Arlind Nocaj
 *
 */
public class Colors {
public static List<Color> getColors(){
	
//	ArrayList<Color> colors=new ArrayList<Color>();
//	colors.add(new Color(247,251,255));
//	colors.add(new Color(222,235,247));
//	colors.add(new Color(198,219,239));
//	colors.add(new Color(158,202,225));
//	colors.add(new Color(107,174,214));
//	colors.add(new Color(66,146,198));
//	colors.add(new Color(33,113,181));
//	colors.add(new Color(8,81,156));
//	colors.add(new Color(8,48,107));

	int alpha=50;
	ArrayList<Color> colors=new ArrayList<Color>();
//	colors.add(Color.gray);
//	colors.add(new Color(255,255,255,100));
	colors.add(new Color(247,251,255,alpha));
	colors.add(new Color(222,235,247,alpha));
	colors.add(new Color(198,219,239,alpha));
	colors.add(new Color(158,202,225,alpha));
	colors.add(new Color(107,174,214,alpha));
	colors.add(new Color(66,146,198,alpha));
	colors.add(new Color(33,113,181,alpha));
	colors.add(new Color(8,81,156,alpha));
	colors.add(new Color(8,48,107,alpha));
//	Collections.reverse(colors);
	return colors;
}

public static List<Color> getColorsRed(){
	
	ArrayList<Color> colors=new ArrayList<Color>();
	colors.add(new Color(255, 255, 178));
	colors.add(new Color(255, 255, 178));
	colors.add(new Color(254, 204, 92));
//	colors.add(new Color(253, 141, 60));
	colors.add(new Color(240, 59, 32));
	colors.add(new Color(189, 0, 38));
colors.add(new Color(189, 0, 38).darker());
	
	return colors;
}

}

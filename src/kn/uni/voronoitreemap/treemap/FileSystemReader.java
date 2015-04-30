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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Reader to extract a hierarchy out of a given entrypoint in the filesystem. This hierarchy could then e.g. visualized as a Voronoi Treemap where the area corresponds to the file/folder size.
 * @author Arlind Nocaj
 *
 */
public class FileSystemReader {
	Integer nodeID=1;
	private String exportFile;
	private String directory;
	private BufferedWriter writer;
	public FileSystemReader(String directory, String exportFile){
		this.directory=directory;
		this.exportFile=exportFile;
	}
	
	public void listDir(File dir, int myID) throws IOException {

		File[] files = dir.listFiles();
			if (files==null || files.length<1) return;
		int startID=nodeID;
		writer.write(new Integer(myID).toString());
		
			for (int i = 0; i < files.length; i++) {
//				System.out.println(";"+nodeID);
				
				writer.write(";"+nodeID);
				nodeID++;
			}
			writer.write("\n");
			for (int i=0;i<files.length;i++){
				if (files[i].isDirectory()) {
//					System.out.print(" (Ordner)\n");
					listDir(files[i],startID+i+1); // ruft sich selbst mit dem 
						// Unterverzeichnis als Parameter auf
					}
				else {
					
//					System.out.print(" (Datei)\n");
				}

			}
		}
	
	
	public static void main(String args[]){
		
		FileSystemReader reader=new FileSystemReader("/home/nocaj/Documents", "documents.txt");
		reader.createTreeFile();
		
	}

	private void createTreeFile() {
		try {
			
			this.writer=new BufferedWriter(new FileWriter(exportFile));
			listDir(new File(directory), 0);
		writer.flush();
		writer.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

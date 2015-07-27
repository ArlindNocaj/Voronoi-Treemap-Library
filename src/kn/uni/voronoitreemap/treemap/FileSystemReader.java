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
 * Reader to extract a hierarchy out of a given entrypoint in the filesystem.
 * This hierarchy could then e.g. visualized as a Voronoi Treemap where the area
 * corresponds to the file/folder size.
 * 
 * @author Arlind Nocaj
 *
 */
public class FileSystemReader {
	Integer nodeCount = 0;
	private String exportFile;
	private String directory;
	private BufferedWriter writer;

	public FileSystemReader(String directory) {		
		if (directory.startsWith("~" + File.separator)) {
			directory = System.getProperty("user.home") + directory.substring(1);
		}
		
		this.directory = directory;
		this.setExportFile("VT-" + new File(directory).getName() + ".txt");
	}

	public void listDir(File dir, int parentId) throws IOException {
//		System.out.println(dir.getName()+"\t"+parentId);
		File[] files = dir.listFiles();
		if (files == null || files.length < 1)
			return;
		for (File f : files) {
			if(!f.exists()) continue;
			int nodeId = ++nodeCount;
			String line = getLine(f, nodeId, parentId);
			writer.write(line + "\n");
			if (f.isDirectory())
				listDir(f, nodeId);
		}
	}

	public static void main(String args[]) {		
		FileSystemReader reader = new FileSystemReader("~/");		
		reader.createTreeFile();
	}

	public String getLine(File file, int nodeId, int parentId) {
		if (!file.exists())
			return null;
		// "nodeId,parrentId,name,size,createdDate,modifiedDate";
		double length = 0;
		if (file.isFile())
			length = file.length();
		
		length=Math.max(length, 1);
		length=Math.log(length)+1;		
		String line = nodeId + ";" + parentId + ";" + file.getName().replace(";", "") + ";"
				+ length;
		
		return line;
	}

	public void createTreeFile() {
		try {
			File expFile=new File(getExportFile());
			if(expFile.exists()) expFile.delete();
			
			this.writer = new BufferedWriter(new FileWriter(getExportFile()));
			String header="nodeId;parentId;name;weight";
			writer.write(header+"\n");
			nodeCount = 0;
			listDir(new File(directory), nodeCount);
			writer.flush();
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getExportFile() {
		return exportFile;
	}

	public void setExportFile(String exportFile) {
		this.exportFile = exportFile;
	}
}

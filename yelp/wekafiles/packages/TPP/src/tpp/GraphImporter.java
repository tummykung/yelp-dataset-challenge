package tpp;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;

import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class GraphImporter {
	
	private static final CSVFileFilter FILE_FILTER = new CSVFileFilter();

	/** The default directory for file operations */
	private static final String DEFAULT_DIRECTORY = ".";
	
	private Graph graph;

	public Graph importGraph() throws Exception {
		
				
		String instancesFileName;
		
		JFileChooser chooser = new JFileChooser(DEFAULT_DIRECTORY);
		chooser.setFileFilter(FILE_FILTER);
		
		int returnVal = chooser.showOpenDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
			instancesFileName = chooser.getSelectedFile().getPath();
		else
			return null;

		// Read data from file
		System.out.println("Reading graph data from file " + instancesFileName);
		
		File selectedFile = chooser.getSelectedFile();
		
		Scanner fileScanner = new Scanner(selectedFile);
			graph = new Graph();
			// read the each line separately 
			while(fileScanner.hasNextLine()){
				readLine(fileScanner.nextLine());
			}
		return graph;	
	}

	private void readLine(String aLine) {
		
		// String nodeA = null;
		// String nodeB = null;
		Scanner lineScanner = new Scanner(aLine).useDelimiter(";");
		
		String nodeA = lineScanner.next();
		
		while(lineScanner.hasNext()){
			String nodeB = lineScanner.next();
			Connection cnxn  = new Connection(nodeA, nodeB);
			graph.add(cnxn);
		}
		
	}
	
	

}

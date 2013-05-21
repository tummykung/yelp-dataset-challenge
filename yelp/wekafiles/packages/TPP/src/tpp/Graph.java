package tpp;

import java.util.ArrayList;

import weka.core.Instances;

public class Graph {
		
	private ArrayList<Connection> cnxns;
	
	public Graph(){
			
		cnxns = new ArrayList<Connection>();
		
	}
	
	public void add(Connection cnxn) {
		cnxns.add(cnxn);
				
	}
	
	public Graph getGraph(){
		return this;
	}

	public ArrayList<Connection> getAllConnections() {
		return cnxns;
	}
		
}

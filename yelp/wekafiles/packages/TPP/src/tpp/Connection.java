package tpp;

import weka.core.Instance;
import weka.core.Instances;

public class Connection {
	
	private String sourceNode;
	
	private String targetNode;
	
	private Instances ins;
	
	private Instance nodeInstance;
	
	private Instances in;

	public Connection(String sourceNode, String targetNode) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
	}
	
	public String getSourceNode() {
		return sourceNode;
	}

	public String getTargetNode() {
		return targetNode;
	}
		
	public Instance getNodeInstance(Instances ins, String node) {
		
		for(int i = 0; i < ins.numInstances(); i++) {
			Instance in = ins.instance(i);
			String attVal = in.stringValue(0);
			
			if(attVal.equals(node))
				nodeInstance = in;
		}
		
		return nodeInstance;	
		
	}
}

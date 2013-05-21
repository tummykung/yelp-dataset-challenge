package weka.gui.explorer;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import weka.core.Instances;
import weka.gui.explorer.Explorer.ExplorerPanel;
import weka.gui.extensions.pcp.DatasetFactory;
import weka.gui.extensions.pcp.DatasetFactory.DatasetResult;

public class ExplorerPCPPanel extends JPanel implements ExplorerPanel{

	private static final long serialVersionUID = -8644936852926558385L;

	private final static String TITLE = "Parallel Coordinates Plot";
	
	private Explorer m_explorer;
	private ChartPanel m_panel;
	private Instances m_instances;
	
	public ExplorerPCPPanel() {
	    super(new BorderLayout());
	}
	
	@Override
	public Explorer getExplorer() {
		return m_explorer;
	}

	@Override
	public String getTabTitle() {
		return TITLE;
	}

	@Override
	public String getTabTitleToolTip() {
		return TITLE;
	}

	@Override
	public void setExplorer(Explorer parent) {
		m_explorer = parent;
		m_explorer.getTabbedPane().addChangeListener(
			new ChangeListener()
			{
			  private Instances m_localInst;
			  
		      public void stateChanged(ChangeEvent e) {
		        if (m_explorer.getTabbedPane().getSelectedComponent() == ExplorerPCPPanel.this && m_localInst != m_instances) {
		        	CategoryDataset dataset = DatasetFactory.INSTANCE.createDatasetFromInstances(m_instances, null, -1, -1);
		    		DatasetResult res = new DatasetResult(dataset, null);
		    		JFreeChart chart = DatasetFactory.INSTANCE.createChart(res);
		    		if(m_panel == null)
		    		{
		    			m_panel = DatasetFactory.INSTANCE.createChartPanel(chart);
		    			add(m_panel, BorderLayout.CENTER);
		    		}
		    		else
		    		{
		    			m_panel.setChart(chart);
		    		}
		    		DatasetFactory.INSTANCE.setLastInstances(m_instances);
		    		m_localInst = m_instances;
		        }
		      }
		    });
	}

	@Override
	public void setInstances(Instances inst) {
		m_instances = inst;
	}
	

}

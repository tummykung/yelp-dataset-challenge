package weka.gui.extensions.pcp.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartPanel;

import weka.gui.extensions.pcp.DatasetFactory;
import weka.gui.extensions.pcp.DatasetFactory.DatasetResult;

public class OpenFileActionListener implements ActionListener{

	private final ChartPanel m_panel;
	private final JFrame m_frame;
	
	public OpenFileActionListener(ChartPanel panel, JFrame frame) {
		super();
		m_panel = panel;
		m_frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DatasetResult res;
		try {
			res = DatasetFactory.INSTANCE.promptAndCreateDataset();
		
			if(res != null && m_panel != null)
			{
				m_panel.setChart(DatasetFactory.INSTANCE.createChart(res));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(m_frame, e1.getMessage());
		}
	}	
}

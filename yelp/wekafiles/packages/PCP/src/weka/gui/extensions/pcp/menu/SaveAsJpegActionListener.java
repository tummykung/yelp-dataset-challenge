package weka.gui.extensions.pcp.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class SaveAsJpegActionListener implements ActionListener{

	private final JFrame m_frame;
	private final JFileChooser m_chooser; 
	private final JFreeChart m_chart;
	
	public SaveAsJpegActionListener(JFrame mFrame, JFreeChart chart) {
		super();
		m_frame = mFrame;
		m_chooser = new JFileChooser(new File(System.getProperty("user.dir")));
		m_chart = chart;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		m_chooser.showOpenDialog(m_frame);
		
		File file = m_chooser.getSelectedFile();
		if(file != null)
		{
			try {
				ChartUtilities.saveChartAsJPEG(file, m_chart, m_frame.getWidth(), m_frame.getHeight());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(m_frame, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}
	

	
}

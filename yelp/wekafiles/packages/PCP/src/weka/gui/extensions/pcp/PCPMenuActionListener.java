package weka.gui.extensions.pcp;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import weka.gui.extensions.PCPMenuExtension;
import weka.gui.extensions.pcp.menu.OpenFileActionListener;
import weka.gui.extensions.pcp.menu.QuitActionListener;
import weka.gui.extensions.pcp.menu.SaveAsJpegActionListener;

public class PCPMenuActionListener implements ActionListener{
	
	
	public PCPMenuActionListener() {
		super();
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			
			JFrame dialog = createFrame(DatasetFactory.INSTANCE.createChart(DatasetFactory.INSTANCE.promptAndCreateDataset()));
			if(dialog != null)
			{
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage());
			return;
		}
	}
	
	
	
	private JFrame createFrame(JFreeChart chart)
	{
		if(chart != null)
		{
			JFrame dialog = new JFrame();
			dialog.setLayout(new BorderLayout());
			ZoomingChartPanel panel = DatasetFactory.INSTANCE.createChartPanel(chart);
			dialog.add(panel, BorderLayout.CENTER);
			
			dialog.setJMenuBar(createMenu(dialog, panel));
			
			
			return dialog;
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	
	
	
	
	
	private JMenuBar createMenu(JFrame frame, ChartPanel panel)
	{
		JMenuBar menu = new JMenuBar();
		
		//File menu
		JMenu fileMenu = new JMenu("File");
		
		fileMenu.add(createMenuItem("Open ...", new OpenFileActionListener(panel, frame)));
		fileMenu.add(createMenuItem("Save As GPEG ...", new SaveAsJpegActionListener(frame, panel.getChart())));
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem("Quit", new QuitActionListener(frame)));
		
		menu.add(fileMenu);
		
		return menu;
	}
	
	private JMenuItem createMenuItem(String text, ActionListener listener)
	{
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(listener);
		return menuItem;
	}

	
	public static void main(String[] args) {
		PCPMenuExtension ext = new PCPMenuExtension();
		ext.getActionListener(null).actionPerformed(null);
	}

}

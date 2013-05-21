package weka.gui.extensions.pcp;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Set;

import javax.swing.JFileChooser;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import weka.core.Instances;
import weka.gui.ExtensionFileFilter;

public class DatasetFactory {
	public static DatasetFactory INSTANCE = new DatasetFactory();

	private final JFileChooser m_fileChooser = new JFileChooser(new File(System
			.getProperty("user.dir")));

	private Instances m_instances;
	private File m_file;

	private DatasetFactory() {
		super();

		m_fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
				Instances.FILE_EXTENSION, "ARFF Files (*"
						+ Instances.FILE_EXTENSION + ")"));
		m_fileChooser.setMultiSelectionEnabled(true);

	}

	public DatasetResult promptAndCreateDataset() throws Exception {
		int retVal = m_fileChooser.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return null;

		File[] files = m_fileChooser.getSelectedFiles();

		if (files != null && files.length > 0) {
			return new DatasetResult(createDataset(files[0], null, -1, -1),
					files[0]);
		} else {
			return null;
		}

	}
	
	public ZoomingChartPanel createChartPanel(JFreeChart chart)
	{
		ZoomingChartPanel panel = propertize(new ZoomingChartPanel(chart));
		
		ZoomerChartMouseListener l = new ZoomerChartMouseListener(panel);
		
		panel.addMouseListener(l);
		panel.addMouseMotionListener(l);
		
		
		return panel;
	}

	public ZoomingChartPanel propertize(ZoomingChartPanel panel)
	{
		panel.setFillZoomRectangle(false);
		panel.setRangeZoomable(false);
		panel.setDomainZoomable(false);
		panel.setVerticalAxisTrace(false);
		panel.setHorizontalAxisTrace(false);
		panel.setMouseZoomable(false);
		return panel;
	}
	
	public CategoryDataset createDataset(final File file,
			Set<String> catFilter, double loD, double hiD) throws Exception {
		String filename = file.getAbsolutePath();

		Reader r = new java.io.BufferedReader(new FileReader(filename));
		Instances instances = new Instances(r);
		instances.setClassIndex(instances.numAttributes() - 1);
		instances.instance(0).numAttributes();

		final DefaultCategoryDataset result = createDatasetFromInstances(
				instances, catFilter, loD, hiD);
		if (result != null) {
			m_instances = instances;
			m_file = file;
		}
		return result;
	}

	public Instances getLastInstances() {
		return m_instances;
	}

	public File getLastFile() {
		return m_file;
	}
	
	public void setLastInstances(Instances inst)
	{
		m_instances = inst;
	}

	public DefaultCategoryDataset createDatasetFromInstances(
			Instances instances, Set<String> catFilter, double loD, double hiD) {
		final DefaultCategoryDataset result = new DefaultCategoryDataset();

		int attrs = instances.numAttributes();
		
		for (int j = 0; j < attrs; ++j) {
			if (catFilter == null || catFilter.contains(instances.attribute(j).name())) {
				for (int i = 0; i < instances.numInstances(); ++i) {
					String name = String.valueOf(i);

					double value = instances.instance(i).value(j);
					if (loD == -1 || hiD == -1 || (value > loD && value < hiD)) {
						result.addValue(value, name, instances.attribute(j).name());
					}					
				}
				
			}
		}
		
		return result;
	}
	
		
	public JFreeChart createChart(DatasetResult datasetRes) {
		if (datasetRes != null) {
			String title = "";
			if(datasetRes.m_file != null)
			{
				title = datasetRes.m_file.getName();
			}
			final JFreeChart chart = ChartFactory.createLineChart(
					title, "Attributes", "Values",
					datasetRes.m_dataset, PlotOrientation.VERTICAL, false,
					true, false);
			chart.setBackgroundPaint(Color.white);

			CategoryPlot plot = chart.getCategoryPlot();
			plot.setBackgroundPaint(Color.lightGray);
			plot.setDomainGridlinePaint(Color.white);
			plot.setRangeGridlinePaint(Color.white);
			plot.setDomainGridlinesVisible(true);
			plot.setRangeGridlinesVisible(false);
			

			plot.getDomainAxis().setCategoryLabelPositions(
					CategoryLabelPositions.DOWN_90);

			return chart;
		} else {
			return null;
		}
	}

	public static final class DatasetResult {
		public final CategoryDataset m_dataset;
		public final File m_file;

		public DatasetResult(CategoryDataset mDataset, File mFile) {
			super();
			m_dataset = mDataset;
			m_file = mFile;
		}

	}

}

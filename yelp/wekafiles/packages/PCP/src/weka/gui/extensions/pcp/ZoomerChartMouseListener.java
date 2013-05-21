package weka.gui.extensions.pcp;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;

import weka.gui.extensions.pcp.DatasetFactory.DatasetResult;

public class ZoomerChartMouseListener implements MouseListener,
		MouseMotionListener {

	private int m_x = -1;
	private int m_y = -1;

	private final ZoomingChartPanel m_panel;

	public ZoomerChartMouseListener(ZoomingChartPanel panel) {
		super();
		m_panel = panel;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override @SuppressWarnings("unchecked")
	public void mouseReleased(MouseEvent arg0) {

		List<String> cats = m_panel.getChart().getCategoryPlot()
				.getCategories();
		CategoryAxis domainAxis = m_panel.getChart().getCategoryPlot()
				.getDomainAxis();

		RectangleEdge axisEdge = m_panel.getChart().getCategoryPlot()
				.getDomainAxisEdge();
		int count = cats.size();
		Set<String> zoomCats = new HashSet<String>();

		Rectangle rect = m_panel.getRect();
		
		if(rect != null)
		{
			int loX = rect.x;
			int hiX = rect.x + rect.width;
			
			int loY = rect.y;
			int hiY = rect.y + rect.height;
			
			if(cats.size() > 0)
			{
				for (int i = 0; i < cats.size(); ++i) {
					
					float xEnd = (float) domainAxis.getCategoryEnd(i, count, m_panel
							.getChartRenderingInfo().getChartArea(), axisEdge);
					
					float xStart = (float) domainAxis.getCategoryStart(i, count, m_panel
							.getChartRenderingInfo().getChartArea(), axisEdge);
					
					
					if((xStart > loX || xEnd > loX) && (xStart < hiX || xEnd < hiX))
					{
						zoomCats.add(cats.get(i));
					}
				}
				
				RectangleEdge rangeAxisEdge = m_panel.getChart().getCategoryPlot().getRangeAxisEdge();
				
				double loD = m_panel.getChart().getCategoryPlot().getRangeAxis().java2DToValue(loY,  m_panel.getScreenDataArea(), rangeAxisEdge);
				double hiD = m_panel.getChart().getCategoryPlot().getRangeAxis().java2DToValue(hiY,  m_panel.getScreenDataArea(), rangeAxisEdge);
	
				double loDn = Math.min(loD, hiD);
				double hiDn = Math.max(loD, hiD);
				
				
				if(!setZoom(zoomCats, loDn, hiDn))
				{
					setZoom(null, -1, -1);
				}
			}
			else
			{
				setZoom(null, -1, -1);
			}
		}
		else
		{
			setZoom(null, -1, -1);
		}
		m_x = -1;
		m_y = -1;
		m_panel.setRect(null);

	}
	
	private boolean setZoom(Set<String> filterSet, double loD, double hiD)
	{
		CategoryDataset dataset = DatasetFactory.INSTANCE.createDatasetFromInstances(DatasetFactory.INSTANCE.getLastInstances(), filterSet, loD, hiD);
		if(dataset != null)
		{
			DatasetResult result = new DatasetResult(dataset, DatasetFactory.INSTANCE.getLastFile());
			
			m_panel.setChart(DatasetFactory.INSTANCE.createChart(result));
			DatasetFactory.INSTANCE.propertize(m_panel);			
			return true;
		}
		return false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (m_x == -1) {
			m_x = e.getX();
			m_y = e.getY();
		}

		int x = e.getX();
		int y = e.getY();

		int width = Math.max(x, m_x) - Math.min(x, m_x);
		int height = Math.max(y, m_y) - Math.min(y, m_y);

		Rectangle rect = new Rectangle();
		rect.x = Math.min(m_x, x);
		rect.y = Math.min(m_y, y);
		rect.width = width;
		rect.height = height;

		m_panel.setRect(rect);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

}

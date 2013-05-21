package weka.gui.extensions.pcp;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class ZoomingChartPanel extends ChartPanel{

	private static final long serialVersionUID = -7256274671353858950L;

	private Rectangle m_rect;
	
	public ZoomingChartPanel(JFreeChart chart, boolean properties,
			boolean save, boolean print, boolean zoom, boolean tooltips) {
		super(chart, properties, save, print, zoom, tooltips);
	}

	public ZoomingChartPanel(JFreeChart chart, boolean useBuffer) {
		super(chart, useBuffer);
	}

	public ZoomingChartPanel(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean copy, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties, copy, save,
				print, zoom, tooltips);
	}

	public ZoomingChartPanel(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean save, boolean print, boolean zoom, boolean tooltips) {
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save,
				print, zoom, tooltips);
	}

	public ZoomingChartPanel(JFreeChart chart) {
		super(chart);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		synchronized(this)
		{
			if(m_rect != null)
			{
				g.drawRect(m_rect.x, m_rect.y, m_rect.width, m_rect.height);
			}
		}
	}
	
	public synchronized void setRect(Rectangle rect)
	{
		m_rect = rect;
		repaint();
	}
	public Rectangle getRect()
	{
		return m_rect;
	}

	
	
}

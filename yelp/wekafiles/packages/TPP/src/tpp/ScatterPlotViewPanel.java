package tpp;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JPanel;
import javax.vecmath.Point2d;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.matrix.Matrix;

/*
 * Created on 16-Feb-2006
 */

/**
 * Panel with a view of a set of instances that can be manipulated thru
 * projection pursuit
 * 
 * @author Joe Faith
 */
public class ScatterPlotViewPanel extends JPanel implements
		TPPModelEventListener, ComponentListener {

	protected ScatterPlotModel spModel = null;

	protected static final double LINE_WIDTH = 1.5;

	/** Noise added to the view to better separate the points */
	private Matrix jitter;

	/** whether to add jitter to the view */
	private boolean showJitter = false;

	public ScatterPlotViewPanel() {
		super();
		initialize();
	}

	/**
	 *
	 */
	private void initialize() {
		addComponentListener(this);
	}

	/**
	 * Load the data panel with the instances to be displayed
	 */
	public void setModel(ScatterPlotModel spModel) {
		this.spModel = spModel;
		if (spModel == null)
			removeAll();
		else {
			spModel.addListener(this);
			spModel.setColours(ColourScheme.DARK);
			jitter = new Matrix(spModel.getNumDataPoints(),
					spModel.getNumDataDimensions());
			spModel.initRetinalAttributes();
			spModel.resizePlot(getWidth(), getHeight());
		}
	}

	/**
	 * Find the indices of the nearest points to the given coordinates in data
	 * space. If no point is found then zero length array returned
	 */
	public int[] findNearestPoints(Point2D.Double pt) {
		double margin = spModel.markerSize * getWidth()
				/ spModel.getTransform().getScaleX();
		double distance;
		Vector<Integer> points = new Vector<Integer>();
		for (int i = 0; i < spModel.getNumDataPoints(); i++) {
			distance = pt.distance(new Point2D.Double(spModel.getView().get(i,
					0), spModel.getView().get(i, 1)));
			if (distance < margin)
				points.add(new Integer(i));
		}
		int[] aPoints = new int[points.size()];
		for (int i = 0; i < points.size(); i++)
			aPoints[i] = points.get(i).intValue();
		return aPoints;

	}

	/**
	 * Find the indices of the nearest axes to the given coordinates in data
	 * space. If no axis is found then zero length array returned
	 */
	public int[] findNearestAxes(java.awt.geom.Point2D.Double pt) {
		double margin = spModel.markerSize * getWidth()
				/ spModel.getTransform().getScaleX();
		double distance;
		Vector<Integer> axes = new Vector<Integer>();
		for (int i = 0; i < spModel.getNumDataDimensions(); i++) {
			distance = pt.distance(new Point2D.Double(spModel.getProjection()
					.get(i, 0), spModel.getProjection().get(i, 1)));
			if (distance < margin)
				axes.add(new Integer(i));
		}
		int[] aAxes = new int[axes.size()];
		for (int i = 0; i < axes.size(); i++)
			aAxes[i] = axes.get(i).intValue();
		return aAxes;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// add a transform so that we can specify coordinates in data space
		// rather than device space
		Graphics2D g2 = (Graphics2D) g;
		paintView(g2, spModel.getTransform(), getWidth(), getHeight());
	}

	/**
	 * Paint the scatter plot to the given Graphics, using the given mapping
	 * from data (aka user) space to device space, and with markers of the given
	 * size (in pixels) If transform is null then use the default one. If
	 * markerSize=0 use the default size.
	 */
	public void paintView(Graphics2D g2, AffineTransform transform, int width,
			int height) {
		double scaledMarkerSize = (spModel.markerSize * width);

		/**
		 * The difference between the maximum and minimum marker size (as a
		 * proportion of screen size).
		 */
		double markerRange = scaledMarkerSize * 2;

		/**
		 * The minimum size of the markers to display. (as a proportion of
		 * screen size).
		 */
		double markerMin = scaledMarkerSize * 0.5;

		double origin = scaledMarkerSize * 2;

		if (spModel != null && spModel.getData() != null) {

			// if a transform is specified then use it, saving the original
			AffineTransform saveAT = null;
			if (transform != null) {
				saveAT = g2.getTransform();
				g2.transform(transform);
			} else {
				transform = g2.getTransform();
			}
			g2.setStroke(new BasicStroke((float) (LINE_WIDTH / transform
					.getScaleX())));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			setBackground(spModel.getColours().getBackgroundColor());

			// find out how big the markers need to be in data space in order to
			// appear the right size in device space
			// nb this assumes that the same scale is used for both x and y
			double markerRadius = scaledMarkerSize / transform.getScaleX();

			double x, y;
			double size;

			double x1, y1, x2, y2;
			Shape circle, marker;
			Line2D line;
			int i, j, p;

			// If the axes are shown and there are points currently selected
			// then calculate mean attribute values for those selected points,
			// compared to the overall attribute means
			// This is then used to color the axes
			//
			// relativeMeanForSelected = L( ats-at / at )
			// where ats = mean of this attribute for the selected points
			// and at = mean of this attribute for all points
			// L = logistic squashing function
			int numPointsSelected = spModel.numPointsSelected();
			double[] relativeMeanForSelected = null;
			if (spModel.showAxes() && (numPointsSelected > 0)) {
				Matrix mPointsSelected = new Matrix(1,
						spModel.getNumDataPoints());
				for (p = 0; p < spModel.getNumDataPoints(); p++)
					if (spModel.isPointSelected(p))
						mPointsSelected.set(0, p, 1);
				Matrix attributeMeanSelected = mPointsSelected.times(
						spModel.getData()).times(
						1d / spModel.numPointsSelected());
				Matrix attributeMean = MatrixUtils.columnMeans(spModel
						.getData());
				relativeMeanForSelected = MatrixUtils
						.logistic(attributeMeanSelected.minus(attributeMean)
								.arrayRightDivide(attributeMeanSelected)
								.getArray()[0]);
				// System.out.println("Data =\n" + tpp.getData());
				// System.out.println("Attribute means = \n"+attributeMean);
				// System.out.println("Attribute means for selected points= \n"+attributeMeanSelected);
				// System.out.println("selected points =\n" + mPointsSelected);
				// System.out.println("Relative attribute means = \n" +
				// MatrixUtils.toString(relativeMeanForSelected));
			}

			// draw series lines;
			if (spModel.showSeries() && (spModel.getSeries() != null)) {
				g2.setColor(spModel.getColours().getAxesColor());
				Iterator<TreeSet<Instance>> allSeries = spModel.getSeries()
						.getAllSeries().values().iterator();
				Iterator<Instance> nextSeries;
				// for all the series
				while (allSeries.hasNext()) {
					nextSeries = allSeries.next().iterator();
					if (nextSeries.hasNext()) {

						// find the start point
						i = spModel.indexOf(nextSeries.next());
						x1 = spModel.getView().get(i, 0) + jitter.get(i, 0);
						y1 = spModel.getView().get(i, 1) + jitter.get(i, 1);
						while (nextSeries.hasNext()) {

							// and draw a line and arrow head to the next point
							i = spModel.indexOf(nextSeries.next());
							x2 = spModel.getView().get(i, 0) + jitter.get(i, 0);
							y2 = spModel.getView().get(i, 1) + jitter.get(i, 1);
							line = new Line2D.Double(x1, y1, x2, y2);
							g2.draw(line);
							g2.fill(MarkerFactory.buildArrowHead(line,
									markerRadius, true));

							x1 = x2;
							y1 = y2;
						}
					}
				}
			}

			// draw the graph
			if (spModel.showGraph()) {
				g2.setColor(spModel.getColours().getAxesColor());

				Iterator<Connection> allConnections = spModel.getGraph()
						.getAllConnections().iterator();

				String sourceNode = null;
				String targetNode = null;
				Instances ins = spModel.getInstances();

				Connection cnxn = null;

				while (allConnections.hasNext()) {

					cnxn = allConnections.next();

					sourceNode = cnxn.getSourceNode();
					// System.out.println("Source Node: "+ sourceNode);
					targetNode = cnxn.getTargetNode();
					// System.out.println("Target Node: "+ targetNode);

					Instance source = cnxn.getNodeInstance(ins, sourceNode);
					// System.out.println(source.stringValue(0));
					Instance target = cnxn.getNodeInstance(ins, targetNode);
					// System.out.println(target.stringValue(0));

					i = spModel.indexOf(source);
					x1 = spModel.getView().get(i, 0) + jitter.get(i, 0);
					y1 = spModel.getView().get(i, 1) + jitter.get(i, 1);

					j = spModel.indexOf(target);
					x2 = spModel.getView().get(j, 0) + jitter.get(j, 0);
					y2 = spModel.getView().get(j, 1) + jitter.get(j, 1);
					line = new Line2D.Double(x1, y1, x2, y2);
					g2.draw(line);
					// g2.fill(MarkerFactory.buildArrowHead(line,
					// markerRadius*2));
				}
			}

			// draw clustering
			if (spModel.showHierarchicalClustering()) {
				// recursively draw lines between the centroids of each cluster
				// nb this assumes the this is a binary HC -- ie that each
				// cluster contains two members
				g2.setColor(spModel.getColours().getAxesColor());
				HierarchicalCluster cluster = spModel.getHierarchicalCluster();
				drawClusterArc(cluster, g2);
			}

			// draw the target
			if (spModel.showTarget()) {
				g2.setColor(spModel.getColours().getAxesColor());
				for (i = 0; i < spModel.getNumDataPoints(); i++) {
					x = spModel.getTarget().get(i, 0);
					y = spModel.getTarget().get(i, 1);
					circle = new Ellipse2D.Double(x - markerRadius, y
							- markerRadius, markerRadius * 2, markerRadius * 2);
					g2.draw(circle);
				}
			}

			// draw the points
			for (i = 0; i < spModel.getNumDataPoints(); i++) {

				// Color of the point depends on whether we are coloring by
				// a numeric or nominal attribute
				if (spModel.getColourAttribute() == null)
					g2.setColor(spModel.getColours().getForegroundColor());
				else {
					if (spModel.getColourAttribute().isNominal())
						g2.setColor(spModel.getColours()
								.getClassificationColor(
										(int) spModel
												.getInstances()
												.instance(i)
												.value(spModel
														.getColourAttribute())));
					if (spModel.getColourAttribute().isNumeric())
						g2.setColor(spModel.getColours().getColorFromSpectrum(
								spModel.getInstances().instance(i)
										.value(spModel.getColourAttribute()),
								spModel.colorAttributeLowerBound,
								spModel.colorAttributeUpperBound));
				}

				// Size of the marker depends on size attribute
				if (spModel.getSizeAttribute() == null)
					size = markerRadius;
				else
					size = (markerMin + markerRange
							* (spModel.getInstances().instance(i)
									.value(spModel.getSizeAttribute()) - spModel.sizeAttributeLowerBound)
							/ (spModel.sizeAttributeUpperBound - spModel.sizeAttributeLowerBound))
							/ transform.getScaleX();

				// shape/fill of the marker depends on respective attributes
				x = spModel.getView().get(i, 0) + jitter.get(i, 0);
				y = spModel.getView().get(i, 1) + jitter.get(i, 1);
				if (spModel.isPointSelected(i)) {
					g2.draw(new Line2D.Double(x - size, y, x + size, y));
					g2.draw(new Line2D.Double(x, y - size, x, y + size));
				} else {
					if (spModel.getShapeAttribute() == null)
						marker = MarkerFactory.buildMarker(0, x, y, size);
					else
						marker = MarkerFactory.buildMarker(
								(int) spModel.instances.instance(i).value(
										spModel.getShapeAttribute()), x, y,
								size);

					if (spModel.getFillAttribute() == null) {
						g2.fill(marker);
					} else {
						switch ((int) spModel.instances.instance(i).value(
								spModel.getFillAttribute())) {
						case 0: {
							g2.fill(marker);
							break;
						}
						case 1: {
							g2.draw(marker);
							break;
						}
						default: {
							// TODO add more textures for filling points (shaded
							// lines etc)
							g2.draw(marker);
						}
						}
					}
				}
			}


			// plot the axes or just the origin
			if (spModel.showAxes()) {

				Graphics labelGraphics = null;
				if (spModel.showAxisLabels()) {
					// We draw labels in device space rather than user space, since
					// fonts may not scale correctly
					labelGraphics = getGraphics();
					labelGraphics.setColor(spModel.getColours().getAxesColor());
				}
				
				for (i = 0; i < spModel.getNumDataDimensions(); i++) {

					// If there are any point(s) selected then color the axes by
					// their (average) weight with the selected point(s)
					if (numPointsSelected > 0)
						g2.setColor(spModel.getColours().getColorFromSpectrum(
								relativeMeanForSelected[i], 0, 1));
					// otherwise highlight the axis if it is selected
					else
						g2.setColor((spModel.isAxisSelected(i) ? spModel
								.getColours().getForegroundColor() : spModel
								.getColours().getAxesColor()));
					g2.draw(new Line2D.Double(0, 0, spModel.getProjection()
							.get(i, 0), spModel.getProjection().get(i, 1)));
					if (spModel.isAxisSelected(i))
						g2.fill(new Ellipse2D.Double(spModel.getProjection()
								.get(i, 0) - markerRadius, spModel
								.getProjection().get(i, 1) - markerRadius,
								markerRadius * 2, markerRadius * 2));

					if (spModel.showAxisLabels()) {

						// write label to the right of the marker
						Point2D labelLocationInDeviceSpace = null;
						labelLocationInDeviceSpace = transform
								.transform(new Point2D.Double(spModel
										.getProjection().get(i, 0), spModel
										.getProjection().get(i, 1)),
										labelLocationInDeviceSpace);
						try {
							labelGraphics.drawString(spModel
									.getNumericAttributes().get(i).name(),
									(int) labelLocationInDeviceSpace.getX(),
									(int) labelLocationInDeviceSpace.getY());
						} catch (Exception e) {
							System.out.println(e);
						}
					}

				}
			} else {
				double originSize = origin / transform.getScaleX();
				g2.setColor(spModel.getColours().getAxesColor());
				g2.draw(new Line2D.Double(-originSize, 0, originSize, 0));
				g2.draw(new Line2D.Double(0, -originSize, 0, originSize));
			}

			// draw the rectangle?
			if (spModel.rectangle != null)
				spModel.rectangle.draw(g2);

			// restore original transform
			if (saveAT != null)
				g2.setTransform(saveAT);

			if (showJitter)
				updateJitter();

		}

	}

	/**
	 * Recursively draw an arc between the centroids of the members of this
	 * (binary) cluster
	 * 
	 * @param g2
	 */
	private void drawClusterArc(HierarchicalCluster cluster, Graphics2D g2) {

		// if this cluster just contains another cluster then draw that
		if (cluster.size() == 1
				&& cluster.get(0) instanceof HierarchicalCluster)
			drawClusterArc((HierarchicalCluster) cluster.get(0), g2);

		// if this cluster contains two subclusters then draw arc between their
		// centroids
		if (cluster.size() == 2) {
			HierarchicalCluster c0, c1;
			Matrix p0, p1;
			c0 = (HierarchicalCluster) cluster.get(0);
			c1 = (HierarchicalCluster) cluster.get(1);
			p0 = spModel.projection.project(c0.getCentroid());
			p1 = spModel.projection.project(c1.getCentroid());
			Double line = new Line2D.Double(p0.get(0, 0), p0.get(0, 1), p1.get(
					0, 0), p1.get(0, 1));
			g2.draw(line);
			drawClusterArc(c0, g2);
			drawClusterArc(c1, g2);
		}

	}

	/** Whether to add noise to the current view */
	public void addJitter(boolean showJitter) {
		this.showJitter = showJitter;
		if (showJitter) {
			updateJitter();
		} else
			// reset noise to null
			jitter = new Matrix(spModel.getNumDataPoints(),
					spModel.getNumDataDimensions());

	}

	/** Change the noise */
	private void updateJitter() {

		double scale = spModel.getTransform().getScaleX();
		Random ran = new Random();
		for (int i = 0; i < jitter.getRowDimension(); i++)
			for (int j = 0; j < jitter.getColumnDimension(); j++)
				jitter.set(i, j, (ran.nextDouble() - 0.5d) * 20d / scale);

	}

	public void modelChanged(TPPModelEvent e) {
		repaint();
	}

	public void componentHidden(ComponentEvent e) {

	}

	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent e) {
		spModel.resizePlot(getWidth(), getHeight());
	}

	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

}

/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    JFreeChartOffscreenRenderer.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.gui.beans;

import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DataUtilities;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.Series;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.SortOrder;

import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.Stats;

/**
 * OffscreenChartRenderer that uses the JFreeChart library for rendering
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: $
 */
public class JFreeChartOffscreenChartRenderer extends
    AbstractOffscreenChartRenderer {

  /**
   * Render histogram(s) (numeric attribute) or bar chart (nominal attribute).
   * Some implementations may not be able to render more than one histogram/bar
   * on the same chart - the implementation can either throw an exception or
   * just process the first series in this case.
   * 
   * @param width the width of the resulting chart in pixels
   * @param height the height of the resulting chart in pixels
   * @param series a list of Instances - one for each series to be plotted
   * @param attsToPlot the attribute to plot corresponding to the Instances in
   *          the series list
   * @param optionalArgs optional arguments to the renderer (may be null)
   * 
   * @return a BufferedImage containing the chart
   * @throws Exception if there is a problem rendering the chart
   */
  public BufferedImage renderHistogram(int width, int height,
      List<Instances> series, String attToPlot, List<String> additionalArgs)
      throws Exception {

    String plotTitle = "Bar Chart";
    String userTitle = getOption(additionalArgs, "-title");
    plotTitle = (userTitle != null) ? userTitle : plotTitle;
    String colorAtt = getOption(additionalArgs, "-color");
    String pareto = getOption(additionalArgs, "-pareto");

    boolean doPareto = false;
    if (pareto != null && pareto.length() == 0 && series.size() == 1) {
      doPareto = true;
    }

    if (series.size() == 1 && colorAtt != null && colorAtt.length() > 0) {
      int colIndex = getIndexOfAttribute(series.get(0), colorAtt);

      if (colIndex >= 0 && series.get(0).attribute(colIndex).isNominal()
          && !doPareto) {
        // split single series out into multiple instances objects - one
        // per class
        series = splitToClasses(series.get(0), colIndex);
        for (Instances insts : series) {
          insts.setClassIndex(colIndex);
        }
      }
    }

    Instances masterInstances = series.get(0);
    int attIndex = getIndexOfAttribute(masterInstances, attToPlot);
    if (attIndex < 0) {
      attIndex = 0;
    }

    if (!(series.get(0).attribute(attIndex).isNominal() || series.get(0)
        .attribute(attIndex).isRelationValued())) {
      doPareto = false;
    }

    // Do a pareto chart
    if (doPareto) {
      final DefaultKeyedValues data = new DefaultKeyedValues();
      AttributeStats attStats = masterInstances.attributeStats(attIndex);
      double[] attValFreqs = attStats.nominalWeights;
      for (int i = 0; i < attValFreqs.length; i++) {
        Number freq = new Double(attValFreqs[i]);
        data.addValue(masterInstances.attribute(attIndex).value(i), freq);
      }

      data.sortByValues(SortOrder.DESCENDING);
      final KeyedValues cumulative = DataUtilities
          .getCumulativePercentages(data);
      final CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
          masterInstances.attribute(attIndex).name(), data);

      final JFreeChart chart = ChartFactory.createBarChart(plotTitle,
          masterInstances.attribute(attIndex).name(), "Fequency/weight mass",
          dataset, PlotOrientation.VERTICAL, true, false, false);

      final CategoryPlot plot = chart.getCategoryPlot();

      final CategoryAxis domainAxis = plot.getDomainAxis();
      domainAxis.setLowerMargin(0.02);
      domainAxis.setUpperMargin(0.02);

      LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
      CategoryDataset dataset2 = DatasetUtilities.createCategoryDataset(
          "Cumulative", cumulative);
      final NumberAxis axis2 = new NumberAxis("Percent");
      axis2.setNumberFormatOverride(NumberFormat.getPercentInstance());
      // plot.
      plot.setRangeAxis(1, axis2);
      plot.setDataset(1, dataset2);
      plot.setRenderer(1, renderer2);
      plot.mapDatasetToRangeAxis(1, 1);
      plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

      chart.setBackgroundPaint(java.awt.Color.white);
      BufferedImage image = chart.createBufferedImage(width, height);
      return image;
    }

    boolean seriesAreClasses = false;
    int classIndex = masterInstances.classIndex();

    if (classIndex >= 0 && !masterInstances.attribute(classIndex).isNumeric()
        && !masterInstances.attribute(classIndex).isRelationValued()
        && masterInstances.attributeStats(classIndex).distinctCount == 1) {
      // series correspond to class labels (assume that subsequent series only
      // contain instances of one class)...
      seriesAreClasses = true;
    }

    // bar chart for a nominal attribute
    if (masterInstances.attribute(attIndex).isNominal()
        || masterInstances.attribute(attIndex).isString()) {
      DefaultCategoryDataset dataset = new DefaultCategoryDataset();

      // do the master series
      String masterSeriesTitle = masterInstances.relationName();
      if (seriesAreClasses) {
        for (int i = 0; i < masterInstances.numInstances(); i++) {
          Instance current = masterInstances.instance(i);
          if (!current.isMissing(classIndex)) {
            masterSeriesTitle = current.stringValue(classIndex);
            break;
          }
        }
      }

      AttributeStats attStats = masterInstances.attributeStats(attIndex);
      double[] attValFreqs = attStats.nominalWeights;
      for (int i = 0; i < attValFreqs.length; i++) {
        Number freq = new Double(attValFreqs[i]);
        dataset.addValue(freq, masterSeriesTitle,
            masterInstances.attribute(attIndex).value(i));
      }

      // any subsequent series
      for (int i = 1; i < series.size(); i++) {
        Instances nextSeries = series.get(i);

        String seriesTitle = nextSeries.relationName();

        if (seriesAreClasses) {
          for (int j = 0; j < nextSeries.numInstances(); j++) {
            Instance current = nextSeries.instance(j);
            if (!current.isMissing(classIndex)) {
              seriesTitle = current.stringValue(classIndex);
              break;
            }
          }

          attStats = nextSeries.attributeStats(attIndex);
          attValFreqs = attStats.nominalWeights;
          for (int j = 0; j < attValFreqs.length; j++) {
            Number freq = new Double(attValFreqs[j]);
            dataset.addValue(freq, seriesTitle, nextSeries.attribute(attIndex)
                .value(j));
          }
        }
      }

      JFreeChart chart = null;

      if (series.size() == 1) {
        chart = ChartFactory.createBarChart(plotTitle, masterInstances
            .attribute(attIndex).name(), "Fequency/weight mass", dataset,
            PlotOrientation.VERTICAL, true, false, false);
      } else {
        chart = ChartFactory.createStackedBarChart(plotTitle, masterInstances
            .attribute(attIndex).name(), "Fequency/weight mass", dataset,
            PlotOrientation.VERTICAL, true, false, false);
      }

      chart.setBackgroundPaint(java.awt.Color.white);
      BufferedImage image = chart.createBufferedImage(width, height);
      return image;

    } else {
      // histogram for numeric attributes
      HistogramDataset dataset = new HistogramDataset();

      // combine all series in order to get overall std dev, and range
      Instances temp = new Instances(masterInstances);
      for (int i = 1; i < series.size(); i++) {
        Instances additional = series.get(i);
        for (Instance tempI : additional) {
          temp.add(tempI);
        }
      }

      AttributeStats stats = temp.attributeStats(attIndex);
      Stats numericStats = stats.numericStats;
      double intervalWidth = 3.49 * numericStats.stdDev
          * StrictMath.pow(temp.numInstances(), (-1.0 / 3.0));
      double range = numericStats.max - numericStats.min;
      int numBins = StrictMath.max(1,
          (int) StrictMath.round(range / intervalWidth));

      // do the master series
      String masterSeriesTitle = masterInstances.relationName();
      if (seriesAreClasses) {
        for (int i = 0; i < masterInstances.numInstances(); i++) {
          Instance current = masterInstances.instance(i);
          if (!current.isMissing(current.classAttribute())) {
            masterSeriesTitle = current.stringValue(current.classAttribute());
            break;
          }
        }
      }

      // have to set min, max and num bins (using heuristic from AttSummPanel).
      // Make sure
      // to set series length to num instances - num missing values for att
      stats = masterInstances.attributeStats(attIndex);
      /*
       * numericStats = stats.numericStats; //numericStats.calculateDerived();
       * intervalWidth = StrictMath.max(1, 3.49 * numericStats.stdDev *
       * StrictMath.pow(masterInstances.numInstances(), (-1.0/3.0)));
       */

      double[] seriesVals = new double[masterInstances.numInstances()
          - stats.missingCount];
      int count = 0;
      for (int i = 0; i < masterInstances.numInstances(); i++) {
        Instance current = masterInstances.instance(i);
        if (!current.isMissing(attIndex)) {
          seriesVals[count++] = current.value(attIndex);
        }
      }

      dataset.addSeries(masterSeriesTitle, seriesVals, numBins,
          numericStats.min, numericStats.max);

      // any subsequent series
      for (int i = 1; i < series.size(); i++) {
        Instances nextSeries = series.get(i);

        String seriesTitle = nextSeries.relationName();

        if (seriesAreClasses) {
          for (int j = 0; j < nextSeries.numInstances(); j++) {
            Instance current = nextSeries.instance(j);
            if (!current.isMissing(nextSeries.classAttribute())) {
              seriesTitle = current.stringValue(nextSeries.classAttribute());
              break;
            }
          }
        }

        stats = nextSeries.attributeStats(attIndex);
        /*
         * numericStats = stats.numericStats; //
         * numericStats.calculateDerived(); intervalWidth = StrictMath.max(1,
         * 3.49 * numericStats.stdDev *
         * StrictMath.pow(masterInstances.numInstances(), (-1.0/3.0))); range =
         * numericStats.max - numericStats.min; numBins = StrictMath.max(1,
         * (int) StrictMath.round(range / intervalWidth));
         */
        seriesVals = new double[nextSeries.numInstances() - stats.missingCount];
        count = 0;
        for (int j = 0; j < nextSeries.numInstances(); j++) {
          Instance current = nextSeries.instance(j);
          if (!current.isMissing(attIndex)) {
            seriesVals[count++] = current.value(attIndex);
          }
        }

        dataset.addSeries(seriesTitle, seriesVals, numBins, numericStats.min,
            numericStats.max);
      }

      JFreeChart chart = ChartFactory.createHistogram(plotTitle,
          masterInstances.attribute(attIndex).name(), null, dataset,
          PlotOrientation.VERTICAL, true, false, false);

      // chart.setBackgroundPaint(java.awt.Color.white);
      XYPlot xyplot = (XYPlot) chart.getPlot();
      xyplot.setForegroundAlpha(0.50F);
      XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
      xybarrenderer.setDrawBarOutline(false);
      xybarrenderer.setShadowVisible(false);

      BufferedImage image = chart.createBufferedImage(width, height);
      return image;
    }
  }

  /**
   * Render an XY line chart
   * 
   * @param width the width of the resulting chart in pixels
   * @param height the height of the resulting chart in pixels
   * @param series a list of Instances - one for each series to be plotted
   * @param xAxis the name of the attribute for the x-axis (all series Instances
   *          are expected to have an attribute of the same type with this name)
   * @param yAxis the name of the attribute for the y-axis (all series Instances
   *          are expected to have an attribute of the same type with this name)
   * @param optionalArgs optional arguments to the renderer (may be null)
   * 
   * @return a BufferedImage containing the chart
   * @throws Exception if there is a problem rendering the chart
   */
  public BufferedImage renderXYLineChart(int width, int height,
      List<Instances> series, String xAxis, String yAxis,
      List<String> optionalArgs) throws Exception {

    String plotTitle = "Line Chart";
    String userTitle = getOption(optionalArgs, "-title");
    plotTitle = (userTitle != null) ? userTitle : plotTitle;
    String colorAtt = getOption(optionalArgs, "-color");

    if (series.size() == 1 & colorAtt != null && colorAtt.length() > 0) {
      int colIndex = getIndexOfAttribute(series.get(0), colorAtt);
      if (colIndex >= 0 && series.get(0).attribute(colIndex).isNominal()) {
        // split single series out into multiple instances objects - one
        // per class
        series = splitToClasses(series.get(0), colIndex);
        for (Instances insts : series) {
          insts.setClassIndex(colIndex);
        }
      }
    }

    Instances masterInstances = series.get(0);
    int xAx = getIndexOfAttribute(masterInstances, xAxis);
    int yAx = getIndexOfAttribute(masterInstances, yAxis);
    if (xAx < 0) {
      xAx = 0;
    }
    if (yAx < 0) {
      yAx = 0;
    }

    // Set the axis names just in case we've been supplied with
    // /first, /last or /<num>
    xAxis = masterInstances.attribute(xAx).name();
    yAxis = masterInstances.attribute(yAx).name();

    XYSeriesCollection xyDataset = new XYSeriesCollection();
    // add master series
    XYSeries master = new XYSeries(masterInstances.relationName(), false);
    for (int i = 0; i < masterInstances.numInstances(); i++) {
      Instance inst = masterInstances.instance(i);
      if (!inst.isMissing(xAx) && !inst.isMissing(yAx)) {
        master.add(inst.value(xAx), inst.value(yAx));
      }
    }
    xyDataset.addSeries(master);

    // remaining series
    for (int i = 1; i < series.size(); i++) {
      Instances aSeriesI = series.get(i);
      XYSeries aSeriesJ = new XYSeries(aSeriesI.relationName(), false);
      for (int j = 0; j < aSeriesI.numInstances(); j++) {
        Instance inst = aSeriesI.instance(j);
        if (!inst.isMissing(xAx) && !inst.isMissing(yAx)) {
          aSeriesJ.add(inst.value(xAx), inst.value(yAx));
        }
      }
      xyDataset.addSeries(aSeriesJ);
    }

    JFreeChart chart = ChartFactory.createXYLineChart(userTitle, xAxis, yAxis,
        xyDataset, PlotOrientation.VERTICAL, true, false, false);
    chart.setBackgroundPaint(java.awt.Color.white);

    BufferedImage image = chart.createBufferedImage(width, height);

    return image;
  }

  /**
   * Render an XY scatter plot
   * 
   * @param width the width of the resulting chart in pixels
   * @param height the height of the resulting chart in pixels
   * @param series a list of Instances - one for each series to be plotted
   * @param xAxis the name of the attribute for the x-axis (all series Instances
   *          are expected to have an attribute of the same type with this name)
   * @param yAxis the name of the attribute for the y-axis (all series Instances
   *          are expected to have an attribute of the same type with this name)
   * @param optionalArgs optional arguments to the renderer (may be null)
   * 
   * @return a BufferedImage containing the chart
   * @throws Exception if there is a problem rendering the chart
   */
  public BufferedImage renderXYScatterPlot(int width, int height,
      List<Instances> series, String xAxis, String yAxis,
      List<String> optionalArgs) throws Exception {

    String plotTitle = "Scatter Plot";
    String userTitle = getOption(optionalArgs, "-title");
    plotTitle = (userTitle != null) ? userTitle : plotTitle;
    String colorAtt = getOption(optionalArgs, "-color");

    if (series.size() == 1 && colorAtt != null && colorAtt.length() > 0) {
      int colIndex = getIndexOfAttribute(series.get(0), colorAtt);
      if (colIndex >= 0 && series.get(0).attribute(colIndex).isNominal()) {
        // split single series out into multiple instances objects - one
        // per class
        series = splitToClasses(series.get(0), colIndex);
        for (Instances insts : series) {
          insts.setClassIndex(colIndex);
        }
      }
    }

    Instances masterInstances = series.get(0);
    int xAx = getIndexOfAttribute(masterInstances, xAxis);
    int yAx = getIndexOfAttribute(masterInstances, yAxis);
    if (xAx < 0) {
      xAx = 0;
    }
    if (yAx < 0) {
      yAx = 0;
    }

    // Set the axis names just in case we've been supplied with
    // /first, /last or /<num>
    xAxis = masterInstances.attribute(xAx).name();
    yAxis = masterInstances.attribute(yAx).name();

    // look for an additional attribute that stores the
    // shape sizes - could be either nominal or numeric errors.
    // We only use numeric error information
    String shapeSize = getOption(optionalArgs, "-shapeSize");

    boolean nominalClass = (masterInstances.classIndex() >= 0 && masterInstances
        .classAttribute().isNominal());

    int shapeSizeI = -1;
    if (shapeSize != null && shapeSize.length() > 0) {
      shapeSizeI = getIndexOfAttribute(masterInstances, shapeSize);
    }

    AbstractIntervalXYDataset xyDataset = null;

    if (shapeSizeI < 0 || nominalClass) {
      xyDataset = new XYSeriesCollection();
    } else {
      xyDataset = new XYIntervalSeriesCollection();
    }
    // add master series
    Series master = null;

    if (shapeSizeI < 0 || nominalClass) {
      master = new XYSeries(masterInstances.relationName());
    } else {
      master = new XYIntervalSeries(masterInstances.relationName());
    }
    AttributeStats xStats = masterInstances.attributeStats(xAx);
    AttributeStats yStats = masterInstances.attributeStats(yAx);
    double sizeRange = 0;
    double sizeMin = 0;
    if (shapeSizeI >= 0 && !nominalClass) {
      AttributeStats sStats = masterInstances.attributeStats(shapeSizeI);
      sizeRange = sStats.numericStats.max - sStats.numericStats.min;
      sizeMin = sStats.numericStats.min;
    }
    double xRange = 0;
    if (masterInstances.attribute(xAx).isNominal()) {
      xRange = masterInstances.attribute(xAx).numValues();
    } else {
      xRange = xStats.numericStats.max - xStats.numericStats.min;
    }
    double yRange = 0;
    if (masterInstances.attribute(yAx).isNominal()) {
      xRange = masterInstances.attribute(yAx).numValues();
    } else {
      yRange = yStats.numericStats.max - yStats.numericStats.min;
    }
    for (int i = 0; i < masterInstances.numInstances(); i++) {
      Instance inst = masterInstances.instance(i);
      if (!inst.isMissing(xAx) && !inst.isMissing(yAx)) {
        if (shapeSizeI < 0 || nominalClass) {
          ((XYSeries) master).add(inst.value(xAx), inst.value(yAx));
        } else {
          double xBar = (inst.value(shapeSizeI) - sizeMin) / sizeRange;
          xBar *= (xRange / 5.0); // max of 1/5th the x range
          double yBar = (inst.value(shapeSizeI) - sizeMin) / sizeRange;
          yBar *= (yRange / 5.0);
          double x = inst.value(xAx);
          double y = inst.value(yAx);
          ((XYIntervalSeries) master).add(x, x - (xBar / 2.0),
              x + (xBar / 2.0), y, y - (yBar / 2.0), y + (yBar / 2.0));
        }
      }
    }

    if (shapeSizeI < 0 || nominalClass) {
      ((XYSeriesCollection) xyDataset).addSeries((XYSeries) master);
    } else {
      ((XYIntervalSeriesCollection) xyDataset)
          .addSeries((XYIntervalSeries) master);
    }

    // remaining series
    for (int i = 1; i < series.size(); i++) {
      Instances aSeriesI = series.get(i);
      Series aSeriesJ = null;
      if (shapeSizeI < 0 || nominalClass) {
        aSeriesJ = new XYSeries(aSeriesI.relationName());
      } else {
        aSeriesJ = new XYIntervalSeries(aSeriesI.relationName());
      }
      for (int j = 0; j < aSeriesI.numInstances(); j++) {
        Instance inst = aSeriesI.instance(j);
        if (!inst.isMissing(xAx) && !inst.isMissing(yAx)) {
          if (shapeSizeI < 0 || nominalClass) {
            ((XYSeries) aSeriesJ).add(inst.value(xAx), inst.value(yAx));
          } else {
            double xBar = (inst.value(shapeSizeI) - sizeMin) / sizeRange;
            xBar *= (xRange / 5.0); // max of 1/10th the x range
            double yBar = (inst.value(shapeSizeI) - sizeMin) / sizeRange;
            yBar *= (yRange / 5.0);
            double x = inst.value(xAx);
            double y = inst.value(yAx);
            ((XYIntervalSeries) aSeriesJ).add(x, x - (xBar / 2.0), x
                + (xBar / 2.0), y, y - (yBar / 2.0), y + (yBar / 2.0));
          }
        }
      }

      if (shapeSizeI < 0 || nominalClass) {
        ((XYSeriesCollection) xyDataset).addSeries((XYSeries) aSeriesJ);
      } else {
        ((XYIntervalSeriesCollection) xyDataset)
            .addSeries((XYIntervalSeries) aSeriesJ);
      }
    }

    JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xAxis, yAxis,
        xyDataset, PlotOrientation.VERTICAL, true, false, false);
    chart.setBackgroundPaint(java.awt.Color.white);
    XYPlot plot = (XYPlot) chart.getPlot();
    if (shapeSizeI < 0 || nominalClass) {
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
      renderer.setBaseShapesFilled(false);
      plot.setRenderer(renderer);
    } else {
      XYErrorRenderer renderer = new XYErrorRenderer();
      renderer.setDrawXError(true);
      renderer.setDrawYError(true);
      renderer.setBaseLinesVisible(false);
      plot.setRenderer(renderer);
    }

    BufferedImage image = chart.createBufferedImage(width, height);

    return image;
  }

  /**
   * The name of this off screen renderer
   * 
   * @return the name of this off screen renderer
   */
  public String rendererName() {
    return "JFreeChart Chart Renderer";
  }

  /**
   * Gets a short list of additional options (if any), suitable for displaying
   * in a tip text, in HTML form
   * 
   * @return additional options description in simple HTML form
   */
  @Override
  public String optionsTipTextHTML() {
    return "<html><ul><li>-title=[chart title]</li>"
        + "<li>-color=[coloring/class attribute name]</li>"
        + "<li>-pareto (nominal bar chart only)</li></html>";
  }
}

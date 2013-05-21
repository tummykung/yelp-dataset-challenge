package tpp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;

import weka.core.Attribute;
import weka.core.Instances;

/**
 * A button used to select points based on the value of a particular attribute.
 * If the attribute is nominal then the points are selected by the class value.
 * If the attribute is numeric then the points are selected by numeric range.
 * 
 */
public class SelectButton extends JButton implements ActionListener {

	private String value;
	private double max;
	private double min;
	private ScatterPlotModel spModel;

	/**
	 * Construct a button representing a particular class
	 * 
	 * @param spmodel
	 */
	public SelectButton(ScatterPlotModel spModel, String value) {
		super(value);
		this.value = value;
		this.spModel = spModel;
		this.addActionListener(this);
	}

	/**
	 * Construct a button representing a numeric range
	 * 
	 * @param spmodel
	 */
	public SelectButton(ScatterPlotModel spModel, double min, double max) {
		super();
		DecimalFormat d2 = new DecimalFormat("0.00");
		setText(d2.format(min) + " - " + d2.format(max));
		this.min = min;
		this.max = max;
		this.spModel = spModel;
		this.addActionListener(this);

	}

	public String getValue() {
		return value;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	/**
	 * Build a set of buttons for selecting by values of an attribute. If the
	 * selection attribute is nominal then there will be one button per class.
	 * If the attribute is numeric then the range will be divided into the
	 * number of intervals in the current colour scheme. If the attribute is
	 * null return an empty vector.
	 */
	public static Vector<SelectButton> buildSelectButtons(ScatterPlotModel spModel) {

		Attribute at = spModel.getSelectAttribute();
		ColourScheme colours = spModel.getColours();
		Instances instances = spModel.getInstances();

		Vector<SelectButton> buttons = new Vector<SelectButton>();
		SelectButton button;

		if (at == null)
			return buttons;

		if (at.isNominal()) {
			Enumeration classValues = at.enumerateValues();
			int b = 0;
			String classValue;
			while (classValues.hasMoreElements()) {
				classValue = (String) classValues.nextElement();
				button = new SelectButton(spModel, classValue);
				button.setForeground(colours.getClassificationColor(b++));
				button.setBackground(colours.getBackgroundColor());
				button.setToolTipText("Select all points of class " + classValue);
				buttons.add(button);
			}
		}

		if (at.isNumeric()) {
			int n = colours.getSpectrum().length;

			// Find the range of the attribute
			// if the color attribute is numeric then find its range
			double atMin = instances.instance(0).value(at);
			double atMax = instances.instance(0).value(at);
			double v;
			for (int i = 1; i < instances.numInstances(); i++) {
				v = instances.instance(i).value(at);
				if (v > atMax)
					atMax = v;
				if (v < atMin)
					atMin = v;
			}

			DecimalFormat d2 = new DecimalFormat("0.00");
			double step = (atMax - atMin) / n;
			for (int i = 0; i < n; i++) {
				button = new SelectButton(spModel, atMin + step * i, atMin + step * (i + 1));
				button.setBackground(colours.getBackgroundColor());
				button.setForeground(colours.getSpectrum()[i]);
				button.setToolTipText("Select all points for which the (normalised) value of attribute " + at.name()
						+ " is in the range [" + d2.format(button.getMin()) + "," + d2.format(button.getMax()) + "].");
				buttons.add(button);
			}
		}
		return buttons;

	}

	public void actionPerformed(ActionEvent event) {

		// if teh control key is simultaneously pressed then added the
		// points to the selection
		boolean addToExistingSelection = (event.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK;

		// find which attribute to use to select points by
		// and select all the points according to the button value
		if (spModel.getSelectAttribute().isNominal()) {
			String value = ((SelectButton) event.getSource()).getValue();
			spModel.selectPointsByClassValue(value, addToExistingSelection);
		}

		if (spModel.getSelectAttribute().isNumeric()) {
			double min = ((SelectButton) event.getSource()).getMin();
			double max = ((SelectButton) event.getSource()).getMax();
			spModel.selectPointsByNumericRange(min, max, addToExistingSelection);
		}

		spModel.drawRectangleAroundSelectedPoints();
	}
}

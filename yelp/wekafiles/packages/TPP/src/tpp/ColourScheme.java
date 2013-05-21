package tpp;

import java.awt.Color;
import java.io.Serializable;

/**
 * A ColourScheme is made up of a palette of colors for drawing a TPP graph.
 * Objects can be colored in one of two ways: unordered or ordered. The first
 * are suitable for nominal classifications, the second for numeric variables
 * with a lower and upper bound.
 * 
 * TODO implement the other color schemes from here:http://colorbrewer2.org/
 */
public class ColourScheme implements Serializable {

	/** The margin of error when calculating a value in a range */
	private static final double MARGIN = 0.00001;

	/**
	 * The number of colors in the spectrum. In fact this is the number of
	 * intervals in a half-range. So if INTERVAL=3 there will be 7 colors in a
	 * bipolar spectrum (+ve and -ve).
	 */
	public static final int INTERVALS = 3;
	private static final float INTERVALSf = INTERVALS * 1f;

	public static final ColourScheme DARK = new ColourScheme(Color.black, new Color(0xC6D9EC), new Color(0x8CB3D9),
			new Color(50, 136, 189), new Color(189, 189, 189), new Color(213, 62, 79), new Color[] {
					new Color(228, 26, 28), new Color(55, 126, 184), new Color(77, 175, 74), new Color(152, 78, 163),
					new Color(255, 127, 0), new Color(255, 255, 51), new Color(166, 86, 40), new Color(247, 129, 191),
					new Color(153, 153, 153) }, "Dark background");

	public static final ColourScheme LIGHT = new ColourScheme(Color.white, Color.black, Color.gray, new Color(50, 136,
			189), Color.gray, new Color(213, 62, 79), new Color[] { new Color(228, 26, 28), new Color(55, 126, 184),
			new Color(77, 175, 74), new Color(152, 78, 163), new Color(255, 127, 0), new Color(255, 255, 51),
			new Color(166, 86, 40), new Color(247, 129, 191), new Color(153, 153, 153) }, "Light background");

	Color backgroundColor;
	Color foregroundColor;
	Color axesColor;
	Color minColor;
	Color maxColor;
	Color midColor;
	Color[] classificationColors;
	Color[] spectrumColors;
	String description;

	private ColourScheme(Color backgroundColor, Color foregroundColor, Color axesColor, Color minColor, Color midColor,
			Color maxColor, Color[] classColors, String description) {
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.axesColor = axesColor;
		this.classificationColors = classColors;

		// min mid and max are the colors denoting the minimum, middle, and
		// maximum values on the ordered range
		this.minColor = minColor;
		this.midColor = midColor;
		this.maxColor = maxColor;
		this.description = description;
		initSpectrum();
	}

	private void initSpectrum() {

		// The spectrum is symmetric around the mid color.
		spectrumColors = new Color[2 * INTERVALS + 1];

		// get the components of the min, mid and max
		int rMin = minColor.getRed();
		int rMid = midColor.getRed();
		int rMax = maxColor.getRed();
		int gMin = minColor.getGreen();
		int gMid = midColor.getGreen();
		int gMax = maxColor.getGreen();
		int bMin = minColor.getBlue();
		int bMid = midColor.getBlue();
		int bMax = maxColor.getBlue();

		for (int c = 1; c <= INTERVALS; c++) {

			// the positive colors
			spectrumColors[INTERVALS + c] = new Color(Math.round(rMid + (c / INTERVALSf) * (rMax - rMid)),
					Math.round(gMid + (c / INTERVALSf) * (gMax - gMid)), Math.round(bMid + (c / INTERVALSf)
							* (bMax - bMid)));

			// the negative colors
			spectrumColors[INTERVALS - c] = new Color(Math.round(rMid - (c / INTERVALSf) * (rMid - rMin)),
					Math.round(gMid - (c / INTERVALSf) * (gMid - gMin)), Math.round(bMid - (c / INTERVALSf)
							* (bMid - bMin)));
		}
		spectrumColors[INTERVALS] = midColor;
	}

	public Color getAxesColor() {
		return axesColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color[] getClassificationColors() {
		return classificationColors;
	}

	public Color getClassificationColor(int c) {
		return classificationColors[c % classificationColors.length];
	}

	/**
	 * Get a color for representing a value of c within a range of
	 * [lower,upper].
	 */
	public Color getColorFromSpectrum(double c, double lowerBound, double upperBound) {
		if (c > upperBound - MARGIN)
			return spectrumColors[2 * INTERVALS];
		if (c < lowerBound + MARGIN)
			return spectrumColors[0];
		int i;
		double step = (upperBound - lowerBound) / (2f * INTERVALSf + 1);
		i = (int) Math.floor((c - lowerBound) / step);
		// System.out.println(c + " in [" + lowerBound + "," + upperBound +
		// "] = "
		// + i);
		return spectrumColors[i];
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public String toString() {
		return description + " background=" + backgroundColor;
	}

	public Color[] getSpectrum() {
		return spectrumColors;
	}
}

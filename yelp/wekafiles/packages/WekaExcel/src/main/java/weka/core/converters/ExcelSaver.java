/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ExcelSaver.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core.converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.Capabilities.Capability;

/**
 <!-- globalinfo-start -->
 * Writes to a destination that is in MS Excel spreadsheet format (97-2007).<br/>
 * For instance for spreadsheets that can be read with the Microsoft Office Suite.
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 *
 * <pre> -M &lt;str&gt;
 *  The string representing a missing value.
 *  (default: ?)</pre>
 *
 * <pre> -i &lt;the input file&gt;
 *  The input file</pre>
 *
 * <pre> -o &lt;the output file&gt;
 *  The output file</pre>
 *
 <!-- options-end -->
 *
 * <p/>
 *
 * For a tutorial on Apache POI/HSSF, see: <br/>
 * <a href="http://poi.apache.org/spreadsheet/how-to.html"
 * target="_blank">http://poi.apache.org/spreadsheet/how-to.html</a>
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 8109 $
 * @see Saver
 */
public class ExcelSaver
  extends AbstractFileSaver
  implements BatchConverter {

  /** for serialization. */
  private static final long serialVersionUID = -7446832500561589653L;

  /** The placeholder for missing values. */
  protected String m_MissingValue = "";

  /** whether to use OOXML. */
  protected boolean m_UseOOXML;

  /**
   * Constructor.
   */
  public ExcelSaver() {
    resetOptions();
  }

  /**
   * Returns a string describing this Saver.
   *
   * @return 		a description of the Saver suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return
        "Writes to a destination that is in MS Excel spreadsheet format (97-2007).\n"
      + "For instance for spreadsheets that can be read with the Microsoft Office Suite.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
    Vector<Option> result = new Vector<Option>();

    result.addElement(new Option(
        "\tThe string representing a missing value.\n"
        + "\t(default: ?)",
        "M", 1, "-M <str>"));

    Enumeration en = super.listOptions();
    while (en.hasMoreElements())
      result.addElement((Option)en.nextElement());

    return result.elements();
  }

  /**
   * Parses a given list of options. <p/>
   *
   <!-- options-start -->
   * Valid options are: <p/>
   *
   * <pre> -M &lt;str&gt;
   *  The string representing a missing value.
   *  (default: ?)</pre>
   *
   * <pre> -i &lt;the input file&gt;
   *  The input file</pre>
   *
   * <pre> -o &lt;the output file&gt;
   *  The output file</pre>
   *
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    String	tmpStr;

    super.setOptions(options);

    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0)
      setMissingValue(tmpStr);
  }

  /**
   * Gets the current settings of the Classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String[] getOptions() {
    Vector<String>	result;
    String[]		options;
    int			i;

    result  = new Vector<String>();

    result.add("-M");
    result.add(getMissingValue());

    options = super.getOptions();
    for (i = 0; i < options.length; i++)
      result.add(options[i]);

    return result.toArray(new String[result.size()]);
  }

  /**
   * Sets the placeholder for missing values.
   *
   * @param value	the placeholder
   */
  public void setMissingValue(String value) {
    m_MissingValue = value;
  }

  /**
   * Returns the current placeholder for missing values.
   *
   * @return		the placeholder
   */
  public String getMissingValue() {
    return m_MissingValue;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   *         		displaying in the explorer/experimenter gui
   */
  public String missingValueTipText() {
    return "The placeholder for missing values, default is '' (empty cell).";
  }

  /**
   * Resets the Saver.
   */
  public void resetOptions() {
    super.resetOptions();

    setUseOOXML(false);
    setFileExtension(ExcelLoader.FILE_EXTENSION);
    setMissingValue("");
  }

  /**
   * Returns a description of the file type.
   *
   * @return a short file description
   */
  public String getFileDescription() {
    return ExcelLoader.FILE_DESCRIPTION;
  }

  /**
   * Gets all the file extensions used for this type of file.
   *
   * @return the file extensions
   */
  public String[] getFileExtensions() {
    return new String[]{ExcelLoader.FILE_EXTENSION, ExcelLoader.FILE_EXTENSION_OOXML};
  }

  /**
   * Sets whether to use OOXML or binary format.
   *
   * @param value	if true then OOXML format is used
   */
  public void setUseOOXML(boolean value) {
    m_UseOOXML = value;
  }

  /**
   * Returns whether to use OOXML or binary format.
   *
   * @return		true if OOXML is used
   */
  public boolean retrieveUseOOXML() {
    return m_UseOOXML;
  }

  /**
   * Sets the destination file.
   *
   * @param outputFile the destination file.
   * @throws IOException throws an IOException if file cannot be set
   */
  public void setFile(File outputFile) throws IOException {
    super.setFile(outputFile);
    if (outputFile.getName().toLowerCase().endsWith(ExcelLoader.FILE_EXTENSION_OOXML))
      setUseOOXML(true);
  }

  /**
   * Returns the Capabilities of this saver.
   *
   * @return            the capabilities of this object
   * @see               Capabilities
   */
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();

    // attributes
    result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.STRING_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);

    // class
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.NUMERIC_CLASS);
    result.enable(Capability.STRING_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    result.enable(Capability.NO_CLASS);

    return result;
  }

  /**
   * Writes a Batch of instances.
   *
   * @throws IOException 	throws IOException if saving in batch mode
   * 				is not possible
   */
  public void writeBatch() throws IOException {
    if (getInstances() == null)
      throw new IOException("No instances to save");

    if (getRetrieval() == INCREMENTAL)
      throw new IOException("Batch and incremental saving cannot be mixed.");

    setRetrieval(BATCH);
    setWriteMode(WRITE);

    try {
      Instances data = getInstances();
      Workbook workbook;
      if (retrieveUseOOXML())
	workbook = new XSSFWorkbook();
      else
	workbook = new HSSFWorkbook();
      Sheet sheet = workbook.createSheet();
      workbook.setSheetName(0, data.relationName());
      Row row;
      Cell cell;
      Instance inst;

      // header
      row = sheet.createRow(0);
      for (int i = 0; i < data.numAttributes(); i++) {
	cell = row.createCell(i);
	cell.setCellValue(data.attribute(i).name());
      }

      // data
      for (int n = 0; n < data.numInstances(); n++) {
	row  = sheet.createRow(n + 1);
	inst = data.instance(n);
	for (int i = 0; i < data.numAttributes(); i++) {
	  cell = row.createCell(i);

	  if (inst.isMissing(i)) {
	    if (m_MissingValue.length() > 0)
	      cell.setCellValue(m_MissingValue);
	    else
	      cell.setCellType(Cell.CELL_TYPE_BLANK);
	    continue;
	  }

	  switch (data.attribute(i).type()) {
	    case Attribute.NUMERIC:
	      cell.setCellValue(inst.value(i));
	      break;

	    case Attribute.NOMINAL:
	    case Attribute.STRING:
	      cell.setCellValue(inst.stringValue(i));
	      break;

	    default:
	      throw new IllegalStateException("Unhandled attribute type: " + data.attribute(i).type());
	  }
	}
      }

      // save
      if (retrieveFile() == null) {
        workbook.write(System.out);
      }
      else {
	OutputStream out = new FileOutputStream(retrieveFile());
	workbook.write(out);
        out.close();
      }
    }
    catch (Exception e) {
      throw new IOException(e);
    }

    setWriteMode(WAIT);
    resetWriter();
    setWriteMode(CANCEL);
  }

  /**
   * Returns the revision string.
   *
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 8109 $");
  }

  /**
   * Main method.
   *
   * @param args 	should contain the options of a Saver.
   */
  public static void main(String[] args) {
    runFileSaver(new ExcelSaver(), args);
  }
}

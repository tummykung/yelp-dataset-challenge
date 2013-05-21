package tpp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import weka.core.Instances;

/**
 * A Frame that allows the user to manipulate views of a (categorised) data set
 * loaded from a data file.
 */
public class TPPFrame extends JFrame implements ActionListener {

	private static final String FRAME_TITLE = "Targeted Projection Pursuit";

	// TODO the default dimension should be determined by the size of the demo
	// slides
	private static final Dimension DEFAULT_DIMENSION = new Dimension(980, 660);

	static final int NO_CLASSIFICATION = -1;

	private ScatterPlotModel model;

	private JMenuBar bar = null;

	private JMenu viewMenu = null;

	private JMenuItem rescaleMenuItem = null;

	private JMenu fileMenu = null;

	private JMenuItem openARFFFileMenuItem = null;

	private JMenuItem openCSVFileMenuItem = null;

	private JMenuItem openALNFileMenuItem = null;

	private JMenuItem openFASTAFileMenuItem = null;

	private JMenuItem pcaViewMenuItem = null;

	private JMenuItem saveNormalisedViewMenuItem = null;

	private JMenuItem saveProjectionMenuItem = null;

	private JMenuItem saveViewDataMenuItem = null;

	private JMenuItem randomViewMenuItem = null;

	private JRadioButtonMenuItem showAxesMenuItem = null;

	private JRadioButtonMenuItem showHierarchicalClusteringMenuItem = null;

	ScatterPlotViewPanel viewPanel;

	ScatterPlotControlPanel controlPanel = null;

	private JMenuItem darkBackgroundMenuItem;

	private JRadioButtonMenuItem showTargetMenuItem;

	private JMenuItem saveSVGMenuItem, saveEPSMenuItem;

	private JMenuItem addNoiseMenuItem;

	private JMenuItem loadGraphMenuItem;

	private JMenuItem removeGraphMenuItem;

	private JMenuItem showDataViewerMenuItem;

	private DataViewer dataViewer;

	private JMenuItem helpMenuItem;

	public TPPFrame() {
		super(FRAME_TITLE);
		// LicenseChecker license = new LicenseChecker();
		// boolean licensed = license.retrieveAndCheckLicense(this);
		// System.out.println(license.getStatusMessage());
		// if (!licensed)
		// return;
		// license = null;
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			System.out.println(e);
		}
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setJMenuBar(getBar());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(
				(int) (screenSize.getWidth() - DEFAULT_DIMENSION.getWidth()) / 2,
				(int) (screenSize.getHeight() - DEFAULT_DIMENSION.getHeight()) / 2);
		this.setSize(DEFAULT_DIMENSION);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * This method initializes bar
	 * 
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getBar() {
		if (bar == null) {
			bar = new JMenuBar();
			bar.add(getFileMenu());
			bar.add(getViewMenu());
			bar.add(getHelpMenuItem());
		}
		return bar;
	}

	private JMenuItem getHelpMenuItem() {
		if (helpMenuItem == null) {
			helpMenuItem = new JMenuItem();
			helpMenuItem.setText("Help");
			helpMenuItem.addActionListener(this);
		}
		return helpMenuItem;

	}

	/**
	 * This method initializes fileMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
			fileMenu.add(getOpenARFFFileMenuItem());
			fileMenu.add(getOpenCSVFileMenuItem());
			fileMenu.add(getOpenFASTAFileMenuItem());
			fileMenu.add(getOpenALNFileMenuItem());
			fileMenu.add(getSaveNormalisedViewMenuItem());
			fileMenu.add(getSaveProjectionMenuItem());
			fileMenu.add(getSaveViewDataMenuItem());
			fileMenu.add(getSaveSVGMenuItem());
			fileMenu.add(getSaveEPSMenuItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes openViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenARFFFileMenuItem() {
		if (openARFFFileMenuItem == null) {
			openARFFFileMenuItem = new JMenuItem();
			openARFFFileMenuItem.setText("Load data from ARFF file");
			openARFFFileMenuItem.addActionListener(this);
		}
		return openARFFFileMenuItem;
	}

	/**
	 * This method initializes openViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenCSVFileMenuItem() {
		if (openCSVFileMenuItem == null) {
			openCSVFileMenuItem = new JMenuItem();
			openCSVFileMenuItem.setText("Load data from CSV file");
			openCSVFileMenuItem.addActionListener(this);
		}
		return openCSVFileMenuItem;
	}

	/**
	 * This method initializes openViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenFASTAFileMenuItem() {
		if (openFASTAFileMenuItem == null) {
			openFASTAFileMenuItem = new JMenuItem();
			openFASTAFileMenuItem.setText("Load data from FASTA file");
			openFASTAFileMenuItem.addActionListener(this);
		}
		return openFASTAFileMenuItem;
	}

	/**
	 * This method initializes openViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenALNFileMenuItem() {
		if (openALNFileMenuItem == null) {
			openALNFileMenuItem = new JMenuItem();
			openALNFileMenuItem.setText("Load data from CLUSTAL (.aln) file");
			openALNFileMenuItem.addActionListener(this);
		}
		return openALNFileMenuItem;
	}

	/**
	 * This method initializes saveNormalisedViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveNormalisedViewMenuItem() {
		if (saveNormalisedViewMenuItem == null) {
			saveNormalisedViewMenuItem = new JMenuItem();
			saveNormalisedViewMenuItem.setText("Save normalised data");
			saveNormalisedViewMenuItem.setEnabled(false);
			saveNormalisedViewMenuItem.addActionListener(this);
		}
		return saveNormalisedViewMenuItem;
	}

	/**
	 * This method initializes saveProjectionMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveProjectionMenuItem() {
		if (saveProjectionMenuItem == null) {
			saveProjectionMenuItem = new JMenuItem();
			saveProjectionMenuItem.setText("Save current projection");
			saveProjectionMenuItem.setEnabled(false);
			saveProjectionMenuItem.addActionListener(this);
		}
		return saveProjectionMenuItem;
	}

	/**
	 * This method initializes saveViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveViewDataMenuItem() {
		if (saveViewDataMenuItem == null) {
			saveViewDataMenuItem = new JMenuItem();
			saveViewDataMenuItem.setText("Save current view data");
			saveViewDataMenuItem.setEnabled(false);
			saveViewDataMenuItem.addActionListener(this);
		}
		return saveViewDataMenuItem;
	}

	/**
	 * This method initializes saveViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveSVGMenuItem() {
		if (saveSVGMenuItem == null) {
			saveSVGMenuItem = new JMenuItem();
			saveSVGMenuItem.setText("Save current view as SVG image");
			saveSVGMenuItem.setEnabled(false);
			saveSVGMenuItem.addActionListener(this);
		}
		return saveSVGMenuItem;
	}

	/**
	 * This method initializes saveViewMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveEPSMenuItem() {
		if (saveEPSMenuItem == null) {
			saveEPSMenuItem = new JMenuItem();
			saveEPSMenuItem.setText("Save current view as EPS image");
			saveEPSMenuItem.setEnabled(false);
			saveEPSMenuItem.addActionListener(this);
		}
		return saveEPSMenuItem;
	}

	private JMenu getViewMenu() {
		if (viewMenu == null) {
			viewMenu = new JMenu();
			viewMenu.setText("View");
			viewMenu.add(getShowDataViewerMenuItem());
			viewMenu.add(getRescaleMenuItem());
			viewMenu.add(getPCAMenuItem());
			viewMenu.add(getRandomMenuItem());
			viewMenu.add(getShowAxesMenuItem());
			viewMenu.add(getShowHierarchicalClusteringMenuItem());
			viewMenu.add(getAddNoiseMenuItem());
			viewMenu.add(getShowTargetMenuItem());
			viewMenu.add(getDarkBackgroundMenuItem());
			viewMenu.add(getLoadGraphMenuItem());
			viewMenu.add(getRemoveGraphMenuItem());
		}
		return viewMenu;
	}

	private JMenuItem getShowHierarchicalClusteringMenuItem() {
		if (showHierarchicalClusteringMenuItem == null) {
			showHierarchicalClusteringMenuItem = new JRadioButtonMenuItem();
			showHierarchicalClusteringMenuItem
					.setText("Show HierarchicalClustering");
			showHierarchicalClusteringMenuItem.setEnabled(false);
			showHierarchicalClusteringMenuItem.setSelected(false);
			showHierarchicalClusteringMenuItem.addActionListener(this);
		}
		return showHierarchicalClusteringMenuItem;
	}

	private JMenuItem getShowAxesMenuItem() {
		if (showAxesMenuItem == null) {
			showAxesMenuItem = new JRadioButtonMenuItem();
			showAxesMenuItem.setText("Show Axes");
			showAxesMenuItem.setEnabled(false);
			showAxesMenuItem.setSelected(false);
			showAxesMenuItem.addActionListener(this);
		}
		return showAxesMenuItem;
	}

	private JMenuItem getShowDataViewerMenuItem() {
		if (showDataViewerMenuItem == null) {
			showDataViewerMenuItem = new JMenuItem();
			showDataViewerMenuItem.setText("Show Data Viewer");
			showDataViewerMenuItem.setEnabled(false);
			showDataViewerMenuItem.addActionListener(this);
		}
		return showDataViewerMenuItem;
	}

	private JMenuItem getShowTargetMenuItem() {
		if (showTargetMenuItem == null) {
			showTargetMenuItem = new JRadioButtonMenuItem();
			showTargetMenuItem.setText("Show Target");
			showTargetMenuItem.setEnabled(false);
			showTargetMenuItem.setSelected(false);
			showTargetMenuItem.addActionListener(this);
		}
		return showTargetMenuItem;
	}

	private JMenuItem getAddNoiseMenuItem() {
		if (addNoiseMenuItem == null) {
			addNoiseMenuItem = new JRadioButtonMenuItem();
			addNoiseMenuItem.setText("Add noise to view");
			addNoiseMenuItem.setEnabled(false);
			addNoiseMenuItem.setSelected(false);
			addNoiseMenuItem.addActionListener(this);
		}
		return addNoiseMenuItem;
	}

	private JMenuItem getDarkBackgroundMenuItem() {
		if (darkBackgroundMenuItem == null) {
			darkBackgroundMenuItem = new JRadioButtonMenuItem();
			darkBackgroundMenuItem.setText("Dark Background");
			darkBackgroundMenuItem.setEnabled(false);
			darkBackgroundMenuItem.setSelected(true);
			darkBackgroundMenuItem.addActionListener(this);
		}
		return darkBackgroundMenuItem;
	}

	private JMenuItem getPCAMenuItem() {
		if (pcaViewMenuItem == null) {
			pcaViewMenuItem = new JMenuItem();
			pcaViewMenuItem.setText("Principal Components View (X=PC1,Y=PC2)");
			pcaViewMenuItem.setEnabled(false);
			pcaViewMenuItem.addActionListener(this);
		}
		return pcaViewMenuItem;
	}

	private JMenuItem getRandomMenuItem() {
		if (randomViewMenuItem == null) {
			randomViewMenuItem = new JMenuItem();
			randomViewMenuItem.setText("Randomise View");
			randomViewMenuItem.setEnabled(false);
			randomViewMenuItem.addActionListener(this);
		}
		return randomViewMenuItem;
	}

	private JMenuItem getRescaleMenuItem() {
		if (rescaleMenuItem == null) {
			rescaleMenuItem = new JMenuItem();
			rescaleMenuItem.setText("Fit points to window (right button)");
			rescaleMenuItem.setEnabled(false);
			rescaleMenuItem.addActionListener(this);
		}
		return rescaleMenuItem;
	}

	private JMenuItem getLoadGraphMenuItem() {
		if (loadGraphMenuItem == null) {
			loadGraphMenuItem = new JMenuItem();
			loadGraphMenuItem.setText("Show Graph");
			loadGraphMenuItem.addActionListener(this);
		}
		return loadGraphMenuItem;
	}

	private JMenuItem getRemoveGraphMenuItem() {
		if (removeGraphMenuItem == null) {
			removeGraphMenuItem = new JMenuItem();
			removeGraphMenuItem.setText("Remove Graph");
			removeGraphMenuItem.addActionListener(this);
		}
		return removeGraphMenuItem;
	}

	/** Enable those menu items that rely on a data file being currently loaded. */
	private void enableViewMenuItems() {
		getSaveNormalisedViewMenuItem().setEnabled(true);
		getSaveProjectionMenuItem().setEnabled(true);
		getRescaleMenuItem().setEnabled(true);
		getPCAMenuItem().setEnabled(true);
		getSaveViewDataMenuItem().setEnabled(true);
		getSaveSVGMenuItem().setEnabled(true);
		getSaveEPSMenuItem().setEnabled(true);
		getRandomMenuItem().setEnabled(true);
		getShowAxesMenuItem().setEnabled(true);
		getShowHierarchicalClusteringMenuItem().setEnabled(true);
		getShowAxesMenuItem().setSelected(false);
		getShowAxesMenuItem().setEnabled(true);
		getShowTargetMenuItem().setEnabled(true);
		getShowTargetMenuItem().setSelected(false);
		getShowDataViewerMenuItem().setEnabled(true);
		getAddNoiseMenuItem().setEnabled(true);
		getAddNoiseMenuItem().setSelected(false);
		getDarkBackgroundMenuItem().setEnabled(true);
		getDarkBackgroundMenuItem().setSelected(true);
	}

	/**
	 * Disable those menu items that rely on a data file being currently loaded.
	 */
	private void disableViewMenuItems() {
		getSaveNormalisedViewMenuItem().setEnabled(false);
		getSaveProjectionMenuItem().setEnabled(false);
		getSaveViewDataMenuItem().setEnabled(false);
		getSaveSVGMenuItem().setEnabled(false);
		getSaveEPSMenuItem().setEnabled(false);
		getRescaleMenuItem().setEnabled(false);
		getPCAMenuItem().setEnabled(false);
		getRandomMenuItem().setEnabled(false);
		getShowAxesMenuItem().setEnabled(false);
		getShowHierarchicalClusteringMenuItem().setEnabled(false);
		getShowTargetMenuItem().setEnabled(false);
		getAddNoiseMenuItem().setEnabled(false);
		getDarkBackgroundMenuItem().setEnabled(false);
		getShowDataViewerMenuItem().setEnabled(false);
	}

	public void actionPerformed(ActionEvent action) {
		if (action.getSource()==getHelpMenuItem())
			browseHelp();
		if (action.getSource() == getSaveNormalisedViewMenuItem())
			Exporter.saveNormalisedData(model, null);
		if (action.getSource() == getSaveProjectionMenuItem())
			Exporter.saveCurrentProjection(model, null);
		if (action.getSource() == getSaveViewDataMenuItem())
			Exporter.saveCurrentViewDataAsTSV(model, null);
		if (action.getSource() == getSaveSVGMenuItem())
			Exporter.saveViewAsSVGImage(viewPanel, model, null);
		if (action.getSource() == getSaveEPSMenuItem())
			Exporter.saveViewAsEPSImage(viewPanel, model, null);
		try {
			if (action.getSource() == getOpenARFFFileMenuItem())
				setData(new ARFFImporter().importData());
			if (action.getSource() == getOpenCSVFileMenuItem())
				setData(new CSVDataImporter().importData());
			if (action.getSource() == getLoadGraphMenuItem())
				model.loadGraph(new GraphImporter().importGraph());
		} catch (Exception e) {
			System.out.println(e);
		}
		if (action.getSource() == getRescaleMenuItem())
			model.resizePlot();
		if (action.getSource() == getPCAMenuItem())
			model.PCA();
		if (action.getSource() == getRandomMenuItem())
			model.randomProjection();
		if (action.getSource() == getShowAxesMenuItem())
			model.setShowAxes(getShowAxesMenuItem().isSelected());
		if (action.getSource() == getShowHierarchicalClusteringMenuItem())
			model.setShowHierarchicalClustering(getShowHierarchicalClusteringMenuItem()
					.isSelected());
		if (action.getSource() == getShowTargetMenuItem())
			model.setShowTarget(getShowTargetMenuItem().isSelected());
		if (action.getSource() == getDarkBackgroundMenuItem())
			model.setColours(getDarkBackgroundMenuItem().isSelected() ? ColourScheme.DARK
					: ColourScheme.LIGHT);
		if (action.getSource() == getAddNoiseMenuItem())
			viewPanel.addJitter(getAddNoiseMenuItem().isSelected());
		if (action.getSource() == getRemoveGraphMenuItem())
			model.removeGraph();
		if (action.getSource() == getShowDataViewerMenuItem())
			showDataViewer(getShowDataViewerMenuItem().isEnabled());

	}

	private static void browseHelp() {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(TargetedProjectionPursuit.HELP_URL));
			} catch (Exception e) {
				// TODO: error handling
			}
		} else {
			// TODO: error handling
		}
	}
	private void showDataViewer(boolean show) {
		if (show) {
			dataViewer = new DataViewer(model);
		} else {
			if (dataViewer != null) {
				dataViewer.setVisible(false);
				dataViewer.dispose();
			}
		}

	}

	/** Set the model that this window is used to visualise */
	void setData(Instances in) {
		try {
			model = new ScatterPlotModel(2);
			model.setInstances(in);
			viewPanel = new ScatterPlotViewPanel();
			controlPanel = new ScatterPlotControlPanel();
			controlPanel.setModel(model);
			viewPanel.setModel(model);
			ScatterPlotViewPanelMouseListener l = new ScatterPlotViewPanelMouseListener(
					viewPanel, model);
			viewPanel.addMouseListener(l);
			viewPanel.addMouseMotionListener(l);
			setTitle(model.getInstances().relationName());
			enableViewMenuItems();
			JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					viewPanel, controlPanel);
			split.setResizeWeight(0.8);
			setContentPane(split);
			setVisible(true);
			split.setDividerLocation(split.getSize().width - 250);
			Dimension minimumSize = new Dimension(0, 0);
			viewPanel.setMinimumSize(minimumSize);
			controlPanel.setMinimumSize(minimumSize);
			viewPanel.repaint();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"There was a problem reading that data: "+e.getMessage());
		}
	}

	public TPPModel getModel() {
		return model;
	}

}
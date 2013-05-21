package tpp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A panel including buttons that allow the user to select points based on the
 * value of the SelectAttribute
 */
public class SelectionPanel extends JPanel{

	private Vector<SelectButton> selectButtons;

	private ScatterPlotModel spModel;

	private GridBagConstraints grid;

	public SelectionPanel(ScatterPlotModel spModel) {
		super();
		this.spModel = spModel;
		initialiseSelectionButtons();
		setBorder(BorderFactory.createLineBorder(spModel.getColours().getBackgroundColor(), 3));
		setVisible(true);
	}

	public void initialiseSelectionButtons() {
		selectButtons = new Vector<SelectButton>();

		// initialise layout
		removeAll();
		setLayout(new GridBagLayout());
		grid = new GridBagConstraints();
		grid.fill = GridBagConstraints.BOTH;
		grid.weightx = 1.0;

		// Create buttons for individual attribute values
		selectButtons.removeAllElements();

		// include buttons to allow the user to select a classes in the current
		// classification, or a numeric range
		selectButtons = SelectButton.buildSelectButtons(spModel);
		grid.gridy = 0;
		for (SelectButton button : selectButtons) {
			add(button, grid);
			grid.gridy++;
		}

	}


}

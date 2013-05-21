package tpp;

import javax.swing.DefaultListSelectionModel;
import javax.swing.RowSorter;

/** Select rows in the Projection Table based on the selection in the TPP Model */
public class ProjectionTableListSelectionModel extends
		DefaultListSelectionModel {

	// TODO known bug: when the projection tabled is re-ordered, the
	// setSelection method is called with wrong parameters. As a result the
	// selection changes. Not sure why. more investigation needed

	private ScatterPlotModel model;
	private RowSorter<ProjectionTableModel> sorter;

	public ProjectionTableListSelectionModel(ScatterPlotModel tpp,
			RowSorter<ProjectionTableModel> sorter) {
		this.model = tpp;
		this.sorter = sorter;
	}

	@Override
	public void addSelectionInterval(int index0, int index1) {
		for (int i = index0; i <= index1; i++) {
			model.selectAxis(sorter.convertRowIndexToModel(i));
		}
		setAnchorSelectionIndex(index0);
		setLeadSelectionIndex(index1);
		model.drawRectangleAroundSelectedAxes();
		fireValueChanged(false);
	}

	@Override
	public void clearSelection() {
		model.unselectAxes();
		fireValueChanged(false);
	}

	@Override
	public void removeIndexInterval(int index0, int index1) {
		for (int i = index0; i <= index1; i++)
			model.unselectAxis(sorter.convertRowIndexToModel(i));
		setAnchorSelectionIndex(index0);
		setLeadSelectionIndex(index1);
		fireValueChanged(false);
	}

	@Override
	public void setSelectionInterval(int index0, int index1) {
		model.unselectAxes();
		addSelectionInterval(index0, index1);
	}

	@Override
	public int getMaxSelectionIndex() {
		for (int i = model.getNumDataDimensions() - 1; i >= 0; i--)
			if (model.isAxisSelected(sorter.convertRowIndexToModel(i)))
				return i;
		return -1;
	}

	@Override
	public int getMinSelectionIndex() {
		for (int i = 0; i < model.getNumDataDimensions(); i++)
			if (model.isAxisSelected(sorter.convertRowIndexToModel(i)))
				return i;
		return -1;
	}

	@Override
	public boolean isSelectedIndex(int index) {
		return model.isAxisSelected(sorter.convertRowIndexToModel(index));
	}

	@Override
	public boolean isSelectionEmpty() {
		return !model.areAxesSelected();
	}
}

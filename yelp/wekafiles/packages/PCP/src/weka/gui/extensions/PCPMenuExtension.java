package weka.gui.extensions;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import weka.gui.MainMenuExtension;
import weka.gui.extensions.pcp.PCPMenuActionListener;

public class PCPMenuExtension implements MainMenuExtension {

	
	@Override
	public void fillFrame(Component frame) {

	}

	@Override
	public ActionListener getActionListener(final JFrame owner) {
		return new PCPMenuActionListener();
	}
	
	@Override
	public String getMenuTitle() {
		return "Parallel Coordinate Plot";
	}

	@Override
	public String getSubmenuTitle() {
		return "Zelant";
	}

}

package weka.gui.extensions.pcp.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

public class QuitActionListener 
implements ActionListener
{
	private final JFrame m_frame;
	
	public QuitActionListener(JFrame mFrame) {
		super();
		m_frame = mFrame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		m_frame.setVisible(false);
		m_frame.dispose();
	}	
}

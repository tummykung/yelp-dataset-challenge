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
 *    PrefuseGraphViewer.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.gui.beans;

import weka.core.Drawable;
import weka.core.FastVector;
import weka.gui.ResultHistoryPanel;
import weka.gui.graphvisualizer.BIFFormatException;
import weka.gui.visualize.plugins.PrefuseTree;
import weka.gui.visualize.plugins.PrefuseGraph;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A bean encapsulating the prefuse tree and graph visualizer
 *
 * @author Mark Hall
 * @version $Revision: 7735 $
 */
@KFStep(category = "Visualization", toolTipText = "Visualize trees and graphs using the prefuse tool")
public class PrefuseGraphViewer 
  extends JPanel
  implements Visible, GraphListener,
	     UserRequestAcceptor, 
             Serializable, BeanContextChild {

  /** for serialization */
  private static final long serialVersionUID = -5183121972114900617L;

  protected BeanVisual m_visual;

  private transient JFrame m_resultsFrame = null;

  protected transient ResultHistoryPanel m_history;

  /**
   * BeanContex that this bean might be contained within
   */
  protected transient BeanContext m_beanContext = null;

  /**
   * BeanContextChild support
   */
  protected BeanContextChildSupport m_bcSupport = 
    new BeanContextChildSupport(this);

  /**
   * True if this bean's appearance is the design mode appearance
   */
  protected boolean m_design;

  public PrefuseGraphViewer() {
    /*    setUpResultHistory();
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER); */

    java.awt.GraphicsEnvironment ge = 
      java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment(); 
    if (!ge.isHeadless()) {
      appearanceFinal();
    }
  }

  protected void appearanceDesign() {
    setUpResultHistory();
    removeAll();
    m_visual = 
      new BeanVisual("PrefuseGraphViewer", 
                     BeanVisual.ICON_PATH+"DefaultGraph.gif",
		   BeanVisual.ICON_PATH+"DefaultGraph_animated.gif");
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
  }

  protected void appearanceFinal() {
    removeAll();
    setLayout(new BorderLayout());
    setUpFinal();
  }

  protected void setUpFinal() {
    setUpResultHistory();
    add(m_history, BorderLayout.CENTER);
  }

  /**
   * Global info for this bean
   *
   * @return a <code>String</code> value
   */
  public String globalInfo() {
    return "Graphically visualize trees or graphs produced by classifiers/clusterers.";
  }

  private void setUpResultHistory() {
    if (m_history == null) {
      m_history = new ResultHistoryPanel(null);
      m_history.setBorder(BorderFactory.createTitledBorder("Graph list"));
      m_history.setHandleRightClicks(false);
      m_history.getList().
        addMouseListener(new ResultHistoryPanel.RMouseAdapter() {
            /** for serialization */
            private static final long serialVersionUID = -4984130887963944249L;

            public void mouseClicked(MouseEvent e) {
              int index = m_history.getList().locationToIndex(e.getPoint());
              if (index != -1) {
                String name = m_history.getNameAtIndex(index);
                doPopup(name);
              }
            }
          });
    }
  }

  /**
   * Set a bean context for this bean
   *
   * @param bc a <code>BeanContext</code> value
   */
  public void setBeanContext(BeanContext bc) {
    m_beanContext = bc;
    m_design = m_beanContext.isDesignTime();
    if (m_design) {
      appearanceDesign();
    } else {
      java.awt.GraphicsEnvironment ge = 
        java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment(); 
      if (!ge.isHeadless()){
        appearanceFinal();
      }
    }
  }

  /**
   * Return the bean context (if any) that this bean is embedded in
   *
   * @return a <code>BeanContext</code> value
   */
  public BeanContext getBeanContext() {
    return m_beanContext;
  }

  /**
   * Add a vetoable change listener to this bean
   *
   * @param name the name of the property of interest
   * @param vcl a <code>VetoableChangeListener</code> value
   */
  public void addVetoableChangeListener(String name,
				       VetoableChangeListener vcl) {
    m_bcSupport.addVetoableChangeListener(name, vcl);
  }
  
  /**
   * Remove a vetoable change listener from this bean
   *
   * @param name the name of the property of interest
   * @param vcl a <code>VetoableChangeListener</code> value
   */
  public void removeVetoableChangeListener(String name,
					   VetoableChangeListener vcl) {
    m_bcSupport.removeVetoableChangeListener(name, vcl);
  }

  /**
   * Accept a graph
   *
   * @param e a <code>GraphEvent</code> value
   */
  public synchronized void acceptGraph(GraphEvent e) {

    FastVector graphInfo = new FastVector();

    if (m_history == null) {
      setUpResultHistory();
    }
    String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());

    name += e.getGraphTitle();
    graphInfo.addElement(new Integer(e.getGraphType()));
    graphInfo.addElement(e.getGraphString());
    m_history.addResult(name, new StringBuffer());
    m_history.addObject(name, graphInfo);
  }

  /**
   * Set the visual appearance of this bean
   *
   * @param newVisual a <code>BeanVisual</code> value
   */
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }

  /**
   * Get the visual appearance of this bean
   *
   */
  public BeanVisual getVisual() {
    return m_visual;
  }

  /**
   * Use the default visual appearance
   *
   */
  public void useDefaultVisual() {
    m_visual.loadIcons(BeanVisual.ICON_PATH+"DefaultGraph.gif",
		       BeanVisual.ICON_PATH+"DefaultGraph_animated.gif");
  }

  /**
   * Popup a result list from which the user can select a graph to view
   *
   */
  public void showResults() {
    if (m_resultsFrame == null) {
      if (m_history == null) {
	setUpResultHistory();
      }
      m_resultsFrame = new JFrame("Prefuse Graph Viewer");
      m_resultsFrame.getContentPane().setLayout(new BorderLayout());
      m_resultsFrame.getContentPane().add(m_history, BorderLayout.CENTER);
      m_resultsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	  public void windowClosing(java.awt.event.WindowEvent e) {
	    m_resultsFrame.dispose();
	    m_resultsFrame = null;
	  }
	});
      m_resultsFrame.pack();
      m_resultsFrame.setVisible(true);
    } else {
      m_resultsFrame.toFront();
    }
  }

  private void doPopup(String name) {

    FastVector graph;  
    String grphString;
    int grphType;

    graph = (FastVector)m_history.getNamedObject(name);
    grphType = ((Integer)graph.firstElement()).intValue();
    grphString = (String)graph.lastElement();

    if(grphType == Drawable.TREE){
        final javax.swing.JFrame jf = 
            new javax.swing.JFrame("Prefuse Tree Visualizer: "+name);
        jf.setSize(800,600);
        jf.getContentPane().setLayout(new BorderLayout());
        PrefuseTree pt = new PrefuseTree();
        javax.swing.JComponent tv = null;
        
        try {
          tv = pt.getDisplay(grphString);
        } catch (Exception ex) {
          System.err.println("unable to visualize tree"); ex.printStackTrace(); 
        }

        jf.getContentPane().add(tv, BorderLayout.CENTER);
        jf.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
            jf.dispose();
            }
        });

        jf.setVisible(true);
    }
    if(grphType == Drawable.BayesNet) {
      final javax.swing.JFrame jf = 
	new javax.swing.JFrame("Prefuse Graph Visualizer: "+name);
      jf.setSize(500,400);
      jf.getContentPane().setLayout(new BorderLayout());
      PrefuseGraph pg = new PrefuseGraph();
      javax.swing.JComponent gv = null;
      try {
        gv = pg.getDisplay(grphString);      
      } catch (Exception be) { 
        System.err.println("unable to visualize BayesNet"); be.printStackTrace(); 
      }
      //      gv.layoutGraph();
      jf.getContentPane().add(gv, BorderLayout.CENTER);
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
	  public void windowClosing(java.awt.event.WindowEvent e) {
            jf.dispose();
	  }
        });
      
      jf.setVisible(true);
    }
  }

  /**
   * Return an enumeration of user requests
   *
   * @return an <code>Enumeration</code> value
   */
  public Enumeration enumerateRequests() {
    Vector newVector = new Vector(0);
    newVector.addElement("Show results");

    return newVector.elements();
  }

  /**
   * Perform the named request
   *
   * @param request a <code>String</code> value
   * @exception IllegalArgumentException if an error occurs
   */
  public void performRequest(String request) 
    {
    if (request.compareTo("Show results") == 0) {
      showResults();
    } else {
      throw new 
	IllegalArgumentException(request
		    + " not supported (PrefuseGraphViewer)");
    }
  }
}

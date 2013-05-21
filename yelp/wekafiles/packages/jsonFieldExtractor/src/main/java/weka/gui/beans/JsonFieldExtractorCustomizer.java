/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

/*
 *    JsonFieldExtractorCustomizer.java
 *    Copyright (C) 2012 Pentaho Corporation
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.gui.JListHelper;
import weka.gui.PropertySheetPanel;

/**
 * Customizer for the JsonFieldExtractor component
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49031 $
 */
public class JsonFieldExtractorCustomizer extends JPanel implements
    EnvironmentHandler, BeanCustomizer, CustomizerCloseRequester {
  
  /** For serialization */
  private static final long serialVersionUID = 4736793456770330505L;
  protected Environment m_env = Environment.getSystemWide();
  protected ModifyListener m_modifyL = null;
  protected JsonFieldExtractor m_extractor;
  
  protected JTextField m_attListField = new JTextField(20);
  protected JTextField m_pathField = new JTextField(20);
  protected JTextField m_attNameField = new JTextField(15);
  protected JComboBox m_attTypeCombo = new JComboBox();
  protected JTextField m_formatOrNomValsField = new JTextField(20);
  
  protected JList m_list = new JList();
  protected DefaultListModel m_listModel;
  
  protected JButton m_newBut = new JButton("New");
  protected JButton m_deleteBut = new JButton("Delete");
  protected JButton m_upBut = new JButton("Move up");
  protected JButton m_downBut = new JButton("Move down");    
  
  protected Window m_parent;
  
  protected PropertySheetPanel m_tempEditor =
    new PropertySheetPanel();
  
  /**
   * Customizer
   */
  public JsonFieldExtractorCustomizer() {
    setLayout(new BorderLayout());
  }
  
  private void setup() {
    JPanel aboutAndControlHolder = new JPanel();
    aboutAndControlHolder.setLayout(new BorderLayout());
    
    JPanel controlHolder = new JPanel();
    controlHolder.setLayout(new BorderLayout());
    JPanel fieldHolder = new JPanel();
    fieldHolder.setLayout(new BorderLayout());
    JPanel attListP = new JPanel();
    attListP.setLayout(new BorderLayout());
    attListP.setBorder(BorderFactory.createTitledBorder("Apply to attributes"));
    attListP.add(m_attListField, BorderLayout.CENTER);
    m_attListField.setToolTipText("<html>Accepts a range of indexes (e.g. '1,2,6-10')<br> " +
                "or a comma-separated list of named attributes</html>");
    
    JPanel pathP = new JPanel();
    pathP.setLayout(new BorderLayout());
    pathP.setBorder(BorderFactory.createTitledBorder("JSON Path"));
    pathP.add(m_pathField, BorderLayout.CENTER);
    m_pathField.setToolTipText("<html>Path to the JSON element to extract " +
    		"<br>from each top-level structure.<br><br> " +
    		"E.g. $.person[1].address.street</html>");
    JPanel topFields = new JPanel();
    topFields.setLayout(new GridLayout(0,2));
    topFields.add(attListP); topFields.add(pathP);
    fieldHolder.add(topFields, BorderLayout.NORTH);
    
    JPanel attNameP = new JPanel();
    attNameP.setLayout(new BorderLayout());
    attNameP.setBorder(BorderFactory.createTitledBorder("Attribute name"));
    attNameP.add(m_attNameField, BorderLayout.CENTER);
    m_attNameField.setToolTipText("The name for this JSON field in the outgoing " +
    		"instances");
    
    JPanel attTypeP = new JPanel();
    attTypeP.setLayout(new BorderLayout());
    attTypeP.setBorder(BorderFactory.createTitledBorder("Attribute type"));
    attTypeP.add(m_attTypeCombo, BorderLayout.CENTER);
    m_attTypeCombo.setToolTipText("The type for this JSON field in the outgoing " +
    		"instances");
    
    JPanel formatOrLabelsP = new JPanel();
    formatOrLabelsP.setLayout(new BorderLayout());
    formatOrLabelsP.setBorder(BorderFactory.createTitledBorder("Date format/nominal labels"));
    formatOrLabelsP.add(m_formatOrNomValsField, BorderLayout.CENTER);
    m_formatOrNomValsField.setToolTipText("<html>Specify formatting string (if output is<br>" +
    		"a date attribute) or nominal labels (if output<br>" +
    		"is a nominal attribute)</html>");
    
    JPanel botFields = new JPanel();
    botFields.setLayout(new GridLayout(0, 3));
    botFields.add(attNameP); botFields.add(attTypeP);
    botFields.add(formatOrLabelsP);
    fieldHolder.add(botFields, BorderLayout.SOUTH);
    
    controlHolder.add(fieldHolder, BorderLayout.NORTH);
    aboutAndControlHolder.add(controlHolder, BorderLayout.SOUTH);
    JPanel aboutP = m_tempEditor.getAboutPanel();
    aboutAndControlHolder.add(aboutP, BorderLayout.NORTH);
    add(aboutAndControlHolder, BorderLayout.NORTH);
    
    m_list.setVisibleRowCount(5);
    m_deleteBut.setEnabled(false);
    
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BorderLayout());
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1, 0));
    butHolder.add(m_newBut); butHolder.add(m_deleteBut);
    butHolder.add(m_upBut); butHolder.add(m_downBut);
    m_upBut.setEnabled(false); m_downBut.setEnabled(false);
    
    listPanel.add(butHolder, BorderLayout.NORTH);
    JScrollPane js = new JScrollPane(m_list);
    js.setBorder(BorderFactory.
        createTitledBorder("JSON field list"));
    listPanel.add(js, BorderLayout.CENTER);
    add(listPanel, BorderLayout.CENTER);
    
    addButtons();
    
    m_list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          if (!m_deleteBut.isEnabled()) {
            m_deleteBut.setEnabled(true);
          }
          
          Object entry = m_list.getSelectedValue();
          if (entry != null) {
            JsonFieldExtractor.JsonFieldPath m = 
              (JsonFieldExtractor.JsonFieldPath)entry;
            m_attListField.setText(m.getAttsToApplyTo());
            m_pathField.setText(m.getPath());
            m_attNameField.setText(m.getOutputAttName());
            m_attTypeCombo.setSelectedItem(m.getOutputAttType());
            m_formatOrNomValsField.setText(m.getDateFormatOrLabels());
          }
        }
      }
    });
    
    m_newBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JsonFieldExtractor.JsonFieldPath m =
          new JsonFieldExtractor.JsonFieldPath();
        
        String atts = (m_attListField.getText() != null) 
          ? m_attListField.getText() : "";
        m.setAttsToApplyTo(atts);
        String path = (m_pathField.getText() != null)
          ? m_pathField.getText() : "";
        m.setPath(path);
        String attName = (m_attNameField.getText() != null)
          ? m_attNameField.getText() : "";
        m.setOutputAttName(attName);
        String attType = m_attTypeCombo.getSelectedItem().toString();
        m.setOutputAttType(attType);
        String formatOrLabels = m_formatOrNomValsField.getText();
        m.setDateFormatOrLabels(formatOrLabels);
        
        m_listModel.addElement(m);
        
        if (m_listModel.size() > 1) {
          m_upBut.setEnabled(true);
          m_downBut.setEnabled(true);
        }
        
        m_list.setSelectedIndex(m_listModel.size() - 1);
      }
    });
    
    m_deleteBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selected = m_list.getSelectedIndex();
        if (selected >= 0) {
          m_listModel.removeElementAt(selected);
          
          if (m_listModel.size() <= 1) {
            m_upBut.setEnabled(false);
            m_downBut.setEnabled(false);
          }
        }
      }
    });
    
    m_upBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JListHelper.moveUp(m_list);
      }
    });
    
    m_downBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JListHelper.moveDown(m_list);
      }
    });
    
    m_attListField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        Object m = m_list.getSelectedValue();
        if (m != null) {
          ((JsonFieldExtractor.JsonFieldPath)m).
            setAttsToApplyTo(m_attListField.getText());
          m_list.repaint();
        }
      }
    });
    
    m_pathField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        Object m = m_list.getSelectedValue();
        if (m != null) {
          ((JsonFieldExtractor.JsonFieldPath)m).
            setPath(m_pathField.getText());
          m_list.repaint();
        }
      }
    });
    
    m_attNameField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        Object m = m_list.getSelectedValue();
        if (m != null) {
          ((JsonFieldExtractor.JsonFieldPath)m).
            setOutputAttName(m_attNameField.getText());
          m_list.repaint();
        }
      }
    });
    
    m_attTypeCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object type = m_attTypeCombo.getSelectedItem();
        if (type != null) {
          Object m = m_list.getSelectedValue();
          if (m != null) {
            ((JsonFieldExtractor.JsonFieldPath)m).
              setOutputAttType(type.toString());
            m_list.repaint();
          }
        }
      }
    });
    
    m_formatOrNomValsField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        Object m = m_list.getSelectedValue();
        if (m != null) {
          ((JsonFieldExtractor.JsonFieldPath)m).
            setDateFormatOrLabels(m_formatOrNomValsField.getText());
          m_list.repaint();
        }
      }
    });    
  }
  
  private void addButtons() {
    JButton okBut = new JButton("OK");
    JButton cancelBut = new JButton("Cancel");
    
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new GridLayout(1, 2));
    butHolder.add(okBut); butHolder.add(cancelBut);
    add(butHolder, BorderLayout.SOUTH);        
    
    okBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {   
        closingOK();
        
        m_parent.dispose();
      }
    });
    
    cancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closingCancel();
        
        m_parent.dispose();
      }
    });
  }
  
  protected void initialize() {
    String mString = m_extractor.getPathDetails();
    m_listModel = new DefaultListModel();
    m_list.setModel(m_listModel);
    
    if (mString != null && mString.length() > 0) {
      String[] parts = mString.split("@@field-path@@");
      
      if (parts.length > 0) {
        m_upBut.setEnabled(true);
        m_downBut.setEnabled(true);
        for (String mPart : parts) {
          JsonFieldExtractor.JsonFieldPath m = 
            new JsonFieldExtractor.JsonFieldPath(mPart);
          m_listModel.addElement(m);
        }
        
        m_list.repaint();
      }
    }
    
    // setup combo box
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    model.addElement("string"); model.addElement("numeric");
    model.addElement("nominal"); model.addElement("date");
    m_attTypeCombo.setModel(model);
  }

  public void setObject(Object o) {
    if (o instanceof JsonFieldExtractor) {
      m_extractor = (JsonFieldExtractor)o;
      m_tempEditor.setTarget(o);
      setup();
      initialize();      
    }
  }

  public void setParentWindow(Window parent) {
    m_parent = parent;
  }

  public void setModifiedListener(ModifyListener l) {   
    m_modifyL = l;
  }

  public void setEnvironment(Environment env) {
    m_env = env;
  }
  
  /**
   * Handle a closing event under an OK condition 
   */
  protected void closingOK() {
   StringBuffer buff = new StringBuffer();
   for (int i = 0; i < m_listModel.size(); i++) {
     JsonFieldExtractor.JsonFieldPath m =
       (JsonFieldExtractor.JsonFieldPath)m_listModel.elementAt(i);
     
     buff.append(m.toStringInternal());
     if (i < m_listModel.size() - 1) {
       buff.append("@@field-path@@");
     }
   }
   
   m_extractor.setPathDetails(buff.toString());
 }
 
 /**
  * Handle a closing event under a CANCEL condition
  */
 protected void closingCancel() {
   // nothing to do
 }

}

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
 *    CassandraLoaderCustomizer.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.core.converters.cassandra.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;

import org.apache.cassandra.thrift.InvalidRequestException;

import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Utils;
import weka.core.converters.CassandraLoader;
import weka.core.converters.cassandra.CassandraColumnMetaData;
import weka.core.converters.cassandra.CassandraConnection;
import weka.gui.beans.EnvironmentField;
import weka.gui.beans.GOECustomizer;
import weka.gui.scripting.Script;
import weka.gui.scripting.SyntaxDocument;
import weka.gui.visualize.VisualizeUtils;

/**
 * Customizer for the CassnadraLoader
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 48815 $
 */
public class CassandraLoaderCustomizer extends JPanel 
  implements GOECustomizer, EnvironmentHandler {
  
  /** For serialization */
  private static final long serialVersionUID = -4714488167405213230L;
  protected ModifyListener m_modifyL = null;
  protected Environment m_env = Environment.getSystemWide();
  
  protected CassandraLoader m_cassandra = null;
  
  protected boolean m_dontShowButs = false;
  
  protected EnvironmentField m_hostText;
  protected EnvironmentField m_portText;
  protected EnvironmentField m_userText;
  protected JPasswordField m_passText;
  protected EnvironmentField m_keyspaceText;
  protected JCheckBox m_outputKeyBut;
  protected JCheckBox m_useCompressionBut;
  protected EnvironmentField m_nominalSpecText;
  protected JTextPane m_cqlEditor;
  protected JButton m_showSchemaBut;
  
  protected Script m_script;
  
  /** editor setup */
  public final static String PROPERTIES_FILE = "weka/core/converters/cassandra/CQL.props";
  
  /**
   * Constructor
   */
  public CassandraLoaderCustomizer() {
    setLayout(new BorderLayout());
  }

  /**
   * Set a listener interested in knowing if the object being
   * edited has changed.
   * 
   * @param l the interested listener
   */
  public void setModifiedListener(ModifyListener l) {
    m_modifyL = l;
  }  

  /**
   * Set the CassandraLoader to edit
   * 
   * @param o the CassandraLoader to edit
   */
  public void setObject(Object o) {
    
    if (o instanceof CassandraLoader) {
      m_cassandra = (CassandraLoader)o;
    }
    
    setup();
  }
  
  private void setup() {
    Properties props = null;
    
    try {
      props = Utils.readProperties(PROPERTIES_FILE);
    } catch (Exception ex) {
      ex.printStackTrace();
      props = new Properties();
    }
    
    JPanel fieldsPanel = new JPanel();
    fieldsPanel.setLayout(new GridLayout(0, 2));
    JLabel hostLab = new JLabel("Cassandra host", SwingConstants.RIGHT);
    fieldsPanel.add(hostLab);
    m_hostText = new EnvironmentField(m_env);
    m_hostText.setText(m_cassandra.getCassandraHost());
    fieldsPanel.add(m_hostText);
    
    JLabel portLab = new JLabel("Cassandra port", SwingConstants.RIGHT);
    fieldsPanel.add(portLab);
    m_portText = new EnvironmentField(m_env);
    m_portText.setText(m_cassandra.getCassandraPort());
    fieldsPanel.add(m_portText);
    
    JLabel usernameLab = new JLabel("Username", SwingConstants.RIGHT);
    fieldsPanel.add(usernameLab);
    m_userText = new EnvironmentField(m_env);
    m_userText.setText(m_cassandra.getUsername());
    fieldsPanel.add(m_userText);
    
    JLabel passLab = new JLabel("Password", SwingConstants.RIGHT);
    fieldsPanel.add(passLab);
    m_passText = new JPasswordField(10);
    m_passText.setText(m_cassandra.getPassword());
    JPanel pHolder = new JPanel(); pHolder.setLayout(new BorderLayout());
    pHolder.add(m_passText); pHolder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    fieldsPanel.add(pHolder);
    
    JLabel keyLab = new JLabel("Keyspace", SwingConstants.RIGHT);
    keyLab.setToolTipText("The keyspace (database) to use");
    fieldsPanel.add(keyLab);
    m_keyspaceText = new EnvironmentField(m_env);
    m_keyspaceText.setText(m_cassandra.getCassandraKeyspace());
    fieldsPanel.add(m_keyspaceText);
    
    JLabel outputKeyLab = new JLabel("Output key as an attribute",
        SwingConstants.RIGHT);
    outputKeyLab.setToolTipText("Whether to output the column family key " +
    		"as an attribute");
    fieldsPanel.add(outputKeyLab);
    m_outputKeyBut = new JCheckBox();
    m_outputKeyBut.setSelected(m_cassandra.getOutputKey());
    fieldsPanel.add(m_outputKeyBut);
    
    JLabel compressLab = new JLabel("Use query compression", SwingConstants.RIGHT);
    compressLab.setToolTipText("Whether to compress the CQL query before sending to " +
    		"the server");
    fieldsPanel.add(compressLab);
    m_useCompressionBut = new JCheckBox();
    m_useCompressionBut.setSelected(m_cassandra.getUseCompression());
    fieldsPanel.add(m_useCompressionBut);
    
    
    JLabel nominalSpecLab = new JLabel("Text to nominal values", SwingConstants.RIGHT);
    nominalSpecLab.setToolTipText("<html>If there are incoming text fields which you want " +
    		"to be converted to nominal,<br> and there is no meta data on legal values " +
    		"for this field in the schema,<br> then you can manually specify the legal " +
    		"values here. <p>The format is<br> " +
    		"attName1:{val1, val2,...};attName2:{val1, val2...};...</html>");
    fieldsPanel.add(nominalSpecLab);
    m_nominalSpecText = new EnvironmentField(m_env);
    m_nominalSpecText.setText(m_cassandra.getTextToNominalValues());
    fieldsPanel.add(m_nominalSpecText);
    
    add(fieldsPanel, BorderLayout.NORTH);
    
    m_cqlEditor = new JTextPane();
    if (props.getProperty("Syntax", "false").equals("true")) {
      SyntaxDocument doc = new SyntaxDocument(props);
      m_cqlEditor.setDocument(doc);
      m_cqlEditor.setBackground(doc.getBackgroundColor());
    } else {
      m_cqlEditor.setForeground(VisualizeUtils.processColour(
          props.getProperty("ForegroundColor", "black"), Color.BLACK));
      m_cqlEditor.setBackground(VisualizeUtils.processColour(
          props.getProperty("BackgroundColor", "white"), Color.WHITE));
      m_cqlEditor.setFont(new Font(props.getProperty("FontName", "monospaced"), 
          Font.PLAIN, Integer.parseInt(props.getProperty("FontSize", "12"))));
    }
    
    //m_script = new Script(m_cqlEditor.getDocument());
    try {
      m_cqlEditor.getDocument().insertString(0, m_cassandra.getCQLSelectQuery(), null);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    //m_cqlEditor.setText(m_cassandra.getCQLSelectQuery());
    
    JPanel cqlPan = new JPanel();
    cqlPan.setLayout(new BorderLayout());
    
    JScrollPane cqlScroller = new JScrollPane(m_cqlEditor);
    cqlScroller.setBorder(BorderFactory.createTitledBorder("CQL"));
    cqlPan.add(cqlScroller, BorderLayout.NORTH);
    add(cqlPan, BorderLayout.CENTER);
    Dimension d = new Dimension(450, 100);
    m_cqlEditor.setMinimumSize(d);
    m_cqlEditor.setPreferredSize(d);
    
    m_showSchemaBut = new JButton("Show schema");
    JPanel schemaPan = new JPanel();
    schemaPan.setLayout(new FlowLayout(FlowLayout.RIGHT));
    schemaPan.add(m_showSchemaBut);
    cqlPan.add(schemaPan, BorderLayout.SOUTH);
    
    m_showSchemaBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        popupShowSchema();
      }
    });
    
    if (m_dontShowButs) {
      return;
    }
    addButtons();    
    
  }
  
  /**
   * Popup a dialog for showing the schema (column family meta data)
   */
  protected void popupShowSchema() {
    CassandraConnection conn = null;
    
    try {
      String hostS = m_env.substitute(m_hostText.getText());
      String portS = m_env.substitute(m_portText.getText());
      String userS = m_env.substitute(m_userText.getText());
      String passS = m_env.substitute(new String(m_passText.getPassword()));
      String keyS = m_env.substitute(m_keyspaceText.getText());
      String cqlS = m_env.substitute(m_cqlEditor.getText());
      
      conn = new CassandraConnection(hostS, Integer.parseInt(portS), 
          userS, passS);
      conn.setKeyspace(keyS);
      
      String colFam = CassandraColumnMetaData.getColumnFamilyNameFromCQLSelectQuery(cqlS);
      if (colFam == null || colFam.length() == 0) {
        throw new Exception("SELECT query does not seem to containt the name of " +
        		"a column family!");
      }
      
      if (!CassandraColumnMetaData.columnFamilyExists(conn, colFam)) {
        throw new Exception("The column family '" + colFam + "' does not " +
            "seem to exist in the keyspace '" + keyS);
      }
      
      CassandraColumnMetaData cassMeta = new CassandraColumnMetaData(conn, colFam);
      String schemaDescription = cassMeta.getSchemaDescription();
      JTextArea jt = new JTextArea(schemaDescription, 30, 50);
      JOptionPane.showMessageDialog(CassandraLoaderCustomizer.this, new JScrollPane(jt),
          "Cassandra Loader", JOptionPane.OK_OPTION);
    } catch (Exception ex) {
      if (conn != null) {
        conn.close();
      }
      String message = ex.getMessage();
      if (ex instanceof InvalidRequestException) {
        message = ((InvalidRequestException)ex).why;
      }

      JOptionPane.showMessageDialog(CassandraLoaderCustomizer.this, message,
          "Cassandra Loader", JOptionPane.ERROR_MESSAGE);
    }
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
      }
    });
    
    cancelBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closingCancel();
      }
    });
  }

  /**
   * Don't show the ok and cancel buttons
   */
  public void dontShowOKCancelButtons() {
    m_dontShowButs = true;
  }

  /**
   * Called when an external component holding this panel
   * is closing under an OK condition.
   */
  public void closingOK() {
    // write gui field values to the loader
    m_cassandra.setCassandraHost(m_hostText.getText());
    m_cassandra.setCassandraPort(m_portText.getText());
    m_cassandra.setUsername(m_userText.getText());
    m_cassandra.setPassword(new String(m_passText.getPassword()));
    m_cassandra.setCassandraKeyspace(m_keyspaceText.getText());
    m_cassandra.setOutputKey(m_outputKeyBut.isSelected());
    m_cassandra.setUseCompression(m_useCompressionBut.isSelected());
    m_cassandra.setTextToNominalValues(m_nominalSpecText.getText());
    m_cassandra.setCQLSelectQuery(m_cqlEditor.getText());
    
    if (m_modifyL != null) {
      m_modifyL.
        setModifiedStatus(CassandraLoaderCustomizer.this, true);
    }
  }

  /**
   * Called when an external component holding this panel
   * is closing under a CANCEL condition
   */
  public void closingCancel() {
    // nothing to do
  }

  /**
   * Set environment variables to use
   * 
   * @param env environment variables to use
   */
  public void setEnvironment(Environment env) {
    m_env = env;
  }
}

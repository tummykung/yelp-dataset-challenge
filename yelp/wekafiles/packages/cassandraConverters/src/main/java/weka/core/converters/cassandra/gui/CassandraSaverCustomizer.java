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
 *    CassandraSaverCustomizer.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.core.converters.cassandra.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import weka.core.converters.CassandraSaver;
import weka.core.converters.cassandra.CassandraColumnMetaData;
import weka.core.converters.cassandra.CassandraConnection;
import weka.gui.beans.EnvironmentField;
import weka.gui.beans.GOECustomizer;
import weka.gui.scripting.Script;
import weka.gui.scripting.SyntaxDocument;
import weka.gui.visualize.VisualizeUtils;

/**
 * Customizer class for the CassandraSaver.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 48815 $
 */
public class CassandraSaverCustomizer extends JPanel implements GOECustomizer,
    EnvironmentHandler {
  
  /** For serialization */
  private static final long serialVersionUID = 4606033852038126702L;
  
  /** Something wanting to know if we've modified the object we're editing */
  protected ModifyListener m_modifyL = null;
  
  /** Environment variables to substitute in our fields */
  protected Environment m_env = Environment.getSystemWide();
  
  /** The cassandra saver to edit */
  protected CassandraSaver m_cassandra = null;
  
  /** True if we won't show ok and cancel buttons */
  protected boolean m_dontShowButs = false;
  
  // Various UI widgets for editing the options of CassandraSaver
  protected EnvironmentField m_hostText;
  protected EnvironmentField m_portText;
  protected EnvironmentField m_userText;
  protected JPasswordField m_passText;
  protected EnvironmentField m_keyspaceText;
  protected JComboBox m_columnFamilyText;
  protected JButton m_getColFamNamesBut;
  protected EnvironmentField m_consistencyText;
  protected EnvironmentField m_batchSizeText;
  protected EnvironmentField m_keyText;
  protected JCheckBox m_generateKeyBut;
  protected EnvironmentField m_initialGeneratedKeyText;
  protected JCheckBox m_createColumnFamilyBut;
  protected JCheckBox m_truncateColumnFamilyBut;
  protected JCheckBox m_updateColumnFamilyMetaDataBut;
  protected JCheckBox m_insertFieldsNotInMetaDataBut;
    
  protected JCheckBox m_useCompressionBut;
  
  protected JButton m_showSchemaBut;
  protected JButton m_aprioriCQLBut;
  
  protected Script m_script;
  protected JTextPane m_cqlEditor;
  
  /** editor setup */
  public final static String PROPERTIES_FILE = "weka/core/converters/cassandra/CQL.props";
  
  /**
   * Constructor
   */
  public CassandraSaverCustomizer() {
    setLayout(new BorderLayout());
  }

  /**
   * Set an object (typlically the Knowledge Flow) that
   * is interested in knowing whether we've made a change
   * to the options of the CassandraSaver we're editing
   * 
   * @param l the interested modify listener
   */
  public void setModifiedListener(ModifyListener l) {
    m_modifyL = l;
  }

  /**
   * Set the CassandraSaver to edit
   * 
   * @param o the CassandraSaver to edit
   */
  public void setObject(Object o) {
    if (o instanceof CassandraSaver) {
      m_cassandra = (CassandraSaver)o;
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
    GridLayout gl = new GridLayout(0, 2);
    //gl.setVgap();
    fieldsPanel.setLayout(gl);
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
    
    JLabel colFamLab = new JLabel("Column family (table)", SwingConstants.RIGHT);
    colFamLab.setToolTipText("The column family (table) to write to");
    fieldsPanel.add(colFamLab);
    m_columnFamilyText = new JComboBox();
    m_columnFamilyText.setEditable(true);
    m_columnFamilyText.setSelectedItem(m_cassandra.getColumnFamilyName());
    m_getColFamNamesBut = new JButton("Get column family names");
    m_getColFamNamesBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setupColumnFamiliesCombo();
      }
    });
    JPanel holderP = new JPanel();
    holderP.setLayout(new BorderLayout());
    holderP.add(m_columnFamilyText, BorderLayout.CENTER);
    holderP.add(m_getColFamNamesBut, BorderLayout.EAST);
    holderP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    fieldsPanel.add(holderP);
    
    JLabel consistencyLab = new JLabel("Consistency level", SwingConstants.RIGHT);
    consistencyLab.setToolTipText("The write consistency to use (ZERO | ONE | " +
    		"ANY | QUORUM | ALL");
    fieldsPanel.add(consistencyLab);
    m_consistencyText = new EnvironmentField(m_env);
    m_consistencyText.setText(m_cassandra.getConsistency());
    fieldsPanel.add(m_consistencyText);
    
    JLabel batchSizeLab = new JLabel("Commit batch size", SwingConstants.RIGHT);
    batchSizeLab.setToolTipText("The number of rows to accumulate before" +
    		" performing a batch insert statement");
    fieldsPanel.add(batchSizeLab);
    m_batchSizeText = new EnvironmentField(m_env);
    m_batchSizeText.setText(m_cassandra.getCommitBatchSize());
    fieldsPanel.add(m_batchSizeText);
    
    final JLabel incomingKeyLab = new JLabel("Incoming attribute to use as the key", 
        SwingConstants.RIGHT);
    fieldsPanel.add(incomingKeyLab);
    m_keyText = new EnvironmentField(m_env);
    m_keyText.setText(m_cassandra.getKeyField());
    fieldsPanel.add(m_keyText);
    
    JLabel generateKeyLab = new JLabel("Generate a key", SwingConstants.RIGHT);
    generateKeyLab.setToolTipText("Generate an artificial key");
    fieldsPanel.add(generateKeyLab);
    m_generateKeyBut = new JCheckBox();
    m_generateKeyBut.setSelected(m_cassandra.getGenerateKey());
    fieldsPanel.add(m_generateKeyBut);
    
    final JLabel generatedKeyStartLab = new JLabel("Initial generated key value", 
        SwingConstants.RIGHT);
    generatedKeyStartLab.setToolTipText("Initial generated key value");
    fieldsPanel.add(generatedKeyStartLab);
    m_initialGeneratedKeyText = new EnvironmentField(m_env);
    m_initialGeneratedKeyText.setText(m_cassandra.getInitialGeneratedKeyValue());
    fieldsPanel.add(m_initialGeneratedKeyText);
    generatedKeyStartLab.setEnabled(m_cassandra.getGenerateKey());
    incomingKeyLab.setEnabled(!m_cassandra.getGenerateKey());
    m_keyText.setEnabled(!m_cassandra.getGenerateKey());
    m_initialGeneratedKeyText.setEnabled(m_cassandra.getGenerateKey());
    m_generateKeyBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        generatedKeyStartLab.setEnabled(m_generateKeyBut.isSelected());
        m_initialGeneratedKeyText.setEnabled(m_generateKeyBut.isSelected());
        incomingKeyLab.setEnabled(!m_generateKeyBut.isSelected());
        m_keyText.setEnabled(!m_generateKeyBut.isSelected());
      }
    });
    
    JLabel createColFamLab = new JLabel("Create column family", 
        SwingConstants.RIGHT);
    createColFamLab.setToolTipText("Create the column family (table) if it " +
    		"doesn't already exist");
    fieldsPanel.add(createColFamLab);
    m_createColumnFamilyBut = new JCheckBox();
    m_createColumnFamilyBut.setSelected(m_cassandra.getCreateColumnFamily());
    fieldsPanel.add(m_createColumnFamilyBut);
    
    JLabel truncateColFamLab = new JLabel("Truncate column family", 
        SwingConstants.RIGHT);
    truncateColFamLab.setToolTipText("Truncate (delete all data) column family " +
    		"(table) before inserting data");
    fieldsPanel.add(truncateColFamLab);
    m_truncateColumnFamilyBut = new JCheckBox();
    m_truncateColumnFamilyBut.setSelected(m_cassandra.getTruncateColumnFamily());
    fieldsPanel.add(m_truncateColumnFamilyBut);
    
    JLabel updateColFamLab = new JLabel("Update colun family meta data", 
        SwingConstants.RIGHT);
    updateColFamLab.setToolTipText("Update column family with unknown " +
    		"incoming attributes");
    fieldsPanel.add(updateColFamLab);
    m_updateColumnFamilyMetaDataBut = new JCheckBox();
    m_updateColumnFamilyMetaDataBut.setSelected(m_cassandra.
        getUpdateColumnFamilyMetaData());
    fieldsPanel.add(m_updateColumnFamilyMetaDataBut);
    
    JLabel insertFieldsNotInMetaLab = 
      new JLabel("Insert attributes not in column family meta data", 
        SwingConstants.RIGHT);
    insertFieldsNotInMetaLab.setToolTipText("<html>Insert incoming unknown " +
    		"incoming attributes.<br>Uses the default validator for the column" +
    		"family.<br><br>Has no affect if 'Update column family with<br>" +
    		"unknown incoming attributes' is turned on.</html>");
    fieldsPanel.add(insertFieldsNotInMetaLab);
    m_insertFieldsNotInMetaDataBut = new JCheckBox();
    m_insertFieldsNotInMetaDataBut.setSelected(m_cassandra.
        getInsertFieldsNotInColumnFamilyMetaData());
    fieldsPanel.add(m_insertFieldsNotInMetaDataBut);
    
    JLabel compressLab = new JLabel("Use compression", SwingConstants.RIGHT);
    compressLab.setToolTipText("Whether to compress the CQL batch insert before sending to " +
                "the server");
    fieldsPanel.add(compressLab);
    m_useCompressionBut = new JCheckBox();
    m_useCompressionBut.setSelected(m_cassandra.getUseCompression());
    fieldsPanel.add(m_useCompressionBut);
    
    m_aprioriCQLBut = new JButton("CQL to execute before inserting first instance");
    m_aprioriCQLBut.setToolTipText("<html>Executes CQL statements (separated by ;'s) before" +
    		"inserting data.<br> This is useful for creating/dropping secondary " +
    		"indexes.</html>");
    m_showSchemaBut = new JButton("Show schema");
    JPanel butHolder = new JPanel();
    butHolder.setLayout(new BorderLayout());
    butHolder.add(m_aprioriCQLBut, BorderLayout.WEST);
    butHolder.add(m_showSchemaBut, BorderLayout.EAST);
    butHolder.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    fieldsPanel.add(new JPanel()); // left column filler
    fieldsPanel.add(butHolder);
    
    m_aprioriCQLBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        popupCQLEditor();
      }
    });
    
    m_showSchemaBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        popupShowSchema();
      }
    });
    
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
      m_cqlEditor.getDocument().insertString(0, m_cassandra.getAprioriCQL(), null);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    
    add(fieldsPanel, BorderLayout.NORTH);
    
    if (m_dontShowButs) {
      return;
    }
    
    addButtons();
  }
  
  protected void setupColumnFamiliesCombo() {
    // any pre-set text in the box
    String current = null; 
    if (m_columnFamilyText.getSelectedItem() != null) {
      current = m_columnFamilyText.getSelectedItem().toString();
    }
    
    CassandraConnection conn = null;
    try {
      String hostS = m_env.substitute(m_hostText.getText());
      String portS = m_env.substitute(m_portText.getText());
      String userS = m_env.substitute(m_userText.getText());
      String passS = m_env.substitute(new String(m_passText.getPassword()));
      String keyS = m_env.substitute(m_keyspaceText.getText());

      conn = new CassandraConnection(hostS, Integer.parseInt(portS), 
          userS, passS);

      try {
        conn.setKeyspace(keyS);
      } catch (InvalidRequestException ire) {
        String message = "Problem getting column family information from " +
        		"Cassandra\n\n" + ire.why;
        JOptionPane.showMessageDialog(CassandraSaverCustomizer.this, message,
            "Cassandra Saver", JOptionPane.ERROR_MESSAGE);
        ire.printStackTrace();
        return;
      }

      List<String> colFams = CassandraColumnMetaData.getColumnFamilyNames(conn);
      m_columnFamilyText.removeAllItems();

      for (String fam : colFams) {
        m_columnFamilyText.addItem(fam);
      }
      
      if (current != null) {
        m_columnFamilyText.setSelectedItem(current);
      }
    } catch (Exception ex) {
      String message = "Problem getting column family information from " +
      "Cassandra\n\n" + ex.getMessage();
      JOptionPane.showMessageDialog(CassandraSaverCustomizer.this, message,
          "Cassandra Saver", JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
    }    
  }
  
  protected void popupCQLEditor() {
    JPanel cqlPan = new JPanel();
    cqlPan.setLayout(new BorderLayout());
    
    JScrollPane cqlScroller = new JScrollPane(m_cqlEditor);
    cqlScroller.setBorder(BorderFactory.createTitledBorder("CQL"));
    cqlPan.add(cqlScroller, BorderLayout.NORTH);
    add(cqlPan, BorderLayout.CENTER);
    Dimension d = new Dimension(450, 100);
    m_cqlEditor.setMinimumSize(d);
    m_cqlEditor.setPreferredSize(d);
    
    int result = JOptionPane.showConfirmDialog(CassandraSaverCustomizer.this, cqlScroller,
        "Cassandra Saver", JOptionPane.OK_CANCEL_OPTION);
    
    if (result == JOptionPane.OK_OPTION) {
      m_cassandra.setAprioriCQL(m_cqlEditor.getText());
    }
  }
  
  protected void popupShowSchema() {
    CassandraConnection conn = null;
    
    try {
      String hostS = m_env.substitute(m_hostText.getText());
      String portS = m_env.substitute(m_portText.getText());
      String userS = m_env.substitute(m_userText.getText());
      String passS = m_env.substitute(new String(m_passText.getPassword()));
      String keyS = m_env.substitute(m_keyspaceText.getText());
      String colFamS = m_env.substitute(m_columnFamilyText.
          getSelectedItem().toString());
      
      conn = new CassandraConnection(hostS, Integer.parseInt(portS), 
          userS, passS);
      conn.setKeyspace(keyS);
      
      if (colFamS == null || colFamS.length() == 0) {
        throw new Exception("No column family name specified!");
      }
      
      if (!CassandraColumnMetaData.columnFamilyExists(conn, colFamS)) {
        throw new Exception("The column family '" + colFamS + "' does not " +
            "seem to exist in the keyspace '" + keyS);
      }
      
      CassandraColumnMetaData cassMeta = new CassandraColumnMetaData(conn, colFamS);
      String schemaDescription = cassMeta.getSchemaDescription();
      JTextArea jt = new JTextArea(schemaDescription, 30, 50);
      JOptionPane.showMessageDialog(CassandraSaverCustomizer.this, new JScrollPane(jt),
          "Cassandra Saver", JOptionPane.OK_OPTION);
    } catch (Exception ex) {
      if (conn != null) {
        conn.close();
      }
      String message = ex.getMessage();
      if (ex instanceof InvalidRequestException) {
        message = ((InvalidRequestException)ex).why;
      }

      JOptionPane.showMessageDialog(CassandraSaverCustomizer.this, message,
          "Cassandra Saver", JOptionPane.ERROR_MESSAGE);
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
   * Set environment variables to use
   * 
   * @param env the environment variables to use
   */
  public void setEnvironment(Environment env) {
    m_env = env;
  }

  /**
   * Don't show the ok and cancel buttons (parent dialog will
   * supply these)
   */
  public void dontShowOKCancelButtons() {
    m_dontShowButs = true;
  }

  /**
   * Closing under an OK condition
   */
  public void closingOK() {
    m_cassandra.setCassandraHost(m_hostText.getText());
    m_cassandra.setCassandraPort(m_portText.getText());
    m_cassandra.setUsername(m_userText.getText());
    m_cassandra.setPassword(new String(m_passText.getPassword()));
    m_cassandra.setCassandraKeyspace(m_keyspaceText.getText());
    m_cassandra.setColumnFamilyName(m_columnFamilyText.getSelectedItem().toString());
    m_cassandra.setConsistency(m_consistencyText.getText());
    m_cassandra.setCommitBatchSize(m_batchSizeText.getText());
    m_cassandra.setKeyField(m_keyText.getText());
    m_cassandra.setGenerateKey(m_generateKeyBut.isSelected());
    m_cassandra.setInitialGeneratedKeyValue(m_initialGeneratedKeyText.getText());
    m_cassandra.setCreateColumnFamily(m_createColumnFamilyBut.isSelected());
    m_cassandra.setTruncateColumnFamily(m_truncateColumnFamilyBut.isSelected());
    m_cassandra.setUpdateColumnFamilyMetaData(m_updateColumnFamilyMetaDataBut.
        isSelected());
    m_cassandra.setInsertFieldsNotInColumnFamilyMetaData(
        m_insertFieldsNotInMetaDataBut.isSelected());
    m_cassandra.setUseCompression(m_useCompressionBut.isSelected());
    m_cassandra.setAprioriCQL(m_cqlEditor.getText());
  }

  /**
   * Closing under a cancel condition
   */
  public void closingCancel() {
    // Nothing to do

  }
}

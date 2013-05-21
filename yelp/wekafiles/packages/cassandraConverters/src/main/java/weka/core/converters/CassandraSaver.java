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
 *    CassandraSaver.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.cassandra.thrift.Compression;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CommandlineRunnable;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.cassandra.CassandraColumnMetaData;
import weka.core.converters.cassandra.CassandraConnection;

/**
<!-- globalinfo-start -->
<!-- globalinfo-end -->
*
* Although a JDBC driver exists for Cassandra, this custom saver was written to
* access Cassandra via the low-level Thrift API. The reason for this is that certain
* useful functions can be accessed at the Thrift level and aren't available via the
* JDBC driver. The most important of these is the ability to access table meta data, 
* including type information for columns (where defined), without having to execute
* a query to the database.
* 
<!-- options-start -->
<!-- options-end -->
*  
* @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
* @version $Revision: 48858 $
*/
public class CassandraSaver extends AbstractSaver implements BatchConverter, IncrementalConverter,
    NoSQLSaver, OptionHandler, EnvironmentHandler, CommandlineRunnable {
  
  /** For serialization */
  private static final long serialVersionUID = -5764550095275276899L;

  /** The host to contact */
  protected String m_cassandraHost = "localhost";
  
  /** The port that cassandra is listening on */
  protected String m_cassandraPort = "9160";
  
  /** Username for authentication */
  protected String m_username = "";
  
  /** Password for authentication */
  protected String m_password = "";
  
  /** The keyspace (database) to use */
  protected String m_cassandraKeyspace = "";
  
  /** The column family to write to */
  protected String m_cassandraColumnFamily = "";
  protected String m_columnFamNameS;
  
  /** The write consistency to use */
  protected String m_consistencyLevel = "";
  protected String m_consistencyLevelS;
  
  /** The size of the batch insert to use */
  protected String m_batchSize = "100";
  protected int m_actualBatchSize;
  
  /** The name of the incoming field to use as the key for inserts */
  protected String m_keyField = "";
  
  /** If true, then an sequentially increasing integer will be used as the key */
  protected boolean m_generateKey = false;
  
  /** Initial value for generated key */
  protected String m_keyInitialValue = "1";
  protected int m_keyIncrementV = 1;
  
  /** If true, the column family will be created (if it doesn't already exist) */
  protected boolean m_createColumnFamily;
  
  /** If true, all data will be deleted from the column family before inserting */
  protected boolean m_truncateColumnFamily;
  
  /** If true, the column family schema will be updated for any fields not already present */
  protected boolean m_updateColumnFamilyMetaData;
  
  /**
   *  If true, any fields not in the schema will be inserted according to the default 
   * column validator for the column family. Has no affect if the update column family
   * meta data option is turned on
   */
  protected boolean m_insertFieldsNotInColumnMetaData;
  
  /** Any CQL commands to execute before inserting first instance */
  protected String m_aprioriCQL = "";
  
  /** Whether to use GZIP compression of CQL queries */
  protected boolean m_useCompression;
  
  protected transient CassandraConnection m_connection;
  
  /** Holds batch insert CQL statement */
  protected StringBuilder m_batchInsert;
  
  /** Holds the index of the attribute used for the key (if not generating a key) */
  protected int m_keyIndex;
  
  /** Number of instances seen so far for current batch when writing incrementally */
  protected int m_rowsSeen;
  
  /** Column meta data and schema information */
  protected transient CassandraColumnMetaData m_cassandraMeta;
  
  protected transient Environment m_env;
  
  protected boolean m_debug;
  
  public CassandraSaver() {
    resetOptions();
  }
  
  /**
   * Returns a string describing this Loader
   * @return a description of the Loader suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Saves data to a cassandra column family (table).";
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
    result.enable(Capability.DATE_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);
    result.enable(Capability.STRING_ATTRIBUTES);
    
    // class
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.NUMERIC_CLASS);
    result.enable(Capability.DATE_CLASS);
    result.enable(Capability.STRING_CLASS);
    result.enable(Capability.NO_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    
    return result;
  }

  /**
   * Set environment variables
   * 
   * @param env the environment variables to use
   */
  public void setEnvironment(Environment env) {
    m_env = env;
  }

  /**
   * Get a list of command line options for this saver
   * 
   * @return an enumeration of the command line options
   */
  public Enumeration listOptions() {

    Vector<Option> result = new Vector<Option>();

    result.add(new Option("\tCassandra host to connect " +
        "to\n\t(default: localhost).", "H", 1, "-H <hostname>"));
    result.add(new Option("\tCassandra port to connect " +
        "to\n\t(default: 9160).", "P", 1, "-P <port>"));
    result.add(new Option("\tKeyspace to use.", "K", 1, "-K <keyspace>"));
    result.add(new Option("\tUsername for authentication to keyspace/column family." +
        "\n\t(default - no authentication)", "username", 1, "-username <username>"));
    result.add(new Option("\tPassword for authentication to keyspace/column family." +
        "\n\t(default - no authentication)", "password", 1, "-password <password>"));
    
    result.add(new Option("\tColumn family (table) to write to.",
         "C", 1, "-C <column family name>"));
    
    result.add(new Option("\tWrite consistency level to use."
        + "\n\t(default - server default)", "consistency", 1, 
        "-consistency <ZERO | ONE | ANY | QUORUM | ALL>"));
    
    result.add(new Option("\tCommit batch size to use.\n\t(default = 100)",
        "B", 1, "-B <batch size>"));
    
    result.add(new Option("\tIncomming attribute to use as the key. Can specify " +
    		"a name, index, or '/first' or '/last' to indicate the first" +
    		" and last attribute in the incoming instances respectively.",
        "key", 1, "-key <att name>"));
    
    result.add(new Option("\tGenerate an integer key to use (overrides the " +
    		"-key option)",
        "generate-key", 0, "-generate-key"));
    result.add(new Option("\tInitial key value. Use in conjunction with " +
    		"\n\t-generate-key.",
        "initial-key-val", 1, "-initial-key-val <initial key value>"));
    
    result.add(new Option("\tCreate column family (table)",
        "create", 0, "-create"));
    result.add(new Option("\tTruncate column family (table)",
        "truncate", 0, "-truncate"));
    result.add(new Option("\tUpdate column family (table) meta data with " +
    		"unknown incoming fields",
        "update-meta", 0, "-update-meta"));
    result.add(new Option("\tInsert fields not in column family (table) " +
    		"meta data.\n\tUses the default column validator for " +
    		"the column family.\n\tHas no affect if -update-meta is turned on",
        "insert-unknown", 0, "-insert-unknown"));
    
    
    result.add(new Option("\tCompress CQL batch inserts", "compress", 0, 
        "-compress"));
    
    result.add(new Option("\tCQL statements to execute before inserting first instance." +
    		"\n\tUseful for adding/dropping secondary indexes after potential" +
    		"\n\tcolumn family meta data updates, but before inserting data.",
        "apriori-cql", 1, "-apriori-cql <cql statement; cql statement; ...;>"));

    return result.elements();
  }

  /**
   * Set the values of command line options for this saver
   * 
   * @param options the values of the options
   */
  public void setOptions(String[] options) throws Exception {    
    
    String tmpStr;
    
    tmpStr = Utils.getOption('H', options);
    if (tmpStr.length() > 0) {
      setCassandraHost(tmpStr);
    } 
    
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() > 0) {
      setCassandraPort(tmpStr);
    }
    
    tmpStr = Utils.getOption("K", options);
    if (tmpStr.length() > 0) {
      setCassandraKeyspace(tmpStr);
    } else {
      throw new Exception("Must specify a keyspace to use!");
    }
    
    tmpStr = Utils.getOption("username", options);
    if (tmpStr.length() > 0) {
      setUsername(tmpStr);
    }
    
    tmpStr = Utils.getOption("password", options);
    if (tmpStr.length() > 0) {
      setPassword(tmpStr);
    }
    
    tmpStr = Utils.getOption("C", options);
    if (tmpStr.length() > 0) {
      setColumnFamilyName(tmpStr);
    }
    
    tmpStr = Utils.getOption("consistency", options);
    if (tmpStr.length() > 0) {
      setConsistency(tmpStr);
    }
    
    tmpStr = Utils.getOption("B", options);
    if (tmpStr.length() > 0) {
      setCommitBatchSize(tmpStr);
    }
    
    tmpStr = Utils.getOption("key", options);
    if (tmpStr.length() > 0) {
      setKeyField(tmpStr);
    }
    
    setGenerateKey(Utils.getFlag("generate-key", options));
    tmpStr = Utils.getOption("initial-key-val", options);
    if (tmpStr.length() > 0) {
      setInitialGeneratedKeyValue(tmpStr);
    }
    
    setCreateColumnFamily(Utils.getFlag("create", options));
    setTruncateColumnFamily(Utils.getFlag("truncate", options));
    setUpdateColumnFamilyMetaData(Utils.getFlag("update-meta", options));
    setInsertFieldsNotInColumnFamilyMetaData(Utils.getFlag("insert-unknown", options));
    setUseCompression(Utils.getFlag("compress", options));
    
    
    setUseCompression(Utils.getFlag("compress", options));
    
    tmpStr = Utils.getOption("apriori-cql", options);
    if (tmpStr.length() > 0) {
      setAprioriCQL(tmpStr);
    }

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Get the values of the command line options for this saver
   * 
   * @return the options of the loader
   */
  public String[] getOptions() {
    Vector<String> result = new Vector<String>();
    
    result.add("-H"); result.add(getCassandraHost());
    result.add("-P"); result.add(getCassandraPort());
    result.add("-K"); result.add(getCassandraKeyspace());
    if (getUsername() != null && getUsername().length() > 0) {
      result.add("-username"); result.add(getUsername());
    }
    if (getPassword() != null && getPassword().length() > 0) {
      result.add("-password"); result.add(getPassword());
    }
    
    result.add("-C"); result.add(getColumnFamilyName());
    if (getConsistency() != null && getConsistency().length() > 0) {
      result.add("-consistency"); result.add(getConsistency());
    }
    if (getCommitBatchSize() != null && getCommitBatchSize().length() > 0) {
      result.add("-B"); result.add(getCommitBatchSize());
    }
    if (getKeyField() != null && getKeyField().length() > 0) {
      result.add("-key"); result.add(getKeyField());
    }
    if (getGenerateKey()) {
      result.add("-generate-key");
    }
    if (getInitialGeneratedKeyValue() != null && 
        getInitialGeneratedKeyValue().length() > 0) {
      result.add("initial-key-val"); result.add(getInitialGeneratedKeyValue());
    }
    if (getCreateColumnFamily()) {
      result.add("-create");
    }
    if (getTruncateColumnFamily()) {
      result.add("-truncate");
    }
    if (getUpdateColumnFamilyMetaData()) {
      result.add("-update-meta");
    }
    if (getInsertFieldsNotInColumnFamilyMetaData()) {
      result.add("-insert-unknown");
    }
    if (getUseCompression()) {
      result.add("-compress");
    }
    if (getAprioriCQL() != null && getAprioriCQL().length() > 0) {
      result.add("-apriori-cql"); result.add(getAprioriCQL());
    }
    
    return result.toArray(new String[result.size()]);
  }

  /**
   * Return the revision string.
   * 
   * @return the revision string
   */
  public String getRevision() {
    return "$Revision: 48858 $";
  }
  
  /**
   * Reset the saver and close any open connection
   */
  public void resetOptions() {
    super.resetOptions();
    
    setRetrieval(NONE);
    
    closeConnection(m_connection, true);
  }
  
  /**
   * Writes the instances structure to the cassandra database. Creates the
   * column family if it doesn't exist already and the user has opted for this.
   * 
   * @param structure the structure of the incoming instances on which to base
   * the table structure
   * 
   * @throws Exception if the column family (table) doesn't exist and the user
   * has not opted to create it or a problem occurs when communicating with
   * the cassandra server
   */
  protected void writeStructure(Instances structure) throws Exception {
    m_rowsSeen = 0;    
    
    if (m_env == null) {
      m_env = Environment.getSystemWide();
    }
    m_columnFamNameS = getColumnFamilyName();
    if (m_columnFamNameS == null || m_columnFamNameS.length() == 0) {
      throw new Exception("No column family name supplied!");
    }
    
    String keyspaceS = m_cassandraKeyspace;
    try {
      m_columnFamNameS = m_env.substitute(m_columnFamNameS);
      keyspaceS = m_env.substitute(keyspaceS);
    } catch (Exception ex) { }
    
    // make sure we have at least one attribute apart from the key
    boolean enoughAtts = getGenerateKey() 
        ? structure.numAttributes() > 0
            : structure.numAttributes() > 1;
            
    if (!enoughAtts) {
      throw new IOException("Need at least one attribute appart from the key!");
    }
    
    // check the key
    if (!getGenerateKey()) {
      if (m_keyField != null && m_keyField.length() > 0) {
        String keyFieldS = m_keyField;
        try {                              
          keyFieldS = m_env.substitute(m_keyField);          
        } catch (Exception ex) { }
            
        boolean ok = true;
        Attribute keyAtt = structure.attribute(keyFieldS);        
        
        if (keyAtt == null) {
          if (keyFieldS.equals("/first")) {
            m_keyIndex = 0;
          } else if (keyFieldS.equals("/last")) {
            m_keyIndex = structure.numAttributes() - 1;
          } else {
            // try to parse as a number
            try {
              m_keyIndex = Integer.parseInt(keyFieldS);
              if (m_keyIndex < 1 || m_keyIndex > structure.numAttributes()) {
                ok = false;
              } else {
                // zero-based
                m_keyIndex--;
              }
            } catch (NumberFormatException n) {
              ok = false;
            }            
          }
        } else {
          m_keyIndex = keyAtt.index();
        }
        
        if (!ok) {
          throw new IOException("Can't resolve key attribute '" 
              + keyFieldS + "' in the incoming instances!");
        }

      } else {
        throw new IOException("No key attribute specified!");
      }
    } else {
      m_keyIndex = -1;
    }
    
    
    if (m_connection != null) {
      m_connection.close();
    }
    m_connection = openConnection(true);
    
    try {
      if (!CassandraColumnMetaData.columnFamilyExists(m_connection, m_columnFamNameS)) {
        if (getCreateColumnFamily()) {
          // create column family (table)
          CassandraColumnMetaData.
            createColumnFamily(m_connection, m_columnFamNameS, structure, m_keyIndex, m_useCompression);
        } else {
          throw new Exception("Column family '" + m_columnFamNameS + "' does not" +
                        " exist in keyspace '" + keyspaceS + "'. Turn on the " +
                                        "create column family option if you want " +
                                        "to have this column family created automatically " +
                                        "using the incoming attribute structure.");
        }
      }      
    } catch (Exception ex) {
      closeConnection(m_connection, true);
      throw new Exception(ex.fillInStackTrace());
    }
    
    
    // get the column family meta data
    try {
      System.out.println("[CassandraSaver] Getting meta data for column family '"
          + m_columnFamNameS + "'");
      m_cassandraMeta = new CassandraColumnMetaData(m_connection, m_columnFamNameS);
      
      // check to see that we have at least one incoming field appart from the key
      if (numFieldsToBeWritten(m_columnFamNameS, structure, m_keyIndex, m_cassandraMeta) < 2) {
        throw new Exception("Must insert at least one other field appart from the key!");
      }
    } catch (Exception ex) {
      closeConnection(m_connection, true);
      throw new Exception(ex.fillInStackTrace());
    }
    
    // batch size to use for batch insert statements
    String batchSizeS = m_batchSize;
    if (batchSizeS == null || batchSizeS.length() == 0) {
      System.err.println("[CassandraSaver] No batch size set - using 100.");
      batchSizeS = "100";
    }
    try {
      batchSizeS = m_env.substitute(batchSizeS);
    } catch (Exception ex) { }
    
    try {
      m_actualBatchSize = Integer.parseInt(batchSizeS);
    } catch (NumberFormatException ex) {
      System.err.println("[CassandraSaver] Can't parse batch size - using 100.");
      m_actualBatchSize = 100;
    }
    
    // Update cassandra meta with incoming unknown fields?
    if (getUpdateColumnFamilyMetaData()) {
      try {
      CassandraColumnMetaData.updateCassandraMeta(m_connection, m_columnFamNameS, 
          structure, m_keyIndex, m_cassandraMeta);
      } catch (Exception ex) {
        closeConnection(m_connection, true);
        throw new Exception(ex.fillInStackTrace());
      }
    }
    
    if (getTruncateColumnFamily()) {
      try {
        CassandraColumnMetaData.truncateColumnFamily(m_connection, 
            m_columnFamNameS);
      } catch (Exception ex) {
        closeConnection(m_connection, true);
        throw new Exception(ex.fillInStackTrace());
      }
    }
    
    // Try to execute any apriori CQL commands?
    if (getAprioriCQL() != null && getAprioriCQL().length() > 0) {
      String apriori = getAprioriCQL();
      try {
        apriori = m_env.substitute(apriori);
      } catch (Exception ex) { }
      
      System.out.println("Executing the following CQL prior to writing to column family '" 
         + m_columnFamNameS + "'\n\n" + apriori);
      m_connection.executeCQL(apriori, null, m_useCompression);
    }
    
    // consistency level
    try {
      m_consistencyLevelS = m_consistencyLevel;
      m_consistencyLevelS = m_env.substitute(m_consistencyLevelS);
    } catch (Exception ex) { }
    
    String keyInitialValueS = m_keyInitialValue;
    try {
      keyInitialValueS = m_env.substitute(keyInitialValueS);      
    } catch (Exception ex) { }
    
    m_keyIncrementV = Integer.parseInt(keyInitialValueS);
    
    m_batchInsert = newBatch(m_actualBatchSize, m_consistencyLevelS); 
  }

  /**
   * Writes instances in batch mode
   * 
   * @throws IOException if a problem occurs during writing to the
   * database
   */
  public void writeBatch() throws IOException {
    // TODO Auto-generated method stub
    
    Instances instances = getInstances();
    if (instances == null) {
      throw new IOException("No instances to save!");
    }
    if (getRetrieval() == INCREMENTAL) {
      throw new IOException("Batch and incremental saving cannot be mixed!");
    }    
    
    try {
      writeStructure(instances);
      
      for (int i = 0; i < instances.numInstances(); i++) {
        
        // add the instance to the batch
        writeNextInstance(instances.instance(i));
      }            
    } catch (Exception ex) {      
      closeConnection(m_connection, true);
      throw new IOException(ex.fillInStackTrace());
    }
    
    // flush last batch
    if (m_rowsSeen > 0) {
      try {
        doBatch();
      } catch (Exception ex) {
        closeConnection(m_connection, true);
        throw new IOException(ex.fillInStackTrace());
      }
    }
    
    m_batchInsert = null;
    closeConnection(m_connection, false);
    
    setWriteMode(WAIT);
    resetStructure();
    
    System.out.println("[CassandraSaver] Finished writing batch data set");
  }
  
  /**
   * Writes an instance to the cassandra table
   * 
   * @param inst the instance to write
   * @throws IOException if a problem occurs
   */
  protected void writeNextInstance(Instance inst) throws IOException {    
    
    // if key is not being generated, check if its missing
    if (!getGenerateKey()) {
      if (inst.isMissing(m_keyIndex)) {
        throw new IOException("Can't insert this instance because the key is " +
        		"missing:\n\n" + inst.toString());
      }
    }
    
    // quick scan to see if we have at least one non-missing value appart from
    // the key
    boolean ok = false;
    for (int i = 0; i < inst.numAttributes(); i++) {
      if (i != m_keyIndex) {
        if (!inst.isMissing(i)) {
          ok = true;
          break;
        }
      }
    }
    
    if (!ok) {
      System.err.println("Skipping instance '" + inst.toString() +"' because " +
      		"there are no non-missing values!");
      return;
    }
    
    m_batchInsert.append("INSERT INTO ").append(m_columnFamNameS).append(" (KEY");

    for (int i = 0; i < inst.numAttributes(); i++) {
      Attribute a = inst.dataset().attribute(i);
      String attName = a.name();
      
      if (!m_cassandraMeta.columnExistsInSchema(attName) &&
          !getInsertFieldsNotInColumnFamilyMetaData()) {
        continue;
      }
      
      // don't insert if missing!
      if (inst.isMissing(i)) {
        continue;
      }
      
      m_batchInsert.append(", '").append(attName).append("'");
    }
    
    m_batchInsert.append(") VALUES (");
    String keyVal = "";
    if (!getGenerateKey()) {
      //keyVal = "" + inst.value(m_keyIndex);
      try {
        keyVal = CassandraColumnMetaData.wekaValueToCQL(inst.dataset().
            attribute(m_keyIndex), inst.value(m_keyIndex));
      } catch (Exception e) {
        throw new IOException(e.fillInStackTrace());
      }
    } else {
      keyVal = "" + m_keyIncrementV;
      m_keyIncrementV++;
    }
    m_batchInsert.append("'").append(keyVal).append("'");
    
    for (int i = 0; i < inst.numAttributes(); i++) {
      if (i != m_keyIndex) {
        Attribute a = inst.dataset().attribute(i);
        String attName = a.name();
        
        if (!m_cassandraMeta.columnExistsInSchema(attName) &&
            !getInsertFieldsNotInColumnFamilyMetaData()) {
          continue;
        }
       
        // don't insert if missing!
        if (inst.isMissing(i)) {
          continue;
        }
        
        try {
          m_batchInsert.append(", '").append(CassandraColumnMetaData.wekaValueToCQL(a, inst.value(i))).append("'");
        } catch (Exception ex) {
          throw new IOException(ex.fillInStackTrace());
        }        
      }            
    }
    
    m_batchInsert.append(")\n");

    m_rowsSeen++;
    
    if (m_rowsSeen == m_actualBatchSize) {
      try {
        doBatch();
      } catch (Exception ex) {
        ex.printStackTrace();
        throw new IOException(ex.fillInStackTrace());
      }
    }    
  }
  
  /** 
   * Saves an instances incrementally. Structure has to be set by using the
   * setStructure() method or setInstances() method.
   * 
   * @param inst the instance to save
   * @throws IOException if a problem occurs while writing to the database
   */
  public void writeIncremental(Instance inst) throws IOException {
    if (getRetrieval() == BATCH) {
      throw new IOException("Batch and incremental saving cannot be mixed!");
    }
    setRetrieval(INCREMENTAL);
    
    Instances structure = getInstances();
    int writeMode = getWriteMode();
    
    if (writeMode == WAIT) {
      if (structure == null) {
        setWriteMode(CANCEL);
        if (inst != null) {
          closeConnection(m_connection, true);
          throw new IOException("[CassandraSaver] structure (Header " +
          		"Information) has to be set in advance");
        }
      } else {
        setWriteMode(STRUCTURE_READY);
      }
      writeMode = getWriteMode();
    }
    
    if (writeMode == CANCEL) {
      cancel();
    }
    
    if (writeMode == STRUCTURE_READY) {
      setWriteMode(WRITE);
      try {
        writeStructure(structure);
        writeMode = getWriteMode();
      } catch (Exception e) {
        closeConnection(m_connection, true);
        throw new IOException(e.fillInStackTrace());
      }
    }
    
    if (writeMode == WRITE) {
      if (structure == null) {
        closeConnection(m_connection, true);
        throw new IOException("[CassandraSaver] no instances structure " +
        		"available!");
      }
      
      if (inst != null) {
        writeNextInstance(inst);
      } else {
        
        // flush last batch
        try {
          doBatch();
        } catch (Exception ex) {
          closeConnection(m_connection, true);
          throw new IOException(ex.fillInStackTrace());
        }
        
        closeConnection(m_connection, false);
        setWriteMode(WAIT);
        resetStructure();
        
        System.out.println("[CassandraSaver] Finished writing incremental " +
        		"data set");
      }
    }
  }
  
  /**
   * Cancel writing
   */
  public void cancel() {    
    
    super.cancel();    
    closeConnection(m_connection, false);
  }
  
  /**
   * Commits a batch insert to the cassandra database
   * 
   * @throws Exception if a problem occurs
   */
  protected void doBatch() throws Exception {
    if (m_debug) {
      System.out.println("[CassandraSaver] committing batch to column family '" +
      		m_columnFamNameS + "'");
    }
    m_batchInsert.append("APPLY BATCH");
    
    //System.err.println(m_batchInsert.toString());
    
    commitBatch(m_batchInsert, m_connection, getUseCompression());
    
    // ready for a new batch
    m_batchInsert = newBatch(m_actualBatchSize, m_consistencyLevelS);
    m_rowsSeen = 0;    
  }
  
  /**
   * Send the batch insert.
   * 
   * @param batch the CQL batch insert statement
   * @param conn the connection to use
   * @param compressCQL true if the CQL should be compressed
   * @throws Exception if a problem occurs
   */
  public void commitBatch(StringBuilder batch, CassandraConnection conn,
      boolean compressCQL) throws Exception {
    
    // compress the batch if necessary
    byte[] toSend = null;
    if (compressCQL) {
      toSend = CassandraConnection.
        compressQuery(batch.toString(), Compression.GZIP);
    } else {
      toSend = batch.toString().
        getBytes(Charset.forName(CassandraColumnMetaData.UTF8));
    }
    
    conn.getClient().execute_cql_query(ByteBuffer.wrap(toSend), 
        compressCQL ? Compression.GZIP : Compression.NONE);
  }
  
  /**
   * Initializes a new BATCH INSERT statement
   * 
   * @param numRows the maximum number of rows in the batch insert
   * @param consistency the consistency level to use
   * @return a StringBuilder initialized with the batch insert statement
   */
  protected static StringBuilder newBatch(int numRows, String consistency) {
    // make a stab at a reasonable initial capacity
    StringBuilder batch = new StringBuilder(numRows * 80);
    batch.append("BEGIN BATCH");
    
    if (consistency != null && consistency.length() > 0) {
      batch.append(" USING CONSISTENCY ").append(consistency);
    }
    
    batch.append("\n");
    
    return batch;
  }
  
  /**
   * Closes the supplied connection to the cassandra server
   * 
   * @param conn the connection to close
   * @param quiet true for no status output to the console
   */
  protected void closeConnection(CassandraConnection conn, boolean quiet) {
    if (conn != null && !quiet) {
      System.err.println("[CassandraSaver] Closing connection...");
      conn.close();
    }
  }
  
  /**
   * Opens a new connection to the cassandra server.
   * 
   * @param quiet true for no status output to the console 
   * @return the connection
   * @throws IOException if a problem occurs while trying to connect
   */
  protected CassandraConnection openConnection(boolean quiet) throws IOException {
    String hostS = getCassandraHost();
    String portS = getCassandraPort();
    String userS = getUsername();
    String passS = getPassword();
    String keyspaceS = getCassandraKeyspace();    
    
    CassandraConnection conn = null;
    
    if (hostS == null || hostS.length() == 0) {
      throw new IOException("Missing host name!");
    }
    if (portS == null || portS.length() == 0) {
      System.out.println("No port specified - using Cassandra default (9160)");
      portS = "9160";
    }
    if (keyspaceS == null || keyspaceS.length() == 0) {
      throw new IOException("Missing keyspace info!");
    }

    
    try {
      hostS = m_env.substitute(hostS);
      portS = m_env.substitute(portS);
      if (userS != null && userS.length() > 0) {
        userS = m_env.substitute(userS);
      }
      if (passS != null && passS.length() > 0) {
        passS = m_env.substitute(passS);
      }
      keyspaceS = m_env.substitute(keyspaceS);      
    } catch (Exception ex) {      
    }
    
    try {
      if (!quiet) {
        System.err.println("[CassandraSaver] Connecting to Cassandra node at '"
            + hostS + ":" + portS + "' using " +
            "keyspace '" + keyspaceS +"'...");
      }
      conn = new CassandraConnection(hostS, Integer.parseInt(portS), 
          userS, passS);
      conn.setKeyspace(keyspaceS);
    } catch (Exception ex) {
      closeConnection(conn, false);
      throw new IOException(ex.fillInStackTrace());
    }
    
    return conn;    
  }
  
  /**
   * Computes the number of fields that will be written to the database
   * with respect to the incoming fields and the user's settings with 
   * respect to updating the meta data or writing fields that are not
   * in the meta data.
   * 
   * @param colFamilyName the column family that will be written to
   * @param structure the incoming instances structure
   * @param keyIndex the index of the key field (if not using an artificial
   * key)
   * @param cassandraMeta the colummn family meta data
   * @return the number of fields that will be written
   */
  protected int numFieldsToBeWritten(String colFamilyName, Instances structure, 
      int keyIndex, CassandraColumnMetaData cassandraMeta) {
    
    boolean insertFieldsNotInMetaData = getInsertFieldsNotInColumnFamilyMetaData();
    
    int count = 1;
    
    for (int i = 0; i < structure.numAttributes(); i++) {
      if (i != keyIndex) {
        Attribute a = structure.attribute(i);
        String colName = a.name();
        if (!cassandraMeta.columnExistsInSchema(colName) && 
            !insertFieldsNotInMetaData) {
          continue;
        }
        count++;
      }
    }
    
    return count;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String cassandraHostTipText() {
    return "The name of the cassandra node to connect to";
  }
  
  /**
   * Set the cassandra node hostname to connect to
   * 
   * @param host the host to connect to
   */
  public void setCassandraHost(String host) {
    m_cassandraHost = host;
  }
  
  /**
   * Get the name of the cassandra node to connect to
   * 
   * @return the name of the cassandra node to connect to
   */
  public String getCassandraHost() {
    return m_cassandraHost;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String cassandraPortTipText() {
    return "The port of the cassandra node to connect to";
  }
  
  /**
   * Set the port that cassandra is listening on
   * 
   * @param port the port that cassandra is listening on
   */
  public void setCassandraPort(String port) {
    m_cassandraPort = port;
  }
  
  /**
   * Get the port that cassandra is listening on
   * 
   * @return the port that cassandra is listening on
   */
  public String getCassandraPort() {
    return m_cassandraPort;
  }
  
  /**
   * Set the username to authenticate with
   * 
   * @param un the username to authenticate with
   */
  public void setUsername(String un) {
    m_username = un;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String usernameTipText() {
    return "Username for authentication (if required by keyspace/column family)";
  }
  
  /**
   * Get the username to authenticate with
   * 
   * @return the username to authenticate with
   */
  public String getUsername() {
    return m_username;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String passwordTipText() {
    return "Password for authentication (if required by keyspace/column family)";
  }
  
  /**
   * Set the password to authenticate with
   * 
   * @param pass the password to authenticate with
   */
  public void setPassword(String pass) {
    m_password = pass;
  }
  
  /**
   * Get the password to authenticate with
   * 
   * @return the password to authenticate with
   */
  public String getPassword() {
    return m_password;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String cassandraKeyspaceTipText() {
    return "The keyspace (database) to use";
  }
  
  /**
   * Set the keyspace (db) to use
   * 
   * @param keyspace the keyspace to use
   */
  public void setCassandraKeyspace(String keyspace) {
    m_cassandraKeyspace = keyspace;
  }
  
  /**
   * Get the keyspace (db) to use
   * 
   * @return the keyspace (db) to use
   */
  public String getCassandraKeyspace() {
    return m_cassandraKeyspace;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String useCompressionTipText() {
    return "Whether or not to compress the CQL SELECT query";
  }
  
  /**
   * Set whether to compress (GZIP) CQL queries when transmitting them
   * to the server
   * 
   * @param c true if CQL queries are to be compressed
   */
  public void setUseCompression(boolean c) {
    m_useCompression = c;
  }
  
  /**
   * Get whether CQL queries will be compressed (GZIP) or not
   * 
   * @return true if CQL queries will be compressed when sending to the server
   */
  public boolean getUseCompression() {
    return m_useCompression;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String columnFamilyNameTipText() {
    return "The name of the column family (table) to write to.";
  }
  
  /**
   * Set the column family (table) to write to
   * 
   * @param colFam the name of the column family to write to
   */
  public void setColumnFamilyName(String colFam) {
    m_cassandraColumnFamily = colFam;
  }
  
  /**
   * Get the name of the column family to write to
   * 
   * @return the name of the columm family to write to
   */
  public String getColumnFamilyName() {
    return m_cassandraColumnFamily;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String consistencyTipText() {
    return "The write consistency to use (e.g. ONE, QUORUM etc)";
  }
  
  /**
   * Set the consistency to use (e.g. ONE, QUORUM etc).
   * 
   * @param consistency the consistency to use
   */
  public void setConsistency(String consistency) {
    m_consistencyLevel = consistency;
  }
  
  /**
   * Get the consistency to use
   * 
   * @return the consistency
   */
  public String getConsistency() {
    return m_consistencyLevel;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String commitBatchSizeTipText() {
    return "Batch size to use - ie max instances to send via CQL batch insert"; 
  }
  
  /**
   * Set the batch size to use (i.e. max rows to send via a CQL batch insert
   * statement)
   * 
   * @param batchSize the max number of rows to send in each CQL batch
   * insert
   */
  public void setCommitBatchSize(String batchSize) {
    m_batchSize = batchSize;
  }
  
  /**
   * Get the batch size to use (i.e. max rows to send via a CQL batch insert
   * statement)
   * 
   * @return the batch size.
   */
  public String getCommitBatchSize() {
    return m_batchSize;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String keyFieldTipText() {
    return "The incoming attribute to use as the column family key. Can be s" +
    		"pecified as an attribute name, index or '/first' or '/last' to" +
    		"indicate the first and last attribute in the incoming instances " +
    		"respectively.";
  }
  
  /**
   * Set the incoming field to use as the key for inserts
   * 
   * @param keyField the name of the incoming field to use
   * as the key
   */
  public void setKeyField(String keyField) {
    m_keyField = keyField;
  }  
  
  /**
   * Get the name of the incoming field to use as the key
   * for inserts
   * 
   * @return the name of the incoming field to use as the key
   * for inserts
   */
  public String getKeyField() {
    return m_keyField;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String generateKeyTipText() {
    return "Use a sequential integer value as the key for inserts. " +
    		"Overrides the keyField property if true.";
  }
  
  /**
   * Set whether to generate a sequentially increasing integer to use
   * as the key value for inserts.
   * 
   * @param g true if a key is to be generated
   */
  public void setGenerateKey(boolean g) {
    m_generateKey = g;
  }
  
  /**
   * Get whether to generate a sequentially increasing integer to use
   * as the key value for inserts.
   * 
   * @return true if a key is to be generated
   */
  public boolean getGenerateKey() {
    return m_generateKey;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String initialGeneratedKeyValueTipText() {
    return "The initial value for the generated key. " +
    		"Must be an integer. Use in conjunction with " +
    		"generateKey.";
  }
  
  /**
   * Set the initial value to use for the generated key. The key will be
   * incremented by 1 from this value.
   * 
   * @param initKey the initial value of the generated key to use.
   */
  public void setInitialGeneratedKeyValue(String initKey) {
    m_keyInitialValue = initKey;
  }
  
  /**
   * Get the initial value to use for the generated key. The key will be
   * incremented by 1 from this value.
   * 
   * @return the initial value of the generated key to use.
   */
  public String getInitialGeneratedKeyValue() {
    return m_keyInitialValue;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String createColumnFamilyTipText() {
    return "Create the column family (table) if it doesn't already exist";
  }

  /**
   * Set whether to create the specified column family (table) if it
   * doesn't already exist
   * 
   * @param create true if the specified column family is to
   * be created if it doesn't already exist
   */
  public void setCreateColumnFamily(boolean create) {
    m_createColumnFamily = create;
  }
  
  /**
   * Get whether to create the specified column family (table) if it
   * doesn't already exist
   * 
   * @return true if the specified column family is to
   * be created if it doesn't already exist
   */
  public boolean getCreateColumnFamily() {
    return m_createColumnFamily;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String truncateColumnFamilyTipText() {
    return "Truncate (remove all data) in the column family before inserting";
  }
  
  /**
   * Set whether to first truncate (remove all data) the column
   * family (table) before inserting.
   * 
   * @param t true if the column family is to be initially truncated.
   */
  public void setTruncateColumnFamily(boolean t) {
    m_truncateColumnFamily = t;
  }
  
  /**
   * Get whether to first truncate (remove all data) the column
   * family (table) before inserting.
   * 
   * @return true if the column family is to be initially truncated.
   */
  public boolean getTruncateColumnFamily() {
    return m_truncateColumnFamily;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String updateColumnFamilyMetaDataTipText() {
    return "Update the schema for the column family with any " +
    		"unknown fields from the incoming instances.";
  }
  
  /**
   * Set whether to update the column family meta data with any
   * unknown incoming columns
   * 
   * @param u true if the meta data is to be updated with any
   * unknown incoming columns
   */
  public void setUpdateColumnFamilyMetaData(boolean u) {
    m_updateColumnFamilyMetaData = u;
  }
  
  /**
   * Get whether to update the column family meta data with
   * any unknown incoming columns
   * 
   * @return true if the meta data is to be updated with any unknown
   * incoming columns
   */
  public boolean getUpdateColumnFamilyMetaData() {
    return m_updateColumnFamilyMetaData;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String insertFieldsNotInColumnFamilyMetaDataTipText() {
    return "Insert any incoming values for fields that are not in" +
    		"the column family meta data. This has no affect if " +
    		"updateColumnFamilyMetaData is turned on. Unknown " +
    		"fields are validated according to the default validator " +
    		"for the column family.";
  }
  
  /**
   * Set whether or not to insert any incoming fields that are not in
   * the Cassandra table's column meta data. This has no affect if the
   * user has opted to first update the meta data with any unknown columns.
   * 
   * @param insert true if incoming fields not found in the table's meta
   * data are to be inserted (and validated according to the default validator
   * for the table)
   */
  public void setInsertFieldsNotInColumnFamilyMetaData(boolean insert) {
    m_insertFieldsNotInColumnMetaData = insert;
  }
  
  /**
   * Get whether or not to insert any incoming fields that are not in
   * the Cassandra table's column meta data. This has no affect if the
   * user has opted to first update the meta data with any unknown
   * columns.
   * 
   * @return true if incoming fields not found in the table's meta
   * data are to be inserted (and validated according to the default validator
   * for the table)
   */
  public boolean getInsertFieldsNotInColumnFamilyMetaData() {
    return m_insertFieldsNotInColumnMetaData;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String aprioriCQLTipText() {
    return "Arbitrary CQL commands (separated by ;'s) to execute " +
    		"prior to inserting any data " +
    		"into the column family. Can be used to do tasks " +
    		"like creating secondary indexes on columns in the " +
    		"table.";
  }
  
  /**
   * Set any cql statements (separated by ;'s) to execute before
   * inserting the first row into the column family. Can be used
   * to do tasks like creating secondary indexes on columns in the
   * table.
   * 
   * @param cql cql statements (separated by ;'s) to execute
   */
  public void setAprioriCQL(String cql) {
    m_aprioriCQL = cql;
  }
  
  /**
   * Get any cql statements (separated by ;'s) to execute before
   * inserting the first row into the column family. Can be used
   * to do tasks like creating secondary indexes on columns in the
   * table.
   * 
   * @return cql statements (separated by ;'s) to execute
   */
  public String getAprioriCQL() {
    return m_aprioriCQL;
  }
  
  /**
   * Make an options string for the saver
   * 
   * @param saver the saver to make a help options string for
   * @return the help options string
   */
  protected static String makeHelpStr(CassandraSaver saver) {
    StringBuffer result = new StringBuffer();
    Option option;
    
    // build option string
    result.append("\n");
    result.append(saver.getClass().getName().replaceAll(".*\\.", ""));
    result.append(" options:\n\n");
    Enumeration enm = saver.listOptions();
    while (enm.hasMoreElements()) {
      option = (Option) enm.nextElement();
      result.append(option.synopsis() + "\n");
      result.append(option.description() + "\n");
    }

    return result.toString();
  }

  /**
   * Run the supplied object using the supplied command line options
   */
  public void run(Object toRun, String[] options)
    throws IllegalArgumentException {
    if (!(toRun instanceof CassandraSaver)) {
      throw new IllegalArgumentException("Object to run is not a CassandraSaver!");
    }    

    CassandraSaver saver = (CassandraSaver)toRun;
    ArffLoader loader = new ArffLoader();

    try {
      if (Utils.getFlag("h", options) || Utils.getFlag("help", options)) {
        // make help output
        System.err.println("\nHelp requested\n" + makeHelpStr(saver));
      }
            
      String sourceArff = Utils.getOption("i", options);

      if (sourceArff.length() == 0) {
        System.err.println("Must specify a source ARFF file to read from with\n\n " +
        		"CassandraSaver -i <file> [options]");
        
        System.exit(1);
      } else {

        // set options on cassandra saver
        saver.setOptions(options);        

        File input = new File(sourceArff);
        loader.setFile(input);
        loader.setRetrieval(Loader.INCREMENTAL);
        Instances structure = loader.getStructure();

        saver.setInstances(structure);
        
        Instance inst = loader.getNextInstance(structure);
        while (inst != null) {
          saver.writeIncremental(inst);          
          inst = loader.getNextInstance(structure);
        }
        
        // make sure last batch gets flushed and connection closed
        saver.writeIncremental(null);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(makeHelpStr(saver));
    }
  }
  
  /**
   * Main method for testing/running this class
   * 
   * @param args command line options
   */
  public static void main(String[] args) {
    try {
      CassandraSaver saver = new CassandraSaver();
      saver.run(saver, args);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

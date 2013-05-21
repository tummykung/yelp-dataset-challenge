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
 *    CassandraLoader.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.core.converters;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.CqlResult;
import org.apache.cassandra.thrift.CqlRow;

import weka.core.Attribute;
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
 * Although a JDBC driver exists for Cassandra, this custom loader was written to
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
 * @version $Revision: 48815 $
 */
public class CassandraLoader extends AbstractLoader implements BatchConverter,
    IncrementalConverter, NoSQLLoader, OptionHandler, EnvironmentHandler,
    CommandlineRunnable {
  
  /** For serialization */
  private static final long serialVersionUID = -5437248285542380729L;

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
  
  /** Whether to use GZIP compression of CQL queries */
  protected boolean m_useCompression;
  
  /** The select query to execute */
  protected String m_cqlSelectQuery = 
    "SELECT <fields> FROM <column family> WHERE <condition>;";
  
  /** The user can specify valid nominal values for string columns if they wish */
  protected String m_nominalSpecs = "";
  
  /** For iterating over a result set */
  protected Iterator<CqlRow> m_resultIterator;
  
  /** Whether to output key the as an attribute or not */
  protected boolean m_outputKey = false;
  
  /** Whether to output sparse instances or not */
  protected boolean m_outputSparseInstances = false;   
  
  protected transient Instances m_structure;
  
  protected transient Environment m_env;
  
  /** Connection to cassandra */
  protected transient CassandraConnection m_connection;
  
  /** Column meta data and schema information */
  protected transient CassandraColumnMetaData m_cassandraMeta;
  
  /**
   * Returns a string describing this Loader
   * @return a description of the Loader suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Reads data from a Cassandra column family via a CQL SELECT statement.\n\n" +
    		"The format of the CQL statement is\n\n" +
    		"SELECT [FIRST N] [REVERSED] (SELECT EXPR) FROM (COLUMN FAMILY) " +
    		"[USING (CONSISTENCY)] [WHERE (CLAUSE)] [LIMIT N];\n\n" +
    		"More info on CQL can be found at: \n" +
    		"http://www.datastax.com/docs/1.0/references/cql";
  }

  /**
   * Return the revision string.
   * 
   * @return the revision string
   */
  public String getRevision() {
    return "$Revision: 48815 $";
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
   * Get a list of command line options for this loader
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
    result.add(new Option("\tCQL select query to execute." +
        "\n\t(SELECT <fields> FROM <column family> WHERE <condition>", 
        "Q", 1, "-Q <query>"));
    result.add(new Option("\tCompress CQL query", "compress", 0, "-compress"));
    result.add(new Option("\tOutput column family key as an attribute", 
        "key", 0, "-key"));
    result.add(new Option("\tOutput sparse instances", "sparse", 0, "-sparse"));

    result.add(new Option("\tSpecify legal nominal values for cassandra text " +
    		"\n\tfields that don't have such meta data already defined " +
    		"\n\tin the schema for the column family. Text fields " +
    		"\n\twithout legal values meta data are converted to String " +
    		"\n\tattributes.", "nominal-values", 1, "-nominal-values " +
    		"<attName:{val1, val2, ...};attName:{val1, val2, ...};..."));
    
    return result.elements();
  }

  /**
   * Set the values of command line options for this loader
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
    
    tmpStr = Utils.getOption('Q', options);
    if (tmpStr.length() > 0) {
      setCQLSelectQuery(tmpStr);
    } else {
      throw new Exception("Must specify a CQL SELECT query to execute!");
    }
    
    tmpStr = Utils.getOption("nominal-values", options);
    if (tmpStr.length() > 0) {
      setTextToNominalValues(tmpStr);
    }
    
    setUseCompression(Utils.getFlag("compress", options));
    setOutputKey(Utils.getFlag("key", options));
    setOutputSparseInstances(Utils.getFlag("sparse", options));
    
    Utils.checkForRemainingOptions(options);
  }

  /**
   * Get the values of the command line options for this loader
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
    result.add("-Q"); result.add(getCQLSelectQuery());
    if (getUseCompression()) {
      result.add("-compress");
    }
    if (getOutputKey()) {
      result.add("-key");
    }
    if (getOutputSparseInstances()) {
      result.add("-sparse");
    }

    if (getTextToNominalValues() != null && getTextToNominalValues().length() > 0) {
      result.add("-nominal-values"); result.add(getTextToNominalValues());
    }
    
    return result.toArray(new String[result.size()]);
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
  public String textToNominalValuesTipText() {
    return "<html>If there are incoming text fields which you want " +
                "to be converted to nominal,<br> and there is no meta data on legal values " +
                "for this field in the schema,<br> then you can manually specify the legal " +
                "values here.<br><br>The format is<br> " +
                "attName1:{val1, val2,...};attName2:{val1, val2...};...</html>";
  }
  
  /**
   * Set any legal nominal values to use for incoming text fields that do not
   * already have legal values stored in the column family meta data. If not
   * specified then text fields become String attributes when converted.<br><br>
   * 
   * The format is:<br><br>
   * 
   * <code>attName1:{val1, val2,...};attName2:{val1, val2,...};...
   * 
   * @param nominalSpecs a semi-colon list of attribute names followed
   * by legal values.
   */
  public void setTextToNominalValues(String nominalSpecs) {
    m_nominalSpecs = nominalSpecs;
  }
  
  /**
   * Get any legal nominal values to use for incoming text fields that do not
   * already have legal values stored in the column family meta data. If not
   * specified then text fields become String attributes when converted.<br><br>
   * 
   * The format is:<br><br>
   * 
   * <code>attName1:{val1, val2,...};attName2:{val1, val2,...};...
   * 
   * @return a semi-colon list of attribute names followed
   * by legal values.
   */
  public String getTextToNominalValues() {
    return m_nominalSpecs;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String CQLSelectQueryTipText() {
    return "The CQL SELECT query to execute";
  }
  
  /**
   * Set the CQL SELECT query to execute.
   * 
   * @param query the query to execute
   */
  public void setCQLSelectQuery(String query) {
    m_cqlSelectQuery = query;
  }
  
  /**
   * Get the CQL SELECT query to execute
   * 
   * @return the query to execute
   */
  public String getCQLSelectQuery() {
    return m_cqlSelectQuery;
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
   * Set whether to output the key of the column family
   * as an attribute or not. The name of the attribute for
   * the key is the same as the name of the column family.
   * 
   * @param k true if the key is to be output as an attribute.
   */
  public void setOutputKey(boolean k) {
    m_outputKey = k;
  }
  
  /**
   * Get whether to output the key of the column family
   * as an attribute or not. The name of the attribute for
   * the key is the same as the name of the column family.
   * 
   * @return true if the key is to be output as an attribute.
   */
  public boolean getOutputKey() {
    return m_outputKey;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String outputKeyTipText() {
    return "Whether to output the key of the column family as an attribute";
  }
  
  /**
   * Set whether to output sparse instances or not. In non-sparse
   * mode, missing columns in incoming cassandra rows get converted
   * to missing values in each dense instance. In sparse mode, 
   * missing columns are zeros. Sparse mode does not support
   * missing values for non-sparse values.
   * 
   * @param o true if sparse instances are to be output.
   */
  public void setOutputSparseInstances(boolean o) {
    m_outputSparseInstances = o;
  }
  
  /**
   * Get whether to output sparse instances or not. In non-sparse
   * mode, missing columns in incoming cassandra rows get converted
   * to missing values in each dense instance. In sparse mode, 
   * missing columns are zeros. Sparse mode does not support
   * missing values for non-sparse values.
   * 
   * @return true if sparse instances are to be output.
   */
  public boolean getOutputSparseInstances() {
    return m_outputSparseInstances;
  }
  
  /**
   * the tip text for this property
   * 
   * @return            the tip text
   */
  public String outputSparesInstancesTipText() {
    return "Whether to output sparse instances";
  }
  
  /**
   * Open a connection to a Cassandra server
   * 
   * @param quiet if true no status info is output to the console
   * @return a new connection
   * @throws IOException if something goes wrong
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
        System.err.println("[CassandraLoader] Connecting to Cassandra node at '"
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
   * Returns the header format
   * 
   * @return                  the header format
   */
  public Instances getStructure() throws IOException {
    
    if (m_env == null) {
      m_env = Environment.getSystemWide();
    }
    
    if (m_structure != null) {
      return new Instances(m_structure, 0);
    }
    
    // make sure that any old connection is closed
    try {
      reset();
      setRetrieval(INCREMENTAL);
    } catch (Exception ex) {
      throw new IOException(ex.fillInStackTrace());
    }
    
    String keyspaceS = getCassandraKeyspace();    
    String subQ = getCQLSelectQuery();
    if (subQ == null || subQ.length() == 0) {
      throw new IOException("No SELECT query provided!");
    }
    
    try {
      subQ = m_env.substitute(subQ);
      keyspaceS = m_env.substitute(keyspaceS);
    } catch (Exception ex) { }

    
    // check for source column family (table) first

    String colFamName = CassandraColumnMetaData.getColumnFamilyNameFromCQLSelectQuery(subQ);
    if (colFamName == null || colFamName.length() == 0) {
      throw new IOException("SELECT query does not seem to contain the name" +
                        " of a column family to read from!");
    }    
    
    // set up the structure
    m_structure = determineStructure(subQ);
    
    return m_structure;
  }
  
  protected Instances determineStructure(String subQ) 
    throws IOException {
    
    if (!subQ.toLowerCase().startsWith("select")) {
      // not a select statement!
      throw new IOException("No 'SELECT' in query!");
    }
    
    if (subQ.indexOf(';') < 0) {
      // query must end with a ';' or it will wait for more!
      throw new IOException("Query must be terminated by a ';'");      
    }
    
    // strip off where clause (if any)      
    if (subQ.toLowerCase().lastIndexOf("where") > 0) {
      subQ = subQ.substring(0, subQ.toLowerCase().lastIndexOf("where"));
    }
    
    // first determine the source column family
    // look for a FROM that is surrounded by space
    int fromIndex = subQ.toLowerCase().indexOf("from");
    String tempS = subQ.toLowerCase();
    int offset = fromIndex;
    while (fromIndex > 0 && tempS.charAt(fromIndex - 1) != ' ' && 
        (fromIndex + 4 < tempS.length()) && tempS.charAt(fromIndex + 4) != ' ') {
      tempS = tempS.substring(fromIndex + 4, tempS.length());
      fromIndex = tempS.indexOf("from");
      offset += (4 + fromIndex);
    }
    
    fromIndex = offset;
    if (fromIndex < 0) {
      throw new IOException("Must specify a column family using a 'FROM' clause");
      // no from clause
    }
    
    String colFamName = null;
    
    colFamName = subQ.substring(fromIndex + 4, subQ.length()).trim();
    if (colFamName.indexOf(' ') > 0) {
      colFamName = colFamName.substring(0, colFamName.indexOf(' '));
    } else {
      colFamName = colFamName.replace(";", "");
    }
    
    if (colFamName.length() == 0) {
      throw new IOException("No column family (table) name specified in query!");
      // no column family specified
    }
    
    // now determine if its a select * or specific set of columns
    String[] cols = null;
    if (subQ.indexOf("*") > 0) {
      // nothing special to do here
    } else {        
      String colsS = subQ.substring(subQ.indexOf('\''), fromIndex);
      cols = colsS.split(",");
    }

    CassandraConnection conn = openConnection(false);
    try {
      if (!CassandraColumnMetaData.columnFamilyExists(conn, colFamName)) {
        throw new IOException("The column family '" + colFamName + "' does not " +
            "seem to exist in the keyspace '" + conn.getKeyspace());
      }
    } catch (Exception ex) {
      closeConnection(conn, false);
      throw new IOException(ex.fillInStackTrace());
    }
    
    
    CassandraColumnMetaData colMeta = null;
    
    try {
      /*conn = new CassandraConnection(hostS, Integer.parseInt(portS), 
          userS, passS); */
      colMeta = new CassandraColumnMetaData(conn, colFamName);
      m_cassandraMeta = colMeta;
      
      if (m_nominalSpecs != null && m_nominalSpecs.length() > 0) {
        
        // null connection as we just want these values in the Map for conversion
        // purposes, and do not wan't to mutate the column family meta data.
        m_cassandraMeta.updateIndexedMeta(m_nominalSpecs);
      }
      
    } catch (Exception e) {
      closeConnection(conn, false);
      throw new IOException(e.fillInStackTrace());
    }
    
    ArrayList<Attribute> atts = new ArrayList<Attribute>();
    // Do the key first
    if (getOutputKey()) {
      Attribute keyAtt = colMeta.getAttributeTypeForKey();
      atts.add(keyAtt);
    }
    
    if (cols == null) {
      List<Attribute> colAtts = colMeta.getAttributeTypesForSchema();
      atts.addAll(colAtts);
    } else {
      for (String col : cols) {
        col = col.trim();
        col = col.replace("'", "");
        col = col.replace("\"", "");
        if (!colMeta.columnExistsInSchema(col)) {
          // this one isn't known about in about in the schema - we can output it
          // as long as its values satisfy the default validator...
          System.out.println("[CassandraLoader] Query specifies column '" + col + "', however this column is " +
                    "not in the column family schema. The default column family " +
                    "validator will be used");
        }
        Attribute colAtt = colMeta.getAttributeTypeForColumm(col);
        atts.add(colAtt);
      }
    }
    
    Instances result = new Instances(colFamName, atts, 0);
    
    closeConnection(conn, true);
    return result;
  }

  /**
   * Return the full data set. If the structure hasn't yet been determined
   * by a call to getStructure then method should do so before processing
   * the rest of the data set.
   *
   * @return the structure of the data set as an empty set of Instances
   * @throws IOException if there is no source or parsing fails
   */
  public Instances getDataSet() throws IOException {

    if (getRetrieval() == INCREMENTAL) {
      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
    }
    
    Instances result = null;
    if (m_connection == null) {
      getStructure();
      m_connection = openConnection(true);
    }
    setRetrieval(BATCH);
    result = new Instances(m_structure, 0);
    
    if (m_connection.isClosed()) {
      m_connection = openConnection(true);
    }
    
    Compression compression = getUseCompression() 
      ? Compression.GZIP : Compression.NONE;
    String queryS = getCQLSelectQuery();
    try {
      queryS = m_env.substitute(queryS);
    } catch (Exception ex) {}
    
    try {
      System.err.println("Executing query '" + queryS + "'" 
          + (getUseCompression() ? " (using GZIP query compression)" : "") 
          + "...");
      
      byte[] queryBytes = getUseCompression() ? 
          CassandraConnection.compressQuery(queryS, compression) : queryS.getBytes();
      CqlResult qResult = m_connection.getClient().
        execute_cql_query(ByteBuffer.wrap(queryBytes), compression);
      m_resultIterator = qResult.getRowsIterator();
      
    } catch (Exception e) {
      m_resultIterator = null;
      closeConnection(m_connection, true);
      throw new IOException(e.fillInStackTrace());
    }
    
    Instance nextI = null;
    while ((nextI = fetchAndConvertNext(m_structure, true)) != null) {
      result.add(nextI);
    }
    
    closeConnection(m_connection, false);
    m_resultIterator = null;
    
    return result;
  }
  
  protected Instance fetchAndConvertNext(Instances structure, boolean retainString) 
    throws IOException {
    
    if (m_resultIterator != null && m_resultIterator.hasNext()) {
      CqlRow nextRow = m_resultIterator.next();
      
      try {
        if (getOutputSparseInstances()) {
          return m_cassandraMeta.cassandraRowToInstanceSparse(nextRow, 
              m_structure, retainString); 
        }

        return m_cassandraMeta.cassandraRowToInstance(nextRow, 
            m_structure, retainString);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    return null; // no more input
  }

  /**
   * Read the data set incrementally---get the next instance in the data 
   * set or returns null if there are no
   * more instances to get. If the structure hasn't yet been 
   * determined by a call to getStructure then method should do so before
   * returning the next instance in the data set.
   *
   * @param structure the dataset header information, will get updated in 
   * case of string or relational attributes
   * @return the next instance in the data set as an Instance object or null
   * if there are no more instances to be read
   * @throws IOException if an error occurs
   */
  public Instance getNextInstance(Instances structure) throws IOException {
    if (getRetrieval() == BATCH) {
      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
    }
    
    if (m_connection == null) {
      getStructure();
      m_connection = openConnection(true);
    }
    
    if (structure == null) {
      structure = m_structure;
    }
    
    setRetrieval(INCREMENTAL);
    
    if (m_connection.isClosed()) {
      m_connection = openConnection(true);
    }
    
    if (m_resultIterator == null) {
      try {
        Compression compression = getUseCompression() 
        ? Compression.GZIP : Compression.NONE;
        String queryS = getCQLSelectQuery();
        try {
          queryS = m_env.substitute(queryS);
        } catch (Exception ex) {}

        System.err.println("Executing query '" + queryS + "'" 
            + (getUseCompression() ? " (using GZIP query compression)" : "") 
            + "...");

        byte[] queryBytes = getUseCompression() ? 
            CassandraConnection.compressQuery(queryS, compression) : queryS.getBytes();
            CqlResult qResult = m_connection.getClient().
            execute_cql_query(ByteBuffer.wrap(queryBytes), compression);
            m_resultIterator = qResult.getRowsIterator();

      } catch (Exception e) {
        m_resultIterator = null;
        closeConnection(m_connection, false);
        e.printStackTrace();
      }
    }
    
    Instance nextI = fetchAndConvertNext(structure, false);
    if (nextI == null) {
      closeConnection(m_connection, false);
      m_resultIterator = null;
    }
    
    return nextI;
  }
  
  /**
   * Reset the loader.
   * 
   * @exception Exception if there is a problem closing any currently open
   * connection.
   */
  public void reset() throws Exception {
    super.reset();
    m_structure = null;
    if (m_connection != null) {
      closeConnection(m_connection, true);
    }
    m_connection = null;
    m_resultIterator = null;
    m_cassandraMeta = null;
  }
  
  protected void closeConnection(CassandraConnection conn, boolean quiet) {
    if (conn != null && !quiet) {
      System.err.println("[CassandraLoader] Closing connection...");
      conn.close();
    }
  }
  
  protected String getUsage() {
    StringBuffer buff = new StringBuffer();
    
    Enumeration enu = listOptions();
    while (enu.hasMoreElements()) {
      Option option = (Option)enu.nextElement();
      buff.append(option.synopsis() + '\n');
      buff.append(option.description() + "\n");
    }
    
    return buff.toString();
  }

  /**
   * Runs a loader from the command line
   * 
   * @param toRun the loader to run (must be a CassandraLoader)
   * @param options command line options to configure the loader with
   * @exception IllegalArgumentException if the loader to run is not
   * a CassandraLoader
   */
  public void run(Object toRun, String[] options)
      throws IllegalArgumentException {
    if (!(toRun instanceof CassandraLoader)) {
      throw new IllegalArgumentException("Object to execute is not " +
      		"a CassandraLoader!");      
    }
    CassandraLoader loader = (CassandraLoader)toRun;
    
    try {
      loader.setOptions(options);      
      
      boolean headerPrinted = false;
      Instance inst = loader.getNextInstance(null);
      while (inst != null) {
        if (!headerPrinted) {
          System.out.println(inst.dataset());
          headerPrinted = true;
        }
        System.out.println(inst);
        inst = loader.getNextInstance(null);
      }
      
      if (!headerPrinted) {
        System.out.println(loader.getStructure());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }        
  }
  
  /**
   * Main method for testing this class
   * 
   * @param args any command line options. -h or -help
   * prints usage.
   */
  public static void main(String[] args) {
    try {
      if (Utils.getFlag("h", args) || Utils.getFlag("help", args)) {
        System.out.println(new CassandraLoader().getUsage());
        System.exit(1);
      }
      
      CassandraLoader c = new CassandraLoader();
      c.run(c, args);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(new CassandraLoader().getUsage());
    }
  }  
}

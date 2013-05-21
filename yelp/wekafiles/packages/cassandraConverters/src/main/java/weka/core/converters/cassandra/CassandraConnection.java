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
 *    CassandraConnection.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.core.converters.cassandra;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.cassandra.thrift.AuthenticationRequest;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import weka.gui.Logger;

/**
 * Class that encapsulates a connection to Cassandra for a specific keyspace.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 48815 $
 */
public class CassandraConnection {

  private TTransport m_transport;
  
  protected Cassandra.Client m_client;
  protected String m_keyspaceName;    
  
  /**
   * Construct an CassandaraConnection
   * 
   * @param host the host to connect to
   * @param port the port to use
   * @throws Exception if the connection fails
   */
  public CassandraConnection(String host, int port) throws Exception {
    this(host, port, null, null);
  }
  
  /**
   * Construct an CassandaraConnection with optional authentication.
   * 
   * @param host the host to connect to
   * @param port the port to use
   * @param username the username to authenticate with (may be null
   * for no authentication)
   * @param password the password to authenticate with (may be null
   * for no authentication)
   * @throws Exception if the connection fails
   */
  public CassandraConnection(String host, int port,
      String username, String password) throws Exception {
    m_transport = new TFramedTransport(new TSocket(host, port));
    TProtocol protocol = new TBinaryProtocol(m_transport);
    m_client = new Cassandra.Client(protocol);      
    m_transport.open();
    
    if (username != null && username.length() > 0 && password != null &&
        password.length() > 0) {
      Map<String, String> creds = new HashMap<String, String>();
      creds.put("username", username);
      creds.put("password", password);
      m_client.login(new AuthenticationRequest(creds));
    }
  }
  
  /**
   * Get the encapsulated Cassandra.Client object
   * 
   * @return the encapsulated Cassandra.Client object
   */
  public Cassandra.Client getClient() {
    return m_client;
  }
  
  /**
   * Get a keyspace definition for the set keyspace
   * 
   * @return a keyspace definition
   * @throws Exception if a problem occurs
   */
  public KsDef describeKeyspace() throws Exception {
    if (m_keyspaceName == null || m_keyspaceName.length() == 0) {
      throw new Exception("No keyspace has been set!");
    }
    
    return m_client.describe_keyspace(m_keyspaceName);
  }
  
  public void close() {
    if (m_transport != null) {
      m_transport.close();
      m_transport = null;
      m_client = null;
    }
  }
  
  private void checkOpen() throws Exception {
    if (m_transport == null && m_client == null) {
      throw new Exception("Connection is closed!");
    }
  }
  
  public boolean isClosed() {
    try {
      checkOpen();
    } catch (Exception e) {
      return true;
    }
    return false;
  }
  
  /**
   * Set the Cassandra keyspace (database) to use.
   * 
   * @param keySpace the name of the keyspace to use
   * @throws Exception if the keyspace doesn't exist
   */
  public void setKeyspace(String keySpace) throws Exception {
    checkOpen();
    
    m_client.set_keyspace(keySpace);
    m_keyspaceName = keySpace;
  }
  
  /**
   * Get the name of the Cassandra keyspace (database) to use.
   * 
   * @return the name of the keyspace to use
   */
  public String getKeyspace() {
    return m_keyspaceName;
  }
 
  /**
   * Get the column family meta data for the supplied column family (table) name
   * 
   * @param colFam the name of the column family to get meta data for
   * @return the column family meta data
   * @throws Exception if a problem occurs
   */
  public CassandraColumnMetaData getColumnFamilyMetaData(String colFam) 
    throws Exception {
    
    if (m_keyspaceName == null) {
      throw new Exception("No keypsace set!");
    }
    
    CassandraColumnMetaData meta = new CassandraColumnMetaData(this, colFam);

    return meta;
  }
  
  public void executeCQL(String cql, Logger log, boolean compressCQL) {
    // split out separate statements
    String[] cqlRequests = cql.split(";");
    if (cqlRequests.length > 0) {
      for (String cqlC : cqlRequests) {
        cqlC = cqlC.trim();
        if (!cqlC.endsWith(";")) {
          cqlC += ";";
        }

        // try and execute it
        byte[] toSend = null;
        if (compressCQL) {
          toSend = compressQuery(cqlC, Compression.GZIP);
        } else {
          toSend = cqlC.getBytes(Charset.forName(CassandraColumnMetaData.UTF8));
        }

        String errorMessage = null;
        try {
          getClient().execute_cql_query(ByteBuffer.wrap(toSend), 
              compressCQL ? Compression.GZIP : Compression.NONE);
        } catch (InvalidRequestException e) {
          errorMessage = e.why;
        } catch (UnavailableException e) {
          errorMessage = e.getMessage();
        } catch (TimedOutException e) {
          errorMessage = e.getMessage();
        } catch (SchemaDisagreementException e) {            
          errorMessage = e.getMessage();
        } catch (TException e) {
          errorMessage = e.getMessage();
        }

        if (errorMessage != null) {
          if (log != null) {
            log.logMessage("Unable to execute a priori CQL command '" + cqlC +
                "'. (" + errorMessage + ")");
          } else {
            System.err.println("Unable to execute a priori CQL command '" + cqlC +
                "'. (" + errorMessage + ")");
          }
        }
      }
    }
  }
  
  /**
   * Static utility method to compress a CQL query
   * 
   * @param queryStr the CQL query
   * @param compression compression option (GZIP is the only option - so far)
   * @return an array of bytes containing the compressed query
   */
  public static byte[] compressQuery(String queryStr, Compression compression) {
    byte[] data = queryStr.getBytes(Charset.forName(CassandraColumnMetaData.UTF8));
    
    Deflater compressor = new Deflater();
    compressor.setInput(data);
    compressor.finish();

    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];

    while (!compressor.finished()) {
      int size = compressor.deflate(buffer);
      byteArray.write(buffer, 0, size);
    }
    
    return byteArray.toByteArray();
  }
}

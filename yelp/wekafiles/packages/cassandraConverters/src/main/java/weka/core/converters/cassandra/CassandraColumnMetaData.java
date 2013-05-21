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
 *    CassandraColumnMetaData.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.core.converters.cassandra;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.AsciiType;
import org.apache.cassandra.db.marshal.BooleanType;
import org.apache.cassandra.db.marshal.DateType;
import org.apache.cassandra.db.marshal.DecimalType;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.FloatType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.IntegerType;
import org.apache.cassandra.db.marshal.LexicalUUIDType;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UUIDType;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.CqlRow;
import org.apache.cassandra.thrift.KsDef;
import org.apache.commons.codec.binary.Base64;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.Utils;

/**
 * Class that encapsulates meta data on a cassandra column family. Also has
 * some utility routines for various bits and pieces related to column families.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 48815 $
 */
public class CassandraColumnMetaData {

  public static final String UTF8 = "UTF-8";
  public static final String ASCII = "US-ASCII";

  //  public static final String CASSANDRA_CQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";

  /** Name of the column family this meta data refers to */
  protected String m_columnFamilyName; // can be used as the key name  

  /** Type of the key */
  protected String m_keyValidator; // name of the class for key validation

  /** Type of the column names (used for sorting columns) */
  protected String m_columnComparator; // name of the class for sorting column names

  /** m_columnComparator converted to Charset encoding string */
  protected String m_columnNameEncoding;

  /** 
   * Default validator for the column family (table) - we can use this as
   * the type for any columns specified in a SELECT clause which *arent* in
   * the meta data
   */
  protected String m_defaultValidationClass;

  /** Map of column names/types */
  protected Map<String, String> m_columnMeta;

  /** Map of column names to indexed values (if any) */
  protected Map<String, HashSet<Object>> m_indexedVals;

  /** Map of column names to relational values (if any) */
  protected Map<String, Instances> m_relationalCols;

  /** Holds the schema textual description */
  protected StringBuffer m_schemaDescription;   

  /**
   * Constructor.
   * 
   * @param conn connection to cassandra
   * @param columnFamily the name of the column family to maintain meta data for.
   * @throws Exception if a problem occurs during connection or when fetching meta
   * data
   */
  public CassandraColumnMetaData(CassandraConnection conn, 
      String columnFamily) throws Exception {
    m_columnFamilyName = columnFamily;

    refresh(conn);
  }

  /**
   * Refreshes the encapsulated meta data for the column family.
   * 
   * @param conn the connection to cassandra to use for refreshing the meta
   * data
   * @throws Exception if a problem occurs during connection or when
   * fetching meta data
   */
  public void refresh(CassandraConnection conn) throws Exception {

    m_schemaDescription = new StringBuffer();

    // column families               
    KsDef keySpace = conn.describeKeyspace();
    List<CfDef> colFams = null;
    if (keySpace != null) {
      colFams = keySpace.getCf_defs();
    } else {
      throw new Exception("Unable to get meta data on keyspace '" 
          + conn.m_keyspaceName + "'");
    }

    // look for the requested column family
    CfDef colDefs = null;
    for (CfDef fam : colFams) {
      String columnFamilyName = fam.getName(); // table name
      if (columnFamilyName.equals(m_columnFamilyName)) {
        m_schemaDescription.append("Column family: " + m_columnFamilyName);
        m_keyValidator = fam.getKey_validation_class(); // key type                                                
        m_columnComparator = fam.getComparator_type(); // column names encoded as
        m_defaultValidationClass = fam.getDefault_validation_class(); // default column type
        m_schemaDescription.append("\n\tKey validator: " 
            + m_keyValidator.substring(m_keyValidator.lastIndexOf(".")+1, m_keyValidator.length()));
        m_schemaDescription.append("\n\tColumn comparator: " 
            + m_columnComparator.substring(m_columnComparator.lastIndexOf(".")+1, m_columnComparator.length()));
        m_schemaDescription.append("\n\tDefault column validator: " 
            + m_defaultValidationClass.substring(m_defaultValidationClass.lastIndexOf(".")+1, 
                m_defaultValidationClass.length()));

        // these seem to have disappeared between 0.8.6 and 1.0.0!
        /*m_schemaDescription.append("\n\tMemtable operations: " + fam.getMemtable_operations_in_millions());
        m_schemaDescription.append("\n\tMemtable throughput: " + fam.getMemtable_throughput_in_mb());
        m_schemaDescription.append("\n\tMemtable flush after: " + fam.getMemtable_flush_after_mins()); */

        m_schemaDescription.append("\n\tRows cached: " + fam.getRow_cache_size());
        m_schemaDescription.append("\n\tRow cache save period: " + fam.getRow_cache_save_period_in_seconds());
        m_schemaDescription.append("\n\tKeys cached: " + fam.getKey_cache_size());
        m_schemaDescription.append("\n\tKey cached save period: " + fam.getKey_cache_save_period_in_seconds());
        m_schemaDescription.append("\n\tRead repair chance: " + fam.getRead_repair_chance());
        m_schemaDescription.append("\n\tGC grace: " + fam.getGc_grace_seconds());
        m_schemaDescription.append("\n\tMin compaction threshold: " + fam.getMin_compaction_threshold());
        m_schemaDescription.append("\n\tMax compaction threshold: " + fam.getMax_compaction_threshold());
        m_schemaDescription.append("\n\tReplicate on write: " + fam.replicate_on_write);
        String rowCacheP = fam.getRow_cache_provider();
        m_schemaDescription.append("\n\tRow cache provider: " 
            + rowCacheP.substring(rowCacheP.lastIndexOf(".")+1, rowCacheP.length()));
        m_schemaDescription.append("\n\n\tColumn metadata:");

        colDefs = fam;
        break;
      }          
    }

    if (colDefs == null) {
      throw new Exception("Unable to find requested column family '" 
          + m_columnFamilyName + "' in keyspace '" + conn.m_keyspaceName + "'");
    }

    if (m_columnComparator.indexOf("UTF8Type") > 0) {
      m_columnNameEncoding = UTF8;
    } else if (m_columnComparator.indexOf("AsciiType") > 0) {
      m_columnNameEncoding = ASCII;
    } else {
      throw new Exception("Column names are neither UTF-8 or ASCII!");
    }

    // set up our meta data map
    m_columnMeta = new TreeMap<String, String>();
    m_indexedVals = new HashMap<String, HashSet<Object>>();
    m_relationalCols = new HashMap<String, Instances>();

    String comment = colDefs.getComment();
    if (comment != null && comment.length() > 0) {
      extractIndexedMeta(comment, m_indexedVals);
      extractRelationalCols(comment, m_relationalCols);
    }

    //List<ColumnDef> colMetaData = colDefs.getColumn_metadata();
    Iterator<ColumnDef> colMetaData = colDefs.getColumn_metadataIterator();
    if (colMetaData != null) {
      // for (int i = 0; i < colMetaData.size(); i++) {
      while (colMetaData.hasNext()) {
        ColumnDef currentDef = colMetaData.next();
        String colName = new String(currentDef.getName(), 
            Charset.forName(m_columnNameEncoding));
        //      System.out.println("Col name: " + colName);
        String colType = currentDef.getValidation_class();
        //      System.out.println("Validation (type): " + colType);
        m_columnMeta.put(colName, colType);

        m_schemaDescription.append("\n\tColumn name: " + colName);
        m_schemaDescription.append("\n\t\tColumn validator: " 
            + colType.substring(colType.lastIndexOf(".")+1, colType.length()));
        String indexName = currentDef.getIndex_name();
        if (indexName != null && indexName.length() > 0) {
          m_schemaDescription.append("\n\t\tIndex name: " + currentDef.getIndex_name());
        }

        if (m_indexedVals.containsKey(colName)) {
          HashSet<Object> indexedVals = m_indexedVals.get(colName);

          m_schemaDescription.append("\n\t\tLegal values: {");
          int count = 0; 
          for (Object val : indexedVals) {
            m_schemaDescription.append(val.toString());
            count++;
            if (count != indexedVals.size()) {
              m_schemaDescription.append(",");
            } else {
              m_schemaDescription.append("}");
            }
          }
        }
      }
    }    

    //    System.out.println(m_schemaDescription.toString());
  }

  /**
   * Update the encapsulated indexed meta data with user-supplied legal values for string attributes. 
   * Note that this does not change the column family meta data stored by cassandra, only the
   * data encapsulated in this class.
   * 
   * @param newIndexedInfo a string in attName1:{legalVal1, legalVal2,...};attName2:{...
   * format
   * @throws Exception if a problem occurs.
   */
  public void updateIndexedMeta(String newIndexedInfo) throws Exception {
    if (newIndexedInfo != null && newIndexedInfo.length() > 0) {
      if (newIndexedInfo.indexOf("@@@") < 0) {
        newIndexedInfo = "@@@" + newIndexedInfo + "@@@";
      }

      extractIndexedMeta(newIndexedInfo, m_indexedVals);      
    }
  }

  protected void extractIndexedMeta(String comment,            
      Map<String, HashSet<Object>> indexedVals) {
    // We abuse the comment field of the column family
    // to store the labels for nominal attributes

    if (comment.indexOf("@@@") < 0) {
      return;
    }

    String meta = comment.substring(comment.indexOf("@@@"), comment.lastIndexOf("@@@"));
    meta = meta.replace("@@@", "");
    String[] fields = meta.split(";");

    for (String field : fields) {
      field = field.trim();
      String[] parts = field.split(":");
      if (parts.length != 2) {
        continue;
      }

      String fieldName = parts[0].trim();
      //      if (m_columnMeta.containsKey(fieldName)) {
      String valsS = parts[1];
      valsS = valsS.replace("{", "");
      valsS = valsS.replace("}", "");

      String[] vals = valsS.split(",");

      if (vals.length > 0) {
        HashSet<Object> valsSet = new HashSet<Object>();

        for (String aVal : vals) {
          valsSet.add(aVal.trim());
        }

        indexedVals.put(fieldName, valsSet);
      }        
    }
    //  }
  }

  protected void extractRelationalCols(String comment,
      Map<String, Instances> relationalHeaders) {

    // We abuse the comment field for the column family to
    // store serialized instances headers for each relational
    // attribute

    if (comment.indexOf("@@relational-start@@") < 0) {
      return;
    }

    String meta = comment.substring(comment.indexOf("@@relational-start@@"), 
        comment.indexOf("@@relational-end@@"));
    meta = meta.replace("@@relational-start@@", "").
    replace("@@relational-end@@", "");

    String[] relAtt = meta.split("@rel-att@"); // separator for relational atts
    for (String att : relAtt) {
      String[] parts = att.split("@@header@@");

      if (parts.length != 2) {
        continue;
      }

      String fieldName = parts[0].trim();
      //      if (m_columnMeta.containsKey(fieldName)) {
      String base64Insts = parts[1];
      if (base64Insts.length() > 0) {
        try {
          byte[] decoded = decodeFromBase64(parts[1]);

          ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
          ObjectInputStream ois = new ObjectInputStream(bis);
          Instances relationalHeader = (Instances)ois.readObject();
          ois.close();

          relationalHeaders.put(fieldName, relationalHeader);
        } catch (Exception ex) {
          ex.printStackTrace();
          continue;
        }
      }        
      //}
    }
  }

  protected static final byte[] decodeFromBase64(String string)
  throws Exception {
    byte[] bytes;
    if (string == null) {
      bytes = new byte[] {};
    } else {
      bytes = Base64.decodeBase64(string.getBytes());
    }
    if (bytes.length > 0) {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      GZIPInputStream gzip = new GZIPInputStream(bais);
      BufferedInputStream bi = new BufferedInputStream(gzip);
      byte[] result = new byte[] {};

      byte[] extra = new byte[1000000];
      int nrExtra = bi.read(extra);
      while (nrExtra>=0) {
        // add it to bytes...
        //
        int newSize = result.length + nrExtra;
        byte[] tmp = new byte[newSize];
        for (int i=0;i<result.length;i++) tmp[i]=result[i];
        for (int i=0;i<nrExtra;i++) tmp[result.length+i] = extra[i];

        // change the result
        result=tmp;
        nrExtra = bi.read(extra);
      }
      bytes = result;
      gzip.close();
    }

    return bytes;
  }

  protected static final String encodeToBase64(byte[] val) 
  throws IOException {
    String string;
    if (val==null) {
      string=null;
    } else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzos = new GZIPOutputStream(baos);
      BufferedOutputStream bos = new BufferedOutputStream(gzos);
      bos.write(val);
      bos.flush();
      bos.close();

      string = new String(Base64.encodeBase64(baos.toByteArray()));
    }
    return string;
  }

  protected static final String encodeInstances(Instances inst)
  throws Exception {

    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    BufferedOutputStream bos = new BufferedOutputStream(bao);
    ObjectOutputStream oo = new ObjectOutputStream(bos);
    oo.writeObject(inst);
    oo.flush();

    byte[] instBytes = bao.toByteArray();

    oo.close();

    return encodeToBase64(instBytes);
  }  

  /**
   * Static utility routine for checking for the existence of
   * a column family (table)
   * 
   * @param conn the connection to use
   * @param columnFamily the column family to check for
   * @return true if the supplied column family name exists in the keyspace
   * @throws Exception if a problem occurs
   */
  public static boolean columnFamilyExists(CassandraConnection conn,
      String columnFamily) throws Exception {

    boolean found = false;

    // column families               
    KsDef keySpace = conn.describeKeyspace();
    List<CfDef> colFams = null;
    if (keySpace != null) {
      colFams = keySpace.getCf_defs();
    } else {
      throw new Exception("Unable to get meta data on keyspace '" 
          + conn.m_keyspaceName + "'");
    }

    // look for the requested column family
    for (CfDef fam : colFams) {
      String columnFamilyName = fam.getName(); // table name
      if (columnFamilyName.equals(columnFamily)) {
        found = true;
        break;
      }
    }

    return found;
  }

  /**
   * Static utility routine that returns a list of column families that
   * exist in the keyspace encapsulated in the supplied connection
   * 
   * @param conn the connection to use
   * @return a list of column families (tables)
   * @throws Exception if a problem occurs
   */
  public static List<String> getColumnFamilyNames(CassandraConnection conn)
  throws Exception {

    KsDef keySpace = conn.describeKeyspace();
    List<CfDef> colFams = null;
    if (keySpace != null) {
      colFams = keySpace.getCf_defs();
    } else {
      throw new Exception("Unable to get meta data on keyspace '" 
          + conn.m_keyspaceName + "'");
    }

    List<String> colFamNames = new ArrayList<String>();
    for (CfDef fam : colFams) {
      colFamNames.add(fam.getName());
    }

    return colFamNames;
  }

  /**
   * Return the schema overview information
   * 
   * @return the textual description of the schema
   */
  public String getSchemaDescription() {
    return m_schemaDescription.toString();
  }

  /**
   * Return the Cassandra column type (internal cassandra class name relative to 
   * org.apache.cassandra.db.marshal) for the given Weka attribute.
   * 
   * @param a the Attribute
   * @return the corresponding internal cassandra type.
   */
  public static String getCassandraTypeForAttribute(Attribute a) {
    switch (a.type()) {
    case Attribute.NOMINAL:
    case Attribute.STRING:
      return "UTF8Type";
    case Attribute.NUMERIC:
      return "DoubleType";
    case Attribute.DATE:
      return "DateType";
    case Attribute.RELATIONAL:
      // ????
      return "UTF8Type";
    }

    return "UTF8Type";
  }

  /**
   * Return the Cassandra CQL column/key type for the given Weka attribute. We
   * use this type for CQL create column family statements since, for some reason,
   * the internal type isn't recognized for the key. Internal types *are* recognized
   * for column definitions. The CQL reference guide states that fully qualified 
   * (or relative to org.apache.cassandra.db.marshal) class names can be used instead
   * of CQL types - however, using these when defining the key type always results in
   * BytesType getting set for the key for some reason. 
   * 
   * @param a the Attribute
   * @return the corresponding CQL type
   */
  public static String getCQLTypeForAttribute(Attribute a) {
    switch (a.type()) {
    case Attribute.NOMINAL:
    case Attribute.STRING:
      return "varchar";
    case Attribute.NUMERIC:
      return "double";
    case Attribute.DATE:
      return "timestamp";
    case Attribute.RELATIONAL:
      // ????
      return "varchar";
    }

    return "varchar";
  }

  public static String wekaValueToCQL(Attribute a, double value) 
  throws Exception {

    if (a.isNominal() || a.isString()) {
      String toConvert = a.value((int)value);
      UTF8Type u = UTF8Type.instance;
      ByteBuffer decomposed = u.decompose(toConvert);
      String cassandraString = u.getString(decomposed);
      return escapeSingleQuotes(cassandraString);
    }

    if (a.isNumeric()) {
      Double toConvert = new Double(value);
      DoubleType dt = DoubleType.instance;
      ByteBuffer decomposed = dt.decompose(toConvert);
      String cassandraString = dt.getString(decomposed);
      return cassandraString;
    }

    if (a.isDate()) {
      DateType d = DateType.instance;
      Date toConvert = new Date((long)value);
      ByteBuffer decomposed = d.decompose(toConvert);
      String cassandraFormattedDateString = d.getString(decomposed);
      return escapeSingleQuotes(cassandraFormattedDateString);
    }

    if (a.isRelationValued()) {
      Instances vInst = a.relation((int)value);

      // values are base64 encoded compressed
      // serialized instances objects
      String base64Encoded = encodeInstances(vInst);
      return base64Encoded;
    }

    throw new Exception("Not sure how to encode attribute '" 
        + a.toString() + "'");
  }
  
  protected static String escapeSingleQuotes(String source) {
    
    // escaped by doubling (as in SQL)
    return source.replace("'", "''");
  }

  /**
   * Get the Weka attribute type that corresponds to the type of the key
   * for this column family.
   * 
   * @return the key's type
   */
  public Attribute getAttributeTypeForKey() {
    return getAttributeTypeForColumm(getKeyName());
  }

  /**
   * Get the Weka attribute type that corresponds to the type of the
   * supplied cassandra column. Note that there is no Cassandra column
   * type that corresponds to a relational attribute. Relational
   * values are stored as base 64 encoded compressed serialized Instances
   * objects. The header instances for each relational column are stored
   * in the comment field of the column family. The string 
   * "@@relational-start@@" denotes the start of a list of relational
   * header definitions, and the string "@@relational-end@@" denotes the
   * end. Between these delimiters are header definitions separated by
   * "@@@". Each definition is <br><br> 
   * 
   * <att name>@@header@@<base 64 encoded gzip compressed serialized instances>
   * 
   * @param colName the name of the column to get a ValueMeta for
   * @return the Weka attribute type appropriate for the type of the supplied
   * column.
   */
  public Attribute getAttributeTypeForColumm(String colName) {
    String type = null;
    Attribute result = null;

    // check the key first
    if (colName.equals(getKeyName())) {
      type = m_keyValidator;
    } else {
      type = m_columnMeta.get(colName);
      if (type == null) {
        type = m_defaultValidationClass;
      }
    }

    if (type.indexOf("UTF8Type") > 0 || type.indexOf("AsciiType") > 0 ||
        type.indexOf("UUIDType") > 0) {

      if (m_relationalCols.containsKey(colName)) {
        Instances relStructure = m_relationalCols.get(colName);
        result = new Attribute(colName, relStructure);
      } else if (m_indexedVals.containsKey(colName)) {
        // indexed values? == nominal
        HashSet<Object> vals = m_indexedVals.get(colName);
        ArrayList<String> attVals = new ArrayList<String>();
        for (Object v : vals) {
          attVals.add(v.toString());
        }
        result = new Attribute(colName, attVals);        
      } else {
        // string
        result = new Attribute(colName, 
            (ArrayList<String>) null);        
      }
    } else if (type.indexOf("LongType") > 0 || type.indexOf("IntegerType") > 0 ||
        type.indexOf("Int32Type") > 0 || type.indexOf("DoubleType") > 0 ||
        type.indexOf("FloatType") > 0 || type.indexOf("DecimalType") > 0 || 
        type.indexOf("BooleanType") > 0) { 
      result = new Attribute(colName);      
    } else if (type.indexOf("DateType") > 0) {
      result = new Attribute(colName, "yyyy-MM-dd'T'HH:mm:ss");
    }

    return result;
  }

  public List<Attribute> getAttributeTypesForSchema() {
    List<Attribute> newL = new ArrayList<Attribute>();

    for (String colName : m_columnMeta.keySet()) {
      Attribute colA = getAttributeTypeForColumm(colName);
      newL.add(colA);
    }

    return newL;
  }

  /**
   * Get a Set of column names that are defined in the meta data for this
   * schema
   * 
   * @return a set of column names.
   */
  public Set<String> getColumnNames() {
    // only returns those column names that are defined in the schema!
    return m_columnMeta.keySet();
  }

  /**
   * Returns true if the supplied column name exists in this schema.
   * 
   * @param colName the name of the column to check.
   * @return true if the column exists in the meta data for this column family.
   */
  public boolean columnExistsInSchema(String colName) {
    return (m_columnMeta.get(colName) != null);
  }

  /**
   * Get the name of the key for this column family (equals the name
   * of the column family).
   * 
   * @return the name of the key
   */
  public String getKeyName() {
    // we use the column family/table name as the key
    return getColumnFamilyName();
  }

  /**
   * Return the name of this column family.
   * 
   * @return the name of this column family.
   */
  public String getColumnFamilyName() {
    return m_columnFamilyName;
  }

  /**
   * Get the value of the key for the supplied cassandra row
   * 
   * @param row the cassandra row to get the key of
   * @return the key
   * @throws Exception if a problem occurs
   */
  public Object getKeyValue(CqlRow row) throws Exception {
    ByteBuffer key = row.bufferForKey();
    return getColumnValue(key, m_keyValidator);
  }

  public String getColumnName(Column aCol) {
    byte[] colName = aCol.getName();
    String decodedColName = new String(colName, Charset.forName(m_columnNameEncoding));

    return decodedColName;
  }

  private Object getColumnValue(ByteBuffer valueBuff, String decoder) throws Exception {
    if (valueBuff == null) {
      return null;
    }

    Object result = null;
    AbstractType deserializer = null;    

    if (decoder.indexOf("UTF8Type") > 0) {
      deserializer = UTF8Type.instance;
    } else if (decoder.indexOf("AsciiType") > 0) {
      deserializer = AsciiType.instance;
    } else if (decoder.indexOf("LongType") > 0) {
      deserializer = LongType.instance;      
    } else if (decoder.indexOf("DoubleType") > 0) {
      deserializer = DoubleType.instance;
    } else if (decoder.indexOf("DateType") > 0) {
      deserializer = DateType.instance;
    } else if (decoder.indexOf("IntegerType") > 0) {
      deserializer = IntegerType.instance;
    } else if (decoder.indexOf("FloatType") > 0) {
      deserializer = FloatType.instance;
    } else if (decoder.indexOf("LexicalUUIDType") > 0) {
      deserializer = LexicalUUIDType.instance;
    } else if (decoder.indexOf("UUIDType") > 0) {
      deserializer = UUIDType.instance;
    } else if (decoder.indexOf("BooleanType") > 0) {
      deserializer = BooleanType.instance;
    } else if (decoder.indexOf("Int32Type") > 0) {
      deserializer = Int32Type.instance;
    } else if (decoder.indexOf("DecimalType") > 0) {
      deserializer = DecimalType.instance;
    }

    if (deserializer == null) {
      throw new Exception("Can't find deserializer for type '" + decoder + "'");
    }

    result = deserializer.compose(valueBuff);

    return result;
  }

  /**
   * Decode the supplied column value. Uses the default validation class to
   * decode the value if the column is not explicitly defined in the schema. 
   * 
   * @param aCol
   * @return the value of the column
   * @throws Exception if a problem occurs
   */
  public Object getColumnValue(Column aCol) throws Exception {
    String colName = getColumnName(aCol);

    // Clients should use getKey() for getting the key
    if (colName.equals("KEY")) {
      return null;
    }

    String decoder = m_columnMeta.get(colName);
    if (decoder == null) {
      // column is not in schema so use default validator
      decoder = m_defaultValidationClass;
    }    

    if (decoder.indexOf("BytesType") > 0) {
      return aCol.getValue(); // raw bytes
    }

    ByteBuffer valueBuff = aCol.bufferForValue();
    return getColumnValue(valueBuff, decoder);    
  }

  protected double objectToWekaIndex(Object value, Attribute att,
      boolean retainStringValues) throws Exception {
    double result = Utils.missingValue();

    if (att.isString()) {
      if (retainStringValues) {
        result = att.addStringValue(value.toString());
      } else {
        att.setStringValue(value.toString());
        result = 0;
      }
    } else if (att.isDate()) {
      result = ((Date)value).getTime();
    } else if (att.isNumeric()) {
      if (value instanceof Boolean) {
        result = ((Boolean)value).booleanValue() ? 1.0 : 0.0;
      } else {
        result = ((Number)value).doubleValue();
      }
    } else if (att.isNominal()) {
      int index = att.indexOfValue(value.toString().trim());
      if (index < 0) {
        result = Utils.missingValue();
      } else {
        result = index;
      }
    } else if (att.isRelationValued()) {

      byte[] decoded = decodeFromBase64(value.toString());

      ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
      ObjectInputStream ois = new ObjectInputStream(bis);
      Instances relational = (Instances)ois.readObject();
      ois.close();

      Instances relationalHeader = att.relation();
      if (relationalHeader == null) {
        throw new IOException("[CassandraColumnMetaData] Can't find a relational header " +
            "for relational column '" + att.name() + "'!");
      } else if (!relationalHeader.equalHeaders(relational)) {
        throw new IOException("[CassandraColumnMetaData] Structure of instances deserialized " +
            "from cassandra row for column '" + att.name() + "' are not " +
            "compatible with the structure stored in the " +
        "header!");
      } else {
        result = att.addRelation(relational);
      }
    }

    return result;
  }

  /**
   * Convert a cassandra row into a Weka dense instance.
   * 
   * @param row the row read from Cassandra
   * @param structure the structure of the instances to convert to
   * @param retainStringValues true if all string values are to be retained
   * in the instances header
   * @return the converted instance
   * @throws Exception if a problem occurs
   */
  public Instance cassandraRowToInstance(CqlRow row, Instances structure, 
      boolean retainStringValues) throws Exception {

    // Dense mode converts non-present columns in a row to missing values

    String keyName = getKeyName();
    Object keyVal = getKeyValue(row);

    double[] rawVals = new double[structure.numAttributes()];
    // make sure that any columns not in this row get set to missing
    for (int i = 0; i < rawVals.length; i++) {
      rawVals[i] = Utils.missingValue();
    }

    // key first
    if (structure.attribute(keyName.trim()) != null) {
      int keyIndex = structure.attribute(keyName).index();

      double val = objectToWekaIndex(keyVal, structure.attribute(keyName.trim()), 
          retainStringValues);
      rawVals[keyIndex] = val;
    }

    // remaining columns
    List<Column> rowColumns = row.getColumns();
    for (Column aCol : rowColumns) {
      String colName = getColumnName(aCol);
      Attribute att = structure.attribute(colName.trim());
      if (att != null) {
        Object colValue = getColumnValue(aCol);
        double val = objectToWekaIndex(colValue, att, retainStringValues);
        rawVals[att.index()] = val;
      }
    }

    Instance result = new DenseInstance(1.0, rawVals);
    result.setDataset(structure);

    return result;
  }

  protected class SparseVal implements Comparator<SparseVal>,
  Comparable<SparseVal> {

    public int m_index;
    public double m_value;

    public int compareTo(SparseVal o) {
      return compare(this, o);

    }

    public int compare(SparseVal val1, SparseVal val2) {
      return val1.m_index - val2.m_index;
    }

    public boolean equals(Object other) {
      if (other == null || !(other instanceof SparseVal)) {
        return false;
      }

      return (compareTo((SparseVal)other) == 0);
    }

    public int hashCode() {
      return new Integer(m_index).hashCode();
    }    
  }

  /**
   * Convert a cassandra row into a Weka sparse instance.
   * 
   * @param row the row read from Cassandra
   * @param structure the structure of the instances to convert to
   * @param retainStringValues true if all string values are to be retained
   * in the instances header
   * @return the converted instance
   * @throws Exception if a problem occurs
   */
  public Instance cassandraRowToInstanceSparse(CqlRow row, Instances structure,
      boolean retainStringValues) throws Exception {

    // Sparse mode does not support missing values for non-zero elements (i.e. columns 
    // that are present in a row)
    //

    String keyName = getKeyName();
    Object keyVal = getKeyValue(row);

    List<SparseVal> sparseVals = new ArrayList<SparseVal>();

    if (structure.attribute(keyName.trim()) != null) {
      int keyIndex = structure.attribute(keyName.trim()).index();
      double val = objectToWekaIndex(keyVal, structure.attribute(keyName.trim()), 
          retainStringValues);
      SparseVal v = new SparseVal();
      v.m_index = keyIndex;
      v.m_value = val;
      sparseVals.add(v);
    }

    // remaining columns
    List<Column> rowColumns = row.getColumns();
    for (Column aCol : rowColumns) {
      String colName = getColumnName(aCol);
      Attribute att = structure.attribute(colName.trim());
      if (att != null) {
        Object colValue = getColumnValue(aCol);
        double val = objectToWekaIndex(colValue, att, retainStringValues);
        SparseVal v = new SparseVal();
        v.m_index = att.index();
        v.m_value = val;
        sparseVals.add(v);
      }
    }

    Collections.sort(sparseVals);
    double[] tempValues = new double[sparseVals.size()];
    int[] tempIndices = new int[sparseVals.size()];
    for (int i = 0; i < sparseVals.size(); i++) {
      tempValues[i] = sparseVals.get(i).m_value;
      tempIndices[i] = sparseVals.get(i).m_index;
    }
    Instance inst = 
      new SparseInstance(1.0, tempValues, tempIndices, structure.numAttributes());
    inst.setDataset(structure);

    return inst;
  }

  /**
   * Extract the column family name (table name) from a CQL SELECT
   * query. Assumes that any environment variables have been already substituted
   * in the query
   * 
   * @param subQ the query with vars substituted
   * @return the column family name or null if the query is malformed
   */
  public static String getColumnFamilyNameFromCQLSelectQuery(String subQ) {

    String result = null;

    if (subQ == null || subQ.length() == 0) {
      return null;
    }

    // assumes env variables already replaced in query!

    if (!subQ.toLowerCase().startsWith("select")) {
      // not a select statement!
      return null;
    }

    if (subQ.indexOf(';') < 0) {
      // query must end with a ';' or it will wait for more!
      return null;
    }

    //subQ = subQ.toLowerCase();

    // strip off where clause (if any)
    if (subQ.toLowerCase().lastIndexOf("where") > 0) {
      subQ = subQ.substring(0, subQ.toLowerCase().lastIndexOf("where"));
    }

    // determine the source column family
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

    //    int fromIndex = subQ.toLowerCase().lastIndexOf("from");
    if (fromIndex < 0) {
      return null; // no from clause
    }

    result = subQ.substring(fromIndex + 4, subQ.length()).trim();
    if (result.indexOf(' ') > 0) {
      result = result.substring(0, result.indexOf(' '));
    } else {
      result = result.replace(";", "");
    }

    if (result.length() == 0) {
      return null; // no column family specified
    }

    return result;
  }

  public static boolean createColumnFamily(CassandraConnection conn, 
      String colFamilyName, Instances structure, int keyIndex, 
      boolean compressCQL) throws Exception {

    // TODO handle optional keywords for column family creation - default comparator, 
    // key_cache_size etc.
    StringBuffer buff = new StringBuffer();
    buff.append("CREATE COLUMNFAMILY " + colFamilyName);

    buff.append(" (KEY ");
    if (keyIndex < 0) {
      // generated key
      buff.append("'bigint'");
    } else {
      buff.append("'" + getCQLTypeForAttribute(structure.attribute(keyIndex))
          + "'");
    }
    buff.append(" PRIMARY KEY");

    List<Attribute> indexedVals = new ArrayList<Attribute>();
    List<Attribute> relationalVals = new ArrayList<Attribute>();    
    int minAtts = (keyIndex >= 0) ? 1 : 0;

    if (structure.numAttributes() > minAtts) {
      for (int i = 0; i < structure.numAttributes(); i++) {
        if (i != keyIndex) {
          Attribute a = structure.attribute(i);
          if (a.isNominal()) {
            indexedVals.add(a);
          } else if (a.isRelationValued()) {
            relationalVals.add(a);
          }

          String colName = a.name();
          String colType = "'" + getCQLTypeForAttribute(a) + "'";

          buff.append(", ");
          buff.append("'" + colName + "'").append(" ");
          buff.append(colType);
        }
      }
    } else {
      return false;
    }

    if (indexedVals.size() == 0 && relationalVals.size() == 0) {
      buff.append(");");
    } else {
      buff.append(") WITH comment = '");

      if (indexedVals.size() > 0) {
        buff.append("@@@");
        int count = 0;
        for (Attribute a : indexedVals) {
          String colName = a.name();
          buff.append(colName).append(":{");

          for (int i = 0; i < a.numValues(); i++) {
            buff.append(a.value(i));
            if (i != a.numValues() - 1) {
              buff.append(",");
            }
          }
          buff.append("}");
          if (count != indexedVals.size() - 1) {
            buff.append(";");
          }
          count++;
        }
        buff.append("@@@");
      }

      if (relationalVals.size() > 0) {
        int count = 0;
        buff.append("@@relational-start@@");
        for (Attribute a : relationalVals) {
          buff.append(a.name());
          buff.append("@@header@@");
          String encodedH = encodeInstances(a.relation());
          buff.append(encodedH);

          if (count != relationalVals.size() - 1) {
            buff.append("@rel-att@");
          }
        }
        buff.append("@@relational-end@@");        
      }

      buff.append("';");
    }
    System.out.println(buff.toString());

    byte[] toSend = null;
    if (compressCQL) {
      toSend = CassandraConnection.compressQuery(buff.toString(), Compression.GZIP);
    } else {
      toSend = buff.toString().getBytes(Charset.forName(CassandraColumnMetaData.UTF8));
    }
    conn.getClient().execute_cql_query(ByteBuffer.wrap(toSend), 
        compressCQL ? Compression.GZIP : Compression.NONE);    

    return true;

  }

  /**
   * Updates the schema information for a given column family with any
   * fields in the supplied Instances structure that aren't defined in the
   * schema. Abuses the schema "comment" field to store information on
   * any nominal and relational values that might be in the Instances structure.
   * 
   * @param conn the connection to use
   * @param colFamilyName the name of the column family to update
   * @param structure the Instances structure containing (potentially) new fields
   * @param keyIndex the index of the key field in the row meta
   * @param cassandraMeta meta data for the cassandra column family
   * @throws Exception if a problem occurs updating the schema
   */
  public static void updateCassandraMeta(CassandraConnection conn,
      String colFamilyName, Instances structure, int keyIndex, 
      CassandraColumnMetaData cassandraMeta) throws Exception {
    // column families               
    KsDef keySpace = conn.describeKeyspace();
    List<CfDef> colFams = null;
    if (keySpace != null) {
      colFams = keySpace.getCf_defs();
    } else {
      throw new Exception("Unable to get meta data on keyspace.");
    }

    // look for the requested column family
    CfDef colFamDefToUpdate = null;
    //    CfDef colDefs = null;
    for (CfDef fam : colFams) {
      String columnFamilyName = fam.getName(); // table name
      if (columnFamilyName.equals(colFamilyName)) {
        colFamDefToUpdate = fam;
        break;
      }
    }

    if (colFamDefToUpdate == null) {
      throw new Exception("Can't update meta data - unable to find " +
          "column family '" + colFamilyName + "'");
    }

    String comment = colFamDefToUpdate.getComment();

    List<Attribute> nominalAtts = new ArrayList<Attribute>();
    List<Attribute> relationalAtts = new ArrayList<Attribute>();    
    for (int i = 0; i < structure.numAttributes(); i++) {
      if (i != keyIndex) {
        Attribute a = structure.attribute(i);
        if (a.isNominal()) {
          nominalAtts.add(a);
        } else if (a.isRelationValued()) {
          relationalAtts.add(a);
        }

        String colName = a.name();
        if (!cassandraMeta.columnExistsInSchema(colName)) {
          String colType = getCassandraTypeForAttribute(a);

          ColumnDef newCol = new ColumnDef(ByteBuffer.wrap(colName.getBytes()), 
              colType);
          colFamDefToUpdate.addToColumn_metadata(newCol);
        }
      }
    }

    // update the comment field for any new nominal or relational vals
    if (nominalAtts.size() > 0 || relationalAtts.size() > 0) {
      String before = "";
      String after = "";
      String metaI = "";
      String metaR = "";

      if (comment != null && comment.length() > 0) {
        // check for existing meta data
        if (comment.indexOf("@@@") >= 0) {
          before = comment.substring(0, comment.indexOf("@@@"));
          if (comment.indexOf("@@relational-end@@") > 0) {
            after = comment.
            substring(comment.lastIndexOf("@@relational-end@@") + 18, comment.length());
          } else {
            after = comment.substring(comment.lastIndexOf("@@@") + 3, comment.length());
          }

          metaI = comment.substring(comment.indexOf("@@@", comment.lastIndexOf("@@@")));
          metaI = metaI.replace("@@@", "");
        }
        if (comment.indexOf("@@relational-start@@") > 0) {
          if (before.length() == 0) {
            before = comment.substring(0, comment.indexOf("@@relational-start@@"));
          }
          if (after.length() == 0) {
            after = comment.
            substring(comment.lastIndexOf("@@relational-end@@") + 18, comment.length());
          }
          metaR = comment.substring(comment.indexOf("@@relational-start@@"), 
              comment.indexOf("@@relational-end@"));
          metaR = metaR.replace("@@relational-start@@", "");
          metaR = metaR.replace("@@relational=end@@", "");
        }
      }

      StringBuffer buff = new StringBuffer();

      // nominal atts
      if (metaI.length() > 0 || nominalAtts.size() > 0) {
        buff.append("@@@");
      }
      if (metaI.length() > 0) {
        buff.append(metaI);
      }
      if (nominalAtts.size() > 0) {
        for (int i = 0; i < nominalAtts.size(); i++) {
          String attName = nominalAtts.get(i).name();
          if (metaI.indexOf(attName) < 0) {
            // add this one
            if (buff.length() > 3) {
              buff.append(";").append(attName).append(":{");
            } else {
              buff.append(attName).append(":{");
            }              
            for (int j = 0; j < nominalAtts.get(i).numValues(); j++) {
              buff.append(nominalAtts.get(i).value(j));
              if (j != nominalAtts.get(i).numValues() - 1) {
                buff.append(",");
              }
            }
            buff.append("}");              
          }
        }
      }
      if (metaI.length() > 0 || nominalAtts.size() > 0) {
        buff.append("@@@");
      }

      // relational atts
      if (metaR.length() > 0 || relationalAtts.size() > 0) {
        buff.append("@@relational-start@@");
      }
      if (metaR.length() > 0) {
        buff.append(metaR);
      }

      boolean first = metaR.length() == 0;
      if (relationalAtts.size() > 0) {
        for (int i = 0; i < relationalAtts.size(); i++) {
          String attName = relationalAtts.get(i).name();
          Instances header = relationalAtts.get(i).relation();

          if (first) {
            first = false;
            buff.append(attName).append("@@header@@");

            // serialized header
            String encoded = encodeInstances(header);
            buff.append(encoded);
          } else {
            buff.append("@@rel-att@@").append(attName).append("@@header@@");

            // serialized header
            String encoded = encodeInstances(header);
            buff.append(encoded);
          }            
        }          
      }

      if (metaR.length() > 0 || relationalAtts.size() > 0) {
        buff.append("@@relational-end@@");
      }
      
      comment = before + buff.toString() + after;
      colFamDefToUpdate.setComment(comment);
    }
    
    conn.getClient().system_update_column_family(colFamDefToUpdate);
    
    // get the cassandraMeta to refresh itself
    cassandraMeta.refresh(conn);
  }  
  
  public static void truncateColumnFamily(CassandraConnection conn, 
      String columnFamily) throws Exception {
    String cqlCommand = "TRUNCATE " + columnFamily;
    
    conn.getClient().execute_cql_query(ByteBuffer.wrap(cqlCommand.getBytes()), 
        Compression.NONE);
  }
  
  
}

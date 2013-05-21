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
 *    FlowRunnerRemote.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.server.knowledgeFlow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import weka.core.CommandlineRunnable;
import weka.core.Environment;
import weka.core.Option;
import weka.core.Utils;
import weka.gui.beans.BeanConnection;
import weka.gui.beans.BeanInstance;
import weka.gui.beans.xml.XMLBeans;
import weka.server.ExecuteTaskServlet;
import weka.server.NamedTask;
import weka.server.Schedule;
import weka.server.WekaServer;
import weka.server.WekaServlet;

/**
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 47640 $
 */
public class FlowRunnerRemote implements CommandlineRunnable {
  
  public static final String NAME_PREFIX = "KF_";
  
  protected Map<String, String> m_environmentParams = 
    new HashMap<String, String>();

  protected String m_name;
  protected StringBuffer m_flowXML;
  protected Schedule m_schedule;
  protected boolean m_sequential;
  Map<String, String> m_parameters;
  
  protected String m_host;
  protected String m_port = "" + WekaServer.PORT;
  
  protected String m_username;
  protected String m_password;
  
  /**
   * Load an XML serialized KnowledgeFlow
   *
   * @param fileName the name of the file to load from
   * @throws Exception if something goes wrong
   */
  public void loadXML(String fileName) throws Exception {
    if (!fileName.endsWith(".kfml")) {
      throw new Exception("File must be an XML flow (*.kfml)");
    }
    
    // check that we can load/deserialize it locally first
    BeanConnection.init();
    BeanInstance.init();
    XMLBeans xml = new XMLBeans(null, null, 0);
    File file = new File(fileName);
    Vector v = (Vector) xml.read(file);
    
    m_name = file.getName().substring(0, file.getName().lastIndexOf("."));
    
    // now just read in the XML text
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line = "";
    m_flowXML = new StringBuffer();
    
    while ((line = br.readLine()) != null) {
      m_flowXML.append(line);
    }
  } 
  
  public String constructURL(String serviceAndArguments) {
    String realHostname = m_host;
    String realPort = m_port;
    try {
      realHostname = Environment.getSystemWide().substitute(m_host);
      realPort = Environment.getSystemWide().substitute(m_port);
    } catch (Exception ex) {}
    
    if (realPort.equals("80")) {
      realPort = "";
    } else {
      realPort = ":" + realPort;
    }
    
    String retVal = "http://" + realHostname + realPort 
      + serviceAndArguments;
    
    retVal = retVal.replace(" ", "%20");    
    
    return retVal;
  }
  
  public void executeFlowRemote() throws Exception {
    Exception exception = null;
    
    NamedTask taskToRun = null;
    
    if (m_schedule == null) {
      taskToRun = 
        new UnscheduledNamedKFTask(NAME_PREFIX + m_name, m_flowXML, m_sequential, m_parameters);
    } else {
      taskToRun = 
        new ScheduledNamedKFTask(NAME_PREFIX + m_name, m_flowXML, m_sequential, m_parameters,
            m_schedule);
      
    }
    
    InputStream is = null;
    //BufferedInputStream bi = null;
    PostMethod post = null;
    
    try {
    
      // serialized and compressed
      byte[] serializedTask = WekaServer.serializeTask(taskToRun);
      System.out.println("Sending " + serializedTask.length + " bytes...");
      
      String service = ExecuteTaskServlet.CONTEXT_PATH + "/?client=Y";
      post = new PostMethod(constructURL(service));
      RequestEntity entity = new ByteArrayRequestEntity(serializedTask);
      post.setRequestEntity(entity);
      
      post.setDoAuthentication(true);
      post.addRequestHeader(new Header("Content-Type", 
          "application/octet-stream"));
      
      // Get HTTP client
      HttpClient client = WekaServer.ConnectionManager.getSingleton().createHttpClient();
      WekaServer.ConnectionManager.addCredentials(client, 
          m_username, m_password);
      
      // Execute request
      
      int result = client.executeMethod(post);
      System.out.println("Response status from server : " + result);
      
      // the response
/*      is = post.getResponseBodyAsStream();
      bi = new BufferedInputStream(is);
      StringBuffer bodyB = new StringBuffer();
      int c;
      while ((c = bi.read())!=-1) {
        bodyB.append((char)c);
      } */
      
      if (result == 401) {
        // Security problem - authentication required
        /*bodyB.setLength(0);
        bodyB.append("Unable to send task to server - " +
        		"authentication required.\n"); */
        System.err.println("Unable to send task to server - authentication required.\n");
      } else {

        is = post.getResponseBodyAsStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        //System.out.println("Number of bytes in response " + ois.available());
        Object response = ois.readObject();
        if (response.toString().startsWith(WekaServlet.RESPONSE_ERROR)) {
          System.err.println("A problem occurred at the sever : \n"
              + "\t" + response.toString());
        } else {
          System.out.println("Task ID from server : " + response.toString());
        }


        /*      String body = bodyB.toString();
      if (body.startsWith(WekaServlet.RESPONSE_ERROR)) {
        System.err.println("A problem occurred at the sever : \n"
            + "\t" + body);
      } else {
        System.out.println("Task ID from server : " + body);
      } */
      }
      
    } catch (Exception ex) {
      ex.printStackTrace();
      exception = ex;
    } finally {
      /*if (bi != null) {
        bi.close();
      } */
      
      if (is != null) {
        is.close();
      }
      
      if (post != null) {
        // Release current connection to the connection pool
        post.releaseConnection();
      }
    }
    
    if (exception != null) {
      throw new Exception(exception.fillInStackTrace());
    }
  }
  
  public static String commandLineUsage() {
    StringBuffer result = new StringBuffer();
    result.append("Usage: FlowRunnerRemote -file <flowFile.kfml> " +
    		"[-sequential] -server <host:[port]> [-username <username>] " +
    		"[-password <password>] [-param <name=value>, " +
    		"-param <name=value>, ... -- [<schedule options>]");
    
    result.append("\n\n");
    result.append("Scheduling options:\n\n");
    Schedule temp = new Schedule();
    Enumeration<Option> opts = temp.listOptions();
    while (opts.hasMoreElements()) {
      Option o = opts.nextElement();
      result.append(o.synopsis() + "\n");
      result.append(o.description() + "\n");
    }
    
    return result.toString();
  }

  public void run(Object toRun, String[] args) throws IllegalArgumentException {    

    if (args.length < 1) {
      System.out.println(FlowRunnerRemote.commandLineUsage());
      System.exit(1);
    } else {
      
      String[] scheduleOpts = Utils.partitionOptions(args);
      if (scheduleOpts.length > 0) {
        m_schedule = new Schedule();
        try {
          m_schedule.setOptions(scheduleOpts);
        } catch (Exception e) {
          throw new IllegalArgumentException(e.getMessage());
        }
      }

      String flowFile = null;
      String host = null;
      String port = "" + WekaServer.PORT;
      m_parameters = new HashMap<String, String>();

      for (int i = 0; i < args.length; i++) {
        if (args[i].equalsIgnoreCase("-file")) {
          if (++i == args.length) {
            System.out.println(FlowRunnerRemote.commandLineUsage());
            System.exit(1);
          }
          flowFile = args[i];
        } else if (args[i].equalsIgnoreCase("-seqential")) {
          m_sequential = true;
          i++;
        } else if (args[i].equalsIgnoreCase("-server")) {
          if (++i == args.length) {
            System.out.println(FlowRunnerRemote.commandLineUsage());
            System.exit(1);
          }
          String[] parts = args[i].split(":");
          host = parts[0];
          if (parts.length == 2) {
            port = "" + Integer.parseInt(parts[1]);
          }
          m_host = host;
          m_port = port;
        } else if (args[i].equalsIgnoreCase("-username")) {
          if (++i == args.length) {
            System.out.println(FlowRunnerRemote.commandLineUsage());
            System.exit(1);
          }
          m_username = args[i];
        } else if (args[i].equalsIgnoreCase("-password")) {
          if (++i == args.length) {
            System.out.println(FlowRunnerRemote.commandLineUsage());
            System.exit(1);
          }
          m_password = args[i];
        } else if (args[i].equalsIgnoreCase("-param")) {
          if (++i == args.length) {
            System.out.println(FlowRunnerRemote.commandLineUsage());
            System.exit(1);
          }
          String[] parts = args[i].split("=");
          if (parts.length != 2) {
            System.out.println(FlowRunnerRemote.commandLineUsage());
            System.exit(1);
          }
          m_parameters.put(parts[0], parts[1]);
        } else if (args[i].length() > 0) {
          System.out.println(FlowRunnerRemote.commandLineUsage());
          System.exit(1);
        }
      }

      try {
        loadXML(flowFile);
      } catch (Exception e) {
        throw new IllegalArgumentException("Unable to load flow '" 
            + args[0] +"'");
      }
      
      try {
        executeFlowRemote();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public static void main(String[] args) {
    try {
      FlowRunnerRemote fr = new FlowRunnerRemote();
      fr.run(fr, args);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

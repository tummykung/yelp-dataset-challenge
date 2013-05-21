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
 *    GetTaskResultServlet.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import weka.experiment.TaskStatusInfo;

/**
 * Get the result of a task (if available). For clients it returns
 * the TaskStatusInfo (which encapsulates the result). So in this
 * case is the same as the GetTaskStatusServlet. For browsers
 * the servlet prints out the result as a string.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 47640 $
 */
public class GetTaskResultServlet extends WekaServlet {

  /**
   * For serialization 
   */
  private static final long serialVersionUID = 9087908005486307445L;
  
  /** The context path for this servlet */
  public static final String CONTEXT_PATH = "/weka/taskResult";

  /**
   * Constructs a new GetTaskResultServlet
   * 
   * @param taskMap the task map maintained by the server
   * @param server a reference to the server itself
   */
  public GetTaskResultServlet(WekaTaskMap taskMap, WekaServer server) {
    super(taskMap, server);
  }
  
  /**
   * Process a HTTP GET
   * 
   * @param request the request
   * @param response the response
   * 
   * @throws ServletException
   * @throws IOException
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {

    if (!request.getRequestURI().startsWith(CONTEXT_PATH)) {
      return;
    }

    String taskName = request.getParameter("name");
    String clientParam = request.getParameter("client");
    boolean client = (clientParam != null && clientParam.equalsIgnoreCase("y"));

    response.setStatus(HttpServletResponse.SC_OK);
    if (client) {
      response.setContentType("application/octet-stream");
    } else {
      response.setCharacterEncoding("UTF-8");
      response.setContentType("text/html;charset=UTF-8");
    }

    ObjectOutputStream oos = null;
    PrintWriter out = null;

    // Get the task
    NamedTask task = m_taskMap.getTask(taskName);
    try {
      if (task == null) {
        if (client) {
          String errorResult = WekaServlet.RESPONSE_ERROR + ": Can't find task " + taskName;
          OutputStream outS = response.getOutputStream();
          oos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(outS)));
          oos.writeObject(errorResult);
          oos.flush();
        } else {
          out = response.getWriter();

          out.println("<HTML>");
          out.println("<HEAD>");
          out.println("<TITLE>Task Result</TITLE>");
          out.println("</HEAD>");
          out.println("<BODY>");

          out.println(WekaServlet.RESPONSE_ERROR + ": Unknown task " + taskName);
          out.println("<p>");
        }
      } else {
        TaskStatusInfo status = null;

        WekaTaskMap.WekaTaskEntry te = m_taskMap.getTaskKey(taskName);

        if (te.getServer().equals(m_server.getHostname() + ":" + m_server.getPort())) {
          
          // ask the task to load it (if they have persisted it to save memory)
          task.loadResult();
          status = task.getTaskStatus();
        } else {
          // need to ask the slave for it (and handle error if slave is down...)
          String slave = te.getServer();
          String remoteTaskID = te.getRemoteID();
          status = getResultRemote(m_server, slave, remoteTaskID, taskName);
        }

        if (client) {
          // is actually the same as GetTaskStatusServlet, since the task status
          // object encapsulates the result
          OutputStream outS = response.getOutputStream();

          // send status (and result if ready) back to client        
          oos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(outS)));
          oos.writeObject(status);
          oos.flush();
          
          // tell the task to free memory (if possible) since the client has collected the
          // result now
          task.freeMemory();
        } else {
          out = response.getWriter();

          out.println("<HTML>");
          out.println("<HEAD>");
          out.println("<TITLE>Task Status</TITLE>");
          out.println("<META http-equiv=\"Refresh\" content=\"20;url=" + CONTEXT_PATH + "?name=" 
              + URLEncoder.encode(taskName, "UTF-8") + "\">");
          out.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
          out.println("</HEAD>");
          out.println("<BODY>");
          out.println("<H1>" + taskName + "</H1>");

          if (status == null) {
            out.println("Status is unavailable");
          } else {
            if (status.getExecutionStatus() == TaskStatusInfo.PROCESSING) {
              out.println("No result available yet - task is processing...");
            } else if (status.getExecutionStatus() == TaskStatusInfo.FAILED) {
              out.println("No result available yet: processing failed " +
              "(check logs)");
            } else {
              if (status.getTaskResult() == null) {
                out.println("Task finished successfully, but no result object " +
                "is available.");
              } else {
                out.println("Task result:<p>");
                out.println("<pre>\n" + status.getTaskResult().toString());
                out.println("</pre>");
              }
            }
          }
          out.println("<p>");
        }
      }
    } catch (Exception ex) {
      if (oos != null) {
        oos.writeObject(WekaServlet.RESPONSE_ERROR + " " + ex.getMessage());
        oos.flush();
      } else if (out != null) {
        out.println("An error occured while getting task result:<br><br>");
        out.println("<pre>\n" + ex.getMessage() + "</pre>");        
      }
      ex.printStackTrace();
    } finally {
      if (oos != null) {
        oos.close();
        oos = null;
      }

      if (out != null) {
        out.println("</BODY>\n</HTML>\n");
        out.close();
        out = null;
      }
    }
  }
  
  protected static TaskStatusInfo getResultRemote(WekaServer server, String slave, 
      String remoteTaskID, String origTaskID) {

    InputStream is = null;
    PostMethod post = null;
    TaskStatusInfo resultInfo = null;

    try {
      String url = "http://" + slave;
      url = url.replace(" ", "%20");
      url += CONTEXT_PATH;
      url += "/?name=" + URLEncoder.encode(remoteTaskID, "UTF-8") + "&client=Y";
      post = new PostMethod(url);
      post.setDoAuthentication(true);
      post.addRequestHeader(new Header("Content-Type", "text/plain"));

      // Get HTTP client
      HttpClient client = WekaServer.ConnectionManager.getSingleton().createHttpClient();
      WekaServer.ConnectionManager.addCredentials(client, 
          server.getUsername(), server.getPassword());

      // Execute request
      int result = client.executeMethod(post);
      //System.out.println("[WekaServer] Response from master server : " + result);
      if (result == 401) {       
        System.err.println("[WekaServer] Unable to get remote result of task'" +
            origTaskID + "' - authentication required.\n");
      } else {

        // the response
        is = post.getResponseBodyAsStream();
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(is)));
        Object response = ois.readObject();
        if (response.toString().startsWith(WekaServlet.RESPONSE_ERROR)) {
          System.err.println("[WekaServer] A problem occurred while " +
              "trying to retrieve remote result of task : '" 
              + origTaskID + "'");

        } else {
          if (response instanceof TaskStatusInfo) {
            resultInfo = ((TaskStatusInfo)response);
          } else {
            System.err.println("[WekaServer] A problem occurred while " +
                "trying to retrieve remote result of task : '" 
                + origTaskID + "'");
          }
        }
      }
    } catch (Exception ex) {
      System.err.println("[WekaServer] A problem occurred while " +
          "trying to retrieve remote result of task : '" 
          + origTaskID + "' (" + ex.getMessage() +")");
      System.err.println("Remote task id: " + remoteTaskID);
    } finally {
      if (is != null) {
        try {
          is.close();
          is = null;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (post != null) {
        post.releaseConnection();
        post = null;
      }
    }

    return resultInfo;
  }
}

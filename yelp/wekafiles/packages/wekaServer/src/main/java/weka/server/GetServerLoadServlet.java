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
 *    GetServerLoadServlet.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns the load of this Weka server instance.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 47640 $
 */
public class GetServerLoadServlet extends WekaServlet {

  /**
   * For serialization
   */
  private static final long serialVersionUID = -4802969238971966291L;
  
  /** The context path for this servlet */
  public static final String CONTEXT_PATH = "/weka/serverLoad";

  /**
   * Constructs a new GetServerLoadServlet
   * 
   * @param taskMap the task map maintained by the server
   * @param server a reference to the server itself
   */
  public GetServerLoadServlet(WekaTaskMap taskMap, WekaServer server) {
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

    String clientParam = request.getParameter("client");
    boolean client = (clientParam != null && clientParam.equalsIgnoreCase("y"));

    response.setStatus(HttpServletResponse.SC_OK);
    if (client) {
      response.setContentType("application/octet-stream");
    } else {
      response.setCharacterEncoding("UTF-8");
      response.setContentType("text/html;charset=UTF-8");
    }

    Double loadFactor = new Double(m_server.getServerLoad());
    ObjectOutputStream oos = null;
    PrintWriter out = null;

    // if we have capacity free (our load factor < 1) then
    // report back our load factor
    if (loadFactor > 1 && m_server.getSlaves().size() > 0) {
      // Report back the lowest load out of ourself and the
      // slaves that we have registered
      Set<String> slaves = m_server.getSlaves();
      for (String slave : slaves) {
        double load = RootServlet.getSlaveLoad(slave, m_server.getUsername(), 
            m_server.getPassword());
        if (load >= 0 && load < loadFactor) {
          loadFactor = load;
        }
      }      
    }

    try {
      if (client) {
        OutputStream outS = response.getOutputStream();

        // send load factor back to client        
        oos = new ObjectOutputStream(new BufferedOutputStream(outS));
        oos.writeObject(loadFactor);
        oos.flush();
      } else {
        out = response.getWriter();

        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>Server Load</TITLE>");
        out.println("</HEAD>");
        out.println("<BODY>\n<H3>");
        out.println("Load factor: " + loadFactor);
        out.println("<a href=\"" + RootServlet.CONTEXT_PATH + "\">" 
            + "Back to status page</a></br>");
      }    
    } catch (Exception ex) {
      if (oos != null) {
        oos.writeObject(WekaServlet.RESPONSE_ERROR + " " + ex.getMessage());
        oos.flush();
      } else if (out != null) {
        out.println("An error occured while getting server load:<br><br>");
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
}

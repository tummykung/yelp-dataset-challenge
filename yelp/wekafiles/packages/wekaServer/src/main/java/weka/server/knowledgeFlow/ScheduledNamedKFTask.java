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
 *    ScheduledNamedKFTask.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.server.knowledgeFlow;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import weka.core.LogHandler;
import weka.experiment.TaskStatusInfo;
import weka.gui.Logger;
import weka.server.NamedTask;
import weka.server.Schedule;
import weka.server.Scheduled;

/**
 * Task for executing a Knowledge Flow process at a scheduled time
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 47640 $
 */
public class ScheduledNamedKFTask implements NamedTask, Scheduled, LogHandler, 
  Serializable {
  
  /**
   * For serialization
   */
  private static final long serialVersionUID = -1931422976728468000L;
  
  /** Delegate for the actual execution */
  protected UnscheduledNamedKFTask m_namedTask;
  
  /** The schedule to use for execution */
  protected Schedule m_schedule;
  
  /**
   * Constructs ScheduledNamedKFTask
   * 
   * @param name the name of the task
   * @param xmlFlow the knowledge flow process
   * @param sequential true if start points in the flow are to be
   * executed sequentially
   * @param parameters environment variables and values
   * @param schedule the schedule for the task
   */
  public ScheduledNamedKFTask(String name, StringBuffer xmlFlow, 
      boolean sequential, Map<String, String> parameters, Schedule schedule) {
    m_namedTask = new UnscheduledNamedKFTask(name, xmlFlow, sequential,
        parameters);
    m_schedule = schedule;
  }
  
  /**
   * Execute the task according to the schedule
   * 
   * @param lastExecution the date of the last execution
   * @throws Exception if a problem occurs
   */
  public void execute(Date lastExecution) throws Exception {
    if (m_schedule == null) {
      throw new Exception("No schedule has been set!");
    }
    
    if (m_schedule.execute(lastExecution)) {
      m_namedTask.execute();
    }
  }
  
  /**
   * Execute the task now (regardless of the schedule)
   */
  public void execute() {    
    // execute now
    m_namedTask.execute();
  }
  
  /**
   * Stop the running task
   */
  public void stop() {
    m_namedTask.stop();
  }
  
  /**
   * Get the schedule associated with this task
   * 
   * @return the schedule associated with this task
   */
  public Schedule getSchedule() {
    return m_schedule;
  }
  
  /**
   * Set the schedule to use with this task
   * 
   * @param s the schedule to use
   */
  public void setSchedule(Schedule s) {
    m_schedule = s;
  }
  
  /**
   * Get the current status of the task
   * 
   * @return the current status of the task
   */
  public synchronized TaskStatusInfo getTaskStatus() {
    return m_namedTask.getTaskStatus();
  }
  
  /**
   * Set the task's name/ID
   * 
   * @param name the name of this task
   */
  public void setName(String name) {
    m_namedTask.setName(name);      
  }

  /**
   * Get the name/ID of this task
   * 
   * @return the name of this task
   */
  public String getName() {
    return m_namedTask.getName();
  }

  /**
   * Set the log to use with this task
   * 
   * @param log the log to use
   */
  public void setLog(Logger log) {
    m_namedTask.setLog(log);
  }

  /**
   * Get the log
   * 
   * @return the log
   */
  public Logger getLog() {
    return m_namedTask.getLog();
  }
  
  /**
   * Tell the task that it can free any resources (memory, results etc.) 
   * that would not be needed for another execution run.
   */
  public void freeMemory() { 
    m_namedTask.freeMemory();
  }
  
  /**
   * Tell the task that it should persist any resources to disk 
   * (e.g. training data, etc.). WekaServer.getTempFile() can be
   * used to get a file to save to.
   */  
  public void persistResources() { 
    m_namedTask.persistResources();
  }
  
  /**
   * Tell the task that it should load any stored resources from disk into
   * memory. 
   */
  public void loadResources() { 
    m_namedTask.loadResources();
  }
    
  /**
   * Tell the task to load its result object (if it has one) from
   * disk (if it has persisted it in order to save memory). This
   * method is called when a client has requested to fetch the
   * result.
   * 
   * @throws Exception if the result can't be loaded for some
   * reason
   */
  public void loadResult() throws Exception { 
    m_namedTask.loadResult();
  }
  
  /**
   * Tell the task to delete any disk-based resources. 
   */
  public void purge() { 
    m_namedTask.purge();
  }
}

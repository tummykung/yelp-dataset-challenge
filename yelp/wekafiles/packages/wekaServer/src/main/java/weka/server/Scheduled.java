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
 *    Scheduled.java
 *    Copyright (C) 2011 Pentaho Corporation
 *
 */

package weka.server;

/**
 * Interface to something that uses a schedule.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 47640 $
 */
public interface Scheduled {
  
  /**
   * Set the schedule to use
   * 
   * @param s the schedule to use
   */
  void setSchedule(Schedule s);
  
  /**
   * Get the schedule in use
   * 
   * @return the schedule in use
   */
  Schedule getSchedule();
}

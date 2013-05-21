/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    JavaGDListener.java
 *    Copyright (C) 2012 Pentaho Corporation
 *
 */

package weka.core;

import java.awt.image.BufferedImage;

/**
 * Interface to something that is interested in graphics produced by R via 
 * the JavaGD graphics device.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 50233 $
 */
public interface JavaGDListener {
  
  /**
   * Called when a graphics image has been generated
   * 
   * @param image the image generated
   */
  void imageGenerated(BufferedImage image);
}

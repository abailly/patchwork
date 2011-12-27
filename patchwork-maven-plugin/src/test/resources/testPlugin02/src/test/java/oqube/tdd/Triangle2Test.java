/*______________________________________________________________________________
 * 
 * Copyright (C) 2007 Arnaud Bailly / OQube 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *  
 * email: contact@oqube.com
 * creation: Tue Feb 27 2007
 */
package oqube.tdd;

/**
 * 
 * @author abailly@oqube.com
 * @version $Id: Triangle2Test.java 1 2007-03-05 22:06:45Z arnaud.oqube $
 */
import junit.framework.TestCase;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

public class Triangle2Test extends TestCase { 

  public void testIsEquilateral() { 
      Triangle2 t = new Triangle2(3,3,3);
      assertTrue(t.isEquilateral());
  }

  public void testIsNotEquilateral() { 
      Triangle2 t = new Triangle2(3,3,2);
      PicoContainer cont = new DefaultPicoContainer();
      assertTrue(!t.isEquilateral());
  }
}

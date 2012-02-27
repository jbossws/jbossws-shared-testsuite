/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.jbws1581;

import java.net.URL;

import javax.naming.InitialContext;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * EJB vehicle using loader repository not sufficiently isolated
 *
 * http://jira.jboss.org/jira/browse/JBWS-1581
 *
 * @author Thomas.Diesler@jboss.com
 * @since 19-Mar-2007
 */
public class JBWS1581EarTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1581EarTestCase.class, "jaxws-jbws1581.ear, jaxws-jbws1581-ejb3.jar");
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1581-pojo?wsdl");
      Definition wsdl = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdlURL.toString());
      assertNotNull("wsdl expected", wsdl);
   }

   public void testEJBVehicle() throws Exception
   {
      InitialContext iniCtx = null;
      try
      {
         iniCtx = getServerInitialContext();
         EJB3Remote remote = (EJB3Remote)iniCtx.lookup("ejb:/jaxws-jbws1581-ejb3//EJB3Bean!" + EJB3Remote.class.getName());
         String retStr = remote.runTest("Hello World!");
         assertEquals("Hello World!", retStr);
      }
      finally
      {
         if (iniCtx != null)
         {
            iniCtx.close();
         }
      }
   }
}

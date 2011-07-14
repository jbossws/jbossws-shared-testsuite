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
package org.jboss.test.ws.jaxrpc.samples.handler;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.holders.StringHolder;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test bound and unbound headers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-Jan-2005
 */
public class HeaderClientTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-samples-handler";

   private static HeaderTestService port;

   public static Test suite()
   {
      return new JBossWSTestSetup(HeaderClientTestCase.class, "jaxrpc-samples-handler.war, jaxrpc-samples-handler-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
         port = (HeaderTestService)service.getPort(HeaderTestService.class);
      }
   }

   public void testBoundInHeader() throws Exception
   {
      port.testInHeader("Hello world!", "IN header message");
   }

   public void testBoundInOutHeader() throws Exception
   {
      StringHolder holder = new StringHolder("INOUT header message");
      port.testInOutHeader("Hello world!", holder);
      assertEquals("INOUT header message - response", holder.value);
   }

   public void testBoundOutHeader() throws Exception
   {
      StringHolder holder = new StringHolder();
      port.testOutHeader("Hello world!", holder);
      assertEquals("OUT header message", holder.value);
   }
}

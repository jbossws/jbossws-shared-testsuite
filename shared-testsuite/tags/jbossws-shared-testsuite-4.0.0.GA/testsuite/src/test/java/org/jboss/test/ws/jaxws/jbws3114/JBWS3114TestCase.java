/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3114;

import java.net.URL;

import javax.xml.ws.BindingProvider;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
/**
 * https://jira.jboss.org/browse/JBWS-3114
 * @author ema@redhat.com
 */
public class JBWS3114TestCase extends JBossWSTest
{

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws3114";

   private static Endpoint port;

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS3114TestCase.class, "jaxws-jbws3114.war");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      EndpointService service = new EndpointService(wsdlURL);
      port = service.getEndpointPort();
   }

   public void testConfigureTimeout() throws Exception
   {
      String response = port.echo("testjbws3114");
      assertEquals("testjbws3114", response);
      ((BindingProvider) port).getRequestContext().put("javax.xml.ws.client.connectionTimeout", "6000");
      ((BindingProvider) port).getRequestContext().put("javax.xml.ws.client.receiveTimeout", "1");
      try
      {
         port.echo("testjbws3114");
         fail("Timeout exeception is expected");
      }
      catch (Exception e)
      {
         //expected
      }

   }
}
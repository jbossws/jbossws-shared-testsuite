/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2630;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2630]
 *
 * Tests a valid endpoint address is shown in context servlet when
 * deploying a jar file containing a war file with the endpoint.
 *
 * @author alessio.soldano@jboss.com
 * @since 05-Aug-2009
 */
public class JBWS2630TestCase extends JBossWSTest
{

   private String endpointAddress = "http://" + this.getServerHost() + ":8080/jaxws-jbws2630-service/Endpoint";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2630TestCase.class, "jaxws-jbws2630.jar");
   }

   public void testJMXConsole() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName("jboss.ws:context=jaxws-jbws2630-service,endpoint=Endpoint");
      String address = (String)server.getAttribute(oname, "Address");
      assertEquals(new URL(endpointAddress).getPath(), new URL(address).getPath());
   }

   public void testWSDL() throws Exception
   {
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      Definition wsdl = getWSDLDefinition(wsdlURL.toExternalForm());

      assertNotNull("Unable to get WSDL", wsdl);
   }

   public void testEndpoint() throws Exception
   {
      URL url = new URL(endpointAddress + "?wsdl");
      QName serviceName = new QName("http://org.jboss/test/ws/jbws2630", "EndpointService");
      Service service = Service.create(url, serviceName);
      Endpoint port = service.getPort(Endpoint.class);
      String s = "Hi";
      assertEquals(s, port.echo(s));
   }

   private Definition getWSDLDefinition(String wsdlLocation) throws Exception
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

      Definition definition = wsdlReader.readWSDL(null, wsdlLocation);
      return definition;
   }

}

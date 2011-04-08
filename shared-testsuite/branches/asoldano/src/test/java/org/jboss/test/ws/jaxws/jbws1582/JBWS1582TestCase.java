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
package org.jboss.test.ws.jaxws.jbws1582;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1582] Protect JBossWS Against XML Attacks
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public class JBWS1582TestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-jbws1582/TestService";
   private String targetNS = "http://jbws1582.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1582TestCase.class, "jaxws-jbws1582.war");
   }

   public void testLegalAccess() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   public void testSOAPMessage() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/message.xml");
      assertTrue(response.contains("HTTP/1.1 200 OK"));
      assertTrue(response.contains("<return>Hello</return>"));
   }

   public void testSOAPMessageAttack1() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/attack-message-1.xml");
      assertTrue(response.contains("HTTP/1.1 500"));
      if (isIntegrationCXF())
      {
         assertTrue(response.contains("Error reading XMLStreamReader"));
      }
      else if(isIntegrationNative()) 
      {
         assertTrue(response.contains("DOCTYPE is disallowed when the feature"));
         assertTrue(response.contains("http://apache.org/xml/features/disallow-doctype-decl"));
      }
      else
      {
         throw new IllegalStateException("Unknown SOAP stack in use");
      }
   }
   
   public void testSOAPMessageAttack2() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/attack-message-2.xml");
      assertTrue(response.contains("HTTP/1.1 500"));
      if (isIntegrationCXF())
      {
         assertTrue(response.contains("Error reading XMLStreamReader"));
      }
      else if(isIntegrationNative()) 
      {
         assertTrue(response.contains("DOCTYPE is disallowed when the feature"));
         assertTrue(response.contains("http://apache.org/xml/features/disallow-doctype-decl"));
      }
      else
      {
         throw new IllegalStateException("Unknown SOAP stack in use");
      }
   }
   
   private String getResponse(String requestFile) throws Exception
   {
      final String CRNL = "\r\n";
      String content = getContent(new FileInputStream(getResourceFile(requestFile)));
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(getServerHost(), 8080));
      OutputStream out = socket.getOutputStream();

      // send an HTTP request to the endpoint
      out.write(("POST /jaxws-jbws1582/TestService HTTP/1.0" + CRNL).getBytes());
      out.write(("Host: " + getServerHost() + ":8080" + CRNL).getBytes());
      out.write(("Content-Type: text/xml" + CRNL).getBytes());
      out.write(("Content-Length: " + content.length() + CRNL).getBytes());
      out.write((CRNL).getBytes());
      out.write((content).getBytes());

      // read the response
      String response = getContent(socket.getInputStream());
      socket.close();
      System.out.println("---");
      System.out.println(response);
      System.out.println("---");
      return response;
   }
   
   public void testAttackedArchiveDeployment() throws Exception
   {
      if (isIntegrationCXF())
      {
         System.out.println("FIXME [JBWS-3262] fix issue in CXF so the following test will start passing"); // TODO: remove once fixed on CXF side
         return;
      }
      try
      {
         deploy("jaxws-jbws1582-attacked.war");
         fail("deployment failure expected");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         log.warn(e.getMessage(), e);
      }
      finally
      {
         undeploy("jaxws-jbws1582-attacked.war");
      }
   }
      
   private static String getContent(InputStream is) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copyStream(baos, is);
      return new String(baos.toByteArray());
   }

}

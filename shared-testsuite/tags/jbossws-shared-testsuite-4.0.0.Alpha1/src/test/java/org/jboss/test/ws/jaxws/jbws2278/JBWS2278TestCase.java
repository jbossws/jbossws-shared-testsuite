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
package org.jboss.test.ws.jaxws.jbws2278;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2278] JBossWS is picking the wrong binding when both Soap1.1 and Soap1.2 bindings are provided for a port
 * 
 * @author alessio.soldano@jboss.com
 * @since 30-Sep-2008
 * @see https://jira.jboss.org/jira/browse/JBWS-2278
 */
public class JBWS2278TestCase extends JBossWSTest
{
   private TestEndpoint port11;
   private TestEndpoint port12;

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2278TestCase.class, "jaxws-jbws2278.war");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2278/soap11?wsdl");
      QName serviceName = new QName("http://org.jboss.test.ws/jbws2278", "TestService");

      Service service = Service.create(wsdlURL, serviceName);
      port11 = service.getPort(new QName("http://org.jboss.test.ws/jbws2278", "TestEndpointSoap11Port"), TestEndpoint.class);
      port12 = service.getPort(new QName("http://org.jboss.test.ws/jbws2278", "TestEndpointSoap12Port"), TestEndpoint.class);

      List<Handler> handlerChain11 = new ArrayList<Handler>();
      handlerChain11.add(new TestHandler(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, SOAPConstants.SOAP_1_1_CONTENT_TYPE));
      ((BindingProvider)port11).getBinding().setHandlerChain(handlerChain11);
      
      List<Handler> handlerChain12 = new ArrayList<Handler>();
      handlerChain12.add(new TestHandler(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, SOAPConstants.SOAP_1_2_CONTENT_TYPE));
      ((BindingProvider)port12).getBinding().setHandlerChain(handlerChain12);
   }

   public void testCallSoap11() throws Exception
   {
      final String message = "Hello!!";
      String response = port11.echo(message);
      assertEquals(message, response);
   }

   public void testCheckedExceptionSoap11() throws Exception
   {
      try
      {
         port11.echo(TestEndpointImpl.TEST_EXCEPTION);
         fail("Expected TestException not thrown.");
      }
      catch (TestException_Exception te)
      {
         //OK
      }
   }

   public void testRuntimeExceptionSoap11()
   {
      try
      {
         port11.echo(TestEndpointImpl.RUNTIME_EXCEPTION);
         fail("Expected Exception not thrown.");
      }
      catch (Exception e)
      {
         assertEquals("Simulated failure", e.getMessage());
      }
   }
   
   public void testCallSoap12() throws Exception
   {
      final String message = "Hello!!";
      String response = port12.echo(message);
      assertEquals(message, response);
   }

   public void testCheckedExceptionSoap12() throws Exception
   {
      try
      {
         port12.echo(TestEndpointImpl.TEST_EXCEPTION);
         fail("Expected TestException not thrown.");
      }
      catch (TestException_Exception te)
      {
         //OK
      }
   }

   public void testRuntimeExceptionSoap12()
   {
      try
      {
         port12.echo(TestEndpointImpl.RUNTIME_EXCEPTION);
         fail("Expected Exception not thrown.");
      }
      catch (Exception e)
      {
         assertEquals("Simulated failure", e.getMessage());
      }
   }

}

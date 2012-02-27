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
package org.jboss.test.ws.jaxrpc.samples.serviceref;

import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the JAXRPC <service-ref>
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class ServiceRefClientTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-samples-serviceref";

   public static Test suite()
   {
      return new JBossWSTestSetup(ServiceRefClientTestCase.class, "jaxrpc-samples-serviceref.war, jaxrpc-samples-serviceref-appclient.ear#jaxrpc-samples-serviceref-appclient.jar");
   }

   public void testDynamicProxyNeg() throws Exception
   {
      URL wsdlURL = getResourceURL("jaxrpc/samples/serviceref/META-INF/wsdl/TestEndpoint.wsdl");
      QName qname = new QName("http://org.jboss.ws/wsref", "TestEndpointService");
      Service service = ServiceFactory.newInstance().createService(wsdlURL, qname);
      try
      {
         TestEndpoint port = (TestEndpoint)service.getPort(java.rmi.activation.Activator.class);
         fail("Expected ServiceException, but got: " + port);
      }
      catch (ServiceException ex)
      {
         // this is tested by the CTS
      }
      catch (Exception ex)
      {
         fail("Expected ServiceException, but got: " + ex);
      }
   }

   public void testApplicationClient() throws Exception
   {
      InitialContext ctx = null;
      try
      {
         ctx = getAppclientInitialContext();
         final TestEndpoint port1 = (TestEndpoint)((Service)ctx.lookup("java:service1")).getPort(TestEndpoint.class);
         final TestEndpoint port2 = ((TestEndpointService)ctx.lookup("java:service2")).getTestEndpointPort();
         final String msg = "Hello World!";

         assertEquals(msg, port1.echo(msg));
         assertEquals(msg, port2.echo(msg));
      }
      finally
      {
         if (ctx != null)
         {
            ctx.close();
         }
      }
   }

}

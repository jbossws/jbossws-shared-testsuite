/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.publish;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;
import org.jboss.wsf.spi.publish.Context;
import org.jboss.wsf.spi.publish.EndpointPublisher;
import org.jboss.wsf.spi.publish.EndpointPublisherFactory;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Apr-2011
 *
 */
@WebServlet(name = "EndpointPublishServlet", urlPatterns = "/*")
public class EndpointPublishServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      Context ctx = null;
      EndpointPublisher publisher = null;
      try
      {
         //deploy endpoints
         ClassLoader loader = ClassLoaderProvider.getDefaultProvider().getWebServiceSubsystemClassLoader();
         EndpointPublisherFactory factory = ServiceLoader.load(EndpointPublisherFactory.class, loader).iterator().next();
         publisher = factory.newEndpointPublisher("default-host");
         
         Map<String,String> map = new HashMap<String, String>();
         map.put("/pattern", "org.jboss.test.ws.publish.EndpointImpl");
         map.put("/pattern2", "org.jboss.test.ws.publish.EndpointImpl2");
         map.put("/pattern3", "org.jboss.test.ws.publish.EndpointImpl3");
         map.put("/pattern4", "org.jboss.test.ws.publish.EndpointImpl4");
         
         ctx = publisher.publish("ep-publish-test", Thread.currentThread().getContextClassLoader(), map, createMetaData());
         
         for (Endpoint ep : ctx.getEndpoints()) {
            System.out.println("State: " + ep.getState());
            System.out.println("Address: " + ep.getAddress());
            System.out.println("TargetBeanClass: " + ep.getTargetBeanClass());
         }
         
         //call endpoint
         invoke(new URL("http://localhost:8080/ep-publish-test/pattern?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService"));
         invoke(new URL("http://localhost:8080/ep-publish-test/pattern2?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService2"));
         invoke(new URL("http://localhost:8080/ep-publish-test/pattern3?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService3"));
         invoke(new URL("http://localhost:8080/ep-publish-test/pattern4?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService4"));
         
         res.getWriter().print("1");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         res.getWriter().print(e.getMessage());
      }
      finally
      {
         if (ctx != null && publisher != null)
         {
            try
            {
               //undeploy endpoints
               publisher.destroy(ctx);
            }
            catch (Exception e)
            {
               e.printStackTrace();
               res.getWriter().print(e.getMessage());
            }
         }
      }
   }
   
   private WebservicesMetaData createMetaData() {
      WebservicesMetaData metadata = new WebservicesMetaData();
      WebserviceDescriptionMetaData webserviceDescription = new WebserviceDescriptionMetaData(metadata);
      metadata.addWebserviceDescription(webserviceDescription);
      webserviceDescription.setWsdlFile("WEB-INF/wsdl/EndpointImpl4.xml");
      PortComponentMetaData portComponent = new PortComponentMetaData(webserviceDescription);
      portComponent.setPortComponentName("PortComponent4"); //unique ID
      portComponent.setServiceEndpointInterface("org.jboss.test.ws.publish.EndpointImpl4");
      portComponent.setWsdlPort(new QName("http://publish.ws.test.jboss.org/", "EndpointPort4"));
      portComponent.setWsdlService(new QName("http://publish.ws.test.jboss.org/", "EndpointService4"));
      webserviceDescription.addPortComponent(portComponent);
      return metadata;
   }
   
   private static void invoke(URL wsdlURL, QName serviceName) throws Exception {
      Service service = Service.create(wsdlURL, serviceName);
      org.jboss.test.ws.publish.Endpoint port = service.getPort(org.jboss.test.ws.publish.Endpoint.class);
      String result = port.echo("Foo");
      if (!"Foo".equals(result))
      {
         throw new Exception("Expected 'Foo' but got '" + result + "'");
      }
   }
}

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
package org.jboss.test.ws.jaxws.jbws3293;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;

import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Element;

/**
 * [JBWS-3293] tests dispatch creation without WSDL.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class JBWS3293DispatchTestCase extends JBossWSTest
{
   private String targetNS = "http://org.jboss.ws/jaxws/jbws3293";
   private String reqPayload = "<ns2:echo xmlns:ns2='" + targetNS + "'><String_1>Hello</String_1></ns2:echo>";
   private Exception handlerException;
   private boolean asyncHandlerCalled;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3293DispatchTestCase.class, "jaxws-jbws3293.war");
   }

   public void testInvokeAsynch() throws Exception
   {
      Source reqObj = new StreamSource(new StringReader(reqPayload));
      Dispatch dispatch = createDispatch();
      Response response = dispatch.invokeAsync(reqObj);
      verifyResponse((Source)response.get(3000, TimeUnit.MILLISECONDS));
   }

   public void testInvokeAsynchHandler() throws Exception
   {
      AsyncHandler handler = new AsyncHandler()
      {
         public void handleResponse(Response response)
         {
            try
            {
               verifyResponse((Source)response.get());
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };
      StreamSource reqObj = new StreamSource(new StringReader(reqPayload));
      Dispatch dispatch = createDispatch();
      Future future = dispatch.invokeAsync(reqObj, handler);
      future.get(1000, TimeUnit.MILLISECONDS);

      if (handlerException != null)
         throw handlerException;

      assertTrue("Async handler called", asyncHandlerCalled);
   }

   private void installHandler(final Dispatch dispatch)
   {
       List<Handler> handlers = dispatch.getBinding().getHandlerChain();
       handlers.add(new SOAPHandler());
       dispatch.getBinding().setHandlerChain(handlers);
   }

   private Dispatch createDispatch() throws MalformedURLException
   {
      QName serviceName = new QName(targetNS, "EndpointBeanService");
      QName portName = new QName(targetNS, "EndpointPort");
      Service service = Service.create(serviceName);
      service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://" + getServerHost() + ":8080/jaxws-jbws3293");
      Dispatch dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      installHandler(dispatch);
      return dispatch;
   }

   private void verifyResponse(Source result) throws IOException
   {
      Element docElement = DOMUtils.sourceToElement(result);
      Element retElement = DOMUtils.getFirstChildElement(docElement);
      assertEquals("result", retElement.getNodeName());
      assertEquals("Hello modified in handler", retElement.getTextContent());
   }

}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.clientConfig;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;

import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Verifies client configuration setup
 *
 * @author alessio.soldano@jboss.com
 * @since 31-May-2012
 */
public class ClientConfigurationTestCase extends JBossWSTest
{
   private static final String targetNS = "http://clientConfig.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return new JBossWSTestSetup(ClientConfigurationTestCase.class, "jaxws-clientConfig.war,jaxws-clientConfig-client.jar");
   }
   
   public void testClientConfigurer()
   {
      Iterator<ClientConfigurer> it = ServiceLoader.load(ClientConfigurer.class).iterator();
      assertTrue(it.hasNext());
      ClientConfigurer configurer = it.next();
      assertNotNull(configurer);
      assertEquals("org.jboss.ws.common.configuration.ConfigHelper", configurer.getClass().getName());
   }

   public void testCustomClientConfiguration() throws Exception
   {
      QName serviceName = new QName(targetNS, "EndpointImplService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-clientConfig/EndpointImpl?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);
      
      BindingProvider bp = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> hc = bp.getBinding().getHandlerChain();
      hc.add(new UserHandler());
      bp.getBinding().setHandlerChain(hc);
      
      ClientConfigurer configurer = ServiceLoader.load(ClientConfigurer.class).iterator().next();
      configurer.addConfigHandlers(bp, "META-INF/jaxws-client-config.xml", "Custom Client Config");

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|RoutOut|CustomOut|UserOut|LogOut|endpoint|LogIn|UserIn|CustomIn|RoutIn", resStr);
   }
}

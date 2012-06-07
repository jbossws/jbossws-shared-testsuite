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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Test;

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
   public static Test suite()
   {
      return new JBossWSTestSetup(ClientConfigurationTestCase.class, "jaxws-clientConfig.war,jaxws-clientConfig-client.jar, jaxws-clientConfig-inContainer-client.war");
   }
   
   public void testClientConfigurer() {
      Helper helper = new Helper();
      assertTrue(helper.testClientConfigurer());
   }

   public void testCustomClientConfiguration() throws Exception {
      Helper helper = new Helper();
      helper.setTargetEndpoint("http://" + getServerHost() + ":8080/jaxws-clientConfig/EndpointImpl");
      assertTrue(helper.testCustomClientConfigurationFromFile());
   }
   
   public void testClientConfigurerInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testClientConfigurer"));
   }
   
   public void testCustomClientConfigurationFromFileInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationFromFile"));
   }
   
   public void testDefaultClientConfigurationInContainer() throws Exception {
      if (true) {
         System.out.println("FIXME: [JBWS-3335] Add client-configuration to AS7 domain model");
         return;
      }
      assertEquals("1", runTestInContainer("testDefaultClientConfiguration"));
   }
   
   public void testCustomClientConfigurationInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfiguration"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-clientConfig-inContainer-client?path=/jaxws-clientConfig/EndpointImpl&method=" + test
            + "&helper=" + Helper.class.getName());
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      return br.readLine();
   }
}

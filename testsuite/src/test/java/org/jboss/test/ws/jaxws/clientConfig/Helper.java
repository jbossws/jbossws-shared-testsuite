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
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import org.jboss.test.helper.ClientHelper;
import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;

/**
 * Verifies client configuration setup
 *
 * @author alessio.soldano@jboss.com
 * @since 06-Jun-2012
 */
public class Helper implements ClientHelper
{
   private String address;
   
   public boolean testClientConfigurer()
   {
      Iterator<ClientConfigurer> it = ServiceLoader.load(ClientConfigurer.class).iterator();
      if (!it.hasNext()) {
         return false;
      }
      ClientConfigurer configurer = it.next();
      if (configurer == null) {
         return false;
      }
      return "org.jboss.ws.common.configuration.ConfigHelper".equals(configurer.getClass().getName());
   }
   
   public boolean testCustomClientConfigurationFromFile() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");

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
      return ("Kermit|RoutOut|CustomOut|UserOut|LogOut|endpoint|LogIn|UserIn|CustomIn|RoutIn".equals(resStr));
   }
   
   /**
    * This test hacks the current ServerConfig temporarily adding an handler from this testcase deployment
    * into the AS default client configuration, verifies the handler is picked up and finally restores the
    * original default client configuration. 
    * 
    * @return
    * @throws Exception
    */
   public boolean testDefaultClientConfiguration() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");
      
      ServerConfig sc = getServerConfig();
      ClientConfig defaultConfig = getAndVerifyDefaultClientConfiguration(sc);
      
      try
      {
         // -- modify default conf --
         modifyDefaultClientConfiguration(defaultConfig);
         // --
         
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);
         
         BindingProvider bp = (BindingProvider)port;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);
         
         String resStr = port.echo("Kermit");
         System.out.println("--> ** " + resStr);
         return ("Kermit|UserOut|LogOut|endpoint|LogIn|UserIn".equals(resStr));
      }
      finally
      {
         // -- restore default conf --
         defaultConfig.setPostHandlerChains(null);
         defaultConfig.setPreHandlerChains(null);
         // --
      }
   }
   
   /**
    * This test hacks the current ServerConfig temporarily adding a test client configuration, uses that
    * for the test client and finally removes it from the ServerConfig.
    * 
    * @return
    * @throws Exception
    */
   public boolean testCustomClientConfiguration() throws Exception
   {
      QName serviceName = new QName("http://clientConfig.jaxws.ws.test.jboss.org/", "EndpointImplService");
      URL wsdlURL = new URL(address + "?wsdl");
      
      ServerConfig sc = getServerConfig();
      final String testConfigName = "MyTestConfig";
      try
      {
         //-- add test client configuration
         addTestCaseClientConfiguration(sc, testConfigName);
         // --
         
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);
         
         BindingProvider bp = (BindingProvider)port;
         @SuppressWarnings("rawtypes")
         List<Handler> hc = bp.getBinding().getHandlerChain();
         hc.add(new UserHandler());
         bp.getBinding().setHandlerChain(hc);
         
         ClientConfigurer configurer = ServiceLoader.load(ClientConfigurer.class).iterator().next();
         configurer.addConfigHandlers(bp, null, testConfigName);
         
         String resStr = port.echo("Kermit");
         return ("Kermit|RoutOut|UserOut|endpoint|UserIn|RoutIn".equals(resStr));
      }
      finally
      {
         // -- remove test client configuration --
         removeTestCaseClientConfiguration(sc, testConfigName);
         // --
      }
   }
   
   private ClientConfig getAndVerifyDefaultClientConfiguration(ServerConfig sc) throws Exception {
      ClientConfig defaultConfig = null;
      for (ClientConfig c : sc.getClientConfigs()) {
         if (ClientConfig.STANDARD_CLIENT_CONFIG.equals(c.getConfigName())) {
            defaultConfig = c;
         }
      }
      if (defaultConfig == null) {
         throw new Exception("Missing AS client config '" + ClientConfig.STANDARD_CLIENT_CONFIG + "'!");
      }
      List<UnifiedHandlerChainMetaData> preHC = defaultConfig.getPreHandlerChains();
      List<UnifiedHandlerChainMetaData> postHC = defaultConfig.getPostHandlerChains();
      if ((preHC != null && !preHC.isEmpty()) || (postHC != null && !postHC.isEmpty())) {
         throw new Exception("'" + ClientConfig.STANDARD_CLIENT_CONFIG + "' is not empty!");
      }
      return defaultConfig;
   }
   
   private void modifyDefaultClientConfiguration(ClientConfig defaultConfig) {
      UnifiedHandlerChainMetaData uhcmd = new UnifiedHandlerChainMetaData();
      UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData();
      handler.setHandlerClass("org.jboss.test.ws.jaxws.clientConfig.LogHandler");
      handler.setHandlerName("Log Handler");
      uhcmd.addHandler(handler);
      List<UnifiedHandlerChainMetaData> postHC = new LinkedList<UnifiedHandlerChainMetaData>();
      postHC.add(uhcmd);
      defaultConfig.setPostHandlerChains(postHC);
   }
   
   private void addTestCaseClientConfiguration(ServerConfig sc, String testConfigName) {
      UnifiedHandlerChainMetaData uhcmd = new UnifiedHandlerChainMetaData();
      UnifiedHandlerMetaData handler = new UnifiedHandlerMetaData();
      handler.setHandlerClass("org.jboss.test.ws.jaxws.clientConfig.RoutingHandler");
      handler.setHandlerName("Routing Handler");
      uhcmd.addHandler(handler);
      ClientConfig config = new ClientConfig();
      config.setConfigName(testConfigName);
      List<UnifiedHandlerChainMetaData> preHC = new LinkedList<UnifiedHandlerChainMetaData>();
      preHC.add(uhcmd);
      config.setPreHandlerChains(preHC);
      sc.addClientConfig(config);
   }
   
   private void removeTestCaseClientConfiguration(ServerConfig sc, String testConfigName) {
      Iterator<ClientConfig> it = sc.getClientConfigs().iterator();
      while (it.hasNext()) {
         ClientConfig c = it.next();
         if (testConfigName.equals(c.getConfigName())) {
            it.remove();
            break;
         }
      }
   }
   
   private ServerConfig getServerConfig()
   {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      return spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
   }

   @Override
   public void setTargetEndpoint(String address)
   {
      this.address = address;
   }
}

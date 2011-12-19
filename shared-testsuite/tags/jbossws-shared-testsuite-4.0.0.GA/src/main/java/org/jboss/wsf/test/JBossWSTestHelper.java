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
package org.jboss.wsf.test;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployer.Deployer;

/**
 * A JBossWS test helper that deals with test deployment/undeployment, etc.
 *
 * @author Thomas.Diesler@jboss.org
 * @author ropalka@redhat.com
 * @author alessio.soldano@jboss.com
 */
public class JBossWSTestHelper
{
   private static final Logger LOGGER = Logger.getLogger(JBossWSTestHelper.class);
   
   private static final String SYSPROP_JBOSSWS_INTEGRATION_TARGET = "jbossws.integration.target";
   private static final String SYSPROP_JBOSS_BIND_ADDRESS = "jboss.bind.address";
   private static final String SYSPROP_TEST_ARCHIVE_DIRECTORY = "test.archive.directory";
   private static final String SYSPROP_TEST_RESOURCES_DIRECTORY = "test.resources.directory";
   private static Deployer DEPLOYER;

   private static MBeanServerConnection server;
   private static String integrationTarget;
   private static String implVendor;
   private static String implTitle;
   private static String implVersion;
   private static String testArchiveDir;
   private static String testResourcesDir;
   
   private static synchronized Deployer getDeployer()
   {
      //lazy loading of deployer
      if (DEPLOYER == null)
      {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         DEPLOYER = spiProvider.getSPI(Deployer.class);
      }
      return DEPLOYER;
   }

   /** Deploy the given archive
    */
   public static void deploy(String archive) throws Exception
   {
      URL archiveURL = getArchiveFile(archive).toURI().toURL();
      getDeployer().deploy(archiveURL);
   }

   /** Undeploy the given archive
    */
   public static void undeploy(String archive) throws Exception
   {
      URL archiveURL = getArchiveFile(archive).toURI().toURL();
      getDeployer().undeploy(archiveURL);
   }

   public static boolean isTargetJBoss6()
   {
       String target = getIntegrationTarget();
       return target.startsWith("jboss6");
   }

   public static boolean isTargetJBoss7()
   {
       String target = getIntegrationTarget();
       return target.startsWith("jboss7");
   }

   public static boolean isTargetJBoss70()
   {
       String target = getIntegrationTarget();
       return target.startsWith("jboss70");
   }

   public static boolean isTargetJBoss71()
   {
       String target = getIntegrationTarget();
       return target.startsWith("jboss71");
   }

   public static boolean isIntegrationNative()
   {
      String vendor = getImplementationVendor();
      return vendor.toLowerCase().indexOf("jboss") != -1;
   }

   public static boolean isIntegrationCXF()
   {
      String vendor = getImplementationVendor();
      return vendor.toLowerCase().indexOf("apache") != -1;
   }

   private static String getImplementationVendor()
   {
      if (implVendor == null)
      {
         Object obj = getImplementationObject();
         implVendor = obj.getClass().getPackage().getImplementationVendor();
         if (implVendor == null)
            implVendor = getImplementationPackage();

         implTitle = obj.getClass().getPackage().getImplementationTitle();
         implVersion = obj.getClass().getPackage().getImplementationVersion();

         System.out.println(implVendor + ", " + implTitle + ", " + implVersion);
      }
      return implVendor;
   }

   private static Object getImplementationObject()
   {
      Service service = Service.create(new QName("dummyService"));
      Object obj = service.getHandlerResolver();
      if (obj == null)
      {
         service.addPort(new QName("dummyPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://dummy-address");
         obj = service.createDispatch(new QName("dummyPort"), Source.class, Mode.PAYLOAD);
      }
      return obj;
   }

   private static String getImplementationPackage()
   {
      return getImplementationObject().getClass().getPackage().getName();
   }

   /**
    * Get the JBoss server host from system property "jboss.bind.address"
    * This defaults to "localhost"
    */
   public static String getServerHost()
   {
      final String host = System.getProperty(SYSPROP_JBOSS_BIND_ADDRESS, "localhost"); 
      return toIPv6URLFormat(host);
   }
   
   private static String toIPv6URLFormat(final String host)
   {
      try
      {
         final boolean isIPv6Address = InetAddress.getByName(host) instanceof Inet6Address;
         final boolean isIPv6Formatted = isIPv6Address && host.startsWith("[");
         return isIPv6Address && !isIPv6Formatted ? "[" + host + "]" : host;
      }
      catch (final UnknownHostException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static MBeanServerConnection getServer()
   {
      if (server == null)
      {
         String integrationTarget = getIntegrationTarget();
         if (integrationTarget.startsWith("jboss7"))
         {
            server = getAS7ServerConnection();
         }
         else
         {
            throw new IllegalStateException("Unsupported target container: " + integrationTarget);
         }
      }
      return server;
   }
   
   private static MBeanServerConnection getAS7ServerConnection()
   {
       String host = getServerHost();
       String urlString = System.getProperty("jmx.service.url", "service:jmx:rmi:///jndi/rmi://" + host + ":" + 1090 + "/jmxrmi");
       try {
           JMXServiceURL serviceURL = new JMXServiceURL(urlString);
           return JMXConnectorFactory.connect(serviceURL, null).getMBeanServerConnection();
       } catch (IOException ex) {
           throw new IllegalStateException("Cannot obtain MBeanServerConnection to: " + urlString, ex);
       }
   }
   
   public static String getIntegrationTarget()
   {
      if (integrationTarget == null)
      {
         integrationTarget = System.getProperty(SYSPROP_JBOSSWS_INTEGRATION_TARGET);

         if (integrationTarget == null)
            throw new IllegalStateException("Cannot obtain system property: " + SYSPROP_JBOSSWS_INTEGRATION_TARGET);

         LOGGER.warn("TODO: [JBWS-3211] implement integrationTarget mismatch check for AS 7.x");
      }

      return integrationTarget;
   }

   /** Try to discover the URL for the deployment archive */
   public static URL getArchiveURL(String archive) throws MalformedURLException
   {
      return getArchiveFile(archive).toURI().toURL();
   }

   /** Try to discover the File for the deployment archive */
   public static File getArchiveFile(String archive)
   {
      File file = new File(archive);
      if (file.exists())
         return file;

      file = new File(getTestArchiveDir() + "/" + archive);
      if (file.exists())
         return file;

      String notSet = (getTestArchiveDir() == null ? " System property '" + SYSPROP_TEST_ARCHIVE_DIRECTORY + "' not set." : "");
      throw new IllegalArgumentException("Cannot obtain '" + getTestArchiveDir() + "/" + archive + "'." + notSet);
   }

   /** Try to discover the URL for the test resource */
   public static URL getResourceURL(String resource) throws MalformedURLException
   {
      return getResourceFile(resource).toURI().toURL();
   }

   /** Try to discover the File for the test resource */
   public static File getResourceFile(String resource)
   {
      File file = new File(resource);
      if (file.exists())
         return file;

      file = new File(getTestResourcesDir() + "/" + resource);
      if (file.exists())
         return file;

      String notSet = (getTestResourcesDir() == null ? " System property '" + SYSPROP_TEST_RESOURCES_DIRECTORY + "' not set." : "");
      throw new IllegalArgumentException("Cannot obtain '" + getTestResourcesDir() + "/" + resource + "'." + notSet);
   }

   public static String getTestArchiveDir()
   {
      if (testArchiveDir == null)
         testArchiveDir = System.getProperty(SYSPROP_TEST_ARCHIVE_DIRECTORY);

      return testArchiveDir;
   }

   public static String getTestResourcesDir()
   {
      if (testResourcesDir == null)
         testResourcesDir = System.getProperty(SYSPROP_TEST_RESOURCES_DIRECTORY);

      return testResourcesDir;
   }
   
   public static void addSecurityDomain(String name, Map<String,String> authenticationOptions) throws Exception
   {
      getDeployer().addSecurityDomain(name, authenticationOptions);
   }
   
   public static void removeSecurityDomain(String name) throws Exception
   {
      getDeployer().removeSecurityDomain(name);
   }
}

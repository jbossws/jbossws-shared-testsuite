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
package org.jboss.wsf.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.MBeanServerConnection;
import javax.naming.NamingException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;

/**
 * A test setup that deploys/undeploys archives
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class JBossWSTestSetup extends TestSetup
{
   // provide logging
   private static Logger log = Logger.getLogger(JBossWSTestSetup.class);
   
   private static final String JBOSSWS_SEC_DOMAIN = "JBossWS";

   private String[] archives = new String[0];
   private ClassLoader originalClassLoader;
   private Map<String, Map<String, String>> securityDomains = new HashMap<String, Map<String,String>>();
   private boolean defaultSecurityDomainRequirement = false;

   public JBossWSTestSetup(Class<?> testClass, String archiveList)
   {
      super(new TestSuite(testClass));
      getArchiveArray(archiveList);
   }
   
   public JBossWSTestSetup(Class<?> testClass, String archiveList, boolean requiresDefaultSecurityDomain)
   {
      this(testClass, archiveList);
      setDefaultSecurityDomainRequirement(requiresDefaultSecurityDomain);
   }

   public JBossWSTestSetup(Test test, String archiveList)
   {
      super(test);
      getArchiveArray(archiveList);
   }
   
   public JBossWSTestSetup(Test test, String archiveList, boolean requiresDefaultSecurityDomain)
   {
      this(test, archiveList);
      setDefaultSecurityDomainRequirement(requiresDefaultSecurityDomain);
   }

   public JBossWSTestSetup(Test test)
   {
      super(test);
   }

   public File getArchiveFile(String archive)
   {
      return JBossWSTestHelper.getArchiveFile(archive);
   }

   public URL getArchiveURL(String archive) throws MalformedURLException
   {
      return JBossWSTestHelper.getArchiveFile(archive).toURI().toURL();
   }

   public File getResourceFile(String resource)
   {
      return JBossWSTestHelper.getResourceFile(resource);
   }

   public URL getResourceURL(String resource) throws MalformedURLException
   {
      return JBossWSTestHelper.getResourceFile(resource).toURI().toURL();
   }

   private void getArchiveArray(String archiveList)
   {
      if (archiveList != null)
      {
         StringTokenizer st = new StringTokenizer(archiveList, ", ");
         archives = new String[st.countTokens()];

         for (int i = 0; i < archives.length; i++)
            archives[i] = st.nextToken();
      }
   }

   protected void setUp() throws Exception
   {
      // verify integration target
      String integrationTarget = JBossWSTestHelper.getIntegrationTarget();
      log.debug("Integration target: " + integrationTarget);
      
      if (!securityDomains.isEmpty())
      {
         for (String key : securityDomains.keySet())
         {
            JBossWSTestHelper.addSecurityDomain(key, securityDomains.get(key));
         }
      }
      if (defaultSecurityDomainRequirement)
      {
         String usersPropFile = System.getProperty("org.jboss.ws.testsuite.securityDomain.users.propfile");
         String rolesPropFile = System.getProperty("org.jboss.ws.testsuite.securityDomain.roles.propfile");
         Map<String, String> authenticationOptions = new HashMap<String, String>();
         if (usersPropFile != null) {
             authenticationOptions.put("usersProperties", usersPropFile);
         }
         if (rolesPropFile != null) {
             authenticationOptions.put("rolesProperties", rolesPropFile);
         }
         authenticationOptions.put("unauthenticatedIdentity", "anonymous");
         JBossWSTestHelper.addSecurityDomain(JBOSSWS_SEC_DOMAIN, authenticationOptions);
      }

      List<URL> clientJars = new ArrayList<URL>();
      for (int i = 0; i < archives.length; i++)
      {
         String archive = archives[i];
         if (archive.endsWith("-client.jar"))
         {
            URL archiveURL = getArchiveURL(archive);
            clientJars.add(archiveURL);
         }
         else
         {
            try
            {
               JBossWSTestHelper.deploy(archive);
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
               JBossWSTestHelper.undeploy(archive);
            }
         }
      }

      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      originalClassLoader = parent;
      // add client jars to the class loader
      if (!clientJars.isEmpty())
      {
         URL[] urls = new URL[clientJars.size()];
         for (int i = 0; i < clientJars.size(); i++)
         {
            urls[i] = clientJars.get(i);
         }
         URLClassLoader cl = new URLClassLoader(urls, parent);
         Thread.currentThread().setContextClassLoader(cl);
      }
   }

   protected void tearDown() throws Exception
   {
      try
      {
         for (int i = 0; i < archives.length; i++)
         {
            String archive = archives[archives.length - i - 1];
            JBossWSTestHelper.undeploy(archive);
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(originalClassLoader);
         
         if (!securityDomains.isEmpty())
         {
            for (String key : securityDomains.keySet())
            {
               JBossWSTestHelper.removeSecurityDomain(key);
            }
         }
         if (defaultSecurityDomainRequirement)
         {
            JBossWSTestHelper.removeSecurityDomain(JBOSSWS_SEC_DOMAIN);
         }
      }
   }
   
   protected ClassLoader getOriginalClassLoader()
   {
      return originalClassLoader;
   }

   public MBeanServerConnection getServer() throws NamingException
   {
      return JBossWSTestHelper.getServer();
   }
   
   public void addSecurityDomainRequirement(String securityDomainName, Map<String, String> authenticationOptions)
   {
      securityDomains.put(securityDomainName, authenticationOptions);
   }
   
   public void setDefaultSecurityDomainRequirement(boolean defaultSecurityDomainRequirement)
   {
      this.defaultSecurityDomainRequirement = defaultSecurityDomainRequirement;
   }
}

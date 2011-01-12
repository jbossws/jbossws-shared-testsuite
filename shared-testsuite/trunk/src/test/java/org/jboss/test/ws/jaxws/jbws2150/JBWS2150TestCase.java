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
package org.jboss.test.ws.jaxws.jbws2150;

import java.net.URL;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.test.JBossWSTest;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;

/**
 * [JBWS-2150] Migrate AddressRewritingTestCase to jaxws
 * @author richard.opalka@jboss.com
 */
public final class JBWS2150TestCase extends JBossWSTest
{
   private static final ObjectName SERVER_CONFIG_OBJECT_NAME = ObjectNameFactory.create("jboss.ws:service=ServerConfig");
   private static final String NAMESPACE = "http://test.jboss.org/addressrewrite";

   private String wsdlLocation;
   private String wsdlLocationSec;
   private Boolean modifySOAPAddress;
   private String webServiceHost;

   public void setUp() throws Exception
   {
      wsdlLocation = "http://" + getServerHost() + ":8080/jaxws-jbws2150/ValidURL?wsdl";
      wsdlLocationSec = "http://" + getServerHost() + ":8080/jaxws-jbws2150-sec/ValidURL?wsdl";
      if (modifySOAPAddress == null)
      {
         modifySOAPAddress = (Boolean)getServer().getAttribute(SERVER_CONFIG_OBJECT_NAME, "ModifySOAPAddress");
         webServiceHost = (String)getServer().getAttribute(SERVER_CONFIG_OBJECT_NAME, "WebServiceHost");
      }
   }

   public void tearDown() throws Exception
   {
      Attribute attr = new Attribute("ModifySOAPAddress", modifySOAPAddress);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
      attr = new Attribute("WebServiceHost", webServiceHost);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }

   /**
    * Test soap:address rewrite
    * @throws Exception
    */
   public void testRewrite() throws Exception
   {
      setModifySOAPAddress(true);
      deploy("jaxws-jbws2150.war");
      try
      {

         Definition definition = getWSDLDefinition(wsdlLocation);

         String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
         assertEquals("http://" + webServiceHost + ":8080/jaxws-jbws2150/ValidURL", address);
         ServiceIface endpoint = getEndpoint(wsdlLocation, "ValidURLService");
         assertEquals(endpoint.echo("hello"), "hello");

         address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
         assertEquals("http://" + webServiceHost + ":8080/jaxws-jbws2150/InvalidURL", address);
         endpoint = getEndpoint(wsdlLocation, "InvalidURLService");
         assertEquals(endpoint.echo("hello"), "hello");

         address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
         assertEquals("https://" + webServiceHost + ":8443/jaxws-jbws2150/ValidSecureURL", address);

         address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
         assertEquals("https://" + webServiceHost + ":8443/jaxws-jbws2150/InvalidSecureURL", address);
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }

   /**
    * [JBWS-454] Test soap:address URL rewrite according to transport guarantee
    * @throws Exception
    */
   public void testSecureRewrite() throws Exception
   {
      setModifySOAPAddress(true);
      deploy("jaxws-jbws2150-sec.war");
      try
      {
         Definition definition = getWSDLDefinition(wsdlLocationSec);

         String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
         assertEquals("https://" + webServiceHost + ":8443/jaxws-jbws2150-sec/ValidURL", address);

         address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
         assertEquals("https://" + webServiceHost + ":8443/jaxws-jbws2150-sec/InvalidURL", address);

         address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
         assertEquals("https://" + webServiceHost + ":8443/jaxws-jbws2150-sec/ValidSecureURL", address);

         address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
         assertEquals("https://" + webServiceHost + ":8443/jaxws-jbws2150-sec/InvalidSecureURL", address);
      }
      finally
      {
         undeploy("jaxws-jbws2150-sec.war");
      }
   }

   /**
    * Test no soap:address rewrite
    * @throws Exception
    */
   public void testNoRewrite() throws Exception
   {
      setModifySOAPAddress(false);
      deploy("jaxws-jbws2150.war");
      try
      {
         Definition definition = getWSDLDefinition(wsdlLocation);

         String address = getPortAddress(definition, "ValidURLService", "ValidURLPort");
         assertEquals("http://somehost:80/somepath", address);

         address = getPortAddress(definition, "InvalidURLService", "InvalidURLPort");
         assertEquals("http://" + webServiceHost + ":8080/jaxws-jbws2150/InvalidURL", address);

         address = getPortAddress(definition, "ValidSecureURLService", "ValidSecureURLPort");
         assertEquals("https://somehost:443/some-secure-path", address);

         address = getPortAddress(definition, "InvalidSecureURLService", "InvalidSecureURLPort");
         assertEquals("https://" + webServiceHost + ":8443/jaxws-jbws2150/InvalidSecureURL", address);
      }
      finally
      {
         undeploy("jaxws-jbws2150.war");
      }
   }

   private void setModifySOAPAddress(Boolean value) throws Exception
   {
      Attribute attr = new Attribute("ModifySOAPAddress", value);
      getServer().setAttribute(SERVER_CONFIG_OBJECT_NAME, attr);
   }

   private ServiceIface getEndpoint(String wsdlLocation, String serviceName) throws Exception
   {
      Service service = Service.create(new URL(wsdlLocation), new QName(NAMESPACE, serviceName));
      QName portName = service.getPorts().next();
      return service.getPort(portName, ServiceIface.class);
   }

   private Definition getWSDLDefinition(String wsdlLocation) throws Exception
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

      Definition definition = wsdlReader.readWSDL(null, wsdlLocation);
      return definition;
   }

   private String getPortAddress(Definition definition, String serviceName, String portName)
   {
      Port port = definition.getService(new QName(NAMESPACE, serviceName)).getPort(portName);
      return ((SOAPAddressImpl)port.getExtensibilityElements().get(0)).getLocationURI();
   }

}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jaxbcust;

import junit.framework.TestCase;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.spi.binding.JAXBBindingCustomization;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Endpoint.EndpointState;

/**
 * @author Heiko.Braun@jboss.com
 * @author alessio.soldano@jboss.com
 * 
 * @since 28-Jun-2007
 */
public class BindingCustomizationTestCase extends TestCase {

   DeploymentModelFactory deploymentModelFactory;

   protected void setUp() throws Exception
   {
      super.setUp();

      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      deploymentModelFactory = spiProvider.getSPI(DeploymentModelFactory.class);
   }

   public void testCustomizationWriteAccess() throws Exception
   {
      Endpoint endpoint = deploymentModelFactory.newEndpoint(null);
      BindingCustomization jaxbCustomization = new JAXBBindingCustomization();
      jaxbCustomization.put("com.sun.xml.bind.defaultNamespaceRemap", "http://org.jboss.bindingCustomization");
      endpoint.addAttachment(BindingCustomization.class, jaxbCustomization);

      // a started endpoint should deny customizations
      try
      {
         endpoint.setState(EndpointState.STARTED);
         endpoint.addAttachment(BindingCustomization.class, jaxbCustomization);

         fail("It should not be possible to change bindinig customizations on a started endpoint");
      }
      catch (Exception e)
      {
         // all fine, this should happen
      }
   }

   public void testCustomizationReadAccess() throws Exception
   {
      Endpoint endpoint = deploymentModelFactory.newEndpoint(null);
      BindingCustomization jaxbCustomization = new JAXBBindingCustomization();
      jaxbCustomization.put("com.sun.xml.bind.defaultNamespaceRemap", "http://org.jboss.bindingCustomization");
      endpoint.addAttachment(BindingCustomization.class, jaxbCustomization);
      endpoint.setState(EndpointState.STARTED);

      // read a single customization
      BindingCustomization knownCustomization = endpoint.getAttachment(BindingCustomization.class);
      assertNotNull(knownCustomization);

      // however the iteratoion should be unmodifiable
      try
      {
         endpoint.addAttachment(BindingCustomization.class, jaxbCustomization);
         fail("Started Endpoints should only axpose read acccess to their binding customizations");
      }
      catch (Exception e)
      {
         // all fine, we'd expect this
      }


   }
}

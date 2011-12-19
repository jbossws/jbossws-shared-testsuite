/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ws.jaxws.as2961;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [AS7-2961] Tests JNDI lookup in pojo endpoint constructor.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class AS2961TestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new JBossWSTestSetup(AS2961TestCase.class, "jaxws-as2961.war");
   }

   public void testEndpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.as2961", "SimpleService");
      final URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-as2961/SimpleService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final EndpointIface port = service.getPort(EndpointIface.class);
      final String result = port.echo("hello");
      assertEquals("hello", result);
   }

}

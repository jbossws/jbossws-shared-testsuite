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
package org.jboss.test.ws.jaxws.jbws981;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.ws.api.annotation.WebContext;

/**
 * 
 * @author darran.lofthouse@jboss.com
 * @since Nov 2, 2006
 */
@Stateless
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.jbws981.EndpointInterface", targetNamespace = "http://www.jboss.org/test/ws/jaxws/jbws981", serviceName = "EndpointService")
@WebContext(virtualHosts = { "localhost", "www.jboss.org" })
public class EJB3Bean implements EJB3RemoteInterface
{

   private static final Logger log = Logger.getLogger(EJB3Bean.class);

   public String hello(final String message)
   {
      try
      {
         MBeanServer mbeanServer = (MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
         ObjectName on = new ObjectName("jboss.web:J2EEApplication=none,J2EEServer=none,WebModule=//www.jboss.org/jaxws-jbws981,j2eeType=Servlet,name=EJB3Bean");
         mbeanServer.getMBeanInfo(on);
      }
      catch (Exception e)
      {
         log.error("Unable to lookup deployment", e);
         return "Unable to get WebModule MBean for virtual host - virtual-host not handled from @WebContext";
      }

      return message;
   }

}

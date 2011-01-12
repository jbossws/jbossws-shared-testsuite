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
package org.jboss.test.ws.jaxws.samples.addressing;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAPBuilderFactory;
import org.jboss.wsf.common.addressing.MAPEndpoint;
import org.jboss.wsf.common.handler.GenericSOAPHandler;
import org.w3c.dom.Element;

/**
 * A server side handler for the ws-addressing
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class ServerHandler extends GenericSOAPHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(ServerHandler.class);

   private static final QName IDQN = StatefulEndpointImpl.IDQN;

   @Override
   public boolean handleInbound(MessageContext msgContext)
   {
      log.info("handleRequest");

      MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
      MAP addrProps = builder.inboundMap(msgContext);

      if (addrProps == null)
         throw new IllegalStateException("Cannot obtain AddressingProperties");

      String clientid = null;
      MAPEndpoint replyTo = addrProps.getReplyTo();
      for (Object obj :replyTo.getReferenceParameters())
      {
         if (obj instanceof Element)
         {
            Element el = (Element)obj;
            QName qname = DOMUtils.getElementQName(el);
            if (qname.equals(IDQN))
            {
               clientid = DOMUtils.getTextContent(el);
            }
         }
         else
         {
            log.warn("Unsupported reference parameter found: " + obj);
         }
      }

      if (clientid == null)
         throw new IllegalStateException("Cannot obtain client id");

      // put the clientid in the message context
      msgContext.put("clientid", clientid);
      msgContext.setScope("clientid", Scope.APPLICATION);
      return true;
   }

   @Override
   public boolean handleOutbound(MessageContext msgContext)
   {
      log.info("handleResponse");
      
      MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
      MAP inProps = builder.inboundMap(msgContext);
      MAP outProps = builder.newMap();
      outProps.initializeAsDestination(inProps.getReplyTo());
      
      outProps.installOutboundMapOnServerSide(msgContext, outProps);
      msgContext.setScope(builder.newConstants().getServerAddressingPropertiesOutbound(), Scope.APPLICATION);

      return true;
   }
}

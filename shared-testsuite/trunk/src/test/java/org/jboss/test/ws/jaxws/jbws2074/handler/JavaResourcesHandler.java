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
package org.jboss.test.ws.jaxws.jbws2074.handler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * This handler is initialized via injections.
 * Injections are specified via @Resource annotation
 * and refer to the ejb-jar.xml or web.xml defined
 * environment entries.
 *
 * @author ropalka@redhat.com
 */
public final class JavaResourcesHandler extends GenericSOAPHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(JavaResourcesHandler.class);

   /**
    * java.lang.Boolean
    */

   @Resource(name = "boolean")
   private Boolean boolean0;

   private Boolean boolean1;

   @Resource(name = "boolean")
   private void setBoolean1(Boolean b)
   {
      this.boolean1 = b;
   }

   /**
    * java.lang.Byte
    */

   @Resource(name = "byte")
   private Byte byte0;

   private Byte byte1;

   @Resource(name = "byte")
   private void setByte1(Byte b)
   {
      this.byte1 = b;
   }

   /**
    * java.lang.Character
    */

   @Resource(name = "character")
   private Character character0;

   private Character character1;

   @Resource(name = "character")
   private void setCharacter1(Character c)
   {
      this.character1 = c;
   }

   /**
    * java.lang.Short
    */

   @Resource(name = "short")
   private Short short0;

   private Short short1;

   @Resource(name = "short")
   private void setShort1(Short i)
   {
      this.short1 = i;
   }

   /**
    * java.lang.Integer
    */

   @Resource(name = "integer")
   private Integer integer0;

   private Integer integer1;

   @Resource(name = "integer")
   private void setInteger1(Integer i)
   {
      this.integer1 = i;
   }

   /**
    * java.lang.Long
    */

   @Resource(name = "long")
   private Long long0;

   private Long long1;

   @Resource(name = "long")
   private void setLong1(Long l)
   {
      this.long1 = l;
   }

   /**
    * java.lang.Float
    */

   @Resource(name = "float")
   private Float float0;

   private Float float1;

   @Resource(name = "float")
   private void setFloat1(Float f)
   {
      this.float1 = f;
   }

   /**
    * java.lang.Double
    */

   @Resource(name = "double")
   private Double double0;

   private Double double1;

   @Resource(name = "double")
   private void setDouble1(Double d)
   {
      this.double1 = d;
   }

   /**
    * java.lang.String
    */

   @Resource
   private String string;

   private String string1;

   @Resource
   private void setString(String s)
   {
      this.string1 = s;
   }

   /**
    * Indicates whether handler is in correct state.
    */
   private boolean correctState;

   @PostConstruct
   private void init()
   {
      boolean correctInitialization = true;

      // java.lang.Boolean
      if (this.boolean0 == null || this.boolean0 != true)
      {
         log.error("@Resource initialization for boolean0 failed");
         correctInitialization = false;
      }
      if (this.boolean1 == null || this.boolean1 != true)
      {
         log.error("@Resource initialization for boolean1 failed");
         correctInitialization = false;
      }

      // java.lang.Byte
      if (this.byte0 == null || this.byte0 != (byte)1)
      {
         log.error("@Resource initialization for byte0 failed");
         correctInitialization = false;
      }
      if (this.byte1 == null || this.byte1 != (byte)1)
      {
         log.error("@Resource initialization for byte1 failed");
         correctInitialization = false;
      }

      // java.lang.Character
      if (this.character0 == null || this.character0 != 'c')
      {
         log.error("@Resource initialization for character0 failed");
         correctInitialization = false;
      }
      if (this.character1 == null || this.character1 != 'c')
      {
         log.error("@Resource initialization for character1 failed");
         correctInitialization = false;
      }

      // java.lang.Short
      if (this.short0 == null || this.short0 != (short)5)
      {
         log.error("@Resource initialization for short0 failed");
         correctInitialization = false;
      }
      if (this.short1 == null || this.short1 != (short)5)
      {
         log.error("@Resource initialization for short1 failed");
         correctInitialization = false;
      }

      // java.lang.Integer
      if (this.integer0 == null || this.integer0 != 7)
      {
         log.error("@Resource initialization for integer0 failed");
         correctInitialization = false;
      }
      if (this.integer1 == null || this.integer1 != 7)
      {
         log.error("@Resource initialization for integer1 failed");
         correctInitialization = false;
      }

      // java.lang.Long
      if (this.long0 == null || this.long0 != 11L)
      {
         log.error("@Resource initialization for long0 failed");
         correctInitialization = false;
      }
      if (this.long1 == null || this.long1 != 11L)
      {
         log.error("@Resource initialization for long1 failed");
         correctInitialization = false;
      }

      // java.lang.Float
      if (this.float0 == null || this.float0 != 13.0f)
      {
         log.error("@Resource initialization for float0 failed");
         correctInitialization = false;
      }
      if (this.float1 == null || this.float1 != 13.0f)
      {
         log.error("@Resource initialization for float1 failed");
         correctInitialization = false;
      }

      // java.lang.Double
      if (this.double0 == null || this.double0 != 17.0)
      {
         log.error("@Resource initialization for double0 failed");
         correctInitialization = false;
      }
      if (this.double1 == null || this.double1 != 17.0)
      {
         log.error("@Resource initialization for double1 failed");
         correctInitialization = false;
      }

      // java.lang.String
      if ("s".equals(this.string) == false)
      {
         log.error("@Resource initialization for string0 failed");
         correctInitialization = false;
      }
      if ("s".equals(this.string1) == false)
      {
         log.error("@Resource initialization for string1 failed");
         correctInitialization = false;
      }

      this.correctState = correctInitialization;
   }

   @Override
   public boolean handleOutbound(MessageContext msgContext)
   {
      return ensureInjectionsAndInitialization(msgContext, "Outbound");
   }

   @Override
   public boolean handleInbound(MessageContext msgContext)
   {
      return ensureInjectionsAndInitialization(msgContext, "Inbound");
   }

   private boolean ensureInjectionsAndInitialization(MessageContext msgContext, String direction)
   {
      if (!this.correctState)
      {
         throw new WebServiceException("Unfunctional javax.annotation.* annotations");
      }

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPElement soapElement = (SOAPElement)soapMessage.getSOAPBody().getChildElements().next();
         soapElement = (SOAPElement)soapElement.getChildElements().next();

         String oldValue = soapElement.getValue();
         String newValue = oldValue + ":" + direction + ":JavaResourcesHandler";
         soapElement.setValue(newValue);

         log.debug("oldValue: " + oldValue);
         log.debug("newValue: " + newValue);

         return true;
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }

}

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
package org.jboss.test.ws.jaxws.jbws2009;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.jbws2009.generated.CountryCodeType;
import org.jboss.test.ws.jaxws.jbws2009.generated.CurrencyCodeType;
import org.jboss.test.ws.jaxws.jbws2009.generated.ServiceType;
import org.jboss.test.ws.jaxws.jbws2009.generated.GetCountryCodesResponse.Response;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2009] JBossWS cannot find local schema with relative urls
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Oct-2007
 */
public class JBWS2009TestCase extends JBossWSTest
{
   private String targetNS = "http://jbws2009.jaxws.ws.test.jboss.org/";
   private ServiceType proxy;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2009TestCase.class, "jaxws-jbws2009.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "EndpointService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2009/Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = service.getPort(ServiceType.class);
   }
   
   public void testCountryCodes() throws Exception
   {
      Response response = proxy.getCountryCodes();
      List<CountryCodeType> countryCodes = response.getCountry();
      assertEquals(countryCodes.get(0), CountryCodeType.CZ);
      assertEquals(countryCodes.get(1), CountryCodeType.DE);
   }

   public void testCurrencyCodes() throws Exception
   {
      org.jboss.test.ws.jaxws.jbws2009.generated.GetCurrencyCodesResponse.Response response = proxy.getCurrencyCodes();
      List<CurrencyCodeType> currencyCodes = response.getCurrency();
      assertEquals(currencyCodes.get(0), CurrencyCodeType.CZK);
      assertEquals(currencyCodes.get(1), CurrencyCodeType.EUR);
   }
   
   public void test() throws Exception
   {
      assertEquals(CurrencyCodeType.CZK, proxy.getCurrency(CountryCodeType.CZ));
      assertEquals(CurrencyCodeType.EUR, proxy.getCurrency(CountryCodeType.DE));
   }

}

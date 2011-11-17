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
package org.jboss.test.ws.jaxrpc.samples.mtom;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.activation.DataHandler;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Stub;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test SOAP with XOP through the JAXRPC dynamic proxy layer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.org
 * @since 18-Jan-2006
 */
public class MTOMTestCase extends JBossWSTest
{
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/mtom";
   private static final String TARGET_ENDPOINT_URL = "http://" + getServerHost() + ":8080/jaxrpc-samples-mtom";
   
   private static EmployeeRecords port;

   /** Deploy the test ear */
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(MTOMTestCase.class, "jaxrpc-samples-mtom.war, jaxrpc-samples-mtom-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         port = getService(EmployeeRecords.class, "EmployeeService", "EmployeeRecordsPort");
      }
   }
   
   protected <T> T getService(final Class<T> clazz, final String serviceName, final String portName) throws Exception {
      ServiceFactory serviceFactory = ServiceFactory.newInstance();
      Service service = serviceFactory.createService(new URL(TARGET_ENDPOINT_URL + "?wsdl"), new QName(TARGET_NAMESPACE, serviceName));
      return (T) service.getPort(new QName(TARGET_NAMESPACE, portName), clazz);
   }

   public void testUpdate() throws Exception
   {
      Employee employee = new Employee();
      employee.setFirstname("Peter");
      employee.setLastname("Pan");
      employee.setLegacyData(getLegacyData());

      Status status = port.updateEmployee(employee);
      assertEquals("OK", status.getCode());
   }

   public void testUpdateMTOMDisabled() throws Exception
   {
      setMTOMEnabled(Boolean.FALSE);
      testUpdate();
   }

   public void testQuery() throws Exception
   {
      Query query = new Query("Peter", "Pan");
      Employee employee = port.queryEmployee(query);
      assertNotNull(employee);
      assertNotNull(employee.getLegacyData());

      StreamSource xmlStream = (StreamSource)employee.getLegacyData().getContent();
      String content = new BufferedReader(new InputStreamReader(xmlStream.getInputStream())).readLine();
      assertTrue(content.contains("<Payroll><Data/></Payroll>"));
   }

   private DataHandler getLegacyData()
   {
      return new DataHandler(new StreamSource(new ByteArrayInputStream("<Payroll><Data/></Payroll>".getBytes())), "application/xml");
   }

   private void setMTOMEnabled(Boolean b)
   {
      ((Stub)port)._setProperty("org.jboss.ws.mtom.enabled", b);
   }
}

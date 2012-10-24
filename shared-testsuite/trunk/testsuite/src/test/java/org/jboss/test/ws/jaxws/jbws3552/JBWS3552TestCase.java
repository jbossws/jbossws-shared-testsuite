/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3552;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-3552] @XmlJavaTypeAdapter ignored on exception classes.
 * 
 * Suffixes abbreviations used in this test case in class names are:
 * <ul>
 *   <li>CA - class level access</li>
 *   <li>FA - field level access</li>
 *   <li>MA - method level access</li>
 *   <li>GA - package level access</li>
 * </ul>
 * @author <a href="ropalka@redhat.com">Richard Opalka</a>
 */
public class JBWS3552TestCase extends JBossWSTest {
    public static Test suite() {
        return new JBossWSTestSetup(JBWS3552TestCase.class, "jaxws-jbws3552.war");
    }

    private EndpointIface getProxy() throws Exception {
        final URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3552/EndpointImpl?wsdl");
        final QName serviceName = new QName("http://jbws3552.jaxws.ws.test.jboss.org/", "EndpointImplService");
        final Service service = Service.create(wsdlURL, serviceName);
        return service.getPort(EndpointIface.class);
    }

    public void testEchoCA() throws Exception {
        EndpointIface endpoint = getProxy();
        AdaptedObjectCA aoCA = new AdaptedObjectCA("object message", "object description", 444, new ComplexObjectCA("a", "b"));
        assertEquals("object message,object description,444,a b", endpoint.echoCA(aoCA).toString());
    }

    public void testEchoAbstractCA() throws Exception {
        EndpointIface endpoint = getProxy();
        AdaptedObjectCA aoCA = new AdaptedObjectCA("object message", "object description", 444, new ComplexObjectCA("a", "b"));
        assertEquals("object message,object description,444,a b", endpoint.echoAbstractCA(aoCA).toString());
    }

    public void testEchoFA() throws Exception {
        EndpointIface endpoint = getProxy();
        AdaptedObjectFA aoFA = new AdaptedObjectFA("object message", "object description", 444, new ComplexObjectFA("a", "b"));
        assertEquals("object message,object description,444,a b", endpoint.echoFA(aoFA).toString());
    }

    public void testEchoAbstractFA() throws Exception {
        EndpointIface endpoint = getProxy();
        AdaptedObjectFA aoFA = new AdaptedObjectFA("object message", "object description", 444, new ComplexObjectFA("a", "b"));
        assertEquals("object message,object description,444,a b", endpoint.echoAbstractFA(aoFA).toString());
    }

    public void testEchoGA() throws Exception {
        EndpointIface endpoint = getProxy();
        AdaptedObjectGA aoGA = new AdaptedObjectGA("object message", "object description", 444, new ComplexObjectGA("a", "b"));
        assertEquals("object message,object description,444,a b", endpoint.echoGA(aoGA).toString());
    }

    public void testEchoAbstractGA() throws Exception {
        EndpointIface endpoint = getProxy();
        AdaptedObjectGA aoGA = new AdaptedObjectGA("object message", "object description", 444, new ComplexObjectGA("a", "b"));
        assertEquals("object message,object description,444,a b", endpoint.echoAbstractGA(aoGA).toString());
    }

    public void testEchoMA() throws Exception {
        EndpointIface endpoint = getProxy();
        AdaptedObjectMA aoMA = new AdaptedObjectMA("object message", "object description", 444, new ComplexObjectMA("a", "b"));
        assertEquals("object message,object description,444,a b", endpoint.echoMA(aoMA).toString());
    }

    public void testEchoAbstractMA() throws Exception {
        EndpointIface endpoint = getProxy();
        AdaptedObjectMA aoMA = new AdaptedObjectMA("object message", "object description", 444, new ComplexObjectMA("a", "b"));
        assertEquals("object message,object description,444,a b", endpoint.echoAbstractMA(aoMA).toString());
    }

    public void testExceptionCA() throws Exception {
        if (true) {
            System.out.println("FIXME: [CXF-4600] Exception inheritance not working over SOAP protocol");
            return;
        }
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwExceptionCA();
            fail("Expected exception not thrown");
        } catch (AdaptedExceptionCA e) {
            assertEquals("exception message,exception description,666,c d", e.toString());
        }
    }

    public void testExtendedExceptionCA() throws Exception {
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwExtendedExceptionCA();
            fail("Expected exception not thrown");
        } catch (ExtendedAdaptedExceptionCA e) {
            assertEquals("exception message,exception description,666,c d", e.toString());
        }
    }

    public void testExceptionFA() throws Exception {
        if (true) {
            System.out.println("FIXME: [CXF-4600] Exception inheritance not working over SOAP protocol");
            return;
        }
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwExceptionFA();
            fail("Expected exception not thrown");
        } catch (AdaptedExceptionFA e) {
            assertEquals("exception message,exception description,666,c d", e.toString());
        }
    }

    public void testExtendedExceptionFA() throws Exception {
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwExtendedExceptionFA();
            fail("Expected exception not thrown");
        } catch (ExtendedAdaptedExceptionFA e) {
            assertEquals("exception message,exception description,666,c d", e.toString());
        }
    }

    public void testExceptionGA() throws Exception {
        if (true) {
            System.out.println("FIXME: [CXF-4600] Exception inheritance not working over SOAP protocol");
            return;
        }
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwExceptionGA();
            fail("Expected exception not thrown");
        } catch (AdaptedExceptionGA e) {
            assertEquals("exception message,exception description,666,c d", e.toString());
        }
    }

    public void testExtendedExceptionGA() throws Exception {
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwExtendedExceptionGA();
            fail("Expected exception not thrown");
        } catch (ExtendedAdaptedExceptionGA e) {
            assertEquals("exception message,exception description,666,c d", e.toString());
        }
    }

    public void testExceptionMA() throws Exception {
        if (true) {
            System.out.println("FIXME: [CXF-4600] Exception inheritance not working over SOAP protocol");
            return;
        }
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwExceptionMA();
            fail("Expected exception not thrown");
        } catch (AdaptedExceptionMA e) {
            assertEquals("exception message,exception description,666,c d", e.toString());
        }
    }

    public void testExtendedExceptionMA() throws Exception {
        EndpointIface endpoint = getProxy();
        try {
            endpoint.throwExtendedExceptionMA();
            fail("Expected exception not thrown");
        } catch (ExtendedAdaptedExceptionMA e) {
            assertEquals("exception message,exception description,666,c d", e.toString());
        }
    }
}

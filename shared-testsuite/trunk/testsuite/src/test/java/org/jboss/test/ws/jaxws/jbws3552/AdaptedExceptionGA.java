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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedExceptionGA extends Exception {
    private String message;
    private String description;
    private int code;
    private ComplexObjectGA complexObject;

    public AdaptedExceptionGA() {
    }

    public AdaptedExceptionGA(String message, String description, int code, ComplexObjectGA complexObject) {
        this.message = message;
        this.description = description;
        this.code = code;
        this.complexObject = complexObject;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public ComplexObjectGA getComplexObject() {
        return complexObject;
    }

    public String toString() {
        return message + "," + description + "," + code + "," + complexObject;
    }
}
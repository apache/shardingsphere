/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mode.repository.standalone.jdbc.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * JDBC repository SQL Loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCRepositorySQLLoader {
    
    private static final String ROOT_DIRECTORY = "sql";
    
    private static final String FILE_EXTENSION = ".xml";
    
    /**
     * Load JDBC repository SQL.
     *
     * @param type type of JDBC repository SQL
     * @return loaded JDBC repository SQL
     */
    @SneakyThrows(IOException.class)
    public static JDBCRepositorySQL load(final String type) {
        JDBCRepositorySQL result = null;
        XMLInputFactory factory = createXMLInputFactory();
        try (Stream<String> resourceNameStream = ClasspathResourceDirectoryReader.read(JDBCRepositorySQLLoader.class.getClassLoader(), ROOT_DIRECTORY)) {
            Iterable<String> resourceNameIterable = resourceNameStream::iterator;
            for (String each : resourceNameIterable) {
                if (!each.endsWith(FILE_EXTENSION)) {
                    continue;
                }
                JDBCRepositorySQL provider;
                try (InputStream inputStream = Objects.requireNonNull(JDBCRepositorySQLLoader.class.getClassLoader().getResourceAsStream(each))) {
                    provider = parse(factory, inputStream);
                }
                if (provider.isDefault()) {
                    result = provider;
                }
                if (Objects.equals(provider.getType(), type)) {
                    result = provider;
                    break;
                }
            }
        }
        return result;
    }
    
    private static XMLInputFactory createXMLInputFactory() {
        XMLInputFactory result = XMLInputFactory.newFactory();
        result.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        result.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        result.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        result.setXMLResolver((publicId, systemId, baseUri, namespace) -> {
            throw new XMLStreamException("External XML resources are not allowed.");
        });
        return result;
    }
    
    private static JDBCRepositorySQL parse(final XMLInputFactory factory, final InputStream inputStream) throws IOException {
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
            try {
                return parse(reader);
            } finally {
                reader.close();
            }
        } catch (final XMLStreamException ex) {
            throw new IOException("Failed to parse JDBC repository SQL XML.", ex);
        }
    }
    
    private static JDBCRepositorySQL parse(final XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (XMLStreamConstants.DTD == event) {
                throw new XMLStreamException("DTD is not allowed.");
            }
            if (XMLStreamConstants.START_ELEMENT == event) {
                validateRootElement(reader);
                JDBCRepositorySQL result = createJDBCRepositorySQL(reader);
                parseSQLElements(reader, result);
                return result;
            }
        }
        throw new XMLStreamException("JDBC repository SQL root element is missing.");
    }
    
    private static void validateRootElement(final XMLStreamReader reader) throws XMLStreamException {
        if (!"sql".equals(reader.getLocalName())) {
            throw new XMLStreamException("Unexpected JDBC repository SQL root element: " + reader.getLocalName());
        }
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attributeName = reader.getAttributeLocalName(i);
            if (!"type".equals(attributeName) && !"driver-class-name".equals(attributeName) && !"default".equals(attributeName)) {
                throw new XMLStreamException("Unexpected JDBC repository SQL attribute: " + attributeName);
            }
        }
    }
    
    private static JDBCRepositorySQL createJDBCRepositorySQL(final XMLStreamReader reader) {
        JDBCRepositorySQL result = new JDBCRepositorySQL();
        result.setType(reader.getAttributeValue(null, "type"));
        result.setDriverClassName(reader.getAttributeValue(null, "driver-class-name"));
        result.setDefault(Boolean.parseBoolean(reader.getAttributeValue(null, "default")));
        return result;
    }
    
    private static void parseSQLElements(final XMLStreamReader reader, final JDBCRepositorySQL result) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (XMLStreamConstants.DTD == event) {
                throw new XMLStreamException("DTD is not allowed.");
            }
            if (XMLStreamConstants.START_ELEMENT == event) {
                parseSQLElement(reader, result);
            } else if (XMLStreamConstants.END_ELEMENT == event) {
                validateDocumentEnd(reader);
                return;
            }
        }
        throw new XMLStreamException("JDBC repository SQL root element is not closed.");
    }
    
    private static void parseSQLElement(final XMLStreamReader reader, final JDBCRepositorySQL result) throws XMLStreamException {
        String elementName = reader.getLocalName();
        if (0 != reader.getAttributeCount()) {
            throw new XMLStreamException("Unexpected attribute on JDBC repository SQL element: " + elementName);
        }
        switch (elementName) {
            case "create-table":
                result.setCreateTableSQL(reader.getElementText());
                break;
            case "select-by-key":
                result.setSelectByKeySQL(reader.getElementText());
                break;
            case "select-by-parent":
                result.setSelectByParentKeySQL(reader.getElementText());
                break;
            case "insert":
                result.setInsertSQL(reader.getElementText());
                break;
            case "update":
                result.setUpdateSQL(reader.getElementText());
                break;
            case "delete":
                result.setDeleteSQL(reader.getElementText());
                break;
            default:
                throw new XMLStreamException("Unexpected JDBC repository SQL element: " + elementName);
        }
    }
    
    private static void validateDocumentEnd(final XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (XMLStreamConstants.START_ELEMENT == event || XMLStreamConstants.DTD == event) {
                throw new XMLStreamException("Unexpected content after JDBC repository SQL root element.");
            }
            if (XMLStreamConstants.CHARACTERS == event && !reader.isWhiteSpace()) {
                throw new XMLStreamException("Unexpected text after JDBC repository SQL root element.");
            }
        }
    }
}

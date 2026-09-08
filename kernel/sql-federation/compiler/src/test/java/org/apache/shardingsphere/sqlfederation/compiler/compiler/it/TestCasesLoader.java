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

package org.apache.shardingsphere.sqlfederation.compiler.compiler.it;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Integration test cases loader.
 */
public final class TestCasesLoader {
    
    private static final TestCasesLoader INSTANCE = new TestCasesLoader();
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static TestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Read a case file and generate a case object.
     *
     * @return collection of test cases
     * @throws IOException exception for read file.
     */
    public Collection<TestCase> generate() throws IOException {
        Collection<TestCase> result = new LinkedList<>();
        URL queryCaseUrl = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("cases/federation-query-sql-cases.xml"));
        URL deleteCaseUrl = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("cases/federation-delete-sql-cases.xml"));
        XMLInputFactory factory = createXMLInputFactory();
        result.addAll(loadTestCase(factory, queryCaseUrl));
        result.addAll(loadTestCase(factory, deleteCaseUrl));
        return result;
    }
    
    private XMLInputFactory createXMLInputFactory() {
        XMLInputFactory result = XMLInputFactory.newFactory();
        result.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        result.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        result.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        result.setXMLResolver((publicId, systemId, baseUri, namespace) -> {
            throw new XMLStreamException("External XML resources are not allowed.");
        });
        return result;
    }
    
    private Collection<TestCase> loadTestCase(final XMLInputFactory factory, final URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return parse(factory, inputStream);
        }
    }
    
    private Collection<TestCase> parse(final XMLInputFactory factory, final InputStream inputStream) throws IOException {
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
            try {
                return parse(reader);
            } finally {
                reader.close();
            }
        } catch (final XMLStreamException ex) {
            throw new IOException("Failed to parse SQL federation test cases XML.", ex);
        }
    }
    
    private Collection<TestCase> parse(final XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            rejectDTD(event);
            if (XMLStreamConstants.START_ELEMENT == event) {
                validateRootElement(reader);
                Collection<TestCase> result = new LinkedList<>();
                parseTestCases(reader, result);
                return result;
            }
        }
        throw new XMLStreamException("SQL federation test cases root element is missing.");
    }
    
    private void validateRootElement(final XMLStreamReader reader) throws XMLStreamException {
        if (!"test-cases".equals(reader.getLocalName())) {
            throw new XMLStreamException("Unexpected SQL federation test cases root element: " + reader.getLocalName());
        }
        if (0 != reader.getAttributeCount()) {
            throw new XMLStreamException("Unexpected attribute on SQL federation test cases root element.");
        }
    }
    
    private void parseTestCases(final XMLStreamReader reader, final Collection<TestCase> result) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            rejectDTD(event);
            if (XMLStreamConstants.START_ELEMENT == event) {
                if (!"test-case".equals(reader.getLocalName())) {
                    throw new XMLStreamException("Unexpected SQL federation test cases element: " + reader.getLocalName());
                }
                result.add(parseTestCase(reader));
            } else if (XMLStreamConstants.END_ELEMENT == event) {
                validateDocumentEnd(reader);
                return;
            } else {
                rejectText(event, reader, "SQL federation test cases");
            }
        }
        throw new XMLStreamException("SQL federation test cases root element is not closed.");
    }
    
    private TestCase parseTestCase(final XMLStreamReader reader) throws XMLStreamException {
        validateAttributes(reader, "sql");
        TestCase result = new TestCase();
        result.setSql(reader.getAttributeValue(null, "sql"));
        List<TestCaseAssertion> assertions = null;
        while (reader.hasNext()) {
            int event = reader.next();
            rejectDTD(event);
            if (XMLStreamConstants.START_ELEMENT == event) {
                if (!"assertion".equals(reader.getLocalName())) {
                    throw new XMLStreamException("Unexpected SQL federation test case element: " + reader.getLocalName());
                }
                if (null == assertions) {
                    assertions = new LinkedList<>();
                }
                assertions.add(parseAssertion(reader));
            } else if (XMLStreamConstants.END_ELEMENT == event) {
                result.setAssertion(assertions);
                return result;
            } else {
                rejectText(event, reader, "SQL federation test case");
            }
        }
        throw new XMLStreamException("SQL federation test case element is not closed.");
    }
    
    private TestCaseAssertion parseAssertion(final XMLStreamReader reader) throws XMLStreamException {
        validateAttributes(reader, "expected-result");
        TestCaseAssertion result = new TestCaseAssertion();
        result.setExpectedResult(reader.getAttributeValue(null, "expected-result"));
        if (!reader.getElementText().trim().isEmpty()) {
            throw new XMLStreamException("Unexpected text in SQL federation test case assertion.");
        }
        return result;
    }
    
    private void validateAttributes(final XMLStreamReader reader, final String expectedAttribute) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            if (!expectedAttribute.equals(reader.getAttributeLocalName(i))) {
                throw new XMLStreamException("Unexpected attribute on SQL federation test case element: " + reader.getAttributeLocalName(i));
            }
        }
    }
    
    private void rejectDTD(final int event) throws XMLStreamException {
        if (XMLStreamConstants.DTD == event) {
            throw new XMLStreamException("DTD is not allowed.");
        }
    }
    
    private void rejectText(final int event, final XMLStreamReader reader, final String element) throws XMLStreamException {
        if ((XMLStreamConstants.CHARACTERS == event || XMLStreamConstants.CDATA == event) && !reader.isWhiteSpace()) {
            throw new XMLStreamException("Unexpected text in " + element + " element.");
        }
    }
    
    private void validateDocumentEnd(final XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            rejectDTD(event);
            if (XMLStreamConstants.START_ELEMENT == event) {
                throw new XMLStreamException("Unexpected content after SQL federation test cases root element.");
            }
            rejectText(event, reader, "SQL federation test cases document");
        }
    }
}

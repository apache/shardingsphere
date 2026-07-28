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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * MCP Builder evaluation catalog.
 */
public final class MCPBuilderEvaluationCatalog {
    
    private static final String RESOURCE_PATH = "/llm/evaluation/mcp-builder-evaluation.xml";
    
    /**
     * Load the evaluation suite.
     *
     * @return evaluation suite
     * @throws IOException IO exception
     */
    public EvaluationSuite load() throws IOException {
        try (InputStream inputStream = MCPBuilderEvaluationCatalog.class.getResourceAsStream(RESOURCE_PATH)) {
            if (null == inputStream) {
                throw new IOException("MCP Builder evaluation catalog is unavailable: " + RESOURCE_PATH);
            }
            Document document = createDocumentBuilderFactory().newDocumentBuilder().parse(inputStream);
            Element root = document.getDocumentElement();
            if (!"mcp_builder_evaluation".equals(root.getTagName())) {
                throw new IOException("Unexpected MCP Builder evaluation catalog root element: " + root.getTagName());
            }
            return new EvaluationSuite(
                    root.getAttribute("standard_reference"),
                    root.getAttribute("operation_mode"),
                    readBooleanAttribute(root, "requires_external_model"),
                    loadCases(root.getElementsByTagName("qa_pair")));
        } catch (final ParserConfigurationException | SAXException ex) {
            throw new IOException("Failed to parse MCP Builder evaluation catalog.", ex);
        }
    }
    
    private DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory result = DocumentBuilderFactory.newInstance();
        result.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        result.setFeature("http://xml.org/sax/features/external-general-entities", false);
        result.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        result.setXIncludeAware(false);
        result.setExpandEntityReferences(false);
        return result;
    }
    
    private List<EvaluationCase> loadCases(final NodeList qaPairs) {
        List<EvaluationCase> result = new LinkedList<>();
        for (int i = 0; i < qaPairs.getLength(); i++) {
            Element each = (Element) qaPairs.item(i);
            result.add(new EvaluationCase(
                    each.getAttribute("id"),
                    each.getAttribute("category"),
                    readBooleanAttribute(each, "read_only"),
                    readElementText(each, "question"),
                    readElementText(each, "answer")));
        }
        return result;
    }
    
    private boolean readBooleanAttribute(final Element element, final String attributeName) {
        String value = element.getAttribute(attributeName);
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalStateException(String.format("MCP Builder evaluation element `%s` attribute `%s` must be `true` or `false`, but was `%s`.",
                    element.getTagName(), attributeName, value));
        }
        return Boolean.parseBoolean(value);
    }
    
    private String readElementText(final Element parent, final String tagName) {
        NodeList elements = parent.getElementsByTagName(tagName);
        if (1 != elements.getLength()) {
            throw new IllegalStateException(String.format("MCP Builder evaluation case `%s` must contain exactly one `%s` element.", parent.getAttribute("id"), tagName));
        }
        return elements.item(0).getTextContent().trim();
    }
    
    /**
     * Evaluation suite.
     *
     * @param standardReference standard reference
     * @param operationMode operation mode
     * @param requiresExternalModel whether an external model is required
     * @param cases evaluation cases
     */
    public record EvaluationSuite(String standardReference, String operationMode, boolean requiresExternalModel, List<EvaluationCase> cases) {
    }
    
    /**
     * Evaluation case.
     *
     * @param id case ID
     * @param category category
     * @param readOnly whether the case is read-only
     * @param question question
     * @param answer expected answer
     */
    public record EvaluationCase(String id, String category, boolean readOnly, String question, String answer) {
    }
}

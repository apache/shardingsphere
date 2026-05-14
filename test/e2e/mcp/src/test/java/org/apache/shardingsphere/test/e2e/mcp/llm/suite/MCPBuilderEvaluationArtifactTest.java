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

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPBuilderEvaluationArtifactTest {
    
    @Test
    void assertMCPBuilderEvaluationArtifact() throws Exception {
        Document document = loadDocument();
        Element root = document.getDocumentElement();
        assertThat(root.getTagName(), is("mcp_builder_evaluation"));
        assertThat(root.getAttribute("operation_mode"), is("read_only"));
        assertThat(root.getAttribute("requires_external_model"), is("false"));
        NodeList qaPairs = root.getElementsByTagName("qa_pair");
        assertThat(qaPairs.getLength(), is(10));
        assertEvaluationQuestions(qaPairs);
    }
    
    private void assertEvaluationQuestions(final NodeList qaPairs) {
        Set<String> categories = new LinkedHashSet<>();
        Set<String> questions = new LinkedHashSet<>();
        for (int i = 0; i < qaPairs.getLength(); i++) {
            Element each = (Element) qaPairs.item(i);
            categories.add(each.getAttribute("category"));
            assertThat(each.getAttribute("read_only"), is("true"));
            assertTrue(questions.add(readElementText(each, "question")));
            assertFalse(readElementText(each, "expected_answer").isBlank());
            assertFalse(readElementText(each, "verification").isBlank());
        }
        assertTrue(categories.containsAll(Set.of("protocol_discovery", "metadata", "read_only_sql", "workflow", "encrypt", "mask", "authorization")));
    }
    
    private Document loadDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (InputStream inputStream = MCPBuilderEvaluationArtifactTest.class.getResourceAsStream("/llm/evaluation/mcp-builder-evaluation.xml")) {
            return factory.newDocumentBuilder().parse(inputStream);
        }
    }
    
    private String readElementText(final Element parent, final String tagName) {
        return parent.getElementsByTagName(tagName).item(0).getTextContent().trim();
    }
}

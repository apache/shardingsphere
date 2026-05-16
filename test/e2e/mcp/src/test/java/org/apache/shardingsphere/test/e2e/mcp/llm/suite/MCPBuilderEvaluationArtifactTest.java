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
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MCPBuilderEvaluationArtifactTest {
    
    private static final int EXPECTED_QA_PAIR_COUNT = 10;
    
    private static final int MINIMUM_QUESTION_WORDS = 24;
    
    private static final int MINIMUM_EXPECTED_ANSWER_WORDS = 45;
    
    private static final int MINIMUM_VERIFICATION_STEPS = 3;
    
    private static final int MINIMUM_STEP_WORDS = 7;
    
    private static final int MINIMUM_EVIDENCE_TERMS = 3;
    
    private static final Set<String> REQUIRED_CATEGORIES = Set.of("protocol_discovery", "metadata", "read_only_sql", "workflow", "encrypt", "mask", "authorization");
    
    private static final List<String> SHALLOW_QUESTION_PHRASES = List.of(
            "Which official MCP method",
            "Which ShardingSphere resource",
            "Which tool",
            "Which planning tool",
            "Which execution_mode",
            "Exact MCP method",
            "Exact tool name",
            "Exact resource URI");
    
    private static final List<String> EVIDENCE_TERMS = List.of(
            "tools/list",
            "resources/templates/list",
            "resources/read",
            "shardingsphere://",
            "database_gateway_",
            "execution_mode",
            "plan_id",
            "next_actions",
            "WWW-Authenticate",
            "resource_metadata",
            "OAuth",
            "authorization",
            "metadata",
            "approval",
            "read-only");
    
    @Test
    void assertMCPBuilderEvaluationArtifact() throws Exception {
        Document document = loadDocument();
        Element root = document.getDocumentElement();
        assertThat(root.getTagName(), is("mcp_builder_evaluation"));
        assertThat(root.getAttribute("operation_mode"), is("read_only"));
        assertThat(root.getAttribute("requires_external_model"), is("false"));
        NodeList qaPairs = root.getElementsByTagName("qa_pair");
        assertThat(qaPairs.getLength(), is(EXPECTED_QA_PAIR_COUNT));
        assertEvaluationQuestions(qaPairs);
    }
    
    private void assertEvaluationQuestions(final NodeList qaPairs) {
        Set<String> categories = new LinkedHashSet<>();
        Set<String> ids = new LinkedHashSet<>();
        Set<String> questions = new LinkedHashSet<>();
        for (int i = 0; i < qaPairs.getLength(); i++) {
            Element each = (Element) qaPairs.item(i);
            assertQAPair(each, ids, categories, questions);
        }
        assertTrue(categories.containsAll(REQUIRED_CATEGORIES));
    }
    
    private void assertQAPair(final Element qaPair, final Set<String> ids, final Set<String> categories, final Set<String> questions) {
        assertTrue(ids.add(qaPair.getAttribute("id")));
        categories.add(qaPair.getAttribute("category"));
        assertThat(qaPair.getAttribute("read_only"), is("true"));
        String question = readElementText(qaPair, "question");
        String expectedAnswer = readElementText(qaPair, "expected_answer");
        assertQuestion(question, questions);
        assertExpectedAnswer(expectedAnswer);
        assertVerification(qaPair, question, expectedAnswer);
    }
    
    private void assertQuestion(final String question, final Set<String> questions) {
        assertTrue(questions.add(question));
        assertTrue(countWords(question) >= MINIMUM_QUESTION_WORDS, () -> "Question is too shallow: " + question);
        for (String each : SHALLOW_QUESTION_PHRASES) {
            assertFalse(question.contains(each), () -> "Question must not ask for shallow exact names: " + question);
        }
    }
    
    private void assertExpectedAnswer(final String expectedAnswer) {
        assertTrue(countWords(expectedAnswer) >= MINIMUM_EXPECTED_ANSWER_WORDS, () -> "Expected answer is too shallow: " + expectedAnswer);
        assertFalse(isSingleTokenAnswer(expectedAnswer), () -> "Expected answer must be a reviewable explanation: " + expectedAnswer);
        assertTrue(expectedAnswer.contains("."), () -> "Expected answer must contain narrative evidence: " + expectedAnswer);
    }
    
    private void assertVerification(final Element qaPair, final String question, final String expectedAnswer) {
        Element verification = readSingleElement(qaPair, "verification");
        NodeList steps = verification.getElementsByTagName("step");
        assertTrue(steps.getLength() >= MINIMUM_VERIFICATION_STEPS, "Verification must contain multiple review steps.");
        Set<String> stepTexts = new LinkedHashSet<>();
        for (int i = 0; i < steps.getLength(); i++) {
            String stepText = steps.item(i).getTextContent().trim();
            assertTrue(stepTexts.add(stepText), () -> "Duplicate verification step: " + stepText);
            assertTrue(countWords(stepText) >= MINIMUM_STEP_WORDS, () -> "Verification step is too shallow: " + stepText);
        }
        assertTrue(countEvidenceTerms(question + " " + expectedAnswer + " " + verification.getTextContent()) >= MINIMUM_EVIDENCE_TERMS,
                "Evaluation question must contain multiple protocol or ShardingSphere evidence terms.");
    }
    
    private int countWords(final String value) {
        return value.trim().split("\\s+").length;
    }
    
    private boolean isSingleTokenAnswer(final String value) {
        return !value.trim().contains(" ");
    }
    
    private int countEvidenceTerms(final String value) {
        int result = 0;
        for (String each : EVIDENCE_TERMS) {
            if (value.contains(each)) {
                result++;
            }
        }
        return result;
    }
    
    private Document loadDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (InputStream inputStream = MCPBuilderEvaluationArtifactTest.class.getResourceAsStream("/llm/evaluation/mcp-builder-evaluation.xml")) {
            assertNotNull(inputStream);
            return factory.newDocumentBuilder().parse(inputStream);
        }
    }
    
    private String readElementText(final Element parent, final String tagName) {
        return readSingleElement(parent, tagName).getTextContent().trim();
    }
    
    private Element readSingleElement(final Element parent, final String tagName) {
        NodeList elements = parent.getElementsByTagName(tagName);
        assertThat(elements.getLength(), is(1));
        return (Element) elements.item(0);
    }
}

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
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MCPBuilderEvaluationArtifactTest {
    
    private static final int EXPECTED_QA_PAIR_COUNT = 10;
    
    private static final int MINIMUM_QUESTION_WORDS = 24;
    
    private static final int MAXIMUM_ANSWER_KEY_LENGTH = 96;
    
    private static final int MINIMUM_EXPECTED_ANSWER_WORDS = 45;
    
    private static final int MINIMUM_VERIFICATION_STEPS = 3;
    
    private static final int MINIMUM_STEP_WORDS = 7;
    
    private static final int MINIMUM_EVIDENCE_TERMS = 3;
    
    private static final Set<String> REQUIRED_CATEGORIES = Set.of("protocol_discovery", "metadata", "read_only_sql", "workflow", "encrypt", "mask", "transport_security");
    
    private static final List<String> SHALLOW_QUESTION_PHRASES = List.of(
            "Which official MCP method",
            "Which ShardingSphere resource",
            "Which tool",
            "Which planning tool",
            "Which execution_mode",
            "Exact MCP method",
            "Exact tool name",
            "Exact resource URI");
    
    private static final List<String> DESTRUCTIVE_QUESTION_PHRASES = List.of(
            "apply the workflow in execute mode",
            "approve the execution",
            "change runtime metadata",
            "run an update statement",
            "execute the mutation");
    
    private static final List<String> EVIDENCE_TERMS = List.of(
            "tools/list",
            "resources/templates/list",
            "resources/read",
            "shardingsphere://",
            "database_gateway_",
            "execution_mode",
            "plan_id",
            "next_actions",
            "transport.type",
            "HTTP",
            "STDIO",
            "Origin",
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
        String content = root.getTextContent();
        for (String each : Set.of(
                "database_gateway_plan_encrypt_rule",
                "database_gateway_plan_mask_rule",
                "database_gateway_plan_broadcast_rule",
                "database_gateway_plan_readwrite_splitting_rule",
                "database_gateway_plan_shadow_rule",
                "database_gateway_plan_sharding_table_rule",
                "completion/complete")) {
            assertTrue(content.contains(each), () -> "Evaluation artifact must cover " + each);
        }
    }
    
    @Test
    void assertRejectsShallowEvaluationQuestion() {
        String xml = """
                <qa_pair id="q01" category="metadata" read_only="true">
                  <question>Which tool lists metadata?</question>
                  <answer>database_gateway_search_metadata</answer>
                  <expected_answer>
                    This answer is intentionally long enough to prove the shallow question is what fails during validation. It mentions
                    tools/list, resources/read, and metadata evidence so that evidence counting cannot be the reason this fixture is rejected.
                  </expected_answer>
                  <verification>
                    <step>Confirm the tool name is copied directly without workflow reasoning.</step>
                    <step>Confirm the question is shorter than the minimum complex question length.</step>
                    <step>Confirm the artifact validator rejects shallow exact-name prompts.</step>
                  </verification>
                </qa_pair>""";
        assertThrows(AssertionError.class, () -> assertQAPair(
                loadQAPair(xml), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>()));
    }
    
    @Test
    void assertRejectsDestructiveEvaluationQuestion() {
        String xml = """
                <qa_pair id="q01" category="workflow" read_only="true">
                  <question>
                    A user wants the assistant to apply the workflow in execute mode against a runtime database before reviewing preview
                    artifacts, then report the changed encrypt or mask rule as complete.
                  </question>
                  <answer>apply_workflow_execute</answer>
                  <expected_answer>
                    This destructive fixture should never be accepted for mcp-builder scoring. It mentions tools/list, resources/read, and
                    approval evidence, but the question still asks the model to perform a side-effecting workflow rather than stay read-only.
                  </expected_answer>
                  <verification>
                    <step>Confirm the question asks for a side-effecting workflow execution path.</step>
                    <step>Confirm read-only scoring cannot include mutation or forged approval.</step>
                    <step>Confirm destructive evaluation intent is rejected before model execution.</step>
                  </verification>
                </qa_pair>""";
        assertThrows(AssertionError.class, () -> assertQAPair(
                loadQAPair(xml), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>()));
    }
    
    @Test
    void assertRejectsUnsafeUpdateReviewQuestion() {
        String xml = """
                <qa_pair id="q01" category="read_only_sql" read_only="true">
                  <question>
                    A reviewer wants the assistant to explain a pending SQL change and compare visible row counts afterward while staying inside
                    a read-only MCP evaluation contract.
                  </question>
                  <answer>execute_update_review|execute_query_verify|no_user_approval</answer>
                  <expected_answer>
                    The answer names database_gateway_execute_update and database_gateway_execute_query, but it leaves the side-effecting SQL
                    review mode ambiguous. That makes the read-only evaluation unsafe because the model could interpret the update review as a
                    real write instead of a bounded preview, even while it still mentions metadata and approval evidence.
                  </expected_answer>
                  <verification>
                    <step>Confirm the response mentions the side-effecting SQL review tool.</step>
                    <step>Confirm the response also performs independent read-only verification.</step>
                    <step>Confirm missing preview wording makes the read-only contract unsafe.</step>
                  </verification>
                </qa_pair>""";
        assertThrows(AssertionError.class, () -> assertQAPair(
                loadQAPair(xml), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>()));
    }
    
    @Test
    void assertRejectsUnverifiableEvaluationAnswer() {
        String xml = """
                <qa_pair id="q01" category="metadata" read_only="true">
                  <question>
                    A reviewer asks for a stable metadata evidence path that combines tool discovery, resource templates, and exact
                    ShardingSphere resource reads before any SQL is attempted.
                  </question>
                  <answer>it depends on what the model decides after reading the available resources</answer>
                  <expected_answer>
                    The narrative explanation is long enough to avoid the shallow-answer check. It cites tools/list, resources/templates/list,
                    resources/read, metadata, and read-only evidence, but the answer key is not stable enough for string comparison.
                  </expected_answer>
                  <verification>
                    <step>Confirm the answer key is a sentence instead of a canonical string.</step>
                    <step>Confirm the expected answer narrative still contains enough evidence terms.</step>
                    <step>Confirm unverifiable answer keys fail artifact validation.</step>
                  </verification>
                </qa_pair>""";
        assertThrows(AssertionError.class, () -> assertQAPair(
                loadQAPair(xml), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>()));
    }
    
    private void assertEvaluationQuestions(final NodeList qaPairs) {
        Set<String> categories = new LinkedHashSet<>();
        Set<String> ids = new LinkedHashSet<>();
        Set<String> questions = new LinkedHashSet<>();
        Set<String> answers = new LinkedHashSet<>();
        for (int i = 0; i < qaPairs.getLength(); i++) {
            Element each = (Element) qaPairs.item(i);
            assertQAPair(each, ids, categories, questions, answers);
        }
        assertTrue(categories.containsAll(REQUIRED_CATEGORIES));
    }
    
    private void assertQAPair(final Element qaPair, final Set<String> ids, final Set<String> categories, final Set<String> questions, final Set<String> answers) {
        assertTrue(ids.add(qaPair.getAttribute("id")));
        categories.add(qaPair.getAttribute("category"));
        assertThat(qaPair.getAttribute("read_only"), is("true"));
        String question = readElementText(qaPair, "question");
        String answer = readElementText(qaPair, "answer");
        String expectedAnswer = readElementText(qaPair, "expected_answer");
        assertQuestion(question, questions);
        assertAnswer(answer, answers);
        assertExpectedAnswer(expectedAnswer);
        assertVerification(qaPair, question, answer, expectedAnswer);
        assertReadOnlyPreviewBoundary(qaPair, question, answer, expectedAnswer);
    }
    
    private void assertQuestion(final String question, final Set<String> questions) {
        assertTrue(questions.add(question));
        assertTrue(countWords(question) >= MINIMUM_QUESTION_WORDS, () -> "Question is too shallow: " + question);
        for (String each : SHALLOW_QUESTION_PHRASES) {
            assertFalse(question.contains(each), () -> "Question must not ask for shallow exact names: " + question);
        }
        String lowerQuestion = question.toLowerCase();
        for (String each : DESTRUCTIVE_QUESTION_PHRASES) {
            assertFalse(lowerQuestion.contains(each), () -> "Question must not require side effects: " + question);
        }
    }
    
    private void assertAnswer(final String answer, final Set<String> answers) {
        assertTrue(answers.add(answer));
        assertTrue(answer.length() <= MAXIMUM_ANSWER_KEY_LENGTH, () -> "Answer key is too long for string comparison: " + answer);
        assertTrue(answer.matches("[a-z0-9_:/|.\\-]+"), () -> "Answer key must be canonical and string-comparable: " + answer);
        assertTrue(answer.contains("|"), () -> "Answer key must encode the required evidence sequence: " + answer);
    }
    
    private void assertExpectedAnswer(final String expectedAnswer) {
        assertTrue(countWords(expectedAnswer) >= MINIMUM_EXPECTED_ANSWER_WORDS, () -> "Expected answer is too shallow: " + expectedAnswer);
        assertFalse(isSingleTokenAnswer(expectedAnswer), () -> "Expected answer must be a reviewable explanation: " + expectedAnswer);
        assertTrue(expectedAnswer.contains("."), () -> "Expected answer must contain narrative evidence: " + expectedAnswer);
    }
    
    private void assertVerification(final Element qaPair, final String question, final String answer, final String expectedAnswer) {
        Element verification = readSingleElement(qaPair, "verification");
        NodeList steps = verification.getElementsByTagName("step");
        assertTrue(steps.getLength() >= MINIMUM_VERIFICATION_STEPS, "Verification must contain multiple review steps.");
        Set<String> stepTexts = new LinkedHashSet<>();
        for (int i = 0; i < steps.getLength(); i++) {
            String stepText = steps.item(i).getTextContent().trim();
            assertTrue(stepTexts.add(stepText), () -> "Duplicate verification step: " + stepText);
            assertTrue(countWords(stepText) >= MINIMUM_STEP_WORDS, () -> "Verification step is too shallow: " + stepText);
        }
        assertTrue(countEvidenceTerms(question + " " + answer + " " + expectedAnswer + " " + verification.getTextContent()) >= MINIMUM_EVIDENCE_TERMS,
                "Evaluation question must contain multiple protocol or ShardingSphere evidence terms.");
    }
    
    private void assertReadOnlyPreviewBoundary(final Element qaPair, final String question, final String answer, final String expectedAnswer) {
        if (!"true".equals(qaPair.getAttribute("read_only"))) {
            return;
        }
        String content = question + " " + answer + " " + expectedAnswer + " " + readSingleElement(qaPair, "verification").getTextContent();
        if (!answer.contains("execute_update") || !content.contains("database_gateway_execute_update")) {
            return;
        }
        assertTrue(content.contains("execution_mode set to preview") || content.contains("execution_mode=preview") || content.contains("execution_mode preview"),
                () -> "Read-only update review must require preview mode: " + question);
        assertTrue(content.contains("must not switch to execution_mode execute") || content.contains("forbids execute mode") || content.contains("no_execute_without_approval"),
                () -> "Read-only update review must forbid execute mode: " + question);
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
    
    private Document loadDocument() throws ParserConfigurationException, SAXException, IOException {
        try (InputStream inputStream = MCPBuilderEvaluationArtifactTest.class.getResourceAsStream("/llm/evaluation/mcp-builder-evaluation.xml")) {
            assertNotNull(inputStream);
            return parseDocument(inputStream);
        }
    }
    
    private Element loadQAPair(final String xml) throws ParserConfigurationException, SAXException, IOException {
        return parseDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
    }
    
    private Document parseDocument(final InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(inputStream);
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

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
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MCPBuilderEvaluationArtifactTest {
    
    private static final int EXPECTED_QA_PAIR_COUNT = 10;
    
    private static final int MAXIMUM_ANSWER_KEY_LENGTH = 96;
    
    private static final Set<String> REQUIRED_CATEGORIES = Set.of("aggregation", "data_lookup", "relationship", "metadata", "view", "index");
    
    private static final List<String> DESTRUCTIVE_QUESTION_PHRASES = List.of(
            "insert ", "update ", "delete ", "alter ", "drop ", "create ", "truncate ", "apply workflow", "execute mode");
    
    @Test
    void assertMCPBuilderEvaluationArtifact() throws IOException {
        MCPBuilderEvaluationCatalog.EvaluationSuite actual = new MCPBuilderEvaluationCatalog().load();
        assertThat(actual.standardReference(), is("MCP 2025-11-25"));
        assertThat(actual.operationMode(), is("read_only"));
        assertTrue(actual.requiresExternalModel());
        assertThat(actual.cases().size(), is(EXPECTED_QA_PAIR_COUNT));
        assertEvaluationCases(actual.cases());
    }
    
    private void assertEvaluationCases(final List<MCPBuilderEvaluationCatalog.EvaluationCase> evaluationCases) {
        Set<String> categories = new LinkedHashSet<>();
        Set<String> ids = new LinkedHashSet<>();
        Set<String> questions = new LinkedHashSet<>();
        for (MCPBuilderEvaluationCatalog.EvaluationCase each : evaluationCases) {
            assertFalse(each.id().isBlank(), "Evaluation ID must not be blank.");
            assertTrue(ids.add(each.id()), () -> "Duplicate evaluation ID: " + each.id());
            categories.add(each.category());
            assertTrue(each.readOnly(), () -> "Evaluation case must be read-only: " + each.id());
            assertTrue(questions.add(each.question()), () -> "Duplicate evaluation question: " + each.id());
            assertFalse(each.question().isBlank(), () -> "Evaluation question is blank: " + each.id());
            assertFalse(each.answer().isBlank(), () -> "Evaluation answer is blank: " + each.id());
            assertTrue(each.answer().length() <= MAXIMUM_ANSWER_KEY_LENGTH, () -> "Evaluation answer is too long: " + each.id());
            assertReadOnlyQuestion(each);
        }
        assertThat(categories, is(REQUIRED_CATEGORIES));
    }
    
    private void assertReadOnlyQuestion(final MCPBuilderEvaluationCatalog.EvaluationCase evaluationCase) {
        String lowerQuestion = evaluationCase.question().toLowerCase(Locale.ENGLISH);
        for (String each : DESTRUCTIVE_QUESTION_PHRASES) {
            assertFalse(lowerQuestion.contains(each), () -> "Evaluation case must not request side effects: " + evaluationCase.id());
        }
    }
}

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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability;

import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationExecutor;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment.LLMUsabilityMetricCalculator;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment.LLMUsabilityReportWriter;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment.LLMUsabilityScenarioResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment.LLMUsabilityScorecard;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPModelContractAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LLMUsabilitySuiteRunner {
    
    private static final Set<String> INFRASTRUCTURE_FAILURE_TYPES = Set.of("model_service_unavailable", "mcp_runtime_unavailable");
    
    private static final Set<String> KNOWN_ACTION_ORIGINS = Set.of(
            MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN,
            MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN,
            MCPInteractionTraceRecord.HARNESS_TEXT_RECOVERY_ORIGIN);
    
    private static final Pattern UNREDACTED_SECRET_PATTERN = Pattern.compile(
            "(?i)(\"(?:api[_-]?key|token|password|authorization|secret)\"\\s*:\\s*\")(?!<redacted>\")([^\"]+)(\")|(Bearer\\s+)(?!<redacted>)[A-Za-z0-9._~+/=-]+");
    
    private final LLMUsabilityMetricCalculator metricCalculator = new LLMUsabilityMetricCalculator();
    
    private final LLMUsabilityReportWriter reportWriter = new LLMUsabilityReportWriter();
    
    void assertCoreSuite(final String suiteId, final Supplier<List<LLMUsabilityScenario>> scenarioSupplier,
                         final ConversationRunner conversationRunner, final LLME2EConfiguration configuration) throws IOException {
        EvaluatedSuite evaluatedSuite = evaluateSuite(suiteId, scenarioSupplier, conversationRunner, configuration);
        assertFullScore(evaluatedSuite.scorecard(), evaluatedSuite.scenarios());
        assertDeterministicContract(evaluatedSuite);
    }
    
    void assertExtendedSuite(final String suiteId, final Supplier<List<LLMUsabilityScenario>> scenarioSupplier,
                             final ConversationRunner conversationRunner, final LLME2EConfiguration configuration) throws IOException {
        EvaluatedSuite evaluatedSuite = evaluateSuite(suiteId, scenarioSupplier, conversationRunner, configuration);
        assertFullScore(evaluatedSuite.scorecard(), evaluatedSuite.scenarios());
        assertDeterministicContract(evaluatedSuite);
    }
    
    private EvaluatedSuite evaluateSuite(final String suiteId, final Supplier<List<LLMUsabilityScenario>> scenarioSupplier,
                                         final ConversationRunner conversationRunner, final LLME2EConfiguration configuration) throws IOException {
        List<LLMUsabilityScenario> scenarios = scenarioSupplier.get();
        assertValidScenarioDefinitions(suiteId, scenarios);
        List<EvaluatedScenario> evaluatedScenarios = new LinkedList<>();
        for (LLMUsabilityScenario each : scenarios) {
            LLMConversationExecutor.ConversationResult conversationResult = conversationRunner.run(each.getLlmScenario());
            evaluatedScenarios.add(new EvaluatedScenario(each, metricCalculator.evaluateScenario(each, conversationResult.artifactBundle()),
                    conversationResult.artifactDirectory()));
        }
        List<LLMUsabilityScenarioResult> scenarioResults = evaluatedScenarios.stream().map(EvaluatedScenario::scenarioResult).toList();
        LLMUsabilityScorecard scorecard = metricCalculator.createScorecard(suiteId, configuration.getRunId(), scenarioResults);
        Path suiteDirectory = configuration.getArtifactRoot().resolve(configuration.getRunId()).resolve(suiteId);
        reportWriter.writeScorecard(suiteDirectory, scorecard);
        return new EvaluatedSuite(scenarios, evaluatedScenarios, scorecard);
    }
    
    private void assertFullScore(final LLMUsabilityScorecard scorecard, final List<LLMUsabilityScenario> scenarios) {
        String actualFailureSummary = createFailureSummary(scorecard);
        assertThat(actualFailureSummary, scorecard.getOverallScore(), is(100.0D));
        assertTrue(scorecard.isFullScore(), actualFailureSummary);
        assertThat(actualFailureSummary, scorecard.getScenarioResults().size(), is(scenarios.size()));
        assertThat(actualFailureSummary, scorecard.getTaskSuccessRate(), is(1.0D));
        assertThat(actualFailureSummary, scorecard.getNaturalTaskSuccessRate(), is(1.0D));
        assertThat(actualFailureSummary, scorecard.getProtocolContractSuccessRate(), is(1.0D));
        assertThat(actualFailureSummary, scorecard.getFirstCorrectActionRate(), is(1.0D));
        assertThat(actualFailureSummary, scorecard.getInvalidCallRate(), is(0.0D));
        assertThat(actualFailureSummary, scorecard.getQueryAnswerFidelity(), is(1.0D));
        assertThat(actualFailureSummary, scorecard.getBoundaryConfusionRate(), is(0.0D));
        assertThat(actualFailureSummary, scorecard.getNextActionFollowRate(), is(1.0D));
        assertThat(actualFailureSummary, scorecard.getApprovalViolationRate(), is(0.0D));
        assertThat(actualFailureSummary, scorecard.getNativeToolCallRate(), is(1.0D));
        assertThat(actualFailureSummary, scorecard.getHarnessRecoveryRate(), is(0.0D));
        if (hasResourceHitExpectation(scenarios)) {
            assertThat(actualFailureSummary, scorecard.getResourceHitRate(), is(1.0D));
        }
        if (hasRecoveryExpectation(scenarios)) {
            assertThat(actualFailureSummary, scorecard.getRecoveryRate(), is(1.0D));
        }
    }
    
    private void assertDeterministicContract(final EvaluatedSuite evaluatedSuite) throws IOException {
        LLMUsabilityScorecard scorecard = evaluatedSuite.scorecard();
        assertThat(scorecard.getScenarioResults().size(), is(evaluatedSuite.scenarios().size()));
        assertScorecardShape(scorecard);
        for (EvaluatedScenario each : evaluatedSuite.evaluatedScenarios()) {
            assertScenarioInfrastructure(each);
            assertArtifactDirectory(each.artifactDirectory());
            assertNoSecretLeak(each.artifactDirectory());
            assertTraceShape(each);
        }
    }
    
    private void assertValidScenarioDefinitions(final String suiteId, final List<LLMUsabilityScenario> scenarios) {
        assertFalse(scenarios.isEmpty(), suiteId + " must contain at least one LLM scenario.");
        Set<String> scenarioIds = new LinkedHashSet<>();
        for (LLMUsabilityScenario each : scenarios) {
            assertTrue(scenarioIds.add(each.getScenarioId()), "Duplicate LLM usability scenario id: " + each.getScenarioId());
            assertFalse(each.getTags().isEmpty(), "LLM usability scenario must declare at least one tag: " + each.getScenarioId());
            assertFalse(each.getLlmScenario().getUserPrompt().contains("First call"), "LLM usability scenario must not script the first call: " + each.getScenarioId());
            assertScenarioClassification(each);
        }
    }
    
    private void assertScenarioClassification(final LLMUsabilityScenario scenario) {
        assertTrue(scenario.isNaturalTask() || scenario.isProtocolContract(), "LLM usability scenario must declare a task classification: " + scenario.getScenarioId());
        assertFalse(scenario.isNaturalTask() && scenario.isProtocolContract(), "LLM usability scenario must not mix task classifications: " + scenario.getScenarioId());
        if (scenario.isNaturalTask()) {
            assertNaturalTaskPrompt(scenario);
        }
    }
    
    private void assertNaturalTaskPrompt(final LLMUsabilityScenario scenario) {
        String prompt = scenario.getLlmScenario().getUserPrompt();
        for (String each : List.of("Use the MCP prompt list", "Read exact runtime resource", "read exact runtime resource", "Before any metadata search", "Do not validate the workflow")) {
            assertFalse(prompt.contains(each), "Natural LLM task must not use protocol-contract scripting phrase `" + each + "`: " + scenario.getScenarioId());
        }
    }
    
    private void assertScorecardShape(final LLMUsabilityScorecard scorecard) {
        assertRange("overallScore", scorecard.getOverallScore(), 0.0D, 100.0D);
        assertRate("taskSuccessRate", scorecard.getTaskSuccessRate());
        assertRate("naturalTaskSuccessRate", scorecard.getNaturalTaskSuccessRate());
        assertRate("protocolContractSuccessRate", scorecard.getProtocolContractSuccessRate());
        assertRate("firstCorrectActionRate", scorecard.getFirstCorrectActionRate());
        assertRate("invalidCallRate", scorecard.getInvalidCallRate());
        assertRange("averageRoundTrips", scorecard.getAverageRoundTrips(), 0.0D, Double.MAX_VALUE);
        assertRate("queryAnswerFidelity", scorecard.getQueryAnswerFidelity());
        assertRate("boundaryConfusionRate", scorecard.getBoundaryConfusionRate());
        assertRate("resourceHitRate", scorecard.getResourceHitRate());
        assertRate("recoveryRate", scorecard.getRecoveryRate());
        assertRate("nextActionFollowRate", scorecard.getNextActionFollowRate());
        assertRate("approvalViolationRate", scorecard.getApprovalViolationRate());
        assertRate("nativeToolCallRate", scorecard.getNativeToolCallRate());
        assertRate("harnessRecoveryRate", scorecard.getHarnessRecoveryRate());
    }
    
    private void assertRate(final String name, final double value) {
        assertRange(name, value, 0.0D, 1.0D);
    }
    
    private void assertRange(final String name, final double value, final double minimum, final double maximum) {
        assertTrue(value >= minimum && value <= maximum, () -> String.format("%s out of range: %s", name, value));
    }
    
    private void assertScenarioInfrastructure(final EvaluatedScenario evaluatedScenario) {
        String failureType = evaluatedScenario.scenarioResult().getFailureType();
        assertFalse(INFRASTRUCTURE_FAILURE_TYPES.contains(failureType),
                () -> evaluatedScenario.scenario().getScenarioId() + " failed deterministic infrastructure setup: " + failureType);
        assertFalse(evaluatedScenario.scenarioResult().isApprovalViolation(), () -> evaluatedScenario.scenario().getScenarioId() + " attempted an unapproved side-effect path.");
    }
    
    private void assertArtifactDirectory(final Path artifactDirectory) {
        assertTrue(Files.isDirectory(artifactDirectory), () -> "Missing artifact directory: " + artifactDirectory);
        for (String each : List.of("run-context.json", "system-prompt.md", "user-prompt.md", "raw-model-output.txt", "interaction-trace.json", "assertion-report.json", "mcp-runtime.log")) {
            assertTrue(Files.isRegularFile(artifactDirectory.resolve(each)), () -> "Missing LLM artifact: " + artifactDirectory.resolve(each));
        }
    }
    
    private void assertNoSecretLeak(final Path artifactDirectory) throws IOException {
        try (Stream<Path> paths = Files.walk(artifactDirectory)) {
            for (Path each : paths.filter(Files::isRegularFile).toList()) {
                String content = Files.readString(each);
                assertFalse(UNREDACTED_SECRET_PATTERN.matcher(content).find(), () -> "Unredacted secret-like value in LLM artifact: " + each);
            }
        }
    }
    
    private void assertTraceShape(final EvaluatedScenario evaluatedScenario) {
        for (MCPInteractionTraceRecord each : evaluatedScenario.scenarioResult().getInteractionTrace()) {
            assertTrue(0 < each.getSequence(), () -> "Trace sequence must be positive in " + evaluatedScenario.scenario().getScenarioId());
            assertFalse(each.getActionKind().isBlank(), () -> "Trace action kind is blank in " + evaluatedScenario.scenario().getScenarioId());
            assertFalse(each.getActionOrigin().isBlank(), () -> "Trace action origin is blank in " + evaluatedScenario.scenario().getScenarioId());
            assertTrue(KNOWN_ACTION_ORIGINS.contains(each.getActionOrigin()), () -> "Unknown trace action origin in " + evaluatedScenario.scenario().getScenarioId());
            assertFalse(each.getTargetName().isBlank(), () -> "Trace target name is blank in " + evaluatedScenario.scenario().getScenarioId());
            MCPModelContractAssertions.assertCanonicalNextActionLists(each.getStructuredContent());
        }
    }
    
    private boolean hasResourceHitExpectation(final List<LLMUsabilityScenario> scenarios) {
        for (LLMUsabilityScenario each : scenarios) {
            if (each.isResourceHitRequired()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasRecoveryExpectation(final List<LLMUsabilityScenario> scenarios) {
        for (LLMUsabilityScenario each : scenarios) {
            if (each.isRecoveryExpected()) {
                return true;
            }
        }
        return false;
    }
    
    private String createFailureSummary(final LLMUsabilityScorecard scorecard) {
        StringBuilder result = new StringBuilder("LLM usability suite did not meet the baseline.");
        result.append(" overallScore=").append(scorecard.getOverallScore());
        result.append(", fullScore=").append(scorecard.isFullScore());
        result.append(" taskSuccessRate=").append(scorecard.getTaskSuccessRate());
        result.append(", naturalTaskSuccessRate=").append(scorecard.getNaturalTaskSuccessRate());
        result.append(", protocolContractSuccessRate=").append(scorecard.getProtocolContractSuccessRate());
        result.append(", resourceHitRate=").append(scorecard.getResourceHitRate());
        result.append(", recoveryRate=").append(scorecard.getRecoveryRate());
        result.append(", nativeToolCallRate=").append(scorecard.getNativeToolCallRate());
        result.append(", harnessRecoveryRate=").append(scorecard.getHarnessRecoveryRate());
        for (LLMUsabilityScenarioResult each : scorecard.getScenarioResults()) {
            if (each.isSuccess()) {
                continue;
            }
            result.append(" [");
            result.append(each.getScenarioId());
            result.append(": ");
            result.append(each.getFailureType());
            result.append(" - ");
            result.append(each.getMessage());
            result.append(']');
        }
        return result.toString();
    }
    
    @FunctionalInterface
    interface ConversationRunner {
        
        LLMConversationExecutor.ConversationResult run(LLME2EScenario scenario) throws IOException;
    }
    
    private record EvaluatedSuite(List<LLMUsabilityScenario> scenarios, List<EvaluatedScenario> evaluatedScenarios, LLMUsabilityScorecard scorecard) {
    }
    
    private record EvaluatedScenario(LLMUsabilityScenario scenario, LLMUsabilityScenarioResult scenarioResult, Path artifactDirectory) {
    }
}

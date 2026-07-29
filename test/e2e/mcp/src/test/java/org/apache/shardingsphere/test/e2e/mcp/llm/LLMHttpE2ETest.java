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

package org.apache.shardingsphere.test.e2e.mcp.llm;

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousLLMConversationRunner;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousLLMConversationRunner.Result;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousLLMConversationRunner.Scenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationExecutor;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationExecutor.ConversationResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMMCPNextActions;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLMAutonomousArtifactWriter;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory.Fixture;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeSupport;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPModelContractAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("llm-e2e")
@EnabledIf("org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition#isDockerEnabled")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LLMHttpE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final int MAX_AUTONOMOUS_TURNS = 8;
    
    private static final String DATABASE_NAME = "logic_db";
    
    private static final String TABLE_NAME = "orders";
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private static final String SYSTEM_PROMPT = """
            You are evaluating an MCP server.
            Use MCP resources, metadata search, SQL tools, prompts, completions, and workflow tools only when they help the task.
            Inspect MCP context before touching unknown runtime, metadata, or side-effecting operations.
            Preview side effects and ask the user before execution.
            Do not guess database structure, workflow state, or query results.
            Return JSON only when asked for the final answer.
            """.trim();
    
    private static final Set<String> NATIVE_ACTION_ORIGINS = Set.of(
            MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN,
            MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN);
    
    private static final Pattern UNREDACTED_SECRET_PATTERN = Pattern.compile(
            "(?i)(\"(?:api[_-]?key|token|password|authorization|secret)\"\\s*:\\s*\")(?!<redacted>\")([^\"]+)(\")|(Bearer\\s+)(?!<redacted>)[A-Za-z0-9._~+/=-]+");
    
    private static final List<String> GUIDED_ARTIFACT_FILES = List.of(
            "run-context.json", "system-prompt.md", "user-prompt.md", "raw-model-output.txt", "interaction-trace.json", "assertion-report.json", "mcp-runtime.log");
    
    private static final List<String> AUTONOMOUS_ARTIFACT_FILES = List.of(
            "run-context.json", "system-prompt.md", "question.txt", "expected-answer.txt", "answer.txt", "raw-model-output.txt", "available-tools.json",
            "interaction-trace.json", "mcp-runtime.log", "assertion-report.json");
    
    private static LLMRuntimeSupport.ModelRuntime llmRuntime;
    
    private final LLMRuntimeFixtureFactory runtimeFixtureFactory = new LLMRuntimeFixtureFactory();
    
    private final LLMAutonomousArtifactWriter autonomousArtifactWriter = new LLMAutonomousArtifactWriter();
    
    private Fixture runtimeFixture;
    
    @BeforeAll
    static void prepareLLMRuntime() throws InterruptedException {
        llmRuntime = LLMRuntimeSupport.prepare(LLME2EConfiguration.load());
    }
    
    @AfterAll
    static void closeLLMRuntime() {
        if (null != llmRuntime) {
            llmRuntime.close();
            llmRuntime = null;
        }
    }
    
    @AfterAll
    void closeRuntimeFixture() {
        if (null != runtimeFixture) {
            runtimeFixture.close();
            runtimeFixture = null;
        }
    }
    
    @Test
    void assertAutonomousReadOnlyQuery() throws IOException {
        assertAutonomousScenario(new Scenario(
                "autonomous-read-only-query",
                "How many rows are currently in the orders table of the logic_db runtime database? Inspect the live MCP server and reply only with the integer.",
                "2"));
    }
    
    @Test
    void assertAutonomousMetadataDiscovery() throws IOException {
        assertAutonomousScenario(new Scenario(
                "autonomous-metadata-discovery",
                "List every table or view name currently visible in the logic_db schema. Inspect MCP metadata and reply only with the names in alphabetical order, separated by comma and one space.",
                "active_orders, order_items, orders"));
    }
    
    @Test
    void assertSideEffectPreview() throws IOException {
        prepareRuntimeFixture();
        String previewUpdateSql = "UPDATE orders SET status = status WHERE order_id = -1";
        assertGuidedScenario(new LLME2EScenario(
                "side-effect-preview",
                SYSTEM_PROMPT,
                "A user is considering SQL `" + previewUpdateSql + "`. Review its side-effect scope without changing data before doing any row-count check. "
                        + "After the preview result is available, verify the row count with `" + COUNT_ORDERS_SQL + "`." + createToolContext(),
                createExpectedAnswer(),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_update", "database_gateway_execute_query"),
                List.of("database_gateway_execute_update", "database_gateway_execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_update"), List.of(), "");
    }
    
    @Test
    void assertRuntimeResourceNavigation() throws IOException {
        prepareRuntimeFixture();
        assertGuidedScenario(new LLME2EScenario(
                "runtime-resource-navigation",
                SYSTEM_PROMPT,
                "Read exact runtime resource `shardingsphere://runtime` and exact database list resource `shardingsphere://databases` before answering. "
                        + "Follow any read-only resource next_actions from the runtime response before verifying `" + COUNT_ORDERS_SQL + "`." + createToolContext(),
                createExpectedAnswer(),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query"),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of("shardingsphere://runtime"), "");
    }
    
    @Test
    void assertMissingDatabaseScopeDiscovery() throws IOException {
        prepareRuntimeFixture();
        assertGuidedScenario(new LLME2EScenario(
                "missing-database-scope-discovery",
                SYSTEM_PROMPT,
                "The user only remembers schema `" + getRequiredRuntimeFixture().schemaName() + "` and table `" + TABLE_NAME + "`. Search metadata broadly with query `" + TABLE_NAME
                        + "` and object type `table` without setting database or schema. Then verify `" + COUNT_ORDERS_SQL + "`.",
                createExpectedAnswer(),
                List.of("database_gateway_search_metadata", "database_gateway_execute_query"),
                List.of("database_gateway_search_metadata", "database_gateway_execute_query")),
                List.of("database_gateway_search_metadata"), List.of(), "");
    }
    
    @Test
    void assertInvalidResourceRecovery() throws IOException {
        prepareRuntimeFixture();
        String tableResourceUri = createTableResourceUri();
        assertGuidedScenario(new LLME2EScenario(
                "invalid-resource-recovery",
                SYSTEM_PROMPT,
                "The user pasted stale resource `shardingsphere://databases/unknown/schemas/unknown/tables/" + TABLE_NAME
                        + "`. Read that stale resource before any live resource. After observing its error response, recover by reading exact live table resource `"
                        + tableResourceUri + "`, and verify `" + COUNT_ORDERS_SQL + "`." + createToolContext(),
                createExpectedAnswer(),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query"),
                List.of(MCPInteractionActionNames.READ_RESOURCE, "database_gateway_execute_query")),
                List.of(MCPInteractionActionNames.READ_RESOURCE), List.of(tableResourceUri), "unknown_database");
    }
    
    private void assertAutonomousScenario(final Scenario scenario) throws IOException {
        prepareRuntimeFixture();
        LLMRuntimeSupport.ModelRuntime modelRuntime = getRequiredLLMRuntime();
        assertTrue(Boolean.TRUE.equals(modelRuntime.getEvidence().get("scoreClosing")), "Autonomous LLM E2E requires Docker-owned runtime evidence.");
        Result actualResult = new AutonomousLLMConversationRunner(
                MAX_AUTONOMOUS_TURNS,
                new LLMChatModelClient(modelRuntime.getConfiguration(), HttpClient.newHttpClient()),
                createInteractionClient(),
                modelRuntime.getConfiguration().getModelName()).run(scenario);
        Path artifactDirectory = modelRuntime.getConfiguration().createArtifactDirectory("llm-http/" + scenario.id());
        autonomousArtifactWriter.write(artifactDirectory, actualResult, modelRuntime.getEvidence());
        assertArtifacts(artifactDirectory, AUTONOMOUS_ARTIFACT_FILES);
        assertTrue(actualResult.assertionReport().isSuccess(), () -> createFailureMessage(scenario.id(), actualResult.assertionReport().getFailureType(),
                actualResult.assertionReport().getMessage(), artifactDirectory));
        assertFalse(actualResult.evidence().interactionTrace().isEmpty(), "Autonomous LLM E2E must capture MCP evidence.");
        assertThat(actualResult.evidence().interactionTrace().getFirst().getTargetName(), is(MCPInteractionActionNames.LIST_TOOLS));
        assertTrace(scenario.id(), actualResult.evidence().interactionTrace());
    }
    
    private void assertGuidedScenario(final LLME2EScenario scenario, final Collection<String> expectedFirstActionNames,
                                      final Collection<String> expectedResourceUris, final String expectedRecoveryCategory) throws IOException {
        prepareRuntimeFixture();
        LLMRuntimeSupport.ModelRuntime modelRuntime = getRequiredLLMRuntime();
        ConversationResult actualResult = new LLMConversationExecutor(modelRuntime.getConfiguration(), modelRuntime.getEvidence())
                .runConversation("llm-http/" + scenario.getScenarioId(), scenario, createInteractionClient());
        Path artifactDirectory = actualResult.artifactDirectory();
        assertArtifacts(artifactDirectory, GUIDED_ARTIFACT_FILES);
        LLME2EArtifactBundle artifactBundle = actualResult.artifactBundle();
        assertTrue(artifactBundle.getAssertionReport().isSuccess(), () -> createFailureMessage(scenario.getScenarioId(),
                artifactBundle.getAssertionReport().getFailureType(), artifactBundle.getAssertionReport().getMessage(), artifactDirectory));
        List<MCPInteractionTraceRecord> interactionTrace = artifactBundle.getInteractionTrace();
        assertFalse(interactionTrace.isEmpty(), scenario.getScenarioId() + " must capture MCP interactions.");
        assertTrue(expectedFirstActionNames.contains(interactionTrace.getFirst().getTargetName()),
                () -> scenario.getScenarioId() + " started with unexpected action `" + interactionTrace.getFirst().getTargetName() + "`.");
        assertExpectedResourceHit(scenario.getScenarioId(), expectedResourceUris, interactionTrace);
        assertExpectedRecovery(scenario.getScenarioId(), expectedRecoveryCategory, interactionTrace);
        assertRequiredToolCoverage(scenario, interactionTrace);
        assertNextActionsFollowed(scenario.getScenarioId(), interactionTrace);
        assertTrace(scenario.getScenarioId(), interactionTrace);
    }
    
    private void assertArtifacts(final Path artifactDirectory, final Collection<String> requiredFiles) throws IOException {
        assertTrue(Files.isDirectory(artifactDirectory), () -> "Missing LLM artifact directory: " + artifactDirectory);
        for (String each : requiredFiles) {
            assertTrue(Files.isRegularFile(artifactDirectory.resolve(each)), () -> "Missing LLM artifact: " + artifactDirectory.resolve(each));
        }
        try (Stream<Path> paths = Files.walk(artifactDirectory)) {
            for (Path each : paths.filter(Files::isRegularFile).toList()) {
                String content = Files.readString(each);
                assertFalse(UNREDACTED_SECRET_PATTERN.matcher(content).find(), () -> "Unredacted secret-like value in LLM artifact: " + each);
                assertFalse(content.contains(getRequiredLLMRuntime().getConfiguration().getApiKey()), () -> "Known model API key leaked into LLM artifact: " + each);
            }
        }
    }
    
    private void assertExpectedResourceHit(final String scenarioId, final Collection<String> expectedResourceUris,
                                           final Collection<MCPInteractionTraceRecord> interactionTrace) {
        if (expectedResourceUris.isEmpty()) {
            return;
        }
        assertTrue(interactionTrace.stream().anyMatch(each -> MCPInteractionActionNames.RESOURCE_READ_KIND.equals(each.getActionKind())
                && expectedResourceUris.contains(Objects.toString(each.getArguments().get("uri"), ""))),
                () -> scenarioId + " did not read an expected resource URI.");
    }
    
    private void assertExpectedRecovery(final String scenarioId, final String expectedRecoveryCategory,
                                        final Collection<MCPInteractionTraceRecord> interactionTrace) {
        boolean expectedRecoveryObserved = false;
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!isErrorInteraction(each)) {
                continue;
            }
            assertTrue(!expectedRecoveryObserved && hasRecoveryCategory(each.getStructuredContent(), expectedRecoveryCategory),
                    () -> scenarioId + " produced unexpected MCP error interaction `" + each.getTargetName() + "`.");
            expectedRecoveryObserved = true;
        }
        if (!expectedRecoveryCategory.isBlank()) {
            assertTrue(expectedRecoveryObserved, () -> scenarioId + " did not observe recovery category `" + expectedRecoveryCategory + "`.");
        }
    }
    
    private boolean isErrorInteraction(final MCPInteractionTraceRecord interactionTraceRecord) {
        return !interactionTraceRecord.isValid()
                || interactionTraceRecord.getStructuredContent().containsKey("error_code")
                || interactionTraceRecord.getStructuredContent().containsKey("empty_state")
                || interactionTraceRecord.getStructuredContent().containsKey("ambiguity_state");
    }
    
    private boolean hasRecoveryCategory(final Map<String, Object> structuredContent, final String expectedRecoveryCategory) {
        if (expectedRecoveryCategory.isBlank()) {
            return false;
        }
        return expectedRecoveryCategory.equals(Objects.toString(structuredContent.get("recovery_category"), ""))
                || expectedRecoveryCategory.equals(Objects.toString(structuredContent.get("error_code"), ""))
                || hasNestedRecoveryCategory(structuredContent.get("recovery"), expectedRecoveryCategory)
                || hasNestedRecoveryCategory(structuredContent.get("empty_state"), expectedRecoveryCategory)
                || hasNestedRecoveryCategory(structuredContent.get("ambiguity_state"), expectedRecoveryCategory);
    }
    
    private boolean hasNestedRecoveryCategory(final Object value, final String expectedRecoveryCategory) {
        if (!(value instanceof Map)) {
            return false;
        }
        Map<?, ?> map = (Map<?, ?>) value;
        return expectedRecoveryCategory.equals(Objects.toString(map.get("recovery_category"), ""))
                || expectedRecoveryCategory.equals(Objects.toString(map.get("category"), ""))
                || expectedRecoveryCategory.equals(Objects.toString(map.get("state"), ""));
    }
    
    private void assertRequiredToolCoverage(final LLME2EScenario scenario, final Collection<MCPInteractionTraceRecord> interactionTrace) {
        for (String each : scenario.getRequiredToolNames()) {
            assertTrue(interactionTrace.stream().anyMatch(record -> record.isValid() && each.equals(record.getTargetName())
                    && NATIVE_ACTION_ORIGINS.contains(record.getActionOrigin())),
                    () -> scenario.getScenarioId() + " did not invoke required MCP action `" + each + "` natively.");
        }
    }
    
    private void assertNextActionsFollowed(final String scenarioId, final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = 0; index < interactionTrace.size() - 1; index++) {
            MCPInteractionTraceRecord current = interactionTrace.get(index);
            List<Map<?, ?>> actions = getImmediateMachineNextActions(current);
            if (!actions.isEmpty()) {
                MCPInteractionTraceRecord next = interactionTrace.get(index + 1);
                assertTrue(actions.stream().anyMatch(each -> matchesNextAction(each, current, next)),
                        () -> scenarioId + " did not follow the next action after `" + current.getTargetName() + "`.");
            }
        }
    }
    
    private List<Map<?, ?>> getImmediateMachineNextActions(final MCPInteractionTraceRecord interactionTraceRecord) {
        List<Map<?, ?>> result = new LinkedList<>();
        for (Map<?, ?> each : LLMMCPNextActions.getNextActions(interactionTraceRecord.getStructuredContent())) {
            if (isMachineAction(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isMachineAction(final Map<?, ?> action) {
        String type = Objects.toString(action.get("type"), "");
        if (!"resource_read".equals(type) && !"tool_call".equals(type) && !"completion".equals(type)) {
            return false;
        }
        if (!"tool_call".equals(type) || !(action.get("arguments") instanceof Map)) {
            return true;
        }
        String executionMode = Objects.toString(((Map<?, ?>) action.get("arguments")).get("execution_mode"), "");
        return !"execute".equals(executionMode) && !"review-then-execute".equals(executionMode);
    }
    
    private boolean matchesNextAction(final Map<?, ?> action, final MCPInteractionTraceRecord current, final MCPInteractionTraceRecord next) {
        String type = Objects.toString(action.get("type"), "");
        if ("resource_read".equals(type)) {
            return MCPInteractionActionNames.RESOURCE_READ_KIND.equals(next.getActionKind())
                    && (Objects.equals(action.get("resource_uri"), next.getArguments().get("uri"))
                            || isRecoverableResourceCorrection(current, next));
        }
        if ("tool_call".equals(type)) {
            return Objects.equals(Objects.toString(action.get("tool_name"), current.getTargetName()), next.getTargetName());
        }
        return "completion".equals(type) && MCPInteractionActionNames.COMPLETION_KIND.equals(next.getActionKind());
    }
    
    private boolean isRecoverableResourceCorrection(final MCPInteractionTraceRecord current, final MCPInteractionTraceRecord next) {
        Object items = next.getStructuredContent().get("items");
        return (current.getStructuredContent().containsKey("empty_state") || current.getStructuredContent().containsKey("ambiguity_state"))
                && items instanceof List && !((List<?>) items).isEmpty();
    }
    
    private void assertTrace(final String scenarioId, final Collection<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            assertTrue(0 < each.getSequence(), () -> "Trace sequence must be positive in " + scenarioId);
            assertFalse(each.getActionKind().isBlank(), () -> "Trace action kind is blank in " + scenarioId);
            assertTrue(NATIVE_ACTION_ORIGINS.contains(each.getActionOrigin()), () -> "Non-native trace action origin in " + scenarioId);
            assertFalse(each.getTargetName().isBlank(), () -> "Trace target name is blank in " + scenarioId);
            MCPModelContractAssertions.assertCanonicalNextActionLists(each.getStructuredContent());
        }
    }
    
    private String createFailureMessage(final String scenarioId, final String failureType, final String message, final Path artifactDirectory) {
        return String.format(Locale.ENGLISH, "%s failed: %s - %s, artifactDirectory=%s", scenarioId, failureType, message, artifactDirectory);
    }
    
    private LLMStructuredAnswer createExpectedAnswer() {
        Fixture fixture = getRequiredRuntimeFixture();
        return new LLMStructuredAnswer(DATABASE_NAME, fixture.schemaName(), TABLE_NAME, COUNT_ORDERS_SQL, fixture.totalOrders(), List.of());
    }
    
    private String createToolContext() {
        return String.format(Locale.ENGLISH,
                " Use logical database `%s` and schema `%s` when the MCP action needs explicit runtime scope.", DATABASE_NAME, getRequiredRuntimeFixture().schemaName());
    }
    
    private String createTableResourceUri() {
        return String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", DATABASE_NAME, getRequiredRuntimeFixture().schemaName(), TABLE_NAME);
    }
    
    @Override
    protected RuntimeTransport getTransport() {
        return RuntimeTransport.HTTP;
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return getRequiredRuntimeFixture().runtimeDatabases();
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        if (null == runtimeFixture) {
            runtimeFixture = runtimeFixtureFactory.createMySQLFixture(DATABASE_NAME, "Docker is required for the MySQL-backed LLM E2E test.");
        }
    }
    
    private Fixture getRequiredRuntimeFixture() {
        if (null == runtimeFixture) {
            throw new IllegalStateException("LLM E2E runtime fixture was not initialized.");
        }
        return runtimeFixture;
    }
    
    private static LLMRuntimeSupport.ModelRuntime getRequiredLLMRuntime() {
        if (null == llmRuntime) {
            throw new IllegalStateException("LLM runtime was not initialized.");
        }
        return llmRuntime;
    }
}

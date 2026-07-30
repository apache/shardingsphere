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
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationRunner;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationRunner.Result;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationRunner.Scenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLMConversationArtifactWriter;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPModelContractAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport.LLMMySQLRuntimeFixture;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("llm-e2e")
@EnabledIf("org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition#isDockerEnabled")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LLMHttpE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final int MAX_TURNS = 8;
    
    private static final String DATABASE_NAME = "logic_db";
    
    private static final String TABLE_NAME = "orders";
    
    private static final String STALE_TABLE_RESOURCE_URI = "shardingsphere://databases/logic_db/schemas/logic_db/tables/missing_orders";
    
    private static final Set<String> EXPECTED_METADATA_NAMES = Set.of("active_orders", "order_items", "orders");
    
    private static final List<String> ARTIFACT_FILES = List.of(
            "run-context.json", "system-prompt.md", "question.txt", "answer.txt", "raw-model-output.txt", "available-tools.json",
            "interaction-trace.json", "mcp-runtime.log", "assertion-report.json");
    
    private static final Pattern UNREDACTED_SECRET_PATTERN = Pattern.compile(
            "(?i)(?<![a-z0-9_])\"?(?:api[_-]?key|access[_-]?token|token|authorization|password|passwd|pwd|secret)\"?\\s*[:=]\\s*[\"']?(?!<redacted>)[^\\s,\"'}]+"
                    + "|(Bearer\\s+)(?!<redacted>)[A-Za-z0-9._~+/=-]+|jdbc:");
    
    private static LLMRuntimeSupport.ModelRuntime llmRuntime;
    
    private final LLMConversationArtifactWriter artifactWriter = new LLMConversationArtifactWriter();
    
    private LLMMySQLRuntimeFixture runtimeFixture;
    
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
    void assertReadOnlyQuery() throws IOException {
        runScenario(new Scenario(
                "read-only-query",
                "How many rows are currently in the orders table of the logic_db runtime database? Inspect the live MCP server and answer concisely.",
                false,
                this::evaluateReadOnlyQuery));
    }
    
    @Test
    void assertMetadataDiscovery() throws IOException {
        runScenario(new Scenario(
                "metadata-discovery",
                "List every table or view currently visible through the live MCP server. The user does not know the database or schema names, so discover the required scope first.",
                false,
                this::evaluateMetadataDiscovery));
    }
    
    @Test
    void assertSideEffectPreview() throws IOException, SQLException {
        prepareRuntimeFixture();
        String statusBefore = queryOrderStatus();
        runScenario(new Scenario(
                "side-effect-preview",
                "A user wants to change the status of order 1 in the orders table to REVIEW_PENDING. Preview the proposed change without executing it, then report whether it "
                        + "was only previewed and whether any data changed. Inspect the live MCP server to discover any required runtime scope.",
                true,
                (answer, trace) -> evaluateSideEffectPreview(answer, trace, statusBefore)));
    }
    
    @Test
    void assertInvalidResourceRecovery() throws IOException {
        runScenario(new Scenario(
                "invalid-resource-recovery",
                "A user pasted stale resource `" + STALE_TABLE_RESOURCE_URI + "`. Inspect that resource, then follow the first safe read-only action in its top-level "
                        + "`next_actions` by reading its `resource_uri` exactly. Do not guess another URI. Then report how many rows are currently in the orders table.",
                false,
                this::evaluateInvalidResourceRecovery));
    }
    
    private void runScenario(final Scenario scenario) throws IOException {
        prepareRuntimeFixture();
        LLMRuntimeSupport.ModelRuntime modelRuntime = getRequiredLLMRuntime();
        assertTrue(Boolean.TRUE.equals(modelRuntime.getEvidence().get("scoreClosing")), "LLM E2E requires Docker-owned runtime evidence.");
        Result actualResult = new LLMConversationRunner(
                MAX_TURNS,
                new LLMChatModelClient(modelRuntime.getConfiguration(), HttpClient.newHttpClient()),
                createInteractionClient(),
                modelRuntime.getConfiguration().getModelName()).run(scenario);
        Path artifactDirectory = modelRuntime.getConfiguration().createArtifactDirectory("llm-http/" + scenario.id());
        Collection<String> sensitiveValues = getArtifactSensitiveValues();
        artifactWriter.write(artifactDirectory, actualResult, modelRuntime.getEvidence(), sensitiveValues);
        assertArtifacts(artifactDirectory, sensitiveValues);
        assertTrue(actualResult.assertionReport().isSuccess(), () -> createFailureMessage(
                scenario.id(), actualResult.assertionReport(), artifactDirectory));
        assertFalse(actualResult.evidence().interactionTrace().isEmpty(), scenario.id() + " must capture MCP evidence.");
        assertTrace(scenario.id(), actualResult.evidence().interactionTrace());
    }
    
    private LLME2EAssertionReport evaluateReadOnlyQuery(final String answer, final List<MCPInteractionTraceRecord> trace) {
        Optional<Integer> actualCount = findQueryCount(trace, 0);
        if (actualCount.isEmpty() || getRequiredRuntimeFixture().getTotalOrders() != actualCount.get()) {
            return LLME2EAssertionReport.failure("query_evidence_mismatch", "The MCP query response did not contain the fixture row count.");
        }
        return containsStandaloneNumber(answer, actualCount.get())
                ? LLME2EAssertionReport.success("The answer matched the row count returned by the live MCP query.")
                : LLME2EAssertionReport.failure("answer_mismatch", "The answer did not contain the row count returned by the live MCP query.");
    }
    
    private LLME2EAssertionReport evaluateMetadataDiscovery(final String answer, final List<MCPInteractionTraceRecord> trace) {
        int unscopedSearchIndex = findUnscopedMetadataSearchIndex(trace);
        if (0 > unscopedSearchIndex) {
            return LLME2EAssertionReport.failure("missing_unscoped_discovery", "The model did not discover metadata without database and schema scope.");
        }
        int discoveryTurn = trace.get(unscopedSearchIndex).getModelTurn();
        Set<String> actualMetadataNames = new LinkedHashSet<>();
        for (int index = unscopedSearchIndex; index < trace.size(); index++) {
            MCPInteractionTraceRecord each = trace.get(index);
            if (isValidModelAction(each, "database_gateway_search_metadata") && (unscopedSearchIndex == index || each.getModelTurn() > discoveryTurn)) {
                actualMetadataNames.addAll(getMetadataNames(each.getStructuredContent()));
            }
        }
        if (!EXPECTED_METADATA_NAMES.equals(actualMetadataNames)) {
            return LLME2EAssertionReport.failure("metadata_evidence_mismatch", "MCP metadata responses did not contain the expected table and view set.");
        }
        String normalizedAnswer = answer.toLowerCase(Locale.ENGLISH);
        return EXPECTED_METADATA_NAMES.stream().allMatch(each -> containsIdentifier(normalizedAnswer, each))
                ? LLME2EAssertionReport.success("The answer named every object returned by the live MCP metadata searches.")
                : LLME2EAssertionReport.failure("answer_mismatch", "The answer omitted an object returned by the live MCP metadata searches.");
    }
    
    private LLME2EAssertionReport evaluateSideEffectPreview(final String answer, final List<MCPInteractionTraceRecord> trace, final String statusBefore) {
        Optional<MCPInteractionTraceRecord> preview = trace.stream()
                .filter(each -> isValidModelAction(each, "database_gateway_execute_update"))
                .filter(each -> "preview".equals(each.getArguments().get("execution_mode")))
                .filter(this::isSentinelPreview)
                .findFirst();
        if (preview.isEmpty()) {
            return LLME2EAssertionReport.failure("preview_evidence_mismatch", "The model did not preview the requested sentinel update.");
        }
        Map<String, Object> response = preview.get().getStructuredContent();
        if (!"preview".equals(response.get("response_mode")) || !"preview".equals(response.get("result_kind")) || !Boolean.FALSE.equals(response.get("would_execute"))) {
            return LLME2EAssertionReport.failure("preview_evidence_mismatch", "The MCP response did not prove classification-only preview behavior.");
        }
        try {
            if (!statusBefore.equals(queryOrderStatus())) {
                return LLME2EAssertionReport.failure("side_effect_detected", "The sentinel row changed during the preview-only scenario.");
            }
        } catch (final SQLException ex) {
            return LLME2EAssertionReport.failure("database_evidence_unavailable", ex.getMessage());
        }
        String normalizedAnswer = answer.toLowerCase(Locale.ENGLISH);
        boolean answerReportsNoExecution = normalizedAnswer.contains("not execut") || normalizedAnswer.contains("without execut")
                || normalizedAnswer.contains("not changed") || normalizedAnswer.contains("unchanged") || normalizedAnswer.contains("no change")
                || normalizedAnswer.contains("didn't execut") || normalizedAnswer.contains("wasn't execut");
        return normalizedAnswer.contains("preview") && answerReportsNoExecution
                ? LLME2EAssertionReport.success("The answer matched the preview response and the unchanged database sentinel.")
                : LLME2EAssertionReport.failure("answer_mismatch", "The answer did not state that the operation was previewed without changing data.");
    }
    
    private boolean isSentinelPreview(final MCPInteractionTraceRecord traceRecord) {
        String sql = Objects.toString(traceRecord.getArguments().get("sql"), "").toLowerCase(Locale.ENGLISH).replaceAll("[\\s`\"]+", "");
        boolean updatesOrders = sql.startsWith("updateordersset") || sql.startsWith("update" + DATABASE_NAME + ".ordersset");
        return updatesOrders && sql.contains("status='review_pending'") && sql.contains("where") && sql.contains("order_id=1");
    }
    
    private LLME2EAssertionReport evaluateInvalidResourceRecovery(final String answer, final List<MCPInteractionTraceRecord> trace) {
        int staleResourceIndex = findStaleResourceIndex(trace);
        if (0 > staleResourceIndex) {
            return LLME2EAssertionReport.failure("missing_stale_resource_error", "The model did not observe the stale resource error.");
        }
        int recoveryResourceIndex = findGuidedRecoveryResourceIndex(trace, staleResourceIndex);
        if (0 > recoveryResourceIndex) {
            return LLME2EAssertionReport.failure("missing_resource_recovery", "The model did not follow the stale response recovery action to a live resource containing orders.");
        }
        Optional<Integer> actualCount = findQueryCount(trace, trace.get(recoveryResourceIndex).getModelTurn());
        if (actualCount.isEmpty() || getRequiredRuntimeFixture().getTotalOrders() != actualCount.get()) {
            return LLME2EAssertionReport.failure("query_evidence_mismatch", "The recovered conversation did not obtain the fixture row count from MCP.");
        }
        return containsStandaloneNumber(answer, actualCount.get())
                ? LLME2EAssertionReport.success("The answer followed stale-resource recovery and matched the subsequent MCP query.")
                : LLME2EAssertionReport.failure("answer_mismatch", "The answer did not contain the row count returned after resource recovery.");
    }
    
    private int findStaleResourceIndex(final List<MCPInteractionTraceRecord> trace) {
        for (int index = 0; index < trace.size(); index++) {
            MCPInteractionTraceRecord each = trace.get(index);
            if (isValidModelAction(each, "mcp_read_resource")
                    && STALE_TABLE_RESOURCE_URI.equals(each.getArguments().get("uri"))
                    && hasRecoveryCategory(each.getStructuredContent(), "object_not_visible")) {
                return index;
            }
        }
        return -1;
    }
    
    private int findGuidedRecoveryResourceIndex(final List<MCPInteractionTraceRecord> trace, final int staleResourceIndex) {
        MCPInteractionTraceRecord staleResource = trace.get(staleResourceIndex);
        List<Map<String, Object>> nextActions = getObjectList(staleResource.getStructuredContent().get("next_actions"));
        if (nextActions.isEmpty()) {
            return -1;
        }
        Map<String, Object> recoveryAction = nextActions.getFirst();
        if (!"resource_read".equals(recoveryAction.get("type"))) {
            return -1;
        }
        for (int index = staleResourceIndex + 1; index < trace.size(); index++) {
            MCPInteractionTraceRecord each = trace.get(index);
            if (each.getModelTurn() > staleResource.getModelTurn()
                    && isValidModelAction(each, "mcp_read_resource")
                    && Objects.equals(recoveryAction.get("resource_uri"), each.getArguments().get("uri"))
                    && containsOrdersTable(each.getStructuredContent())) {
                return index;
            }
        }
        return -1;
    }
    
    private boolean containsOrdersTable(final Map<String, Object> structuredContent) {
        return getObjectList(structuredContent.get("items")).stream()
                .anyMatch(each -> DATABASE_NAME.equals(each.get("database")) && TABLE_NAME.equals(each.get("table")));
    }
    
    private boolean hasRecoveryCategory(final Map<String, Object> structuredContent, final String expectedCategory) {
        Map<String, Object> recovery = getObjectMap(structuredContent.get("recovery"));
        return expectedCategory.equals(structuredContent.get("recovery_category"))
                || expectedCategory.equals(recovery.get("category")) || expectedCategory.equals(recovery.get("recovery_category"));
    }
    
    private Optional<Integer> findQueryCount(final List<MCPInteractionTraceRecord> trace, final int previousModelTurn) {
        for (MCPInteractionTraceRecord each : trace) {
            if (each.getModelTurn() <= previousModelTurn || !isValidModelAction(each, "database_gateway_execute_query") || !isOrdersCountQuery(each.getArguments())) {
                continue;
            }
            for (Map<String, Object> row : getObjectList(each.getStructuredContent().get("row_objects"))) {
                for (Object value : row.values()) {
                    if (value instanceof Number) {
                        return Optional.of(((Number) value).intValue());
                    }
                }
            }
            for (Object row : getList(each.getStructuredContent().get("rows"))) {
                for (Object value : getList(row)) {
                    if (value instanceof Number) {
                        return Optional.of(((Number) value).intValue());
                    }
                }
            }
        }
        return Optional.empty();
    }
    
    private boolean isOrdersCountQuery(final Map<String, Object> arguments) {
        String sql = Objects.toString(arguments.get("sql"), "").toLowerCase(Locale.ENGLISH).replaceAll("[\\s`]+", "");
        return sql.contains("count(") && (sql.contains("fromorders") || sql.contains(".orders"));
    }
    
    private Set<String> getMetadataNames(final Map<String, Object> structuredContent) {
        Set<String> result = new LinkedHashSet<>();
        for (Map<String, Object> each : getObjectList(structuredContent.get("items"))) {
            if ("table".equals(each.get("objectType")) || "view".equals(each.get("objectType"))) {
                result.add(Objects.toString(each.get("name"), ""));
            }
        }
        return result;
    }
    
    private int findUnscopedMetadataSearchIndex(final List<MCPInteractionTraceRecord> trace) {
        for (int index = 0; index < trace.size(); index++) {
            MCPInteractionTraceRecord each = trace.get(index);
            if (isValidModelAction(each, "database_gateway_search_metadata")
                    && !each.getArguments().containsKey("database") && !each.getArguments().containsKey("schema")
                    && getObjectList(each.getStructuredContent().get("items")).stream().anyMatch(item -> DATABASE_NAME.equals(item.get("database")))) {
                return index;
            }
        }
        return -1;
    }
    
    private boolean isValidModelAction(final MCPInteractionTraceRecord traceRecord, final String actionName) {
        return traceRecord.isValid() && MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN.equals(traceRecord.getActionOrigin())
                && actionName.equals(traceRecord.getTargetName());
    }
    
    private boolean containsStandaloneNumber(final String answer, final int expected) {
        return Pattern.compile("(?<!\\d)" + expected + "(?!\\d)").matcher(answer).find();
    }
    
    private boolean containsIdentifier(final String answer, final String identifier) {
        return Pattern.compile("(?<![a-z0-9_])" + Pattern.quote(identifier) + "(?![a-z0-9_])").matcher(answer).find();
    }
    
    private Collection<String> getArtifactSensitiveValues() {
        Set<String> result = new LinkedHashSet<>();
        addArtifactSensitiveValue(result, getRequiredLLMRuntime().getConfiguration().getApiKey());
        for (RuntimeDatabaseConfiguration each : getRequiredRuntimeFixture().getRuntimeDatabases().values()) {
            addArtifactSensitiveValue(result, each.getJdbcUrl());
            addArtifactSensitiveValue(result, each.getPassword());
        }
        return result;
    }
    
    private void addArtifactSensitiveValue(final Collection<String> sensitiveValues, final String value) {
        if (8 <= value.length()) {
            sensitiveValues.add(value);
        }
    }
    
    private void assertArtifacts(final Path artifactDirectory, final Collection<String> sensitiveValues) throws IOException {
        assertTrue(Files.isDirectory(artifactDirectory), () -> "Missing LLM artifact directory: " + artifactDirectory);
        for (String each : ARTIFACT_FILES) {
            assertTrue(Files.isRegularFile(artifactDirectory.resolve(each)), () -> "Missing LLM artifact: " + artifactDirectory.resolve(each));
        }
        try (Stream<Path> paths = Files.walk(artifactDirectory)) {
            for (Path each : paths.filter(Files::isRegularFile).toList()) {
                String content = Files.readString(each);
                assertFalse(UNREDACTED_SECRET_PATTERN.matcher(content).find(), () -> "Unredacted secret-like value in LLM artifact: " + each);
                for (String sensitiveValue : sensitiveValues) {
                    assertFalse(content.contains(sensitiveValue), () -> "Known sensitive value leaked into LLM artifact: " + each);
                }
            }
        }
    }
    
    private void assertTrace(final String scenarioId, final Collection<MCPInteractionTraceRecord> trace) {
        for (MCPInteractionTraceRecord each : trace) {
            assertTrue(0 < each.getSequence(), () -> "Trace sequence must be positive in " + scenarioId);
            assertTrue(0 < each.getModelTurn(), () -> "Trace model turn must be positive in " + scenarioId);
            assertFalse(each.getActionKind().isBlank(), () -> "Trace action kind is blank in " + scenarioId);
            assertTrue(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN.equals(each.getActionOrigin()), () -> "Non-model trace action origin in " + scenarioId);
            assertFalse(each.getTargetName().isBlank(), () -> "Trace target name is blank in " + scenarioId);
            MCPModelContractAssertions.assertCanonicalNextActionLists(each.getStructuredContent());
        }
    }
    
    private String queryOrderStatus() throws SQLException {
        RuntimeDatabaseConfiguration databaseConfig = getRequiredRuntimeFixture().getRuntimeDatabases().get(DATABASE_NAME);
        try (
                Connection connection = databaseConfig.openConnection(DATABASE_NAME);
                PreparedStatement statement = connection.prepareStatement("SELECT status FROM orders WHERE order_id = 1");
                ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException("The sentinel order row is unavailable.");
            }
            return resultSet.getString(1);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getObjectList(final Object value) {
        return value instanceof List ? (List<Map<String, Object>>) value : List.of();
    }
    
    private List<?> getList(final Object value) {
        return value instanceof List ? (List<?>) value : List.of();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getObjectMap(final Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }
    
    private String createFailureMessage(final String scenarioId, final LLME2EAssertionReport report, final Path artifactDirectory) {
        return String.format(Locale.ENGLISH, "%s failed: %s - %s, artifactDirectory=%s",
                scenarioId, report.getFailureType(), report.getMessage(), artifactDirectory);
    }
    
    @Override
    protected RuntimeTransport getTransport() {
        return RuntimeTransport.HTTP;
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return getRequiredRuntimeFixture().getRuntimeDatabases();
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        if (null != runtimeFixture) {
            return;
        }
        if (!MySQLRuntimeTestSupport.isDockerAvailable()) {
            throw new IllegalStateException(MySQLRuntimeTestSupport.createDockerRequiredMessage("Docker is required for the MySQL-backed LLM E2E test."));
        }
        try {
            runtimeFixture = MySQLRuntimeTestSupport.createLLMRuntimeFixture(DATABASE_NAME);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    private LLMMySQLRuntimeFixture getRequiredRuntimeFixture() {
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

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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.smoke;

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationExecutor;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory.Fixture;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeSupport;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("llm-e2e")
@EnabledIf("isEnabled")
class LLMSmokeE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final String DATABASE_NAME = "logic_db";
    
    private static final String TABLE_NAME = "orders";
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private static final String EXECUTE_QUERY_TOOL_NAME = "database_gateway_execute_query";
    
    private static final String SYSTEM_PROMPT = """
            You are evaluating an MCP server.
            Use MCP tools when they help the task.
            Do not guess database structure or query results.
            Return JSON only when asked for the final answer.
            """.trim();
    
    private static LLMRuntimeSupport.ModelRuntime llmRuntime;
    
    private final LLMRuntimeFixtureFactory runtimeFixtureFactory = new LLMRuntimeFixtureFactory();
    
    private RuntimeTransport currentTransport;
    
    private Fixture currentRuntimeFixture;
    
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
    
    @AfterEach
    void closeRuntimeFixture() {
        if (null != currentRuntimeFixture) {
            currentRuntimeFixture.close();
            currentRuntimeFixture = null;
        }
        currentTransport = null;
    }
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isLLMEnabled();
    }
    
    static Stream<Arguments> getTestCases() {
        return Stream.of(
                Arguments.of("llm-smoke-mysql-http", RuntimeTransport.HTTP),
                Arguments.of("llm-smoke-mysql-stdio", RuntimeTransport.STDIO));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void assertSmoke(final String suiteId, final RuntimeTransport transport) throws IOException, InterruptedException {
        currentTransport = transport;
        LLMConversationExecutor conversationExecutor = new LLMConversationExecutor(getRequiredLLMConfiguration(), getRequiredLLMRuntimeEvidence());
        conversationExecutor.assertModelReady();
        prepareRuntimeFixture();
        LLMConversationExecutor.ConversationResult actualResult = conversationExecutor.runConversation(suiteId, createScenario(suiteId), createInteractionClient());
        assertSuccess(actualResult);
    }
    
    private LLME2EScenario createScenario(final String scenarioId) {
        Fixture fixture = getRequiredRuntimeFixture();
        return new LLME2EScenario(scenarioId, SYSTEM_PROMPT,
                "A user asks how many rows are in `" + TABLE_NAME + "` right now. Use logical database `" + DATABASE_NAME + "`, schema `"
                        + fixture.schemaName() + "`, and SQL `" + COUNT_ORDERS_SQL + "`.",
                new LLMStructuredAnswer(DATABASE_NAME, fixture.schemaName(), TABLE_NAME, COUNT_ORDERS_SQL, fixture.totalOrders(), List.of()),
                List.of(EXECUTE_QUERY_TOOL_NAME), List.of(EXECUTE_QUERY_TOOL_NAME));
    }
    
    private void assertSuccess(final LLMConversationExecutor.ConversationResult actualResult) {
        LLME2EAssertionReport actualReport = actualResult.artifactBundle().getAssertionReport();
        assertTrue(actualReport.isSuccess(),
                () -> String.format("LLM smoke scenario failed: %s - %s", actualReport.getFailureType(), actualReport.getMessage()));
        assertFalse(actualResult.artifactBundle().getInteractionTrace().isEmpty(), "LLM smoke scenario must record at least one MCP interaction.");
    }
    
    private static LLME2EConfiguration getRequiredLLMConfiguration() {
        return getRequiredLLMRuntime().getConfiguration();
    }
    
    private static Map<String, Object> getRequiredLLMRuntimeEvidence() {
        return getRequiredLLMRuntime().getEvidence();
    }
    
    private static LLMRuntimeSupport.ModelRuntime getRequiredLLMRuntime() {
        if (null == llmRuntime) {
            throw new IllegalStateException("LLM runtime was not initialized.");
        }
        return llmRuntime;
    }
    
    @Override
    protected RuntimeTransport getTransport() {
        return getRequiredTransport();
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return getRequiredRuntimeFixture().runtimeDatabases();
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        if (null != currentRuntimeFixture) {
            return;
        }
        currentRuntimeFixture = runtimeFixtureFactory.createMySQLFixture(DATABASE_NAME, "Docker is required for the MySQL-backed LLM smoke E2E test.");
    }
    
    private Fixture getRequiredRuntimeFixture() {
        if (null == currentRuntimeFixture) {
            throw new IllegalStateException("LLM smoke runtime fixture was not initialized.");
        }
        return currentRuntimeFixture;
    }
    
    private RuntimeTransport getRequiredTransport() {
        if (null == currentTransport) {
            throw new IllegalStateException("LLM smoke test case was not initialized.");
        }
        return currentTransport;
    }
}

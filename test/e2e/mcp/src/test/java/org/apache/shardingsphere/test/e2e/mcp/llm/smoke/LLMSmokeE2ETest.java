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

package org.apache.shardingsphere.test.e2e.mcp.llm.smoke;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.framework.LLMConversationExecutor;
import org.apache.shardingsphere.test.e2e.mcp.llm.framework.LLMRuntimeFixtureFactory;
import org.apache.shardingsphere.test.e2e.mcp.llm.framework.LLMRuntimeFixtureFactory.Backend;
import org.apache.shardingsphere.test.e2e.mcp.llm.framework.LLMRuntimeFixtureFactory.Fixture;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMSmokeE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private final LLMConversationExecutor conversationExecutor = new LLMConversationExecutor();
    
    private final LLMRuntimeFixtureFactory runtimeFixtureFactory = new LLMRuntimeFixtureFactory();
    
    private final LLMSmokeScenarioFactory scenarioFactory = new LLMSmokeScenarioFactory();
    
    private SmokeTestCase currentTestCase;
    
    private Fixture currentRuntimeFixture;
    
    @AfterEach
    void closeRuntimeFixture() {
        if (null != currentRuntimeFixture) {
            currentRuntimeFixture.close();
            currentRuntimeFixture = null;
        }
        currentTestCase = null;
    }
    
    static List<SmokeTestCase> getTestCases() {
        return List.of(
                new SmokeTestCase("minimal-smoke-h2", RuntimeTransport.HTTP, Backend.H2),
                new SmokeTestCase("minimal-smoke-mysql", RuntimeTransport.HTTP, Backend.MYSQL),
                new SmokeTestCase("minimal-smoke-h2-stdio", RuntimeTransport.STDIO, Backend.H2),
                new SmokeTestCase("minimal-smoke-mysql-stdio", RuntimeTransport.STDIO, Backend.MYSQL));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void assertSmoke(final SmokeTestCase testCase) throws IOException {
        currentTestCase = testCase;
        Assumptions.assumeTrue(conversationExecutor.isEnabled(),
                "Set -Dmcp.llm.e2e.enabled=true or MCP_LLM_E2E_ENABLED=true to run the LLM MCP smoke tests.");
        prepareRuntimeFixture();
        LLME2EScenario scenario = scenarioFactory.createMinimalSmokeScenario(testCase.scenarioId(), "logic_db",
                getRequiredRuntimeFixture().schemaName(), "orders", COUNT_ORDERS_SQL, getRequiredRuntimeFixture().totalOrders());
        LLMConversationExecutor.ConversationResult actualResult = conversationExecutor.runConversation(
                scenario.getScenarioId(), scenario, createInteractionClient());
        LLME2EAssertionReport assertionReport = actualResult.artifactBundle().getAssertionReport();
        assertTrue(assertionReport.isSuccess(),
                () -> String.format(Locale.ENGLISH, "%s: %s (artifacts: %s)",
                        assertionReport.getFailureType(), assertionReport.getMessage(), actualResult.artifactDirectory()));
    }
    
    @Override
    protected RuntimeTransport getTransport() {
        return getRequiredTestCase().transport();
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
        SmokeTestCase testCase = getRequiredTestCase();
        currentRuntimeFixture = Backend.H2 == testCase.backend()
                ? runtimeFixtureFactory.createSingleDatabaseH2Fixture(getTempDir(), testCase.scenarioId(), "logic_db", testCase.transport())
                : runtimeFixtureFactory.createMySQLFixture("logic_db", "Docker is required for the MySQL-backed LLM MCP smoke test.");
    }
    
    private SmokeTestCase getRequiredTestCase() {
        if (null == currentTestCase) {
            throw new IllegalStateException("LLM smoke test case was not initialized.");
        }
        return currentTestCase;
    }
    
    private Fixture getRequiredRuntimeFixture() {
        if (null == currentRuntimeFixture) {
            throw new IllegalStateException("LLM smoke runtime fixture was not initialized.");
        }
        return currentRuntimeFixture;
    }
    
    private record SmokeTestCase(String scenarioId, RuntimeTransport transport, Backend backend) {
        
        @Override
        public String toString() {
            return scenarioId;
        }
    }
}

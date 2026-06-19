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

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationExecutor;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeSupport;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory.Fixture;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenarioCatalog;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Tag("llm-e2e")
@EnabledIf("isEnabled")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LLMUsabilitySuiteE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final String RUNTIME_KIND = "mysql";
    
    private static final String DATABASE_NAME = "logic_db";
    
    private static final String TABLE_NAME = "orders";
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private static final String FULL_TRANSPORT_MATRIX_PROPERTY = "mcp.e2e.llm.full-transport-matrix";
    
    private static LLMRuntimeSupport.ModelRuntime llmRuntime;
    
    private final LLMRuntimeFixtureFactory runtimeFixtureFactory = new LLMRuntimeFixtureFactory();
    
    private final LLMUsabilitySuiteRunner suiteRunner = new LLMUsabilitySuiteRunner();
    
    private final LLMUsabilityScenarioCatalog scenarioCatalog = new LLMUsabilityScenarioCatalog();
    
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
    
    @AfterAll
    void closeRuntimeFixture() {
        if (null != currentRuntimeFixture) {
            currentRuntimeFixture.close();
            currentRuntimeFixture = null;
        }
    }
    
    @AfterEach
    void clearCurrentTransport() {
        currentTransport = null;
    }
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    static Stream<Arguments> getTestCases() {
        return isFullTransportMatrixEnabled()
                ? Stream.of(
                        Arguments.of("llm-usability-mysql-http", RuntimeTransport.HTTP),
                        Arguments.of("llm-usability-mysql-stdio", RuntimeTransport.STDIO))
                : Stream.of(Arguments.of("llm-usability-mysql-http", RuntimeTransport.HTTP));
    }
    
    private static boolean isFullTransportMatrixEnabled() {
        return Boolean.parseBoolean(System.getProperty(FULL_TRANSPORT_MATRIX_PROPERTY, "false"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void assertUsabilityBaseline(final String suiteId, final RuntimeTransport transport) throws IOException, InterruptedException {
        currentTransport = transport;
        LLMConversationExecutor conversationExecutor = new LLMConversationExecutor(getRequiredLLMConfiguration(), getRequiredLLMRuntimeEvidence());
        conversationExecutor.assertModelReady();
        prepareRuntimeFixture();
        suiteRunner.assertCoreSuite(suiteId + "/core",
                this::createCoreScenarios,
                each -> conversationExecutor.runConversation(suiteId + "/core/" + each.getScenarioId(), each, createInteractionClient()),
                conversationExecutor.getConfiguration());
        suiteRunner.assertExtendedSuite(suiteId + "/extended",
                this::createExtendedScenarios,
                each -> conversationExecutor.runConversation(suiteId + "/extended/" + each.getScenarioId(), each, createInteractionClient()),
                conversationExecutor.getConfiguration());
    }
    
    private List<LLMUsabilityScenario> createCoreScenarios() {
        Fixture fixture = getRequiredRuntimeFixture();
        return scenarioCatalog.createCoreGate(RUNTIME_KIND, DATABASE_NAME, fixture.schemaName(), TABLE_NAME, COUNT_ORDERS_SQL,
                fixture.totalOrders());
    }
    
    private List<LLMUsabilityScenario> createExtendedScenarios() {
        Fixture fixture = getRequiredRuntimeFixture();
        return scenarioCatalog.createExtendedScore(RUNTIME_KIND, DATABASE_NAME, fixture.schemaName(), TABLE_NAME, COUNT_ORDERS_SQL,
                fixture.totalOrders());
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
        currentRuntimeFixture = runtimeFixtureFactory.createMySQLFixture(DATABASE_NAME, "Docker is required for the MySQL-backed LLM usability E2E test.");
    }
    
    private Fixture getRequiredRuntimeFixture() {
        if (null == currentRuntimeFixture) {
            throw new IllegalStateException("LLM usability runtime fixture was not initialized.");
        }
        return currentRuntimeFixture;
    }
    
    private RuntimeTransport getRequiredTransport() {
        if (null == currentTransport) {
            throw new IllegalStateException("LLM usability test case was not initialized.");
        }
        return currentTransport;
    }
}

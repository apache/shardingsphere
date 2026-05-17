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
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationExecutor;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory.Fixture;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.OllamaLLMRuntimeSupport;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenarioCatalog;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag("llm-e2e")
@EnabledIf("isEnabled")
class LLMUsabilitySuiteE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final String SUITE_ID = "llm-usability-h2";
    
    private static final String RUNTIME_KIND = "h2";
    
    private static final String DATABASE_NAME = "logic_db";
    
    private static final String TABLE_NAME = "orders";
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private static OllamaLLMRuntimeSupport.ModelRuntime llmRuntime;
    
    private final LLMRuntimeFixtureFactory runtimeFixtureFactory = new LLMRuntimeFixtureFactory();
    
    private final LLMUsabilitySuiteRunner suiteRunner = new LLMUsabilitySuiteRunner();
    
    private final LLMUsabilityScenarioCatalog scenarioCatalog = new LLMUsabilityScenarioCatalog();
    
    private Fixture currentRuntimeFixture;
    
    @BeforeAll
    static void prepareLLMRuntime() throws InterruptedException {
        llmRuntime = OllamaLLMRuntimeSupport.prepare(LLME2EConfiguration.load());
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
    }
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isLLMEnabled();
    }
    
    @Test
    void assertUsabilityBaseline() throws IOException, InterruptedException {
        LLMConversationExecutor conversationExecutor = new LLMConversationExecutor(getRequiredLLMConfiguration(), getRequiredLLMRuntimeEvidence());
        conversationExecutor.assertModelReady();
        prepareRuntimeFixture();
        suiteRunner.assertCoreSuite(SUITE_ID + "/core",
                this::createCoreScenarios,
                each -> conversationExecutor.runConversation(SUITE_ID + "/core/" + each.getScenarioId(), each, createInteractionClient()),
                conversationExecutor.getConfiguration());
        suiteRunner.assertExtendedSuite(SUITE_ID + "/extended",
                this::createExtendedScenarios,
                each -> conversationExecutor.runConversation(SUITE_ID + "/extended/" + each.getScenarioId(), each, createInteractionClient()),
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
        OllamaLLMRuntimeSupport.ModelRuntime runtime = getRequiredLLMRuntime();
        return Map.of(
                "runtimeMode", runtime.getRuntimeMode().getValue(),
                "dockerOwned", RuntimeMode.DOCKER == runtime.getRuntimeMode(),
                "imageName", runtime.getImageName(),
                "imageDigest", runtime.getImageDigest());
    }
    
    private static OllamaLLMRuntimeSupport.ModelRuntime getRequiredLLMRuntime() {
        if (null == llmRuntime) {
            throw new IllegalStateException("LLM runtime was not initialized.");
        }
        return llmRuntime;
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
        if (null != currentRuntimeFixture) {
            return;
        }
        currentRuntimeFixture = runtimeFixtureFactory.createMultiDatabaseH2Fixture(getTempDir(), DATABASE_NAME, "analytics_db",
                getTransport());
    }
    
    private Fixture getRequiredRuntimeFixture() {
        if (null == currentRuntimeFixture) {
            throw new IllegalStateException("LLM usability runtime fixture was not initialized.");
        }
        return currentRuntimeFixture;
    }
}

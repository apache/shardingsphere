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

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMConversationExecutor;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory.Backend;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory.Fixture;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.scenario.LLMUsabilityScenarioCatalog;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@EnabledIf("isEnabled")
class LLMUsabilitySuiteE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM orders";
    
    private final LLMConversationExecutor conversationExecutor = new LLMConversationExecutor();
    
    private final LLMRuntimeFixtureFactory runtimeFixtureFactory = new LLMRuntimeFixtureFactory();
    
    private final LLMUsabilitySuiteRunner suiteRunner = new LLMUsabilitySuiteRunner();
    
    private final LLMUsabilityScenarioCatalog scenarioCatalog = new LLMUsabilityScenarioCatalog();
    
    private UsabilitySuiteTestCase currentTestCase;
    
    private Fixture currentRuntimeFixture;
    
    @AfterEach
    void closeRuntimeFixture() {
        if (null != currentRuntimeFixture) {
            currentRuntimeFixture.close();
            currentRuntimeFixture = null;
        }
        currentTestCase = null;
    }
    
    static List<UsabilitySuiteTestCase> getTestCases() {
        return List.of(
                new UsabilitySuiteTestCase("minimal-usability-h2", RuntimeTransport.HTTP, Backend.H2),
                new UsabilitySuiteTestCase("minimal-usability-mysql", RuntimeTransport.HTTP, Backend.MYSQL),
                new UsabilitySuiteTestCase("minimal-usability-h2-stdio", RuntimeTransport.STDIO, Backend.H2),
                new UsabilitySuiteTestCase("minimal-usability-mysql-stdio", RuntimeTransport.STDIO, Backend.MYSQL));
    }
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isLLMEnabled();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void assertMinimalBaseline(final UsabilitySuiteTestCase testCase) throws IOException {
        currentTestCase = testCase;
        prepareRuntimeFixture();
        suiteRunner.assertUsabilitySuite(testCase.suiteId(),
                () -> scenarioCatalog.createMinimalBaseline(getRuntimeKind(), "logic_db", getRequiredRuntimeFixture().schemaName(),
                        "orders", COUNT_ORDERS_SQL, getRequiredRuntimeFixture().totalOrders()),
                each -> conversationExecutor.runConversation(each.getScenarioId(), each, createInteractionClient()),
                conversationExecutor.getConfiguration());
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
        UsabilitySuiteTestCase testCase = getRequiredTestCase();
        currentRuntimeFixture = Backend.H2 == testCase.backend()
                ? runtimeFixtureFactory.createMultiDatabaseH2Fixture(getTempDir(), "logic_db", "analytics_db", testCase.transport())
                : runtimeFixtureFactory.createMySQLFixture("logic_db", "Docker is required for the MySQL-backed usability suite.");
    }
    
    private String getRuntimeKind() {
        return Backend.H2 == getRequiredTestCase().backend() ? "h2" : "mysql";
    }
    
    private UsabilitySuiteTestCase getRequiredTestCase() {
        if (null == currentTestCase) {
            throw new IllegalStateException("LLM usability suite test case was not initialized.");
        }
        return currentTestCase;
    }
    
    private Fixture getRequiredRuntimeFixture() {
        if (null == currentRuntimeFixture) {
            throw new IllegalStateException("LLM usability runtime fixture was not initialized.");
        }
        return currentRuntimeFixture;
    }
    
    private record UsabilitySuiteTestCase(String suiteId, RuntimeTransport transport, Backend backend) {

        @Override
        public String toString() {
            return suiteId;
        }
    }
}

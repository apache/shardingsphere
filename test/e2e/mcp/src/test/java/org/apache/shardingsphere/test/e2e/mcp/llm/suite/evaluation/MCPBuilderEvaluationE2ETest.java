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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.evaluation;

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeFixtureFactory.Fixture;
import org.apache.shardingsphere.test.e2e.mcp.llm.fixture.LLMRuntimeSupport;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.MCPBuilderEvaluationCatalog;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.util.Map;

@Tag("llm-e2e")
@EnabledIf("isEnabled")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MCPBuilderEvaluationE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final String DATABASE_NAME = "logic_db";
    
    private static LLMRuntimeSupport.ModelRuntime llmRuntime;
    
    private final LLMRuntimeFixtureFactory runtimeFixtureFactory = new LLMRuntimeFixtureFactory();
    
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
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    void assertAutonomousEvaluation() throws IOException, InterruptedException {
        prepareRuntimeFixture();
        LLMRuntimeSupport.ModelRuntime modelRuntime = getRequiredLLMRuntime();
        new MCPBuilderEvaluationSuiteRunner(modelRuntime.getConfiguration(), modelRuntime.getEvidence())
                .assertFullScore(new MCPBuilderEvaluationCatalog().load(), this::createInteractionClient);
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
            runtimeFixture = runtimeFixtureFactory.createMySQLFixture(DATABASE_NAME, "Docker is required for the MCP Builder evaluation.");
        }
    }
    
    private Fixture getRequiredRuntimeFixture() {
        if (null == runtimeFixture) {
            throw new IllegalStateException("MCP Builder evaluation runtime fixture was not initialized.");
        }
        return runtimeFixture;
    }
    
    private static LLMRuntimeSupport.ModelRuntime getRequiredLLMRuntime() {
        if (null == llmRuntime) {
            throw new IllegalStateException("MCP Builder evaluation LLM runtime was not initialized.");
        }
        return llmRuntime;
    }
}

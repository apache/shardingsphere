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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPConversationArtifactsTest {
    
    @Test
    void assertAddRawModelOutput() {
        LLMMCPConversationArtifacts artifacts = new LLMMCPConversationArtifacts("provider", "model");
        artifacts.addRawModelOutput("raw-output");
        assertThat(artifacts.createArtifactBundle(createScenario(), LLME2EAssertionReport.success("ok")).getRawModelOutputs(), is(List.of("raw-output")));
    }
    
    @Test
    void assertAddInteractionTrace() {
        LLMMCPConversationArtifacts artifacts = new LLMMCPConversationArtifacts("provider", "model");
        MCPInteractionTraceRecord traceRecord = createTrace();
        artifacts.addInteractionTrace(traceRecord);
        assertThat(artifacts.getInteractionTrace(), is(List.of(traceRecord)));
    }
    
    @Test
    void assertAddRuntimeLogLine() {
        LLMMCPConversationArtifacts artifacts = new LLMMCPConversationArtifacts("provider", "model");
        artifacts.addRuntimeLogLine("runtime-log");
        assertThat(artifacts.createArtifactBundle(createScenario(), LLME2EAssertionReport.success("ok")).getMcpRuntimeLogLines(), is(List.of("runtime-log")));
    }
    
    @Test
    void assertNextSequence() {
        LLMMCPConversationArtifacts artifacts = new LLMMCPConversationArtifacts("provider", "model");
        artifacts.addInteractionTrace(createTrace());
        assertThat(artifacts.nextSequence(), is(2));
    }
    
    @Test
    void assertGetFinalAnswerJson() {
        assertThat(new LLMMCPConversationArtifacts("provider", "model").getFinalAnswerJson(), is(""));
    }
    
    @Test
    void assertSetFinalAnswerJson() {
        LLMMCPConversationArtifacts artifacts = new LLMMCPConversationArtifacts("provider", "model");
        artifacts.setFinalAnswerJson("{\"ok\":true}");
        assertThat(artifacts.getFinalAnswerJson(), is("{\"ok\":true}"));
    }
    
    @Test
    void assertCreateArtifactBundle() {
        LLMMCPConversationArtifacts artifacts = new LLMMCPConversationArtifacts("provider", "model");
        artifacts.setFinalAnswerJson("{\"ok\":true}");
        artifacts.addRawModelOutput("raw-output");
        MCPInteractionTraceRecord traceRecord = createTrace();
        artifacts.addInteractionTrace(traceRecord);
        artifacts.addRuntimeLogLine("runtime-log");
        LLME2EAssertionReport assertionReport = LLME2EAssertionReport.success("ok");
        LLME2EArtifactBundle actual = artifacts.createArtifactBundle(createScenario(), assertionReport);
        assertThat(actual.getScenarioId(), is("scenario"));
        assertThat(actual.getSystemPrompt(), is("system"));
        assertThat(actual.getUserPrompt(), is("user"));
        assertThat(actual.getModelProvider(), is("provider"));
        assertThat(actual.getModelName(), is("model"));
        assertThat(actual.getFinalAnswerJson(), is("{\"ok\":true}"));
        assertThat(actual.getRawModelOutputs(), is(List.of("raw-output")));
        assertThat(actual.getInteractionTrace(), is(List.of(traceRecord)));
        assertThat(actual.getMcpRuntimeLogLines(), is(List.of("runtime-log")));
        assertThat(actual.getAssertionReport(), is(assertionReport));
    }
    
    private LLME2EScenario createScenario() {
        return new LLME2EScenario("scenario", "system", "user", new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT 1", 1, List.of()), List.of(), List.of());
    }
    
    private MCPInteractionTraceRecord createTrace() {
        return new MCPInteractionTraceRecord(1, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, "database_gateway_execute_query", Map.of(), Map.of(), true, 0L);
    }
}

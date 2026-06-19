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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class LLMMCPConversationArtifacts {
    
    private final String modelProvider;
    
    private final String modelName;
    
    private final List<String> rawModelOutputs = new LinkedList<>();
    
    private final List<MCPInteractionTraceRecord> interactionTrace = new LinkedList<>();
    
    private final List<String> mcpRuntimeLogLines = new LinkedList<>();
    
    private String finalAnswerJson = "";
    
    void addRawModelOutput(final String rawModelOutput) {
        rawModelOutputs.add(rawModelOutput);
    }
    
    void addInteractionTrace(final MCPInteractionTraceRecord traceRecord) {
        interactionTrace.add(traceRecord);
    }
    
    void addRuntimeLogLine(final String runtimeLogLine) {
        mcpRuntimeLogLines.add(runtimeLogLine);
    }
    
    List<MCPInteractionTraceRecord> getInteractionTrace() {
        return interactionTrace;
    }
    
    int nextSequence() {
        return interactionTrace.size() + 1;
    }
    
    String getFinalAnswerJson() {
        return finalAnswerJson;
    }
    
    void setFinalAnswerJson(final String finalAnswerJson) {
        this.finalAnswerJson = finalAnswerJson;
    }
    
    LLME2EArtifactBundle createArtifactBundle(final LLME2EScenario scenario, final LLME2EAssertionReport assertionReport) {
        return new LLME2EArtifactBundle(scenario.getScenarioId(), scenario.getSystemPrompt(), scenario.getUserPrompt(), modelProvider, modelName,
                finalAnswerJson, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, assertionReport);
    }
}

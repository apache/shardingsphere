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

package org.apache.shardingsphere.test.e2e.mcp;

import java.util.List;

final class LLME2EArtifactBundle {
    
    private final String scenarioId;
    
    private final String systemPrompt;
    
    private final String userPrompt;
    
    private final String finalAnswerJson;
    
    private final List<String> rawModelOutputs;
    
    private final List<MCPInteractionTraceRecord> interactionTrace;
    
    private final List<String> mcpRuntimeLogLines;
    
    private final LLME2EAssertionReport assertionReport;
    
    LLME2EArtifactBundle(final String scenarioId, final String systemPrompt, final String userPrompt,
                         final String finalAnswerJson, final List<String> rawModelOutputs,
                         final List<MCPInteractionTraceRecord> interactionTrace, final List<String> mcpRuntimeLogLines,
                         final LLME2EAssertionReport assertionReport) {
        this.scenarioId = scenarioId;
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.finalAnswerJson = finalAnswerJson;
        this.rawModelOutputs = List.copyOf(rawModelOutputs);
        this.interactionTrace = List.copyOf(interactionTrace);
        this.mcpRuntimeLogLines = List.copyOf(mcpRuntimeLogLines);
        this.assertionReport = assertionReport;
    }
    
    String scenarioId() {
        return scenarioId;
    }
    
    String systemPrompt() {
        return systemPrompt;
    }
    
    String userPrompt() {
        return userPrompt;
    }
    
    String finalAnswerJson() {
        return finalAnswerJson;
    }
    
    List<String> rawModelOutputs() {
        return rawModelOutputs;
    }
    
    List<MCPInteractionTraceRecord> interactionTrace() {
        return interactionTrace;
    }
    
    List<String> mcpRuntimeLogLines() {
        return mcpRuntimeLogLines;
    }
    
    LLME2EAssertionReport assertionReport() {
        return assertionReport;
    }
}

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

import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

final class LLMMCPConversationTurnPlanner {
    
    private final LLMMCPConversationInstructionFactory instructionFactory;
    
    LLMMCPConversationTurnPlanner(final LLMMCPConversationInstructionFactory instructionFactory) {
        this.instructionFactory = instructionFactory;
    }
    
    List<String> createTurnToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!interactionTrace.isEmpty()) {
            String immediateActionName = instructionFactory.findImmediateNextActionName(interactionTrace.getLast());
            if (!immediateActionName.isEmpty() && scenario.getAllowedToolNames().contains(immediateActionName)) {
                return List.of(immediateActionName);
            }
        }
        if (instructionFactory.hasSideEffectExecutionNextAction(interactionTrace)) {
            List<String> readOnlyToolNames = findMissingReadOnlyToolNames(scenario, interactionTrace);
            if (!readOnlyToolNames.isEmpty()) {
                return List.of(readOnlyToolNames.getFirst());
            }
        }
        List<String> missingToolNames = findMissingAllowedToolNames(scenario, interactionTrace);
        return missingToolNames.isEmpty() ? scenario.getAllowedToolNames() : List.of(missingToolNames.getFirst());
    }
    
    String createToolChoice(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace, final boolean finalAnswerRequested) {
        if (finalAnswerRequested) {
            return "none";
        }
        return LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), interactionTrace) ? "auto" : "required";
    }
    
    private List<String> findMissingAllowedToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        return LLMMCPInteractionCoverage.findMissingRequiredInteractionNames(
                scenario.getRequiredToolNames(), interactionTrace).stream().filter(each -> scenario.getAllowedToolNames().contains(each)).collect(Collectors.toList());
    }
    
    private List<String> findMissingReadOnlyToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        List<String> result = new LinkedList<>();
        for (String each : LLMMCPInteractionCoverage.findMissingRequiredInteractionNames(scenario.getRequiredToolNames(), interactionTrace)) {
            if (scenario.getAllowedToolNames().contains(each) && isReadOnlyToolName(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isReadOnlyToolName(final String toolName) {
        return MCPInteractionActionNames.LIST_RESOURCES.equals(toolName)
                || MCPInteractionActionNames.READ_RESOURCE.equals(toolName)
                || MCPInteractionActionNames.LIST_PROMPTS.equals(toolName)
                || MCPInteractionActionNames.GET_PROMPT.equals(toolName)
                || MCPInteractionActionNames.COMPLETE.equals(toolName)
                || "database_gateway_search_metadata".equals(toolName)
                || "database_gateway_execute_query".equals(toolName);
    }
}

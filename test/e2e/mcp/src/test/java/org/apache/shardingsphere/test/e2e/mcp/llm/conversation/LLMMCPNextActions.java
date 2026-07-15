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
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Model-facing MCP next action extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LLMMCPNextActions {
    
    /**
     * Get next actions from a structured MCP payload.
     *
     * @param structuredContent structured content
     * @return next actions
     */
    public static List<Map<?, ?>> getNextActions(final Map<String, Object> structuredContent) {
        List<Map<?, ?>> result = new LinkedList<>();
        appendActions(result, structuredContent.get("next_actions"));
        Object recovery = structuredContent.get("recovery");
        if (recovery instanceof Map) {
            appendActions(result, ((Map<?, ?>) recovery).get("next_actions"));
        }
        return result;
    }
    
    /**
     * Judge whether the latest trace record has an immediate machine action.
     *
     * @param interactionTrace interaction trace
     * @return whether an immediate machine action is pending
     */
    static boolean hasPendingImmediateNextAction(final List<MCPInteractionTraceRecord> interactionTrace) {
        return !interactionTrace.isEmpty() && !findImmediateNextActionName(interactionTrace.getLast()).isEmpty();
    }
    
    /**
     * Find the immediate machine action name.
     *
     * @param traceRecord interaction trace record
     * @return immediate machine action name
     */
    static String findImmediateNextActionName(final MCPInteractionTraceRecord traceRecord) {
        for (Map<?, ?> each : getNextActions(traceRecord.getStructuredContent())) {
            String result = findMachineNextActionName(each);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }
    
    /**
     * Judge whether the latest trace record recommends side-effect execution.
     *
     * @param interactionTrace interaction trace
     * @return whether side-effect execution is recommended
     */
    static boolean hasSideEffectExecutionNextAction(final List<MCPInteractionTraceRecord> interactionTrace) {
        return !interactionTrace.isEmpty() && getNextActions(interactionTrace.getLast().getStructuredContent()).stream().anyMatch(LLMMCPSideEffectNextAction::isExecutionAction);
    }
    
    private static void appendActions(final List<Map<?, ?>> actions, final Object value) {
        if (!(value instanceof List)) {
            return;
        }
        for (Object each : (List<?>) value) {
            if (each instanceof Map) {
                actions.add((Map<?, ?>) each);
            }
        }
    }
    
    private static String findMachineNextActionName(final Map<?, ?> action) {
        if (LLMMCPSideEffectNextAction.isExecutionAction(action)) {
            return "";
        }
        String actionType = Objects.toString(action.get("type"), "").trim();
        if ("resource_read".equals(actionType) && !Objects.toString(action.get("resource_uri"), "").trim().isEmpty()) {
            return MCPInteractionActionNames.READ_RESOURCE;
        }
        if ("tool_call".equals(actionType)) {
            return Objects.toString(action.get("tool_name"), "").trim();
        }
        return "completion".equals(actionType) ? MCPInteractionActionNames.COMPLETE : "";
    }
}

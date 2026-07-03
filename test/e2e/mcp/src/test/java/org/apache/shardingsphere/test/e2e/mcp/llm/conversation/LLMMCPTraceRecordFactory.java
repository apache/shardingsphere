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

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.Map;
import java.util.Objects;

final class LLMMCPTraceRecordFactory {
    
    MCPInteractionTraceRecord createTraceRecord(final int sequence, final String actionName, final String actionOrigin, final Map<String, Object> args,
                                                final Map<String, Object> structuredContent, final long latencyMillis) {
        String bridgeActionOrigin = MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN;
        if (MCPInteractionActionNames.LIST_RESOURCES.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.RESOURCE_LIST_KIND, bridgeActionOrigin, MCPInteractionActionNames.LIST_RESOURCES,
                    Map.of(), structuredContent, true, latencyMillis);
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.RESOURCE_READ_KIND, bridgeActionOrigin, MCPInteractionActionNames.READ_RESOURCE,
                    Map.of("uri", Objects.toString(args.get("uri"), "").trim()), structuredContent, true, latencyMillis);
        }
        if (MCPInteractionActionNames.LIST_PROMPTS.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.PROMPT_LIST_KIND, bridgeActionOrigin, MCPInteractionActionNames.LIST_PROMPTS,
                    Map.of(), structuredContent, true, latencyMillis);
        }
        if (MCPInteractionActionNames.GET_PROMPT.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.PROMPT_GET_KIND, bridgeActionOrigin, MCPInteractionActionNames.GET_PROMPT,
                    Map.of("name", Objects.toString(args.get("name"), "").trim(), "arguments", LLMMCPJsonValues.castToMap(args.getOrDefault("arguments", Map.of()))),
                    structuredContent, true, latencyMillis);
        }
        if (MCPInteractionActionNames.COMPLETE.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.COMPLETION_KIND, bridgeActionOrigin, MCPInteractionActionNames.COMPLETE,
                    args, structuredContent, true, latencyMillis);
        }
        return new MCPInteractionTraceRecord(sequence, "tool_call", actionOrigin, actionName, args, structuredContent, true, latencyMillis);
    }
}

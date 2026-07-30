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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class LLMMCPActionExecutor {
    
    private final MCPInteractionClient mcpInteractionClient;
    
    Map<String, Object> executeSafely(final String actionName, final Map<String, Object> args) throws InterruptedException {
        try {
            return LLMConversationRunner.READ_RESOURCE_TOOL_NAME.equals(actionName)
                    ? mcpInteractionClient.readResource(getRequiredResourceUri(args))
                    : mcpInteractionClient.call(actionName, args);
        } catch (final IOException | IllegalStateException ex) {
            throw new IllegalStateException(String.format("MCP action `%s` failed: %s", actionName, ex.getMessage()), ex);
        }
    }
    
    private String getRequiredResourceUri(final Map<String, Object> args) {
        Object value = args.get("uri");
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException("Resource URI is required.");
        }
        return ((String) value).trim();
    }
}

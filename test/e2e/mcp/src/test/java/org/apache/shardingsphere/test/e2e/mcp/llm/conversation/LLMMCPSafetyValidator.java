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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class LLMMCPSafetyValidator {
    
    private static final String EXECUTION_MODE_PREVIEW = "preview";
    
    Optional<ValidationFailure> validate(final String actionName, final Map<String, Object> arguments) {
        if (LLMConversationRunner.READ_RESOURCE_TOOL_NAME.equals(actionName) && Objects.toString(arguments.get("uri"), "").trim().isEmpty()) {
            return Optional.of(new ValidationFailure("invalid_tool_arguments", "Model returned an empty resource URI."));
        }
        if ("database_gateway_execute_query".equals(actionName) && isExplain(arguments)) {
            return Optional.of(new ValidationFailure("invalid_tool_arguments",
                    "Model routed EXPLAIN SQL to database_gateway_execute_query instead of database_gateway_execute_explain_query."));
        }
        if ("database_gateway_execute_update".equals(actionName) && !EXECUTION_MODE_PREVIEW.equals(Objects.toString(arguments.get("execution_mode"), ""))) {
            return Optional.of(new ValidationFailure("unsafe_sql_execution_attempted",
                    "Model attempted to execute side-effecting SQL in an LLM E2E scenario."));
        }
        return Optional.empty();
    }
    
    private boolean isExplain(final Map<String, Object> arguments) {
        String sql = Objects.toString(arguments.get("sql"), "").trim();
        return "EXPLAIN".equalsIgnoreCase(sql) || 7 < sql.length() && sql.regionMatches(true, 0, "EXPLAIN", 0, 7)
                && (Character.isWhitespace(sql.charAt(7)) || '(' == sql.charAt(7));
    }
    
    record ValidationFailure(String failureType, String message) {
    }
}

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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SQLExecutionToolHandlerSupport {

    static boolean isReadOnlyStatement(final SupportedMCPStatement statementClass) {
        return SupportedMCPStatement.QUERY == statementClass || SupportedMCPStatement.EXPLAIN_ANALYZE == statementClass;
    }

    static SQLExecutionRequest createExecutionRequest(final MCPToolCall toolCall, final MCPToolArguments toolArguments, final String sql) {
        return new SQLExecutionRequest(toolCall.getSessionId(), toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), sql,
                toolArguments.getIntegerArgument("max_rows", 0), toolArguments.getIntegerArgument("timeout_ms", 0));
    }

    static void putIfNotEmpty(final Map<String, Object> target, final String key, final String value) {
        if (!value.isEmpty()) {
            target.put(key, value);
        }
    }
}

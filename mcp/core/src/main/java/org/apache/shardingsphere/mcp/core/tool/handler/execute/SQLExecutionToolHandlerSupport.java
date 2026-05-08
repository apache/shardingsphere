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
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidToolArgumentException;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SQLExecutionToolHandlerSupport {
    
    private static final int DEFAULT_MAX_ROWS = 100;
    
    private static final int MAX_ROWS_LIMIT = 5000;
    
    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 0;
    
    private static final int MAX_TIMEOUT_MILLISECONDS = 300000;
    
    static boolean isReadOnlyStatement(final SupportedMCPStatement statementClass) {
        return SupportedMCPStatement.QUERY == statementClass || SupportedMCPStatement.EXPLAIN_ANALYZE == statementClass;
    }
    
    static SQLExecutionRequest createExecutionRequest(final MCPToolCall toolCall, final MCPToolArguments toolArguments, final String sql, final String sourceTool) {
        return new SQLExecutionRequest(toolCall.getSessionId(), toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), sql,
                resolveMaxRows(toolArguments, sourceTool), getIntegerArgument(toolArguments, sourceTool, "timeout_ms", DEFAULT_TIMEOUT_MILLISECONDS, DEFAULT_TIMEOUT_MILLISECONDS,
                        MAX_TIMEOUT_MILLISECONDS, DEFAULT_TIMEOUT_MILLISECONDS));
    }
    
    private static int resolveMaxRows(final MCPToolArguments toolArguments, final String sourceTool) {
        int result = getIntegerArgument(toolArguments, sourceTool, "max_rows", DEFAULT_MAX_ROWS, 0, MAX_ROWS_LIMIT, DEFAULT_MAX_ROWS);
        return 0 == result ? DEFAULT_MAX_ROWS : result;
    }
    
    private static int getIntegerArgument(final MCPToolArguments toolArguments, final String sourceTool, final String argumentPath, final int defaultValue, final int minimumValue,
                                          final int maximumValue, final int suggestedValue) {
        try {
            return toolArguments.getIntegerArgument(argumentPath, defaultValue, minimumValue, maximumValue);
        } catch (final MCPInvalidRequestException ex) {
            throw new MCPInvalidToolArgumentException(sourceTool, sourceTool, argumentPath, minimumValue, maximumValue, suggestedValue, ex);
        }
    }
    
    static void putIfNotEmpty(final Map<String, Object> target, final String key, final String value) {
        if (!value.isEmpty()) {
            target.put(key, value);
        }
    }
}

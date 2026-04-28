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

package org.apache.shardingsphere.mcp.tool.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResultKind;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SQL execution response.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class SQLExecutionResponse implements MCPResponse {
    
    private final ExecuteQueryResultKind resultKind;
    
    private final SupportedMCPStatement statementClass;
    
    private final List<ExecuteQueryColumnDefinition> columns;
    
    private final List<List<Object>> rows;
    
    private final int affectedRows;
    
    private final String statementType;
    
    private final String status;
    
    private final String message;
    
    private final boolean truncated;
    
    /**
     * Create a result-set response.
     *
     * @param columns column definitions
     * @param rows result rows
     * @param truncated truncation flag
     * @return result-set response
     */
    public static SQLExecutionResponse resultSet(final List<ExecuteQueryColumnDefinition> columns, final List<List<Object>> rows, final boolean truncated) {
        return resultSet(SupportedMCPStatement.QUERY, "SELECT", columns, rows, truncated);
    }
    
    /**
     * Create a result-set response.
     *
     * @param statementClass statement class
     * @param statementType statement type
     * @param columns column definitions
     * @param rows result rows
     * @param truncated truncation flag
     * @return result-set response
     */
    public static SQLExecutionResponse resultSet(final SupportedMCPStatement statementClass, final String statementType,
                                                 final List<ExecuteQueryColumnDefinition> columns, final List<List<Object>> rows, final boolean truncated) {
        return new SQLExecutionResponse(ExecuteQueryResultKind.RESULT_SET, statementClass, normalizeColumns(columns), normalizeRows(rows), 0, statementType, "OK", "", truncated);
    }
    
    /**
     * Create an update-count response.
     *
     * @param statementType statement type
     * @param affectedRows affected row count
     * @return update-count response
     */
    public static SQLExecutionResponse updateCount(final String statementType, final int affectedRows) {
        return updateCount(SupportedMCPStatement.DML, statementType, affectedRows);
    }
    
    /**
     * Create an update-count response.
     *
     * @param statementClass statement class
     * @param statementType statement type
     * @param affectedRows affected row count
     * @return update-count response
     */
    public static SQLExecutionResponse updateCount(final SupportedMCPStatement statementClass, final String statementType, final int affectedRows) {
        return new SQLExecutionResponse(ExecuteQueryResultKind.UPDATE_COUNT, statementClass, Collections.emptyList(), Collections.emptyList(), affectedRows, statementType, "OK", "", false);
    }
    
    /**
     * Create a statement acknowledgement response.
     *
     * @param statementClass statement class
     * @param statementType statement type
     * @param message acknowledgement message
     * @return acknowledgement response
     */
    public static SQLExecutionResponse statementAck(final SupportedMCPStatement statementClass, final String statementType, final String message) {
        return new SQLExecutionResponse(ExecuteQueryResultKind.STATEMENT_ACK, statementClass, Collections.emptyList(), Collections.emptyList(), 0, statementType, "OK", message, false);
    }
    
    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(32, 1F);
        result.put("result_kind", resultKind.name().toLowerCase(Locale.ENGLISH));
        result.put("statement_class", statementClass.name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", statementType);
        result.put("status", status);
        if (ExecuteQueryResultKind.RESULT_SET == resultKind) {
            result.put("columns", columns);
            result.put("rows", rows);
        }
        if (ExecuteQueryResultKind.UPDATE_COUNT == resultKind) {
            result.put("affected_rows", affectedRows);
        }
        if (ExecuteQueryResultKind.STATEMENT_ACK == resultKind) {
            result.put("message", message);
        }
        result.put("truncated", truncated);
        return result;
    }
    
    private static List<ExecuteQueryColumnDefinition> normalizeColumns(final List<ExecuteQueryColumnDefinition> columns) {
        return null == columns ? Collections.emptyList() : columns;
    }
    
    private static List<List<Object>> normalizeRows(final List<List<Object>> rows) {
        return null == rows ? Collections.emptyList() : rows;
    }
}

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

package org.apache.shardingsphere.mcp.support.database.tool.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.support.database.protocol.ExecuteQueryResultKind;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * SQL execution response.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class SQLExecutionResponse implements MCPResponse {

    private static final int ROW_OBJECT_LIMIT = 100;

    private final ExecuteQueryResultKind resultKind;

    private final SupportedMCPStatement statementClass;

    private final List<ExecuteQueryColumnDefinition> columns;

    private final List<List<Object>> rows;

    private final int affectedRows;

    private final String statementType;

    private final String status;

    private final String message;

    private final boolean truncated;

    private final int appliedMaxRows;

    private final int appliedTimeoutMs;

    private final String normalizedSql;

    private final String responseMode;

    private final String executionMode;

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
        return new SQLExecutionResponse(ExecuteQueryResultKind.RESULT_SET, statementClass, normalizeColumns(columns), normalizeRows(rows), 0, statementType, "OK", "", truncated, 0, 0, "",
                MCPResponseMode.QUERY, "");
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
        return new SQLExecutionResponse(ExecuteQueryResultKind.UPDATE_COUNT, statementClass, Collections.emptyList(), Collections.emptyList(), affectedRows, statementType, "OK", "",
                false, 0, 0, "", MCPResponseMode.EXECUTED, "");
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
        return new SQLExecutionResponse(ExecuteQueryResultKind.STATEMENT_ACK, statementClass, Collections.emptyList(), Collections.emptyList(), 0, statementType, "OK", message, false,
                0, 0, "", MCPResponseMode.EXECUTED, "");
    }

    /**
     * Create a copy with applied execution hints.
     *
     * @param appliedMaxRows applied max rows argument
     * @param appliedTimeoutMs applied timeout milliseconds argument
     * @return response with execution hints
     */
    public SQLExecutionResponse withExecutionHints(final int appliedMaxRows, final int appliedTimeoutMs) {
        return new SQLExecutionResponse(resultKind, statementClass, columns, rows, affectedRows, statementType, status, message, truncated, appliedMaxRows, appliedTimeoutMs, normalizedSql,
                responseMode, executionMode);
    }

    /**
     * Create a copy with normalized SQL.
     *
     * @param normalizedSql normalized SQL
     * @return response with normalized SQL
     */
    public SQLExecutionResponse withNormalizedSql(final String normalizedSql) {
        return new SQLExecutionResponse(resultKind, statementClass, columns, rows, affectedRows, statementType, status, message, truncated, appliedMaxRows, appliedTimeoutMs,
                null == normalizedSql ? "" : normalizedSql, responseMode, executionMode);
    }

    /**
     * Create a copy with side-effect tool execution mode markers.
     *
     * @param executionMode execution mode
     * @return response with execution mode markers
     */
    public SQLExecutionResponse withExecutionMode(final String executionMode) {
        return new SQLExecutionResponse(resultKind, statementClass, columns, rows, affectedRows, statementType, status, message, truncated, appliedMaxRows, appliedTimeoutMs, normalizedSql,
                MCPResponseMode.EXECUTED, null == executionMode ? "" : executionMode);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(32, 1F);
        result.put("response_mode", responseMode);
        result.put("result_kind", resultKind.name().toLowerCase(Locale.ENGLISH));
        if (!executionMode.isEmpty()) {
            result.put("execution_mode", executionMode);
        }
        result.put("statement_class", statementClass.name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", statementType);
        result.put("status", status);
        if (!normalizedSql.isEmpty()) {
            result.put("normalized_sql", normalizedSql);
        }
        if (ExecuteQueryResultKind.RESULT_SET == resultKind) {
            result.put("columns", columns);
            result.put("rows", rows);
            appendRowObjects(result);
            result.put("returned_row_count", rows.size());
        }
        if (ExecuteQueryResultKind.UPDATE_COUNT == resultKind) {
            result.put("affected_rows", affectedRows);
        }
        if (ExecuteQueryResultKind.STATEMENT_ACK == resultKind) {
            result.put("message", message);
        }
        result.put("applied_max_rows", appliedMaxRows);
        result.put("applied_timeout_ms", appliedTimeoutMs);
        result.put("truncated", truncated);
        result.put("next_actions", createNextActions());
        return result;
    }

    private List<Map<String, Object>> createNextActions() {
        if (ExecuteQueryResultKind.RESULT_SET == resultKind && truncated) {
            return List.of(MCPNextActionUtils.askUser("The result was truncated by max_rows. Ask for a narrower SELECT, stronger WHERE clause, or smaller projection before retrying.",
                    List.of("sql")));
        }
        return List.of(createStopAction(ExecuteQueryResultKind.RESULT_SET == resultKind
                ? "Return the result rows to the user or ask a follow-up question if the user requested more analysis."
                : "Report the execution status to the user and stop unless the user asks for another operation."));
    }

    private Map<String, Object> createStopAction(final String reason) {
        return MCPNextActionUtils.stop(reason);
    }

    private void appendRowObjects(final Map<String, Object> payload) {
        String rowObjectStatus = getRowObjectStatus();
        payload.put("row_object_status", rowObjectStatus);
        if ("available".equals(rowObjectStatus)) {
            payload.put("row_objects", createRowObjects());
        }
    }

    private String getRowObjectStatus() {
        String unavailableStatus = getUnavailableRowObjectStatus();
        if (!unavailableStatus.isEmpty()) {
            return unavailableStatus;
        }
        return ROW_OBJECT_LIMIT < rows.size() ? "omitted_large_result" : "available";
    }

    private String getUnavailableRowObjectStatus() {
        if (columns.isEmpty()) {
            return rows.isEmpty() ? "" : "unnamed_columns";
        }
        Set<String> columnNames = new LinkedHashSet<>(columns.size());
        for (ExecuteQueryColumnDefinition each : columns) {
            if (null == each.getColumnName() || each.getColumnName().isBlank()) {
                return "unnamed_columns";
            }
            if (!columnNames.add(each.getColumnName())) {
                return "duplicate_column_labels";
            }
        }
        return "";
    }

    private List<Map<String, Object>> createRowObjects() {
        return rows.stream().map(this::createRowObject).toList();
    }

    private Map<String, Object> createRowObject(final List<Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(columns.size(), 1F);
        for (int i = 0; i < columns.size(); i++) {
            result.put(columns.get(i).getColumnName(), i < row.size() ? row.get(i) : null);
        }
        return result;
    }

    private static List<ExecuteQueryColumnDefinition> normalizeColumns(final List<ExecuteQueryColumnDefinition> columns) {
        return null == columns ? Collections.emptyList() : columns;
    }

    private static List<List<Object>> normalizeRows(final List<List<Object>> rows) {
        return null == rows ? Collections.emptyList() : rows;
    }
}

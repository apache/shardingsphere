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

package org.apache.shardingsphere.mcp.core.tool.payload;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionColumnDefinition;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResultKind;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * SQL execution payload.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLExecutionPayload implements MCPSuccessPayload {
    
    private static final int ROW_OBJECT_LIMIT = 100;
    
    private static final String EXECUTED_TRUNCATION_SUMMARY = " Returned rows were truncated; do not replay the statement automatically.";
    
    private static final String EXECUTED_TRUNCATION_ACTION = "The side-effecting statement already executed and returned truncated rows; do not replay it automatically. "
            + "Use a separate read-only query if more data is needed.";
    
    private final SQLExecutionResult executionResult;
    
    private final String responseMode;
    
    /**
     * Create a payload for a read-only query result.
     *
     * @param executionResult SQL execution result
     * @return query payload
     */
    public static SQLExecutionPayload query(final SQLExecutionResult executionResult) {
        return new SQLExecutionPayload(executionResult, MCPResponseMode.QUERY);
    }
    
    /**
     * Create a payload for an executed side-effecting statement result.
     *
     * @param executionResult SQL execution result
     * @return executed payload
     */
    public static SQLExecutionPayload executed(final SQLExecutionResult executionResult) {
        return new SQLExecutionPayload(executionResult, MCPResponseMode.EXECUTED);
    }
    
    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(33, 1F);
        result.put("response_mode", responseMode);
        result.put("result_kind", executionResult.getResultKind().name().toLowerCase(Locale.ENGLISH));
        if (isExecuted()) {
            result.put(MCPPayloadFieldNames.EXECUTION_MODE, "execute");
        }
        result.put("statement_class", executionResult.getStatementClass().name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", executionResult.getStatementType());
        result.put("status", "OK");
        result.put(MCPPayloadFieldNames.SUMMARY, createSummary());
        if (!executionResult.getNormalizedSql().isEmpty()) {
            result.put("normalized_sql", executionResult.getNormalizedSql());
        }
        if (SQLExecutionResultKind.RESULT_SET == executionResult.getResultKind()) {
            result.put("columns", executionResult.getColumns());
            result.put("rows", executionResult.getRows());
            appendRowObjects(result);
            result.put("returned_row_count", executionResult.getRows().size());
        }
        if (SQLExecutionResultKind.UPDATE_COUNT == executionResult.getResultKind()) {
            result.put("affected_rows", executionResult.getAffectedRows());
        }
        if (SQLExecutionResultKind.STATEMENT_ACK == executionResult.getResultKind()) {
            result.put(MCPPayloadFieldNames.MESSAGE, createStatementAcknowledgement());
        }
        result.put("applied_max_rows", executionResult.getAppliedMaxRows());
        result.put("applied_timeout_ms", executionResult.getAppliedTimeoutMs());
        result.put("truncated", executionResult.isTruncated());
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createNextActions());
        return result;
    }
    
    private boolean isExecuted() {
        return MCPResponseMode.EXECUTED.equals(responseMode);
    }
    
    private String createSummary() {
        return switch (executionResult.getResultKind()) {
            case RESULT_SET -> createResultSetSummary();
            case UPDATE_COUNT -> String.format("Executed %s statement and affected %d row(s).", executionResult.getStatementType(), executionResult.getAffectedRows());
            case STATEMENT_ACK -> createStatementAcknowledgement();
        };
    }
    
    private String createResultSetSummary() {
        String result = isExecuted()
                ? String.format("Executed side-effecting SQL (statement type %s) and returned %d row(s).", executionResult.getStatementType(), executionResult.getRows().size())
                : String.format("Executed %s statement and returned %d row(s).", executionResult.getStatementType(), executionResult.getRows().size());
        if (!executionResult.isTruncated()) {
            return result;
        }
        return result + (isExecuted() ? EXECUTED_TRUNCATION_SUMMARY : " Result was truncated.");
    }
    
    private String createStatementAcknowledgement() {
        switch (executionResult.getStatementType()) {
            case "BEGIN", "START TRANSACTION":
                return "Transaction started.";
            case "COMMIT":
                return "Transaction committed.";
            case "ROLLBACK":
                return "Transaction rolled back.";
            case "SAVEPOINT":
                return "Savepoint created.";
            case "ROLLBACK TO SAVEPOINT":
                return "Savepoint rolled back.";
            case "RELEASE SAVEPOINT":
                return "Savepoint released.";
            default:
                return "Statement executed.";
        }
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
        return ROW_OBJECT_LIMIT < executionResult.getRows().size() ? "omitted_large_result" : "available";
    }
    
    private String getUnavailableRowObjectStatus() {
        if (executionResult.getColumns().isEmpty()) {
            return executionResult.getRows().isEmpty() ? "" : "unnamed_columns";
        }
        Set<String> columnNames = new LinkedHashSet<>(executionResult.getColumns().size());
        for (SQLExecutionColumnDefinition each : executionResult.getColumns()) {
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
        return executionResult.getRows().stream().map(this::createRowObject).toList();
    }
    
    private Map<String, Object> createRowObject(final List<Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(executionResult.getColumns().size(), 1F);
        for (int i = 0; i < executionResult.getColumns().size(); i++) {
            result.put(executionResult.getColumns().get(i).getColumnName(), i < row.size() ? row.get(i) : null);
        }
        return result;
    }
    
    private List<Map<String, Object>> createNextActions() {
        if (SQLExecutionResultKind.RESULT_SET == executionResult.getResultKind() && executionResult.isTruncated()) {
            return isExecuted()
                    ? List.of(MCPNextActionUtils.stop(EXECUTED_TRUNCATION_ACTION))
                    : List.of(MCPNextActionUtils.askUser(
                            "The result was truncated by max_rows. Ask for a narrower SELECT, stronger WHERE clause, or smaller projection before retrying.", List.of("sql")));
        }
        return List.of(MCPNextActionUtils.stop(SQLExecutionResultKind.RESULT_SET == executionResult.getResultKind()
                ? "Return the result rows to the user or ask a follow-up question if the user requested more analysis."
                : "Report the execution status to the user and stop unless the user asks for another operation."));
    }
}

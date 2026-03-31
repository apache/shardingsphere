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

package org.apache.shardingsphere.mcp.protocol;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Unified response model for the MCP {@code execute_query} tool.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ExecuteQueryResponse {
    
    private final ExecuteQueryResultKind resultKind;
    
    private final List<ExecuteQueryColumnDefinition> columns;
    
    private final List<List<Object>> rows;
    
    private final int affectedRows;
    
    private final String statementType;
    
    private final String status;
    
    private final String message;
    
    private final boolean truncated;
    
    private final boolean successful;
    
    @Getter(AccessLevel.NONE)
    private final ExecuteQueryErrorDetail error;
    
    /**
     * Create a result-set response.
     *
     * @param columns column definitions
     * @param rows result rows
     * @param truncated truncation flag
     * @return result-set response
     */
    public static ExecuteQueryResponse resultSet(final List<ExecuteQueryColumnDefinition> columns, final List<List<Object>> rows, final boolean truncated) {
        return new ExecuteQueryResponse(ExecuteQueryResultKind.RESULT_SET, columns, rows, 0, "QUERY", "OK", "", truncated, true, createAbsentErrorDetail());
    }
    
    /**
     * Create an update-count response.
     *
     * @param statementType statement type
     * @param affectedRows affected row count
     * @return update-count response
     */
    public static ExecuteQueryResponse updateCount(final String statementType, final int affectedRows) {
        return new ExecuteQueryResponse(ExecuteQueryResultKind.UPDATE_COUNT, Collections.emptyList(), Collections.emptyList(), affectedRows,
                statementType, "OK", "", false, true, createAbsentErrorDetail());
    }
    
    /**
     * Create a statement acknowledgement response.
     *
     * @param statementType statement type
     * @param message acknowledgement message
     * @return acknowledgement response
     */
    public static ExecuteQueryResponse statementAck(final String statementType, final String message) {
        return new ExecuteQueryResponse(ExecuteQueryResultKind.STATEMENT_ACK, Collections.emptyList(), Collections.emptyList(), 0,
                statementType, "OK", message, false, true, createAbsentErrorDetail());
    }
    
    /**
     * Create an error response.
     *
     * @param errorCode unified error code
     * @param message error message
     * @return error response
     */
    public static ExecuteQueryResponse error(final MCPErrorCode errorCode, final String message) {
        ExecuteQueryErrorDetail errorDetail = new ExecuteQueryErrorDetail(errorCode, message);
        return new ExecuteQueryResponse(ExecuteQueryResultKind.STATEMENT_ACK, Collections.emptyList(), Collections.emptyList(), 0,
                "ERROR", "ERROR", errorDetail.getMessage(), false, false, errorDetail);
    }
    
    /**
     * Get the error detail when one exists.
     *
     * @return optional error detail
     */
    public Optional<ExecuteQueryErrorDetail> getError() {
        return successful ? Optional.empty() : Optional.of(error);
    }
    
    private static ExecuteQueryErrorDetail createAbsentErrorDetail() {
        return new ExecuteQueryErrorDetail(MCPErrorCode.INVALID_REQUEST, "");
    }
}

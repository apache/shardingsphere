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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Unified response model for the MCP {@code execute_query} tool.
 */
@Getter
public final class ExecuteQueryResponse {
    
    private final ResultKind resultKind;
    
    private final List<ColumnDefinition> columns;
    
    private final List<List<Object>> rows;
    
    private final int affectedRows;
    
    private final String statementType;
    
    private final String status;
    
    private final String message;
    
    private final boolean truncated;
    
    @Getter(AccessLevel.NONE)
    private final boolean errorPresent;
    
    @Getter(AccessLevel.NONE)
    private final ErrorDetail error;
    
    private ExecuteQueryResponse(final ResultKind resultKind, final List<ColumnDefinition> columns, final List<List<Object>> rows, final int affectedRows,
                                 final String statementType, final String status, final String message, final boolean truncated, final boolean errorPresent, final ErrorDetail error) {
        this.resultKind = resultKind;
        this.columns = columns;
        this.rows = rows;
        this.affectedRows = affectedRows;
        this.statementType = statementType;
        this.status = status;
        this.message = message;
        this.truncated = truncated;
        this.errorPresent = errorPresent;
        this.error = error;
    }
    
    /**
     * Create a result-set response.
     *
     * @param columns column definitions
     * @param rows result rows
     * @param truncated truncation flag
     * @return result-set response
     */
    public static ExecuteQueryResponse resultSet(final List<ColumnDefinition> columns, final List<List<Object>> rows, final boolean truncated) {
        return new ExecuteQueryResponse(ResultKind.RESULT_SET, columns, rows, 0, "QUERY", "OK", "", truncated, false, createAbsentErrorDetail());
    }
    
    /**
     * Create an update-count response.
     *
     * @param statementType statement type
     * @param affectedRows affected row count
     * @return update-count response
     */
    public static ExecuteQueryResponse updateCount(final String statementType, final int affectedRows) {
        return new ExecuteQueryResponse(ResultKind.UPDATE_COUNT, Collections.emptyList(), Collections.emptyList(), affectedRows,
                statementType, "OK", "", false, false, createAbsentErrorDetail());
    }
    
    /**
     * Create a statement acknowledgement response.
     *
     * @param statementType statement type
     * @param message acknowledgement message
     * @return acknowledgement response
     */
    public static ExecuteQueryResponse statementAck(final String statementType, final String message) {
        return new ExecuteQueryResponse(ResultKind.STATEMENT_ACK, Collections.emptyList(), Collections.emptyList(), 0,
                statementType, "OK", message, false, false, createAbsentErrorDetail());
    }
    
    /**
     * Create an error response.
     *
     * @param errorCode unified error code
     * @param message error message
     * @return error response
     */
    public static ExecuteQueryResponse error(final ErrorCode errorCode, final String message) {
        ErrorDetail errorDetail = new ErrorDetail(errorCode, message);
        return new ExecuteQueryResponse(ResultKind.STATEMENT_ACK, Collections.emptyList(), Collections.emptyList(), 0,
                "ERROR", "ERROR", errorDetail.getMessage(), false, true, errorDetail);
    }
    
    /**
     * Determine whether the response completed successfully.
     *
     * @return {@code true} when no unified error is attached
     */
    public boolean isSuccessful() {
        return !errorPresent;
    }
    
    /**
     * Get the error detail when one exists.
     *
     * @return optional error detail
     */
    public Optional<ErrorDetail> getError() {
        return errorPresent ? Optional.of(error) : Optional.empty();
    }
    
    private static ErrorDetail createAbsentErrorDetail() {
        return new ErrorDetail(ErrorCode.INVALID_REQUEST, "");
    }
}

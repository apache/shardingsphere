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

package org.apache.shardingsphere.mcp.support.database.tool.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;

import java.util.List;

/**
 * SQL execution result.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class SQLExecutionResult {
    
    private final SQLExecutionResultKind resultKind;
    
    private final SupportedMCPStatement statementClass;
    
    private final String statementType;
    
    private final List<SQLExecutionColumnDefinition> columns;
    
    private final List<List<Object>> rows;
    
    private final int affectedRows;
    
    private final boolean truncated;
    
    private final int appliedMaxRows;
    
    private final int appliedTimeoutMs;
    
    private final String normalizedSql;
    
    /**
     * Create a result-set result.
     *
     * @param statementClass statement class
     * @param statementType statement type
     * @param columns column definitions
     * @param rows result rows
     * @param truncated truncation flag
     * @param appliedMaxRows applied max rows argument
     * @param appliedTimeoutMs applied timeout milliseconds argument
     * @param normalizedSql normalized SQL
     * @return result-set result
     */
    public static SQLExecutionResult resultSet(final SupportedMCPStatement statementClass, final String statementType,
                                               final List<SQLExecutionColumnDefinition> columns, final List<List<Object>> rows, final boolean truncated,
                                               final int appliedMaxRows, final int appliedTimeoutMs, final String normalizedSql) {
        return new SQLExecutionResult(SQLExecutionResultKind.RESULT_SET, statementClass, statementType, columns, rows, 0, truncated,
                appliedMaxRows, appliedTimeoutMs, normalizedSql);
    }
    
    /**
     * Create an update-count result.
     *
     * @param statementClass statement class
     * @param statementType statement type
     * @param affectedRows affected row count
     * @param appliedMaxRows applied max rows argument
     * @param appliedTimeoutMs applied timeout milliseconds argument
     * @param normalizedSql normalized SQL
     * @return update-count result
     */
    public static SQLExecutionResult updateCount(final SupportedMCPStatement statementClass, final String statementType, final int affectedRows,
                                                 final int appliedMaxRows, final int appliedTimeoutMs, final String normalizedSql) {
        return new SQLExecutionResult(SQLExecutionResultKind.UPDATE_COUNT, statementClass, statementType, List.of(), List.of(), affectedRows, false,
                appliedMaxRows, appliedTimeoutMs, normalizedSql);
    }
    
    /**
     * Create a statement-acknowledgement result.
     *
     * @param statementClass statement class
     * @param statementType statement type
     * @param appliedMaxRows applied max rows argument
     * @param appliedTimeoutMs applied timeout milliseconds argument
     * @param normalizedSql normalized SQL
     * @return statement-acknowledgement result
     */
    public static SQLExecutionResult statementAck(final SupportedMCPStatement statementClass, final String statementType,
                                                  final int appliedMaxRows, final int appliedTimeoutMs, final String normalizedSql) {
        return new SQLExecutionResult(SQLExecutionResultKind.STATEMENT_ACK, statementClass, statementType, List.of(), List.of(), 0, false,
                appliedMaxRows, appliedTimeoutMs, normalizedSql);
    }
}

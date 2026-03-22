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

package org.apache.shardingsphere.mcp.execute;

import lombok.Getter;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.StatementClass;

import java.util.Objects;
import java.util.Optional;

/**
 * Classify one SQL statement into the MCP V1 statement classes.
 */
public final class StatementClassifier {
    
    /**
     * Classify one SQL statement.
     *
     * @param sql SQL text
     * @return classification result
     * @throws UnsupportedOperationException when the SQL is banned by contract
     * @throws IllegalArgumentException when the SQL is empty, multi-statement, or unsupported
     */
    public ClassificationResult classify(final String sql) {
        String actualSql = normalizeSingleStatement(sql);
        String upperSql = actualSql.toUpperCase();
        if (isBannedCommand(upperSql)) {
            throw new UnsupportedOperationException("Statement is banned by the MCP contract.");
        }
        if (upperSql.startsWith("EXPLAIN ANALYZE")) {
            return new ClassificationResult(StatementClass.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", actualSql, extractTargetObject(actualSql), extractSavepointName(actualSql));
        }
        if (upperSql.startsWith("ROLLBACK TO SAVEPOINT") || upperSql.startsWith("RELEASE SAVEPOINT") || upperSql.startsWith("SAVEPOINT ")) {
            return new ClassificationResult(StatementClass.SAVEPOINT, extractStatementType(upperSql), actualSql, Optional.empty(), extractSavepointName(actualSql));
        }
        if (isTransactionControlStatement(upperSql)) {
            return new ClassificationResult(StatementClass.TRANSACTION_CONTROL, extractStatementType(upperSql), actualSql, Optional.empty(), Optional.empty());
        }
        if (upperSql.startsWith("SELECT") || upperSql.startsWith("WITH")) {
            return new ClassificationResult(StatementClass.QUERY, "QUERY", actualSql, extractTargetObject(actualSql), Optional.empty());
        }
        if (upperSql.startsWith("INSERT") || upperSql.startsWith("UPDATE") || upperSql.startsWith("DELETE") || upperSql.startsWith("MERGE")) {
            return new ClassificationResult(StatementClass.DML, extractStatementType(upperSql), actualSql, extractTargetObject(actualSql), Optional.empty());
        }
        if (upperSql.startsWith("CREATE") || upperSql.startsWith("ALTER") || upperSql.startsWith("DROP") || upperSql.startsWith("TRUNCATE")) {
            return new ClassificationResult(StatementClass.DDL, extractStatementType(upperSql), actualSql, extractTargetObject(actualSql), Optional.empty());
        }
        if (upperSql.startsWith("GRANT") || upperSql.startsWith("REVOKE")) {
            return new ClassificationResult(StatementClass.DCL, extractStatementType(upperSql), actualSql, extractTargetObject(actualSql), Optional.empty());
        }
        throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
    }
    
    private String normalizeSingleStatement(final String sql) {
        String result = Objects.requireNonNull(sql, "sql cannot be null").trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("sql cannot be empty.");
        }
        if (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).trim();
        }
        if (result.contains(";")) {
            throw new IllegalArgumentException("Only one SQL statement is allowed.");
        }
        return result;
    }
    
    private boolean isBannedCommand(final String upperSql) {
        return upperSql.startsWith("USE ")
                || upperSql.startsWith("SET ")
                || upperSql.startsWith("COPY ")
                || upperSql.startsWith("LOAD ")
                || upperSql.startsWith("CALL ");
    }
    
    private boolean isTransactionControlStatement(final String upperSql) {
        return "BEGIN".equals(upperSql)
                || upperSql.startsWith("START TRANSACTION")
                || "COMMIT".equals(upperSql)
                || "ROLLBACK".equals(upperSql);
    }
    
    private String extractStatementType(final String upperSql) {
        if (upperSql.startsWith("START TRANSACTION")) {
            return "START TRANSACTION";
        }
        if (upperSql.startsWith("ROLLBACK TO SAVEPOINT")) {
            return "ROLLBACK TO SAVEPOINT";
        }
        if (upperSql.startsWith("RELEASE SAVEPOINT")) {
            return "RELEASE SAVEPOINT";
        }
        if (upperSql.startsWith("EXPLAIN ANALYZE")) {
            return "EXPLAIN ANALYZE";
        }
        return upperSql.split("\\s+")[0];
    }
    
    private Optional<String> extractTargetObject(final String sql) {
        String[] tokens = sql.replace(",", " ").split("\\s+");
        for (int index = 0; index < tokens.length - 1; index++) {
            String upperToken = tokens[index].toUpperCase();
            if ("FROM".equals(upperToken) || "INTO".equals(upperToken) || "UPDATE".equals(upperToken) || "TABLE".equals(upperToken) || "VIEW".equals(upperToken)) {
                return Optional.of(tokens[index + 1].replaceAll("[()]", ""));
            }
        }
        return Optional.empty();
    }
    
    private Optional<String> extractSavepointName(final String sql) {
        String[] tokens = sql.split("\\s+");
        if (tokens.length < 2) {
            return Optional.empty();
        }
        if ("SAVEPOINT".equalsIgnoreCase(tokens[0])) {
            return Optional.of(tokens[tokens.length - 1]);
        }
        if ("RELEASE".equalsIgnoreCase(tokens[0]) && tokens.length >= 3) {
            return Optional.of(tokens[tokens.length - 1]);
        }
        if ("ROLLBACK".equalsIgnoreCase(tokens[0]) && tokens.length >= 4) {
            return Optional.of(tokens[tokens.length - 1]);
        }
        return Optional.empty();
    }
    
    /**
     * Statement classification result.
     */
    @Getter
    public static final class ClassificationResult {
        
        private final StatementClass statementClass;
        
        private final String statementType;
        
        private final String normalizedSql;
        
        private final Optional<String> targetObjectName;
        
        private final Optional<String> savepointName;
        
        /**
         * Construct a statement classification result.
         *
         * @param statementClass statement class
         * @param statementType statement type
         * @param normalizedSql normalized SQL
         * @param targetObjectName target object name
         * @param savepointName savepoint name
         */
        public ClassificationResult(final StatementClass statementClass, final String statementType, final String normalizedSql,
                                    final Optional<String> targetObjectName, final Optional<String> savepointName) {
            this.statementClass = Objects.requireNonNull(statementClass, "statementClass cannot be null");
            this.statementType = Objects.requireNonNull(statementType, "statementType cannot be null");
            this.normalizedSql = Objects.requireNonNull(normalizedSql, "normalizedSql cannot be null");
            this.targetObjectName = Objects.requireNonNull(targetObjectName, "targetObjectName cannot be null");
            this.savepointName = Objects.requireNonNull(savepointName, "savepointName cannot be null");
        }
    }
}

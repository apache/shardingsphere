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

package org.apache.shardingsphere.mcp.tool.handler.execute;

import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;

/**
 * Classify one SQL statement into the MCP statement classes.
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
            return new ClassificationResult(SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", actualSql, extractTargetObject(actualSql), extractSavepointName(actualSql));
        }
        if (upperSql.startsWith("ROLLBACK TO SAVEPOINT") || upperSql.startsWith("RELEASE SAVEPOINT") || upperSql.startsWith("SAVEPOINT ")) {
            return new ClassificationResult(SupportedMCPStatement.SAVEPOINT, extractStatementType(upperSql), actualSql, "", extractSavepointName(actualSql));
        }
        if (isTransactionControlStatement(upperSql)) {
            return new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, extractStatementType(upperSql), actualSql, "", "");
        }
        if (upperSql.startsWith("SELECT") || upperSql.startsWith("WITH")) {
            return new ClassificationResult(SupportedMCPStatement.QUERY, "QUERY", actualSql, extractTargetObject(actualSql), "");
        }
        if (upperSql.startsWith("INSERT") || upperSql.startsWith("UPDATE") || upperSql.startsWith("DELETE") || upperSql.startsWith("MERGE")) {
            return new ClassificationResult(SupportedMCPStatement.DML, extractStatementType(upperSql), actualSql, extractTargetObject(actualSql), "");
        }
        if (upperSql.startsWith("CREATE") || upperSql.startsWith("ALTER") || upperSql.startsWith("DROP") || upperSql.startsWith("TRUNCATE")) {
            return new ClassificationResult(SupportedMCPStatement.DDL, extractStatementType(upperSql), actualSql, extractTargetObject(actualSql), "");
        }
        if (upperSql.startsWith("GRANT") || upperSql.startsWith("REVOKE")) {
            return new ClassificationResult(SupportedMCPStatement.DCL, extractStatementType(upperSql), actualSql, extractTargetObject(actualSql), "");
        }
        throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
    }
    
    private String normalizeSingleStatement(final String sql) {
        String result = sql.trim();
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
    
    private String extractTargetObject(final String sql) {
        String[] tokens = sql.replace(",", " ").split("\\s+");
        for (int index = 0; index < tokens.length - 1; index++) {
            String upperToken = tokens[index].toUpperCase();
            if ("FROM".equals(upperToken) || "INTO".equals(upperToken) || "UPDATE".equals(upperToken) || "TABLE".equals(upperToken) || "VIEW".equals(upperToken)) {
                return tokens[index + 1].replaceAll("[()]", "");
            }
        }
        return "";
    }
    
    private String extractSavepointName(final String sql) {
        String[] tokens = sql.split("\\s+");
        if (tokens.length < 2) {
            return "";
        }
        if ("SAVEPOINT".equalsIgnoreCase(tokens[0])) {
            return tokens[tokens.length - 1];
        }
        if ("RELEASE".equalsIgnoreCase(tokens[0]) && tokens.length >= 3) {
            return tokens[tokens.length - 1];
        }
        if ("ROLLBACK".equalsIgnoreCase(tokens[0]) && tokens.length >= 4) {
            return tokens[tokens.length - 1];
        }
        return "";
    }
}

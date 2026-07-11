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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;

import java.util.List;

final class ExplainSQLCandidateValidator {
    
    private final StatementClassifier statementClassifier = new StatementClassifier();
    
    private final SQLStatementScanner scanner = new SQLStatementScanner();
    
    ClassificationResult validate(final String sql, final String explainSql) {
        ClassificationResult explainedStatement = statementClassifier.classify(sql);
        ShardingSpherePreconditions.checkState(SupportedMCPStatement.QUERY == explainedStatement.getStatementClass(),
                () -> new MCPInvalidRequestException("database_gateway_execute_explain_query only supports QUERY statements as the explained SQL."));
        String actualExplainSql = scanner.normalizeSingleStatement(explainSql);
        List<SQLStatementToken> tokens = scanner.tokenize(actualExplainSql);
        checkExplainCandidate(tokens, sql, actualExplainSql);
        return new ClassificationResult(SupportedMCPStatement.EXPLAIN, "EXPLAIN", actualExplainSql, explainedStatement.getTargetObjectName().orElse(""), "",
                SupportedMCPStatement.QUERY, explainedStatement.getReferencedObjectNames());
    }
    
    private void checkExplainCandidate(final List<SQLStatementToken> tokens, final String sql, final String explainSql) {
        ShardingSpherePreconditions.checkState(!tokens.isEmpty() && scanner.isKeyword(tokens.get(0), "EXPLAIN"),
                () -> new MCPInvalidRequestException("explain_sql must start with EXPLAIN."));
        ShardingSpherePreconditions.checkState(!containsKeywordSequence(tokens, "EXPLAIN", "PLAN", "FOR"),
                () -> new MCPInvalidRequestException("EXPLAIN PLAN FOR workflows are not supported by the MCP explain query tool."));
        String explainPrefix = extractExplainPrefix(explainSql, sql);
        List<SQLStatementToken> explainPrefixTokens = scanner.tokenize(explainPrefix);
        ShardingSpherePreconditions.checkState(!containsKeyword(explainPrefixTokens, "ANALYZE"),
                () -> new MCPInvalidRequestException("EXPLAIN ANALYZE is not supported by the MCP explain query tool."));
        ShardingSpherePreconditions.checkState(!containsKeyword(explainPrefixTokens, "INTO"),
                () -> new MCPInvalidRequestException("EXPLAIN output redirection is not supported by the MCP explain query tool."));
    }
    
    private String extractExplainPrefix(final String explainSql, final String sql) {
        String normalizedExplainSql = normalizeForComparison(explainSql);
        String normalizedSql = normalizeForComparison(sql);
        ShardingSpherePreconditions.checkState(normalizedExplainSql.endsWith(normalizedSql),
                () -> new MCPInvalidRequestException("explain_sql must include the original sql argument without rewriting it."));
        return normalizedExplainSql.substring(0, normalizedExplainSql.length() - normalizedSql.length());
    }
    
    private boolean containsKeyword(final List<SQLStatementToken> tokens, final String keyword) {
        for (SQLStatementToken each : tokens) {
            if (scanner.isKeyword(each, keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsKeywordSequence(final List<SQLStatementToken> tokens, final String... keywords) {
        for (int index = 0; index + keywords.length <= tokens.size(); index++) {
            if (containsKeywordSequence(tokens, index, keywords)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsKeywordSequence(final List<SQLStatementToken> tokens, final int startIndex, final String... keywords) {
        for (int index = 0; index < keywords.length; index++) {
            if (!scanner.isKeyword(tokens.get(startIndex + index), keywords[index])) {
                return false;
            }
        }
        return true;
    }
    
    private String normalizeForComparison(final String sql) {
        return scanner.normalizeSingleStatement(sql).replaceAll("\\s+", " ").trim();
    }
}

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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ExplainSQLCandidateValidator {
    
    private static final String EXPLAIN_PREFIX = "EXPLAIN ";
    
    private final MCPStatementAnalyzer statementAnalyzer;
    
    ClassificationResult validate(final String sql, final String explainSql, final MCPDatabaseCapability databaseCapability) {
        ClassificationResult explainedStatement = statementAnalyzer.analyze(sql, databaseCapability);
        ShardingSpherePreconditions.checkState(SupportedMCPStatement.QUERY == explainedStatement.getStatementClass(),
                () -> new MCPInvalidRequestException("database_gateway_execute_explain_query only supports QUERY statements as the explained SQL."));
        String actualExplainSql = new SQLStatementScanner(databaseCapability.getDatabaseType(), explainSql).sql();
        ShardingSpherePreconditions.checkState(actualExplainSql.regionMatches(true, 0, EXPLAIN_PREFIX, 0, EXPLAIN_PREFIX.length()),
                () -> new MCPInvalidRequestException("explain_sql must start with EXPLAIN."));
        ShardingSpherePreconditions.checkState(actualExplainSql.substring(EXPLAIN_PREFIX.length()).equals(explainedStatement.getNormalizedSql()),
                () -> new MCPInvalidRequestException("explain_sql must be EXPLAIN followed by the original sql argument without rewriting it."));
        return new ClassificationResult(SupportedMCPStatement.EXPLAIN, "EXPLAIN", actualExplainSql, "", explainedStatement.getReferencedObjects(), false);
    }
}

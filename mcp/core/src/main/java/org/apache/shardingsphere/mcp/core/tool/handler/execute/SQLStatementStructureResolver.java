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
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SQLStatementStructureResolver {
    
    private final SQLStatementScanner scanner;
    
    SQLStatementStructure resolve(final String sql) {
        int startIndex = scanner.skipInsignificant(sql, 0);
        if (!scanner.matchesKeyword(sql, startIndex, "WITH")) {
            String mainSql = sql.substring(startIndex).trim();
            return new SQLStatementStructure(mainSql, scanner.extractLeadingKeyword(mainSql), false, List.of());
        }
        return resolveWithClause(sql, startIndex);
    }
    
    private SQLStatementStructure resolveWithClause(final String sql, final int startIndex) {
        int currentIndex = scanner.skipKeyword(sql, startIndex, "WITH");
        currentIndex = scanner.skipInsignificant(sql, currentIndex);
        if (scanner.matchesKeyword(sql, currentIndex, "RECURSIVE")) {
            currentIndex = scanner.skipKeyword(sql, currentIndex, "RECURSIVE");
        }
        Collection<SQLCommonTableExpression> commonTableExpressions = new LinkedList<>();
        while (currentIndex < sql.length()) {
            CommonTableExpressionResolution commonTableExpression = resolveCommonTableExpression(sql, currentIndex);
            commonTableExpressions.add(new SQLCommonTableExpression(commonTableExpression.aliasName(), resolve(commonTableExpression.sql())));
            currentIndex = commonTableExpression.nextIndex();
            if (currentIndex < sql.length() && ',' == sql.charAt(currentIndex)) {
                currentIndex++;
                continue;
            }
            break;
        }
        String mainSql = sql.substring(scanner.skipInsignificant(sql, currentIndex)).trim();
        if (mainSql.isEmpty()) {
            throw new MCPUnsupportedSQLStatementException();
        }
        return new SQLStatementStructure(mainSql, scanner.extractLeadingKeyword(mainSql), containsDataModifyingCommonTableExpression(commonTableExpressions), commonTableExpressions);
    }
    
    private CommonTableExpressionResolution resolveCommonTableExpression(final String sql, final int startIndex) {
        int result = scanner.skipInsignificant(sql, startIndex);
        result = scanner.skipIdentifier(sql, result);
        result = scanner.skipInsignificant(sql, result);
        if (result < sql.length() && '(' == sql.charAt(result)) {
            result = scanner.skipParenthesizedSegment(sql, result);
        }
        result = scanner.skipKeyword(sql, result, "AS");
        result = scanner.skipInsignificant(sql, result);
        if (scanner.matchesKeyword(sql, result, "NOT")) {
            result = scanner.skipKeyword(sql, result, "NOT");
            result = scanner.skipInsignificant(sql, result);
        }
        if (scanner.matchesKeyword(sql, result, "MATERIALIZED")) {
            result = scanner.skipKeyword(sql, result, "MATERIALIZED");
            result = scanner.skipInsignificant(sql, result);
        }
        if (result >= sql.length() || '(' != sql.charAt(result)) {
            throw new MCPUnsupportedSQLStatementException();
        }
        int stopIndex = scanner.findClosingParenthesis(sql, result);
        int aliasStartIndex = scanner.skipInsignificant(sql, startIndex);
        int aliasStopIndex = scanner.skipIdentifier(sql, aliasStartIndex);
        return new CommonTableExpressionResolution(scanner.normalizeIdentifier(sql.substring(aliasStartIndex, aliasStopIndex)),
                sql.substring(result + 1, stopIndex).trim(), scanner.skipInsignificant(sql, stopIndex + 1));
    }
    
    private boolean containsDataModifyingCommonTableExpression(final Collection<SQLCommonTableExpression> commonTableExpressions) {
        for (SQLCommonTableExpression each : commonTableExpressions) {
            if (isDataModifyingStatement(each.statementStructure().statementType()) || each.statementStructure().containsDataModifyingCommonTableExpression()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isDataModifyingStatement(final String statementType) {
        return "INSERT".equals(statementType) || "UPDATE".equals(statementType) || "DELETE".equals(statementType) || "MERGE".equals(statementType);
    }
    
    private record CommonTableExpressionResolution(String aliasName, String sql, int nextIndex) {
    }
}

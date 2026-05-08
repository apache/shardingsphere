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

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

final class SQLStatementTargetResolver {
    
    private final SQLStatementScanner scanner;
    
    private final SQLStatementClassResolver statementClassResolver;
    
    SQLStatementTargetResolver(final SQLStatementScanner scanner, final SQLStatementClassResolver statementClassResolver) {
        this.scanner = scanner;
        this.statementClassResolver = statementClassResolver;
    }
    
    String resolve(final SQLStatementStructure statementStructure, final SupportedMCPStatement statementClass) {
        return resolve(statementStructure, statementClass, new LinkedList<>());
    }
    
    private String resolve(final SQLStatementStructure statementStructure, final SupportedMCPStatement statementClass, final Collection<String> visitedAliases) {
        if ("SELECT".equals(statementStructure.statementType())) {
            String result = extractSelectTargetObjectName(statementStructure, visitedAliases);
            if (!result.isEmpty()) {
                return result;
            }
            return SupportedMCPStatement.DML == statementClass ? extractDataModifyingTargetObjectName(statementStructure, visitedAliases) : "";
        }
        return extractDirectTargetObjectName(statementStructure.mainSql(), statementStructure.statementType());
    }
    
    private String extractSelectTargetObjectName(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases) {
        List<SQLStatementToken> tokens = scanner.tokenize(statementStructure.mainSql());
        for (int each : findKeywordIndexes(tokens, "FROM")) {
            String sourceObjectName = readObjectName(tokens, each + 1, "ONLY");
            if (sourceObjectName.isEmpty()) {
                continue;
            }
            SQLCommonTableExpression commonTableExpression = findCommonTableExpression(statementStructure, sourceObjectName);
            if (null == commonTableExpression) {
                return sourceObjectName;
            }
            String normalizedAliasName = scanner.normalizeIdentifierForComparison(commonTableExpression.aliasName());
            if (!visitedAliases.contains(normalizedAliasName)) {
                String result = resolve(commonTableExpression.statementStructure(), statementClassResolver.resolve(commonTableExpression.statementStructure()),
                        appendVisitedAlias(visitedAliases, normalizedAliasName));
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return "";
    }
    
    private String extractDirectTargetObjectName(final String sql, final String statementType) {
        List<SQLStatementToken> tokens = scanner.tokenize(sql);
        if ("INSERT".equals(statementType) || "MERGE".equals(statementType)) {
            return extractObjectNameAfterKeyword(tokens, "INTO");
        }
        if ("UPDATE".equals(statementType)) {
            return extractObjectNameAfterKeyword(tokens, "UPDATE", "ONLY");
        }
        if ("DELETE".equals(statementType)) {
            return extractObjectNameAfterKeyword(tokens, "FROM", "ONLY");
        }
        if ("CREATE".equals(statementType)) {
            return extractObjectNameAfterTypeKeyword(tokens, List.of("TABLE", "VIEW", "INDEX", "SEQUENCE", "DATABASE", "SCHEMA"), "IF", "NOT", "EXISTS");
        }
        if ("ALTER".equals(statementType)) {
            return extractObjectNameAfterTypeKeyword(tokens, List.of("TABLE", "VIEW", "INDEX", "SEQUENCE", "DATABASE", "SCHEMA"), "ONLY");
        }
        if ("DROP".equals(statementType)) {
            return extractObjectNameAfterTypeKeyword(tokens, List.of("TABLE", "VIEW", "INDEX", "SEQUENCE", "DATABASE", "SCHEMA"), "IF", "EXISTS");
        }
        if ("TRUNCATE".equals(statementType)) {
            String result = extractObjectNameAfterKeyword(tokens, "TABLE", "ONLY");
            return result.isEmpty() ? readObjectName(tokens, 1, "ONLY") : result;
        }
        if ("GRANT".equals(statementType) || "REVOKE".equals(statementType)) {
            return extractObjectNameAfterKeyword(tokens, "ON", "TABLE", "VIEW", "INDEX", "SEQUENCE", "DATABASE", "SCHEMA");
        }
        return "";
    }
    
    private String extractDataModifyingTargetObjectName(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases) {
        for (SQLCommonTableExpression each : statementStructure.commonTableExpressions()) {
            SupportedMCPStatement statementClass = statementClassResolver.resolve(each.statementStructure());
            if (SupportedMCPStatement.DML != statementClass) {
                continue;
            }
            String normalizedAliasName = scanner.normalizeIdentifierForComparison(each.aliasName());
            if (visitedAliases.contains(normalizedAliasName)) {
                continue;
            }
            String result = resolve(each.statementStructure(), statementClass, appendVisitedAlias(visitedAliases, normalizedAliasName));
            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }
    
    private Collection<String> appendVisitedAlias(final Collection<String> visitedAliases, final String aliasName) {
        Collection<String> result = new LinkedList<>(visitedAliases);
        result.add(aliasName);
        return result;
    }
    
    private SQLCommonTableExpression findCommonTableExpression(final SQLStatementStructure statementStructure, final String aliasName) {
        String normalizedAliasName = scanner.normalizeIdentifierForComparison(aliasName);
        for (SQLCommonTableExpression each : statementStructure.commonTableExpressions()) {
            if (scanner.normalizeIdentifierForComparison(each.aliasName()).equals(normalizedAliasName)) {
                return each;
            }
        }
        return null;
    }
    
    private String extractObjectNameAfterTypeKeyword(final List<SQLStatementToken> tokens, final List<String> typeKeywords, final String... optionalKeywords) {
        for (int index = 0; index < tokens.size(); index++) {
            if (typeKeywords.contains(tokens.get(index).upperText())) {
                return readObjectName(tokens, index + 1, optionalKeywords);
            }
        }
        return "";
    }
    
    private String extractObjectNameAfterKeyword(final List<SQLStatementToken> tokens, final String keyword, final String... optionalKeywords) {
        for (int index = 0; index < tokens.size(); index++) {
            if (scanner.isKeyword(tokens.get(index), keyword)) {
                return readObjectName(tokens, index + 1, optionalKeywords);
            }
        }
        return "";
    }
    
    private String readObjectName(final List<SQLStatementToken> tokens, final int startIndex, final String... optionalKeywords) {
        int result = startIndex;
        while (result < tokens.size()) {
            SQLStatementToken token = tokens.get(result);
            if (scanner.isKeyword(token, optionalKeywords)) {
                result++;
                continue;
            }
            if ("(".equals(token.text())) {
                return "";
            }
            return readQualifiedName(tokens, result);
        }
        return "";
    }
    
    private String readQualifiedName(final List<SQLStatementToken> tokens, final int startIndex) {
        if (startIndex >= tokens.size() || !tokens.get(startIndex).identifier()) {
            return "";
        }
        StringBuilder result = new StringBuilder(scanner.normalizeIdentifier(tokens.get(startIndex).text()));
        int currentIndex = startIndex + 1;
        while (currentIndex + 1 < tokens.size() && ".".equals(tokens.get(currentIndex).text()) && tokens.get(currentIndex + 1).identifier()) {
            result.append('.').append(scanner.normalizeIdentifier(tokens.get(currentIndex + 1).text()));
            currentIndex += 2;
        }
        return result.toString();
    }
    
    private List<Integer> findKeywordIndexes(final List<SQLStatementToken> tokens, final String keyword) {
        List<Integer> result = new ArrayList<>(tokens.size());
        for (int index = 0; index < tokens.size(); index++) {
            if (scanner.isKeyword(tokens.get(index), keyword)) {
                result.add(index);
            }
        }
        return result;
    }
}

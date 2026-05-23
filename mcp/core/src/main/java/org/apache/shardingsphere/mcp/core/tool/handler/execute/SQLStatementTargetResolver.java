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
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SQLStatementTargetResolver {
    
    private final SQLStatementScanner scanner;
    
    private final SQLStatementStructureResolver structureResolver;
    
    private final SQLStatementClassResolver statementClassResolver;
    
    String resolve(final SQLStatementStructure statementStructure, final SupportedMCPStatement statementClass) {
        Collection<String> objectNames = resolveAll(statementStructure, statementClass);
        return objectNames.isEmpty() ? "" : objectNames.iterator().next();
    }
    
    Collection<String> resolveAll(final SQLStatementStructure statementStructure, final SupportedMCPStatement statementClass) {
        Set<String> result = new LinkedHashSet<>(16, 1F);
        collect(statementStructure, statementClass, new LinkedList<>(), result);
        return result;
    }
    
    private void collect(final SQLStatementStructure statementStructure, final SupportedMCPStatement statementClass, final Collection<String> visitedAliases, final Collection<String> result) {
        if ("SELECT".equals(statementStructure.statementType())) {
            collectSelectTargetObjectNames(statementStructure, visitedAliases, result);
            if (SupportedMCPStatement.DML == statementClass) {
                collectDataModifyingTargetObjectNames(statementStructure, visitedAliases, result);
            }
            return;
        }
        addObjectName(result, extractDirectTargetObjectName(statementStructure.mainSql(), statementStructure.statementType()));
        collectClauseObjectNames(statementStructure, visitedAliases, result, "FROM", "JOIN", "USING");
    }
    
    private void collectSelectTargetObjectNames(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases, final Collection<String> result) {
        collectClauseObjectNames(statementStructure, visitedAliases, result, "FROM", "JOIN");
    }
    
    private void collectClauseObjectNames(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases, final Collection<String> result, final String... keywords) {
        List<SQLStatementToken> tokens = scanner.tokenize(statementStructure.mainSql());
        for (int each : findKeywordIndexes(tokens, keywords)) {
            String objectName = readObjectName(tokens, each + 1, "ONLY");
            if (!objectName.isEmpty()) {
                collectObjectName(statementStructure, objectName, visitedAliases, result);
            }
        }
        collectNestedQueryObjectNames(tokens, visitedAliases, result);
    }
    
    private void collectNestedQueryObjectNames(final List<SQLStatementToken> tokens, final Collection<String> visitedAliases, final Collection<String> result) {
        int index = 0;
        while (index < tokens.size()) {
            if (!"(".equals(tokens.get(index).text())) {
                index++;
                continue;
            }
            int closingParenthesisIndex = findClosingParenthesis(tokens, index);
            if (isNestedQuery(tokens, index + 1, closingParenthesisIndex)) {
                SQLStatementStructure statementStructure = structureResolver.resolve(reconstructSql(tokens, index + 1, closingParenthesisIndex));
                collect(statementStructure, statementClassResolver.resolve(statementStructure), visitedAliases, result);
                index = closingParenthesisIndex + 1;
                continue;
            }
            index++;
        }
    }
    
    private void collectObjectName(final SQLStatementStructure statementStructure, final String objectName, final Collection<String> visitedAliases, final Collection<String> result) {
        Optional<SQLCommonTableExpression> commonTableExpression = findCommonTableExpression(statementStructure, objectName);
        if (commonTableExpression.isEmpty()) {
            addObjectName(result, objectName);
            return;
        }
        SQLCommonTableExpression actualCommonTableExpression = commonTableExpression.get();
        String normalizedAliasName = scanner.normalizeIdentifierForComparison(actualCommonTableExpression.aliasName());
        if (!visitedAliases.contains(normalizedAliasName)) {
            collect(actualCommonTableExpression.statementStructure(), statementClassResolver.resolve(actualCommonTableExpression.statementStructure()),
                    appendVisitedAlias(visitedAliases, normalizedAliasName), result);
        }
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
    
    private void collectDataModifyingTargetObjectNames(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases, final Collection<String> result) {
        for (SQLCommonTableExpression each : statementStructure.commonTableExpressions()) {
            SupportedMCPStatement statementClass = statementClassResolver.resolve(each.statementStructure());
            if (SupportedMCPStatement.DML != statementClass) {
                continue;
            }
            String normalizedAliasName = scanner.normalizeIdentifierForComparison(each.aliasName());
            if (visitedAliases.contains(normalizedAliasName)) {
                continue;
            }
            collect(each.statementStructure(), statementClass, appendVisitedAlias(visitedAliases, normalizedAliasName), result);
        }
    }
    
    private Collection<String> appendVisitedAlias(final Collection<String> visitedAliases, final String aliasName) {
        Collection<String> result = new LinkedList<>(visitedAliases);
        result.add(aliasName);
        return result;
    }
    
    private Optional<SQLCommonTableExpression> findCommonTableExpression(final SQLStatementStructure statementStructure, final String aliasName) {
        String normalizedAliasName = scanner.normalizeIdentifierForComparison(aliasName);
        for (SQLCommonTableExpression each : statementStructure.commonTableExpressions()) {
            if (scanner.normalizeIdentifierForComparison(each.aliasName()).equals(normalizedAliasName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
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
    
    private void addObjectName(final Collection<String> result, final String objectName) {
        if (!objectName.isEmpty()) {
            result.add(objectName);
        }
    }
    
    private List<Integer> findKeywordIndexes(final List<SQLStatementToken> tokens, final String... keywords) {
        List<Integer> result = new ArrayList<>(tokens.size());
        int parenthesesDepth = 0;
        for (int index = 0; index < tokens.size(); index++) {
            if ("(".equals(tokens.get(index).text())) {
                parenthesesDepth++;
                continue;
            }
            if (")".equals(tokens.get(index).text())) {
                parenthesesDepth--;
                continue;
            }
            if (0 == parenthesesDepth && scanner.isKeyword(tokens.get(index), keywords)) {
                result.add(index);
            }
        }
        return result;
    }
    
    private int findClosingParenthesis(final List<SQLStatementToken> tokens, final int startIndex) {
        int parenthesesDepth = 0;
        for (int index = startIndex; index < tokens.size(); index++) {
            if ("(".equals(tokens.get(index).text())) {
                parenthesesDepth++;
                continue;
            }
            if (")".equals(tokens.get(index).text())) {
                parenthesesDepth--;
                if (0 == parenthesesDepth) {
                    return index;
                }
            }
        }
        return tokens.size();
    }
    
    private boolean isNestedQuery(final List<SQLStatementToken> tokens, final int startIndex, final int stopIndex) {
        return startIndex < stopIndex && scanner.isKeyword(tokens.get(startIndex), "SELECT", "WITH");
    }
    
    private String reconstructSql(final List<SQLStatementToken> tokens, final int startIndex, final int stopIndex) {
        StringBuilder result = new StringBuilder();
        for (int index = startIndex; index < stopIndex; index++) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(tokens.get(index).text());
        }
        return result.toString();
    }
}

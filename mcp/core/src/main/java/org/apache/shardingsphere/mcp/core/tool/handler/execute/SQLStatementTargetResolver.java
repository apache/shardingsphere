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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SQLStatementTargetResolver {
    
    private static final List<String> SQL_OBJECT_TYPE_KEYWORDS = List.of("TABLE", "VIEW", "INDEX", "SEQUENCE", "DATABASE", "SCHEMA", "TYPE", "FUNCTION", "PROCEDURE", "TRIGGER", "POLICY");
    
    private static final List<String> OBJECT_LIST_STOP_KEYWORDS = List.of("JOIN", "WHERE", "ON", "USING", "SET", "VALUES", "RETURNING", "GROUP", "ORDER", "HAVING", "LIMIT", "OFFSET",
            "FETCH", "UNION", "EXCEPT", "INTERSECT", "WHEN", "FROM", "TO");
    
    private static final List<String> DELETE_MODIFIER_KEYWORDS = List.of("LOW_PRIORITY", "QUICK", "IGNORE");
    
    private final SQLStatementScanner scanner;
    
    private final SQLStatementStructureResolver structureResolver;
    
    String resolve(final SQLStatementStructure statementStructure) {
        Collection<String> objectNames = resolveAll(statementStructure);
        return objectNames.isEmpty() ? "" : objectNames.iterator().next();
    }
    
    Collection<String> resolveAll(final SQLStatementStructure statementStructure) {
        Set<String> result = new LinkedHashSet<>(16, 1F);
        collect(statementStructure, new LinkedList<>(), result);
        return result;
    }
    
    private void collect(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases, final Collection<String> result) {
        if ("SELECT".equals(statementStructure.statementType())) {
            collectCommonTableExpressionObjectNames(statementStructure, visitedAliases, result);
            collectSelectTargetObjectNames(statementStructure, visitedAliases, result);
            return;
        }
        collectDirectTargetObjectNames(statementStructure, visitedAliases, result);
        collectClauseObjectNames(statementStructure, visitedAliases, result, getClauseKeywords(statementStructure.statementType()));
        collectCommonTableExpressionObjectNames(statementStructure, visitedAliases, result);
    }
    
    private void collectCommonTableExpressionObjectNames(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases, final Collection<String> result) {
        for (SQLCommonTableExpression each : statementStructure.commonTableExpressions()) {
            String normalizedAliasName = scanner.normalizeIdentifierForComparison(each.aliasName());
            if (visitedAliases.contains(normalizedAliasName)) {
                continue;
            }
            collect(each.statementStructure(), appendVisitedAlias(visitedAliases, normalizedAliasName), result);
        }
    }
    
    private void collectSelectTargetObjectNames(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases, final Collection<String> result) {
        collectClauseObjectNames(statementStructure, visitedAliases, result, "FROM", "JOIN");
    }
    
    private void collectClauseObjectNames(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases, final Collection<String> result, final String... keywords) {
        List<SQLStatementToken> tokens = scanner.tokenize(statementStructure.mainSql());
        for (int each : findKeywordIndexes(tokens, keywords)) {
            collectObjectNamesFromClause(statementStructure, tokens, each, visitedAliases, result);
        }
        collectNestedQueryObjectNames(tokens, visitedAliases, result);
        collectQualifiedFunctionNames(tokens, result);
    }
    
    private void collectObjectNamesFromClause(final SQLStatementStructure statementStructure, final List<SQLStatementToken> tokens, final int keywordIndex,
                                              final Collection<String> visitedAliases, final Collection<String> result) {
        int objectStartIndex = keywordIndex + 1;
        if (scanner.isKeyword(tokens.get(keywordIndex), "INHERITS") && objectStartIndex < tokens.size() && "(".equals(tokens.get(objectStartIndex).text())) {
            objectStartIndex++;
        }
        collectObjectNamesFromList(statementStructure, tokens, objectStartIndex, visitedAliases, result, "ONLY", "LATERAL");
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
                collect(statementStructure, visitedAliases, result);
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
            collect(actualCommonTableExpression.statementStructure(), appendVisitedAlias(visitedAliases, normalizedAliasName), result);
        }
    }
    
    private void collectDirectTargetObjectNames(final SQLStatementStructure statementStructure, final Collection<String> visitedAliases, final Collection<String> result) {
        List<SQLStatementToken> tokens = scanner.tokenize(statementStructure.mainSql());
        String statementType = statementStructure.statementType();
        if ("INSERT".equals(statementType) || "MERGE".equals(statementType)) {
            collectObjectNamesAfterKeyword(statementStructure, tokens, "INTO", visitedAliases, result);
            return;
        }
        if ("UPDATE".equals(statementType)) {
            collectObjectNamesAfterKeyword(statementStructure, tokens, "UPDATE", visitedAliases, result, "ONLY");
            return;
        }
        if ("DELETE".equals(statementType)) {
            collectDeleteTargetObjectNames(statementStructure, tokens, visitedAliases, result);
            return;
        }
        if ("CREATE".equals(statementType)) {
            collectObjectNamesAfterTypeKeyword(statementStructure, tokens, visitedAliases, result, "CONCURRENTLY", "IF", "NOT", "EXISTS");
            collectCreateSourceObjectNames(statementStructure, tokens, visitedAliases, result);
            return;
        }
        if ("ALTER".equals(statementType)) {
            collectObjectNamesAfterTypeKeyword(statementStructure, tokens, visitedAliases, result, "IF", "EXISTS", "ONLY");
            collectAlterDestinationObjectNames(statementStructure, tokens, visitedAliases, result);
            return;
        }
        if ("DROP".equals(statementType)) {
            collectObjectNamesAfterTypeKeyword(statementStructure, tokens, visitedAliases, result, "CONCURRENTLY", "IF", "EXISTS");
            collectDropSourceObjectNames(statementStructure, tokens, visitedAliases, result);
            return;
        }
        if ("TRUNCATE".equals(statementType)) {
            if (!collectObjectNamesAfterKeyword(statementStructure, tokens, "TABLE", visitedAliases, result, "ONLY")) {
                collectObjectNamesFromList(statementStructure, tokens, 1, visitedAliases, result, "ONLY");
            }
            return;
        }
        if ("GRANT".equals(statementType) || "REVOKE".equals(statementType)) {
            collectObjectNamesAfterKeyword(statementStructure, tokens, "ON", visitedAliases, result, "TABLE", "VIEW", "INDEX", "SEQUENCE", "DATABASE", "SCHEMA");
        }
    }
    
    private void collectDeleteTargetObjectNames(final SQLStatementStructure statementStructure, final List<SQLStatementToken> tokens, final Collection<String> visitedAliases,
                                                final Collection<String> result) {
        int deleteTargetStartIndex = skipDeleteModifierKeywords(tokens, 1);
        if (deleteTargetStartIndex < tokens.size() && !scanner.isKeyword(tokens.get(deleteTargetStartIndex), "FROM", "USING")) {
            collectObjectNamesFromList(statementStructure, tokens, deleteTargetStartIndex, visitedAliases, result);
        }
        collectObjectNamesAfterKeyword(statementStructure, tokens, "FROM", visitedAliases, result, "ONLY");
    }
    
    private int skipDeleteModifierKeywords(final List<SQLStatementToken> tokens, final int startIndex) {
        int result = startIndex;
        while (result < tokens.size() && DELETE_MODIFIER_KEYWORDS.contains(tokens.get(result).upperText())) {
            result++;
        }
        return result;
    }
    
    private String[] getClauseKeywords(final String statementType) {
        if ("DELETE".equals(statementType)) {
            return new String[]{"FROM", "JOIN", "USING"};
        }
        if ("CREATE".equals(statementType) || "ALTER".equals(statementType)) {
            return new String[]{"FROM", "JOIN", "REFERENCES", "INHERITS", "INHERIT"};
        }
        return "MERGE".equals(statementType) ? new String[]{"USING", "JOIN"} : new String[]{"FROM", "JOIN"};
    }
    
    private void collectCreateSourceObjectNames(final SQLStatementStructure statementStructure, final List<SQLStatementToken> tokens, final Collection<String> visitedAliases,
                                                final Collection<String> result) {
        if (isStatementObjectType(tokens, "INDEX")) {
            collectObjectNamesAfterKeyword(statementStructure, tokens, "ON", visitedAliases, result);
        }
        if (isStatementObjectType(tokens, "TRIGGER", "POLICY")) {
            collectObjectNamesAfterKeyword(statementStructure, tokens, "ON", visitedAliases, result);
        }
        if (isStatementObjectType(tokens, "TABLE")) {
            collectObjectNamesAfterKeyword(statementStructure, tokens, "LIKE", visitedAliases, result);
        }
    }
    
    private void collectDropSourceObjectNames(final SQLStatementStructure statementStructure, final List<SQLStatementToken> tokens, final Collection<String> visitedAliases,
                                              final Collection<String> result) {
        if (isStatementObjectType(tokens, "INDEX")) {
            collectObjectNamesAfterKeyword(statementStructure, tokens, "ON", visitedAliases, result);
        }
    }
    
    private void collectAlterDestinationObjectNames(final SQLStatementStructure statementStructure, final List<SQLStatementToken> tokens, final Collection<String> visitedAliases,
                                                    final Collection<String> result) {
        for (int index = 0; index < tokens.size(); index++) {
            if (isRenameToDestination(tokens, index)) {
                collectObjectNamesFromList(statementStructure, tokens, index + 2, visitedAliases, result);
            }
            if (isSetSchemaDestination(tokens, index)) {
                collectObjectNamesFromList(statementStructure, tokens, index + 2, visitedAliases, result);
            }
        }
        if (isStatementObjectType(tokens, "TRIGGER", "POLICY")) {
            collectObjectNamesAfterKeyword(statementStructure, tokens, "ON", visitedAliases, result);
        }
    }
    
    private boolean isRenameToDestination(final List<SQLStatementToken> tokens, final int index) {
        return index + 2 < tokens.size() && scanner.isKeyword(tokens.get(index), "RENAME") && scanner.isKeyword(tokens.get(index + 1), "TO");
    }
    
    private boolean isSetSchemaDestination(final List<SQLStatementToken> tokens, final int index) {
        return index + 2 < tokens.size() && scanner.isKeyword(tokens.get(index), "SET") && scanner.isKeyword(tokens.get(index + 1), "SCHEMA");
    }
    
    private boolean isStatementObjectType(final List<SQLStatementToken> tokens, final String... keywords) {
        for (SQLStatementToken each : tokens) {
            if (SQL_OBJECT_TYPE_KEYWORDS.contains(each.upperText())) {
                return scanner.isKeyword(each, keywords);
            }
        }
        return false;
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
    
    private void collectObjectNamesAfterTypeKeyword(final SQLStatementStructure statementStructure, final List<SQLStatementToken> tokens, final Collection<String> visitedAliases,
                                                    final Collection<String> result, final String... optionalKeywords) {
        for (int index = 0; index < tokens.size(); index++) {
            if (SQL_OBJECT_TYPE_KEYWORDS.contains(tokens.get(index).upperText())) {
                collectObjectNamesFromList(statementStructure, tokens, index + 1, visitedAliases, result, optionalKeywords);
                return;
            }
        }
    }
    
    private boolean collectObjectNamesAfterKeyword(final SQLStatementStructure statementStructure, final List<SQLStatementToken> tokens, final String keyword,
                                                   final Collection<String> visitedAliases, final Collection<String> result, final String... optionalKeywords) {
        for (int index = 0; index < tokens.size(); index++) {
            if (scanner.isKeyword(tokens.get(index), keyword)) {
                collectObjectNamesFromList(statementStructure, tokens, index + 1, visitedAliases, result, optionalKeywords);
                return true;
            }
        }
        return false;
    }
    
    private void collectObjectNamesFromList(final SQLStatementStructure statementStructure, final List<SQLStatementToken> tokens, final int startIndex,
                                            final Collection<String> visitedAliases, final Collection<String> result, final String... optionalKeywords) {
        int currentIndex = startIndex;
        while (currentIndex < tokens.size()) {
            ObjectNameToken objectName = readObjectName(tokens, currentIndex, optionalKeywords);
            if (objectName.objectName().isEmpty()) {
                return;
            }
            collectObjectName(statementStructure, objectName.objectName(), visitedAliases, result);
            currentIndex = skipObjectTail(tokens, objectName.nextIndex());
            if (currentIndex >= tokens.size() || !",".equals(tokens.get(currentIndex).text())) {
                return;
            }
            currentIndex++;
        }
    }
    
    private int skipObjectTail(final List<SQLStatementToken> tokens, final int startIndex) {
        int result = startIndex;
        int parenthesesDepth = 0;
        while (result < tokens.size()) {
            SQLStatementToken token = tokens.get(result);
            if ("(".equals(token.text())) {
                parenthesesDepth++;
                result++;
                continue;
            }
            if (")".equals(token.text())) {
                parenthesesDepth--;
                result++;
                continue;
            }
            if (0 == parenthesesDepth && (",".equals(token.text()) || OBJECT_LIST_STOP_KEYWORDS.contains(token.upperText()))) {
                return result;
            }
            result++;
        }
        return result;
    }
    
    private ObjectNameToken readObjectName(final List<SQLStatementToken> tokens, final int startIndex, final String... optionalKeywords) {
        int currentIndex = startIndex;
        while (currentIndex < tokens.size()) {
            SQLStatementToken token = tokens.get(currentIndex);
            if (scanner.isKeyword(token, optionalKeywords)) {
                currentIndex++;
                continue;
            }
            if ("(".equals(token.text())) {
                return new ObjectNameToken("", currentIndex);
            }
            return readQualifiedName(tokens, currentIndex);
        }
        return new ObjectNameToken("", currentIndex);
    }
    
    private ObjectNameToken readQualifiedName(final List<SQLStatementToken> tokens, final int startIndex) {
        if (startIndex >= tokens.size() || !isObjectNameSegment(tokens.get(startIndex))) {
            return new ObjectNameToken("", startIndex);
        }
        StringBuilder result = new StringBuilder(scanner.normalizeIdentifier(tokens.get(startIndex).text()));
        int currentIndex = startIndex + 1;
        while (currentIndex + 1 < tokens.size() && ".".equals(tokens.get(currentIndex).text()) && isObjectNameSegment(tokens.get(currentIndex + 1))) {
            result.append('.').append(scanner.normalizeIdentifier(tokens.get(currentIndex + 1).text()));
            currentIndex += 2;
        }
        return new ObjectNameToken(result.toString(), currentIndex);
    }
    
    private boolean isObjectNameSegment(final SQLStatementToken token) {
        return token.identifier() || "*".equals(token.text());
    }
    
    private void collectQualifiedFunctionNames(final List<SQLStatementToken> tokens, final Collection<String> result) {
        int index = 0;
        while (index < tokens.size()) {
            ObjectNameToken objectName = readQualifiedName(tokens, index);
            if (isQualifiedFunctionName(tokens, objectName)) {
                addObjectName(result, objectName.objectName());
                index = objectName.nextIndex() - 1;
            }
            index++;
        }
    }
    
    private boolean isQualifiedFunctionName(final List<SQLStatementToken> tokens, final ObjectNameToken objectName) {
        return objectName.objectName().contains(".") && !objectName.objectName().endsWith(".*") && objectName.nextIndex() < tokens.size()
                && "(".equals(tokens.get(objectName.nextIndex()).text());
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
    
    private record ObjectNameToken(String objectName, int nextIndex) {
    }
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
        String leadingSql = actualSql.substring(skipInsignificant(actualSql, 0)).trim();
        String upperLeadingSql = leadingSql.toUpperCase(Locale.ENGLISH);
        if (isBannedCommand(upperLeadingSql)) {
            throw new UnsupportedOperationException("Statement is banned by the MCP contract.");
        }
        if (upperLeadingSql.startsWith("EXPLAIN ANALYZE")) {
            StatementStructure explainedStatementStructure = resolveStatementStructure(leadingSql.substring("EXPLAIN ANALYZE".length()).trim());
            SupportedMCPStatement explainedStatementClass = resolveStatementClass(explainedStatementStructure);
            return new ClassificationResult(SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", actualSql,
                    resolveTargetObjectName(explainedStatementStructure, explainedStatementClass), "");
        }
        if (isSavepointStatement(upperLeadingSql)) {
            String statementType = extractStatementType(upperLeadingSql);
            String savepointName = extractSavepointName(leadingSql);
            validateSavepointName(statementType, savepointName);
            return new ClassificationResult(SupportedMCPStatement.SAVEPOINT, statementType, actualSql, "", savepointName);
        }
        if (isTransactionControlStatement(upperLeadingSql)) {
            return new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, extractStatementType(upperLeadingSql), actualSql, "", "");
        }
        StatementStructure statementStructure = resolveStatementStructure(actualSql);
        SupportedMCPStatement statementClass = resolveStatementClass(statementStructure);
        return new ClassificationResult(statementClass, statementStructure.getStatementType(), actualSql, resolveTargetObjectName(statementStructure, statementClass), "");
    }
    
    private String normalizeSingleStatement(final String sql) {
        String result = sql.trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("sql cannot be empty.");
        }
        int statementDelimiterIndex = findStatementDelimiter(result);
        if (-1 == statementDelimiterIndex) {
            return result;
        }
        if (skipInsignificant(result, statementDelimiterIndex + 1) < result.length()) {
            throw new IllegalArgumentException("Only one SQL statement is allowed.");
        }
        result = result.substring(0, statementDelimiterIndex).trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("sql cannot be empty.");
        }
        return result;
    }
    
    private int findStatementDelimiter(final String sql) {
        int result = 0;
        while (result < sql.length()) {
            char currentChar = sql.charAt(result);
            if (isLineCommentStart(sql, result)) {
                result = skipLineComment(sql, result) + 1;
                continue;
            }
            if (isBlockCommentStart(sql, result)) {
                result = skipBlockComment(sql, result) + 1;
                continue;
            }
            if (isQuotedTextStart(currentChar)) {
                result = skipQuotedText(sql, result) + 1;
                continue;
            }
            if (';' == currentChar) {
                return result;
            }
            result++;
        }
        return -1;
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
    
    private boolean isSavepointStatement(final String upperSql) {
        return "SAVEPOINT".equals(upperSql)
                || upperSql.startsWith("SAVEPOINT ")
                || upperSql.startsWith("ROLLBACK TO SAVEPOINT")
                || upperSql.startsWith("RELEASE SAVEPOINT");
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
        return upperSql.split("\\s+")[0];
    }
    
    private StatementStructure resolveStatementStructure(final String sql) {
        int startIndex = skipInsignificant(sql, 0);
        if (!matchesKeyword(sql, startIndex, "WITH")) {
            String mainSql = sql.substring(startIndex).trim();
            return new StatementStructure(mainSql, extractLeadingKeyword(mainSql), false, new LinkedList<>());
        }
        int currentIndex = skipKeyword(sql, startIndex, "WITH");
        currentIndex = skipInsignificant(sql, currentIndex);
        if (matchesKeyword(sql, currentIndex, "RECURSIVE")) {
            currentIndex = skipKeyword(sql, currentIndex, "RECURSIVE");
        }
        Collection<CommonTableExpression> commonTableExpressions = new LinkedList<>();
        while (currentIndex < sql.length()) {
            CommonTableExpressionResolution commonTableExpression = resolveCommonTableExpression(sql, currentIndex);
            commonTableExpressions.add(new CommonTableExpression(commonTableExpression.getAliasName(), resolveStatementStructure(commonTableExpression.getSql())));
            currentIndex = commonTableExpression.getNextIndex();
            if (currentIndex < sql.length() && ',' == sql.charAt(currentIndex)) {
                currentIndex++;
                continue;
            }
            break;
        }
        String mainSql = sql.substring(skipInsignificant(sql, currentIndex)).trim();
        if (mainSql.isEmpty()) {
            throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
        }
        return new StatementStructure(mainSql, extractLeadingKeyword(mainSql), containsDataModifyingCommonTableExpression(commonTableExpressions), commonTableExpressions);
    }
    
    private SupportedMCPStatement resolveStatementClass(final StatementStructure statementStructure) {
        String statementType = statementStructure.getStatementType();
        if ("SELECT".equals(statementType)) {
            return statementStructure.isContainsDataModifyingCommonTableExpression() ? SupportedMCPStatement.DML : SupportedMCPStatement.QUERY;
        }
        if ("INSERT".equals(statementType) || "UPDATE".equals(statementType) || "DELETE".equals(statementType) || "MERGE".equals(statementType)) {
            return SupportedMCPStatement.DML;
        }
        if ("CREATE".equals(statementType) || "ALTER".equals(statementType) || "DROP".equals(statementType) || "TRUNCATE".equals(statementType)) {
            return SupportedMCPStatement.DDL;
        }
        if ("GRANT".equals(statementType) || "REVOKE".equals(statementType)) {
            return SupportedMCPStatement.DCL;
        }
        throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
    }
    
    private String resolveTargetObjectName(final StatementStructure statementStructure, final SupportedMCPStatement statementClass) {
        return resolveTargetObjectName(statementStructure, statementClass, new LinkedList<>());
    }
    
    private String resolveTargetObjectName(final StatementStructure statementStructure, final SupportedMCPStatement statementClass, final Collection<String> visitedAliases) {
        if ("SELECT".equals(statementStructure.getStatementType())) {
            String result = extractSelectTargetObjectName(statementStructure, visitedAliases);
            if (!result.isEmpty()) {
                return result;
            }
            return SupportedMCPStatement.DML == statementClass ? extractDataModifyingTargetObjectName(statementStructure, visitedAliases) : "";
        }
        return extractDirectTargetObjectName(statementStructure.getMainSql(), statementStructure.getStatementType());
    }
    
    private String extractSelectTargetObjectName(final StatementStructure statementStructure, final Collection<String> visitedAliases) {
        List<Token> tokens = tokenize(statementStructure.getMainSql());
        for (int each : findKeywordIndexes(tokens, "FROM")) {
            String sourceObjectName = readObjectName(tokens, each + 1, "ONLY");
            if (sourceObjectName.isEmpty()) {
                continue;
            }
            CommonTableExpression commonTableExpression = findCommonTableExpression(statementStructure, sourceObjectName);
            if (null == commonTableExpression) {
                return sourceObjectName;
            }
            String normalizedAliasName = normalizeIdentifierForComparison(commonTableExpression.getAliasName());
            if (visitedAliases.contains(normalizedAliasName)) {
                continue;
            }
            String result = resolveTargetObjectName(commonTableExpression.getStatementStructure(), resolveStatementClass(commonTableExpression.getStatementStructure()),
                    appendVisitedAlias(visitedAliases, normalizedAliasName));
            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }
    
    private String extractDirectTargetObjectName(final String sql, final String statementType) {
        List<Token> tokens = tokenize(sql);
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
    
    private String extractDataModifyingTargetObjectName(final StatementStructure statementStructure, final Collection<String> visitedAliases) {
        for (CommonTableExpression each : statementStructure.getCommonTableExpressions()) {
            SupportedMCPStatement statementClass = resolveStatementClass(each.getStatementStructure());
            if (SupportedMCPStatement.DML != statementClass) {
                continue;
            }
            String normalizedAliasName = normalizeIdentifierForComparison(each.getAliasName());
            if (visitedAliases.contains(normalizedAliasName)) {
                continue;
            }
            String result = resolveTargetObjectName(each.getStatementStructure(), statementClass, appendVisitedAlias(visitedAliases, normalizedAliasName));
            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }
    
    private boolean containsDataModifyingCommonTableExpression(final Collection<CommonTableExpression> commonTableExpressions) {
        for (CommonTableExpression each : commonTableExpressions) {
            if (SupportedMCPStatement.DML == resolveStatementClass(each.getStatementStructure())) {
                return true;
            }
        }
        return false;
    }
    
    private CommonTableExpressionResolution resolveCommonTableExpression(final String sql, final int startIndex) {
        int result = skipInsignificant(sql, startIndex);
        result = skipIdentifier(sql, result);
        result = skipInsignificant(sql, result);
        if (result < sql.length() && '(' == sql.charAt(result)) {
            result = skipParenthesizedSegment(sql, result);
        }
        result = skipKeyword(sql, result, "AS");
        result = skipInsignificant(sql, result);
        if (matchesKeyword(sql, result, "NOT")) {
            result = skipKeyword(sql, result, "NOT");
            result = skipInsignificant(sql, result);
        }
        if (matchesKeyword(sql, result, "MATERIALIZED")) {
            result = skipKeyword(sql, result, "MATERIALIZED");
            result = skipInsignificant(sql, result);
        }
        if (result >= sql.length() || '(' != sql.charAt(result)) {
            throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
        }
        int stopIndex = findClosingParenthesis(sql, result);
        int aliasStopIndex = skipIdentifier(sql, skipInsignificant(sql, startIndex));
        return new CommonTableExpressionResolution(normalizeIdentifier(sql.substring(skipInsignificant(sql, startIndex), aliasStopIndex)),
                sql.substring(result + 1, stopIndex).trim(), skipInsignificant(sql, stopIndex + 1));
    }
    
    private Collection<String> appendVisitedAlias(final Collection<String> visitedAliases, final String aliasName) {
        Collection<String> result = new LinkedList<>(visitedAliases);
        result.add(aliasName);
        return result;
    }
    
    private CommonTableExpression findCommonTableExpression(final StatementStructure statementStructure, final String aliasName) {
        String normalizedAliasName = normalizeIdentifierForComparison(aliasName);
        for (CommonTableExpression each : statementStructure.getCommonTableExpressions()) {
            if (normalizeIdentifierForComparison(each.getAliasName()).equals(normalizedAliasName)) {
                return each;
            }
        }
        return null;
    }
    
    private String extractObjectNameAfterTypeKeyword(final List<Token> tokens, final List<String> typeKeywords, final String... optionalKeywords) {
        for (int index = 0; index < tokens.size(); index++) {
            if (typeKeywords.contains(tokens.get(index).getUpperText())) {
                return readObjectName(tokens, index + 1, optionalKeywords);
            }
        }
        return "";
    }
    
    private String extractObjectNameAfterKeyword(final List<Token> tokens, final String keyword, final String... optionalKeywords) {
        for (int index = 0; index < tokens.size(); index++) {
            if (isKeyword(tokens.get(index), keyword)) {
                return readObjectName(tokens, index + 1, optionalKeywords);
            }
        }
        return "";
    }
    
    private String readObjectName(final List<Token> tokens, final int startIndex, final String... optionalKeywords) {
        int result = startIndex;
        while (result < tokens.size()) {
            Token token = tokens.get(result);
            if (isKeyword(token, optionalKeywords)) {
                result++;
                continue;
            }
            if ("(".equals(token.getText())) {
                return "";
            }
            return readQualifiedName(tokens, result);
        }
        return "";
    }
    
    private String readQualifiedName(final List<Token> tokens, final int startIndex) {
        if (startIndex >= tokens.size() || !tokens.get(startIndex).isIdentifier()) {
            return "";
        }
        StringBuilder result = new StringBuilder(normalizeIdentifier(tokens.get(startIndex).getText()));
        int currentIndex = startIndex + 1;
        while (currentIndex + 1 < tokens.size() && ".".equals(tokens.get(currentIndex).getText()) && tokens.get(currentIndex + 1).isIdentifier()) {
            result.append('.').append(normalizeIdentifier(tokens.get(currentIndex + 1).getText()));
            currentIndex += 2;
        }
        return result.toString();
    }
    
    private List<Integer> findKeywordIndexes(final List<Token> tokens, final String keyword) {
        List<Integer> result = new ArrayList<>();
        for (int index = 0; index < tokens.size(); index++) {
            if (isKeyword(tokens.get(index), keyword)) {
                result.add(index);
            }
        }
        return result;
    }
    
    private List<Token> tokenize(final String sql) {
        List<Token> result = new ArrayList<>();
        int currentIndex = 0;
        while (currentIndex < sql.length()) {
            currentIndex = skipInsignificant(sql, currentIndex);
            if (currentIndex >= sql.length()) {
                break;
            }
            char currentChar = sql.charAt(currentIndex);
            if ('\'' == currentChar) {
                currentIndex = skipQuotedText(sql, currentIndex) + 1;
                continue;
            }
            if (isQuotedIdentifierStart(currentChar)) {
                int stopIndex = skipQuotedText(sql, currentIndex);
                result.add(new Token(sql.substring(currentIndex, stopIndex + 1), true));
                currentIndex = stopIndex + 1;
                continue;
            }
            if (isWordCharacter(currentChar)) {
                int stopIndex = currentIndex;
                while (stopIndex < sql.length() && isWordCharacter(sql.charAt(stopIndex))) {
                    stopIndex++;
                }
                result.add(new Token(sql.substring(currentIndex, stopIndex), false));
                currentIndex = stopIndex;
                continue;
            }
            if ('.' == currentChar || '(' == currentChar || ')' == currentChar || ',' == currentChar) {
                result.add(new Token(String.valueOf(currentChar), false));
            }
            currentIndex++;
        }
        return result;
    }
    
    private boolean isKeyword(final Token token, final String... keywords) {
        for (String each : keywords) {
            if (isKeyword(token, each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isKeyword(final Token token, final String keyword) {
        return !token.isQuotedIdentifier() && token.getUpperText().equals(keyword);
    }
    
    private int skipParenthesizedSegment(final String sql, final int startIndex) {
        return skipInsignificant(sql, findClosingParenthesis(sql, startIndex) + 1);
    }
    
    private int findClosingParenthesis(final String sql, final int startIndex) {
        int parenthesesDepth = 0;
        int result = startIndex;
        while (result < sql.length()) {
            char currentChar = sql.charAt(result);
            if (isLineCommentStart(sql, result)) {
                result = skipLineComment(sql, result) + 1;
                continue;
            }
            if (isBlockCommentStart(sql, result)) {
                result = skipBlockComment(sql, result) + 1;
                continue;
            }
            if (isQuotedTextStart(currentChar)) {
                result = skipQuotedText(sql, result) + 1;
                continue;
            }
            if ('(' == currentChar) {
                parenthesesDepth++;
                result++;
                continue;
            }
            if (')' == currentChar) {
                parenthesesDepth--;
                if (0 == parenthesesDepth) {
                    return result;
                }
            }
            result++;
        }
        throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
    }
    
    private int skipIdentifier(final String sql, final int startIndex) {
        if (startIndex >= sql.length()) {
            throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
        }
        char currentChar = sql.charAt(startIndex);
        if (isQuotedIdentifierStart(currentChar)) {
            return skipQuotedText(sql, startIndex) + 1;
        }
        int result = startIndex;
        while (result < sql.length()) {
            currentChar = sql.charAt(result);
            if (Character.isWhitespace(currentChar) || '(' == currentChar || ',' == currentChar) {
                break;
            }
            result++;
        }
        return result;
    }
    
    private int skipQuotedText(final String sql, final int startIndex) {
        char closingChar = '[' == sql.charAt(startIndex) ? ']' : sql.charAt(startIndex);
        int result = startIndex + 1;
        while (result < sql.length()) {
            if (closingChar == sql.charAt(result)) {
                if (result + 1 < sql.length() && closingChar == sql.charAt(result + 1)) {
                    result += 2;
                    continue;
                }
                return result;
            }
            result++;
        }
        throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
    }
    
    private String extractLeadingKeyword(final String sql) {
        int startIndex = skipInsignificant(sql, 0);
        int stopIndex = startIndex;
        while (stopIndex < sql.length() && Character.isLetter(sql.charAt(stopIndex))) {
            stopIndex++;
        }
        if (startIndex == stopIndex) {
            throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
        }
        return sql.substring(startIndex, stopIndex).toUpperCase(Locale.ENGLISH);
    }
    
    private boolean matchesKeyword(final String sql, final int startIndex, final String keyword) {
        int keywordLength = keyword.length();
        if (startIndex + keywordLength > sql.length() || !sql.regionMatches(true, startIndex, keyword, 0, keywordLength)) {
            return false;
        }
        return startIndex + keywordLength == sql.length() || !isIdentifierCharacter(sql.charAt(startIndex + keywordLength));
    }
    
    private int skipKeyword(final String sql, final int startIndex, final String keyword) {
        if (!matchesKeyword(sql, startIndex, keyword)) {
            throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
        }
        return startIndex + keyword.length();
    }
    
    private int skipInsignificant(final String sql, final int startIndex) {
        int result = startIndex;
        while (result < sql.length()) {
            if (Character.isWhitespace(sql.charAt(result))) {
                result++;
                continue;
            }
            if (isLineCommentStart(sql, result)) {
                result = skipLineComment(sql, result) + 1;
                continue;
            }
            if (isBlockCommentStart(sql, result)) {
                result = skipBlockComment(sql, result) + 1;
                continue;
            }
            break;
        }
        return result;
    }
    
    private boolean isIdentifierCharacter(final char value) {
        return Character.isLetterOrDigit(value) || '_' == value || '$' == value;
    }
    
    private boolean isWordCharacter(final char value) {
        return isIdentifierCharacter(value);
    }
    
    private boolean isQuotedIdentifierStart(final char value) {
        return '"' == value || '`' == value || '[' == value;
    }
    
    private boolean isQuotedTextStart(final char value) {
        return '\'' == value || isQuotedIdentifierStart(value);
    }
    
    private boolean isLineCommentStart(final String sql, final int startIndex) {
        return startIndex + 1 < sql.length() && '-' == sql.charAt(startIndex) && '-' == sql.charAt(startIndex + 1);
    }
    
    private boolean isBlockCommentStart(final String sql, final int startIndex) {
        return startIndex + 1 < sql.length() && '/' == sql.charAt(startIndex) && '*' == sql.charAt(startIndex + 1);
    }
    
    private int skipLineComment(final String sql, final int startIndex) {
        int result = startIndex + 2;
        while (result < sql.length() && '\n' != sql.charAt(result)) {
            result++;
        }
        return result;
    }
    
    private int skipBlockComment(final String sql, final int startIndex) {
        int result = startIndex + 2;
        while (result + 1 < sql.length()) {
            if ('*' == sql.charAt(result) && '/' == sql.charAt(result + 1)) {
                return result + 1;
            }
            result++;
        }
        throw new IllegalArgumentException("Statement is not supported by the MCP contract.");
    }
    
    private String normalizeIdentifier(final String identifier) {
        if (identifier.length() >= 2 && '"' == identifier.charAt(0) && '"' == identifier.charAt(identifier.length() - 1)) {
            return identifier.substring(1, identifier.length() - 1).replace("\"\"", "\"");
        }
        if (identifier.length() >= 2 && '`' == identifier.charAt(0) && '`' == identifier.charAt(identifier.length() - 1)) {
            return identifier.substring(1, identifier.length() - 1).replace("``", "`");
        }
        if (identifier.length() >= 2 && '[' == identifier.charAt(0) && ']' == identifier.charAt(identifier.length() - 1)) {
            return identifier.substring(1, identifier.length() - 1).replace("]]", "]");
        }
        return identifier;
    }
    
    private String normalizeIdentifierForComparison(final String identifier) {
        return normalizeIdentifier(identifier).toUpperCase(Locale.ENGLISH);
    }
    
    private String extractSavepointName(final String sql) {
        String[] tokens = sql.split("\\s+");
        if ("SAVEPOINT".equalsIgnoreCase(tokens[0]) && tokens.length >= 2) {
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
    
    private void validateSavepointName(final String statementType, final String savepointName) {
        if (!savepointName.isEmpty()) {
            return;
        }
        if ("SAVEPOINT".equals(statementType) || "ROLLBACK TO SAVEPOINT".equals(statementType) || "RELEASE SAVEPOINT".equals(statementType)) {
            throw new IllegalArgumentException("Savepoint name is required.");
        }
    }
    
    private static final class StatementStructure {
        
        private final String mainSql;
        
        private final String statementType;
        
        private final boolean containsDataModifyingCommonTableExpression;
        
        private final Collection<CommonTableExpression> commonTableExpressions;
        
        private StatementStructure(final String mainSql, final String statementType, final boolean containsDataModifyingCommonTableExpression,
                                   final Collection<CommonTableExpression> commonTableExpressions) {
            this.mainSql = mainSql;
            this.statementType = statementType;
            this.containsDataModifyingCommonTableExpression = containsDataModifyingCommonTableExpression;
            this.commonTableExpressions = commonTableExpressions;
        }
        
        private String getMainSql() {
            return mainSql;
        }
        
        private String getStatementType() {
            return statementType;
        }
        
        private boolean isContainsDataModifyingCommonTableExpression() {
            return containsDataModifyingCommonTableExpression;
        }
        
        private Collection<CommonTableExpression> getCommonTableExpressions() {
            return commonTableExpressions;
        }
    }
    
    private static final class CommonTableExpression {
        
        private final String aliasName;
        
        private final StatementStructure statementStructure;
        
        private CommonTableExpression(final String aliasName, final StatementStructure statementStructure) {
            this.aliasName = aliasName;
            this.statementStructure = statementStructure;
        }
        
        private String getAliasName() {
            return aliasName;
        }
        
        private StatementStructure getStatementStructure() {
            return statementStructure;
        }
    }
    
    private static final class CommonTableExpressionResolution {
        
        private final String aliasName;
        
        private final String sql;
        
        private final int nextIndex;
        
        private CommonTableExpressionResolution(final String aliasName, final String sql, final int nextIndex) {
            this.aliasName = aliasName;
            this.sql = sql;
            this.nextIndex = nextIndex;
        }
        
        private String getAliasName() {
            return aliasName;
        }
        
        private String getSql() {
            return sql;
        }
        
        private int getNextIndex() {
            return nextIndex;
        }
    }
    
    private static final class Token {
        
        private final String text;
        
        private final String upperText;
        
        private final boolean quotedIdentifier;
        
        private Token(final String text, final boolean quotedIdentifier) {
            this.text = text;
            this.upperText = text.toUpperCase(Locale.ENGLISH);
            this.quotedIdentifier = quotedIdentifier;
        }
        
        private String getText() {
            return text;
        }
        
        private String getUpperText() {
            return upperText;
        }
        
        private boolean isQuotedIdentifier() {
            return quotedIdentifier;
        }
        
        private boolean isIdentifier() {
            return quotedIdentifier || !text.isEmpty() && (Character.isLetterOrDigit(text.charAt(0)) || '_' == text.charAt(0) || '$' == text.charAt(0));
        }
    }
}

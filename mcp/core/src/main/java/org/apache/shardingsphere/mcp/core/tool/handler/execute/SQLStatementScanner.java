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
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMultipleSQLStatementsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class SQLStatementScanner {
    
    String normalizeSingleStatement(final String sql) {
        String result = sql.trim();
        ShardingSpherePreconditions.checkState(!result.isEmpty(), () -> new IllegalArgumentException("sql cannot be empty."));
        int statementDelimiterIndex = findStatementDelimiter(result);
        if (-1 == statementDelimiterIndex) {
            return result;
        }
        if (skipInsignificant(result, statementDelimiterIndex + 1) < result.length()) {
            throw new MCPMultipleSQLStatementsException();
        }
        result = result.substring(0, statementDelimiterIndex).trim();
        ShardingSpherePreconditions.checkState(!result.isEmpty(), () -> new IllegalArgumentException("sql cannot be empty."));
        return result;
    }
    
    int skipInsignificant(final String sql, final int startIndex) {
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
    
    String extractLeadingKeyword(final String sql) {
        int startIndex = skipInsignificant(sql, 0);
        int stopIndex = startIndex;
        while (stopIndex < sql.length() && Character.isLetter(sql.charAt(stopIndex))) {
            stopIndex++;
        }
        if (startIndex == stopIndex) {
            throw new MCPUnsupportedSQLStatementException();
        }
        return sql.substring(startIndex, stopIndex).toUpperCase(Locale.ENGLISH);
    }
    
    boolean matchesKeyword(final String sql, final int startIndex, final String keyword) {
        int keywordLength = keyword.length();
        if (startIndex + keywordLength > sql.length() || !sql.regionMatches(true, startIndex, keyword, 0, keywordLength)) {
            return false;
        }
        return startIndex + keywordLength == sql.length() || !isIdentifierCharacter(sql.charAt(startIndex + keywordLength));
    }
    
    int skipKeyword(final String sql, final int startIndex, final String keyword) {
        if (!matchesKeyword(sql, startIndex, keyword)) {
            throw new MCPUnsupportedSQLStatementException();
        }
        return startIndex + keyword.length();
    }
    
    int skipIdentifier(final String sql, final int startIndex) {
        if (startIndex >= sql.length()) {
            throw new MCPUnsupportedSQLStatementException();
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
    
    int skipParenthesizedSegment(final String sql, final int startIndex) {
        return skipInsignificant(sql, findClosingParenthesis(sql, startIndex) + 1);
    }
    
    int findClosingParenthesis(final String sql, final int startIndex) {
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
        throw new MCPUnsupportedSQLStatementException();
    }
    
    List<SQLStatementToken> tokenize(final String sql) {
        List<SQLStatementToken> result = new ArrayList<>(Math.max(1, sql.length() / 4));
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
                result.add(new SQLStatementToken(sql.substring(currentIndex, stopIndex + 1), true));
                currentIndex = stopIndex + 1;
                continue;
            }
            if (isWordCharacter(currentChar)) {
                int stopIndex = currentIndex;
                while (stopIndex < sql.length() && isWordCharacter(sql.charAt(stopIndex))) {
                    stopIndex++;
                }
                result.add(new SQLStatementToken(sql.substring(currentIndex, stopIndex), false));
                currentIndex = stopIndex;
                continue;
            }
            if ('.' == currentChar || '(' == currentChar || ')' == currentChar || ',' == currentChar || '*' == currentChar) {
                result.add(new SQLStatementToken(String.valueOf(currentChar), false));
            }
            currentIndex++;
        }
        return result;
    }
    
    boolean isKeyword(final SQLStatementToken token, final String... keywords) {
        for (String each : keywords) {
            if (isKeyword(token, each)) {
                return true;
            }
        }
        return false;
    }
    
    boolean isKeyword(final SQLStatementToken token, final String keyword) {
        return !token.quotedIdentifier() && token.upperText().equals(keyword);
    }
    
    String normalizeIdentifier(final String identifier) {
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
    
    String normalizeIdentifierForComparison(final String identifier) {
        return normalizeIdentifier(identifier).toUpperCase(Locale.ENGLISH);
    }
    
    boolean containsMySQLExecutableComment(final String sql) {
        int currentIndex = 0;
        while (currentIndex < sql.length()) {
            if (isLineCommentStart(sql, currentIndex)) {
                currentIndex = skipLineComment(sql, currentIndex) + 1;
                continue;
            }
            if (isBlockCommentStart(sql, currentIndex)) {
                if (currentIndex + 2 < sql.length() && '!' == sql.charAt(currentIndex + 2)) {
                    return true;
                }
                currentIndex = skipBlockComment(sql, currentIndex) + 1;
                continue;
            }
            if (isQuotedTextStart(sql.charAt(currentIndex))) {
                currentIndex = skipQuotedText(sql, currentIndex) + 1;
                continue;
            }
            currentIndex++;
        }
        return false;
    }
    
    boolean containsUserVariableAssignment(final String sql) {
        int currentIndex = 0;
        while (currentIndex < sql.length()) {
            if (isLineCommentStart(sql, currentIndex)) {
                currentIndex = skipLineComment(sql, currentIndex) + 1;
                continue;
            }
            if (isBlockCommentStart(sql, currentIndex)) {
                currentIndex = skipBlockComment(sql, currentIndex) + 1;
                continue;
            }
            if (isQuotedTextStart(sql.charAt(currentIndex))) {
                currentIndex = skipQuotedText(sql, currentIndex) + 1;
                continue;
            }
            if ('@' == sql.charAt(currentIndex) && isUserVariableAssignment(sql, currentIndex + 1)) {
                return true;
            }
            currentIndex++;
        }
        return false;
    }
    
    private boolean isUserVariableAssignment(final String sql, final int startIndex) {
        int currentIndex = startIndex;
        if (currentIndex < sql.length() && isQuotedTextStart(sql.charAt(currentIndex))) {
            currentIndex = skipQuotedText(sql, currentIndex) + 1;
        } else {
            while (currentIndex < sql.length() && isIdentifierCharacter(sql.charAt(currentIndex))) {
                currentIndex++;
            }
            if (startIndex == currentIndex) {
                return false;
            }
        }
        currentIndex = skipWhitespace(sql, currentIndex);
        return currentIndex + 1 < sql.length() && ':' == sql.charAt(currentIndex) && '=' == sql.charAt(currentIndex + 1);
    }
    
    private int skipWhitespace(final String sql, final int startIndex) {
        int result = startIndex;
        while (result < sql.length() && Character.isWhitespace(sql.charAt(result))) {
            result++;
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
        throw new MCPUnsupportedSQLStatementException();
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
        throw new MCPUnsupportedSQLStatementException();
    }
}

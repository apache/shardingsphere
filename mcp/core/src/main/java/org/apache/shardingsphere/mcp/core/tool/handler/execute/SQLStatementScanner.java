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

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMultipleSQLStatementsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.engine.core.SQLParserFactory;
import org.apache.shardingsphere.sql.parser.spi.DialectSQLParserFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class SQLStatementScanner {
    
    private static final String BEGIN_DOLLAR_STRING_CONSTANT = "BEGIN_DOLLAR_STRING_CONSTANT";
    
    private static final String END_DOLLAR_STRING_CONSTANT = "END_DOLLAR_STRING_CONSTANT";
    
    private final Class<? extends SQLLexer> lexerClass;
    
    private final Class<? extends SQLParser> parserClass;
    
    private final String sql;
    
    private final boolean lineCommentsHandledByLexer;
    
    private final List<Token> rawTokens;
    
    private final List<Token> visibleTokens;
    
    private final List<SQLStatementToken> tokens;
    
    SQLStatementScanner(final String databaseTypeName, final String sql) {
        this(getParserFacade(databaseTypeName), sql);
    }
    
    private SQLStatementScanner(final DialectSQLParserFacade parserFacade, final String sql) {
        this(parserFacade.getLexerClass(), parserFacade.getParserClass(), sql);
    }
    
    private SQLStatementScanner(final Class<? extends SQLLexer> lexerClass, final Class<? extends SQLParser> parserClass, final String sql) {
        this.lexerClass = lexerClass;
        this.parserClass = parserClass;
        String trimmedSQL = sql.trim();
        ShardingSpherePreconditions.checkState(!trimmedSQL.isEmpty(), () -> new MCPInvalidRequestException("sql cannot be empty."));
        List<Token> allTokens = getTokens(trimmedSQL);
        lineCommentsHandledByLexer = areLineCommentsHandledByLexer(allTokens);
        List<Token> allVisibleTokens = getVisibleTokens(trimmedSQL, allTokens);
        int statementEndIndex = findStatementEndIndex(allVisibleTokens, trimmedSQL.length());
        this.sql = trimmedSQL.substring(0, statementEndIndex).trim();
        ShardingSpherePreconditions.checkState(!this.sql.isEmpty(), () -> new MCPInvalidRequestException("sql cannot be empty."));
        rawTokens = getTokensBefore(allTokens, this.sql.length());
        visibleTokens = getTokensBefore(allVisibleTokens, this.sql.length());
        tokens = createStatementTokens(visibleTokens);
    }
    
    private static DialectSQLParserFacade getParserFacade(final String databaseTypeName) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, databaseTypeName);
        return DatabaseTypedSPILoader.findService(DialectSQLParserFacade.class, databaseType).orElseThrow(
                () -> new MCPUnsupportedException(String.format("SQL parser is not available for database type `%s`.", databaseTypeName)));
    }
    
    private int findStatementEndIndex(final List<Token> tokens, final int sqlLength) {
        for (int index = 0; index < tokens.size(); index++) {
            Token each = tokens.get(index);
            if (!";".equals(each.getText())) {
                continue;
            }
            if (index + 1 < tokens.size()) {
                throw new MCPMultipleSQLStatementsException();
            }
            return each.getStartIndex();
        }
        return sqlLength;
    }
    
    private List<Token> getTokensBefore(final List<Token> tokens, final int stopIndex) {
        List<Token> result = new ArrayList<>();
        for (Token each : tokens) {
            if (each.getStartIndex() < stopIndex) {
                result.add(each);
            }
        }
        return result;
    }
    
    SQLStatementScanner scan(final String sql) {
        return new SQLStatementScanner(lexerClass, parserClass, sql);
    }
    
    String sql() {
        return sql;
    }
    
    String leadingSql() {
        return sql.substring(visibleTokens.isEmpty() ? sql.length() : visibleTokens.get(0).getStartIndex()).trim();
    }
    
    String extractLeadingKeyword() {
        int startIndex = visibleTokens.isEmpty() ? sql.length() : visibleTokens.get(0).getStartIndex();
        int stopIndex = startIndex;
        while (stopIndex < sql.length() && Character.isLetter(sql.charAt(stopIndex))) {
            stopIndex++;
        }
        if (startIndex == stopIndex) {
            throw new MCPUnsupportedSQLStatementException();
        }
        return sql.substring(startIndex, stopIndex).toUpperCase(Locale.ENGLISH);
    }
    
    List<SQLStatementToken> tokens() {
        return tokens;
    }
    
    private List<SQLStatementToken> createStatementTokens(final List<Token> tokens) {
        List<SQLStatementToken> result = new ArrayList<>();
        for (Token each : tokens) {
            if (BEGIN_DOLLAR_STRING_CONSTANT.equals(getTokenName(each))) {
                continue;
            }
            if (".*".equals(each.getText())) {
                result.add(new SQLStatementToken(".", each.getStartIndex()));
                result.add(new SQLStatementToken("*", each.getStartIndex() + 1));
            } else {
                result.add(new SQLStatementToken(each.getText(), each.getStartIndex()));
            }
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
    
    boolean containsKeywordSequence(final List<SQLStatementToken> tokens, final String... keywords) {
        for (int index = 0; index + keywords.length <= tokens.size(); index++) {
            if (containsKeywordSequence(tokens, index, keywords)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsKeywordSequence(final List<SQLStatementToken> tokens, final int startIndex, final String... keywords) {
        for (int index = 0; index < keywords.length; index++) {
            if (!isKeyword(tokens.get(startIndex + index), keywords[index])) {
                return false;
            }
        }
        return true;
    }
    
    boolean containsExecutableComment() {
        int nextIndex = 0;
        int skippedCommentEnd = -1;
        for (int index = 0; index < rawTokens.size(); index++) {
            Token each = rawTokens.get(index);
            if (each.getStartIndex() <= skippedCommentEnd) {
                continue;
            }
            if (each.getStartIndex() > nextIndex && containsExecutableCommentMarker(sql.substring(nextIndex, each.getStartIndex()))) {
                return true;
            }
            if (Token.DEFAULT_CHANNEL != each.getChannel() && startsWithExecutableCommentMarker(each.getText())) {
                return true;
            }
            int blockCommentEnd = findBlockCommentEnd(sql, rawTokens, index);
            if (-1 != blockCommentEnd) {
                if (startsWithExecutableCommentMarker(sql, each.getStartIndex())) {
                    return true;
                }
                skippedCommentEnd = blockCommentEnd;
                nextIndex = blockCommentEnd + 1;
                continue;
            }
            int lineCommentEnd = findLineCommentEnd(sql, rawTokens, index);
            if (-1 != lineCommentEnd) {
                skippedCommentEnd = lineCommentEnd;
                nextIndex = lineCommentEnd + 1;
                continue;
            }
            nextIndex = Math.max(nextIndex, each.getStopIndex() + 1);
        }
        return nextIndex < sql.length() && containsExecutableCommentMarker(sql.substring(nextIndex));
    }
    
    private boolean containsExecutableCommentMarker(final String text) {
        int currentIndex = 0;
        while (currentIndex < text.length()) {
            if (text.startsWith("--", currentIndex) || '#' == text.charAt(currentIndex)) {
                int lineEndIndex = text.indexOf('\n', currentIndex + 1);
                currentIndex = -1 == lineEndIndex ? text.length() : lineEndIndex + 1;
                continue;
            }
            if (!text.startsWith("/*", currentIndex)) {
                currentIndex++;
                continue;
            }
            if (startsWithExecutableCommentMarker(text, currentIndex)) {
                return true;
            }
            int commentEndIndex = text.indexOf("*/", currentIndex + 2);
            currentIndex = -1 == commentEndIndex ? text.length() : commentEndIndex + 2;
        }
        return false;
    }
    
    private boolean startsWithExecutableCommentMarker(final String text) {
        return text.startsWith("/*!") || text.toUpperCase(Locale.ENGLISH).startsWith("/*M!");
    }
    
    private boolean startsWithExecutableCommentMarker(final String sql, final int startIndex) {
        return sql.startsWith("/*!", startIndex) || sql.regionMatches(true, startIndex, "/*M!", 0, 4);
    }
    
    boolean containsUserVariableAssignment() {
        for (int index = 0; index < visibleTokens.size(); index++) {
            Token variable = visibleTokens.get(index);
            if (variable.getText().startsWith("@") && containsUserVariableAssignment(index)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsUserVariableAssignment(final int variableIndex) {
        Token variable = visibleTokens.get(variableIndex);
        int assignmentIndex = variableIndex + 1;
        if ("@".equals(variable.getText())) {
            if (assignmentIndex >= visibleTokens.size() || visibleTokens.get(assignmentIndex).getStartIndex() != variable.getStopIndex() + 1) {
                return false;
            }
            variable = visibleTokens.get(assignmentIndex++);
        }
        if (1 >= variable.getText().length() && !isVariableName(variable.getText())) {
            return false;
        }
        return assignmentIndex < visibleTokens.size() && ":=".equals(visibleTokens.get(assignmentIndex).getText())
                && isWhitespace(variable.getStopIndex() + 1, visibleTokens.get(assignmentIndex).getStartIndex());
    }
    
    private boolean isVariableName(final String text) {
        if (text.isEmpty()) {
            return false;
        }
        char firstChar = text.charAt(0);
        return Character.isLetterOrDigit(firstChar) || '_' == firstChar || '$' == firstChar || '\'' == firstChar || '"' == firstChar || '`' == firstChar;
    }
    
    private boolean isWhitespace(final int startIndex, final int stopIndex) {
        for (int index = startIndex; index < stopIndex; index++) {
            if (!Character.isWhitespace(sql.charAt(index))) {
                return false;
            }
        }
        return true;
    }
    
    private List<Token> getVisibleTokens(final String sql, final List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        boolean executableComment = false;
        boolean dollarQuotedString = false;
        int skippedCommentEnd = -1;
        for (int index = 0; index < tokens.size(); index++) {
            Token each = tokens.get(index);
            if (each.getStartIndex() <= skippedCommentEnd) {
                continue;
            }
            String tokenName = getTokenName(each);
            if (BEGIN_DOLLAR_STRING_CONSTANT.equals(tokenName)) {
                dollarQuotedString = true;
                result.add(each);
                continue;
            }
            if (END_DOLLAR_STRING_CONSTANT.equals(tokenName)) {
                dollarQuotedString = false;
                continue;
            }
            if (Token.DEFAULT_CHANNEL != each.getChannel()) {
                if (executableComment && "*/".equals(each.getText())) {
                    executableComment = false;
                } else if (startsWithExecutableCommentMarker(each.getText()) && !each.getText().endsWith("*/")) {
                    executableComment = true;
                }
                continue;
            }
            int blockCommentEnd = findBlockCommentEnd(sql, tokens, index);
            if (-1 != blockCommentEnd) {
                skippedCommentEnd = blockCommentEnd;
                continue;
            }
            int lineCommentEnd = findLineCommentEnd(sql, tokens, index);
            if (-1 != lineCommentEnd) {
                skippedCommentEnd = lineCommentEnd;
                continue;
            }
            if (Token.EOF != each.getType() && !executableComment && !dollarQuotedString) {
                result.add(each);
            }
        }
        if (executableComment || dollarQuotedString) {
            throw new MCPUnsupportedSQLStatementException();
        }
        validateDelimiters(result);
        return result;
    }
    
    private int findBlockCommentEnd(final String sql, final List<Token> tokens, final int startIndex) {
        Token start = tokens.get(startIndex);
        if (!"/".equals(start.getText()) || startIndex + 1 >= tokens.size() || !"*".equals(tokens.get(startIndex + 1).getText())
                || tokens.get(startIndex + 1).getStartIndex() != start.getStopIndex() + 1) {
            return -1;
        }
        int result = sql.indexOf("*/", start.getStartIndex() + 2);
        if (-1 == result) {
            throw new MCPUnsupportedSQLStatementException();
        }
        return result + 1;
    }
    
    private int findLineCommentEnd(final String sql, final List<Token> tokens, final int startIndex) {
        Token start = tokens.get(startIndex);
        if (lineCommentsHandledByLexer || !"-".equals(start.getText()) || startIndex + 1 >= tokens.size() || !"-".equals(tokens.get(startIndex + 1).getText())
                || tokens.get(startIndex + 1).getStartIndex() != start.getStopIndex() + 1) {
            return -1;
        }
        int result = sql.indexOf('\n', start.getStartIndex() + 2);
        return -1 == result ? sql.length() - 1 : result;
    }
    
    private boolean areLineCommentsHandledByLexer(final List<Token> tokens) {
        for (String each : ((Lexer) tokens.get(0).getTokenSource()).getRuleNames()) {
            if (each.contains("LINE_COMMENT")) {
                return true;
            }
        }
        return false;
    }
    
    private void validateDelimiters(final List<Token> tokens) {
        int bracketDepth = 0;
        for (int index = 0; index < tokens.size(); index++) {
            Token each = tokens.get(index);
            if ("'".equals(each.getText()) || "\"".equals(each.getText()) || "`".equals(each.getText())) {
                throw new MCPUnsupportedSQLStatementException();
            }
            if ("[".equals(each.getText())) {
                bracketDepth++;
            } else if ("]".equals(each.getText()) && 0 < bracketDepth) {
                bracketDepth--;
            }
            if ("/".equals(each.getText()) && index + 1 < tokens.size() && "*".equals(tokens.get(index + 1).getText())
                    && tokens.get(index + 1).getStartIndex() == each.getStopIndex() + 1) {
                throw new MCPUnsupportedSQLStatementException();
            }
        }
        if (0 < bracketDepth) {
            throw new MCPUnsupportedSQLStatementException();
        }
    }
    
    private String getTokenName(final Token token) {
        return ((Lexer) token.getTokenSource()).getVocabulary().getSymbolicName(token.getType());
    }
    
    private List<Token> getTokens(final String sql) {
        SQLParser sqlParser = SQLParserFactory.newInstance(sql, lexerClass, parserClass);
        CommonTokenStream tokenStream = (CommonTokenStream) ((Parser) sqlParser).getTokenStream();
        tokenStream.fill();
        return tokenStream.getTokens();
    }
}

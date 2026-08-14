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

package org.apache.shardingsphere.sql.parser.statement.core.util;

import com.google.common.base.CharMatcher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Multi SQL splitter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiSQLSplitter {
    
    /**
     * Determine whether SQL contains multi statements that match the same DML type.
     *
     * @param sqlStatementSample parsed SQL statement sample
     * @param sqls SQLs
     * @return whether multi statements exist
     */
    public static boolean hasSameTypeMultiStatements(final SQLStatement sqlStatementSample, final Collection<String> sqls) {
        if (sqls.size() <= 1) {
            return false;
        }
        for (String each : sqls) {
            if (!matchesStatementType(stripLeadingComments(each), sqlStatementSample)) {
                return false;
            }
        }
        return true;
    }
    
    private static String stripLeadingComments(final String sql) {
        int index = 0;
        while (index < sql.length()) {
            index = skipWhitespace(sql, index);
            if (index >= sql.length()) {
                break;
            }
            if ('/' == sql.charAt(index) && index + 1 < sql.length() && '*' == sql.charAt(index + 1)) {
                int end = sql.indexOf("*/", index + 2);
                if (end < 0) {
                    return "";
                }
                index = end + 2;
                continue;
            }
            if (isDashCommentStart(sql, index)) {
                index = skipLine(sql, index + 2);
                continue;
            }
            if ('#' == sql.charAt(index)) {
                index = skipLine(sql, index + 1);
                continue;
            }
            break;
        }
        return sql.substring(index).trim();
    }
    
    private static int skipWhitespace(final String sql, final int start) {
        int index = CharMatcher.whitespace().negate().indexIn(sql, start);
        return -1 == index ? sql.length() : index;
    }
    
    private static int skipLine(final String sql, final int startIndex) {
        int index = startIndex;
        while (index < sql.length()) {
            char current = sql.charAt(index);
            index++;
            if ('\n' == current) {
                break;
            }
            if ('\r' == current) {
                if (index < sql.length() && '\n' == sql.charAt(index)) {
                    index++;
                }
                break;
            }
        }
        return index;
    }
    
    private static boolean matchesStatementType(final String sql, final SQLStatement sqlStatementSample) {
        if (sql.isEmpty()) {
            return false;
        }
        if (sqlStatementSample instanceof InsertStatement) {
            return startsWithIgnoreCase(sql, "insert");
        }
        if (sqlStatementSample instanceof UpdateStatement) {
            return startsWithIgnoreCase(sql, "update");
        }
        if (sqlStatementSample instanceof DeleteStatement) {
            return startsWithIgnoreCase(sql, "delete");
        }
        return false;
    }
    
    private static boolean startsWithIgnoreCase(final String text, final String prefix) {
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }
    
    /**
     * Split SQL text by semicolon ignoring literals and comments.
     *
     * @param sql SQL text
     * @return SQL statements
     */
    public static Collection<String> split(final String sql) {
        if (null == sql || sql.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<String> result = new LinkedList<>();
        StringBuilder current = new StringBuilder(sql.length());
        ScanState state = ScanState.NORMAL;
        QuoteCharacter quote = QuoteCharacter.NONE;
        int index = 0;
        int length = sql.length();
        while (index < length) {
            char ch = sql.charAt(index);
            char next = index + 1 < length ? sql.charAt(index + 1) : '\0';
            int step = 1;
            switch (state) {
                case QUOTE:
                    current.append(ch);
                    if (QuoteCharacter.BACK_QUOTE != quote && '\\' == ch && index + 1 < length) {
                        current.append(sql.charAt(index + 1));
                        step = 2;
                        break;
                    }
                    if (isQuoteEnd(quote, ch)) {
                        if (isRepeatedQuote(sql, quote, index)) {
                            current.append(sql.charAt(index + 1));
                            step = 2;
                        } else {
                            quote = QuoteCharacter.NONE;
                            state = ScanState.NORMAL;
                        }
                    }
                    break;
                case LINE_COMMENT:
                    current.append(ch);
                    if ('\n' == ch || '\r' == ch) {
                        state = ScanState.NORMAL;
                    }
                    break;
                case BLOCK_COMMENT:
                    current.append(ch);
                    if ('*' == ch && '/' == next) {
                        current.append(next);
                        step = 2;
                        state = ScanState.NORMAL;
                    }
                    break;
                default:
                    if (';' == ch) {
                        appendStatement(result, current);
                        break;
                    }
                    QuoteCharacter quoteCandidate = QuoteCharacter.getQuoteCharacter(String.valueOf(ch));
                    if (isSupportedQuote(quoteCandidate)) {
                        quote = quoteCandidate;
                        state = ScanState.QUOTE;
                        current.append(ch);
                        break;
                    }
                    if (isDashCommentStart(sql, index)) {
                        state = ScanState.LINE_COMMENT;
                        current.append(ch);
                        current.append(next);
                        step = 2;
                        break;
                    }
                    if ('#' == ch) {
                        state = ScanState.LINE_COMMENT;
                        current.append(ch);
                        break;
                    }
                    if ('/' == ch && '*' == next) {
                        state = ScanState.BLOCK_COMMENT;
                        current.append(ch);
                        current.append(next);
                        step = 2;
                        break;
                    }
                    current.append(ch);
                    break;
            }
            index += step;
        }
        appendStatement(result, current);
        return result;
    }
    
    private static void appendStatement(final Collection<String> statements, final StringBuilder current) {
        String value = current.toString().trim();
        if (!value.isEmpty()) {
            statements.add(value);
        }
        current.setLength(0);
    }
    
    private static boolean isDashCommentStart(final String sql, final int index) {
        if (index + 1 >= sql.length()) {
            return false;
        }
        if ('-' != sql.charAt(index) || '-' != sql.charAt(index + 1)) {
            return false;
        }
        int commentContentIndex = index + 2;
        return commentContentIndex >= sql.length() || Character.isWhitespace(sql.charAt(commentContentIndex));
    }
    
    private static boolean isSupportedQuote(final QuoteCharacter quote) {
        return QuoteCharacter.SINGLE_QUOTE == quote || QuoteCharacter.QUOTE == quote || QuoteCharacter.BACK_QUOTE == quote;
    }
    
    private static boolean isQuoteEnd(final QuoteCharacter quote, final char ch) {
        return QuoteCharacter.NONE != quote && quote.getEndDelimiter().charAt(0) == ch;
    }
    
    private static boolean isRepeatedQuote(final String sql, final QuoteCharacter quote, final int index) {
        int nextIndex = index + 1;
        return nextIndex < sql.length() && sql.charAt(nextIndex) == quote.getEndDelimiter().charAt(0);
    }
    
    private enum ScanState {
        NORMAL, QUOTE, LINE_COMMENT, BLOCK_COMMENT
    }
}

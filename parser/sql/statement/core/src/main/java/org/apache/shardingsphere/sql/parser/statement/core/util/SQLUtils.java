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
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.enums.Paren;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NullLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.TemporalLiteralValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLUtils {
    
    private static final String BACKTICK = "`";
    
    private static final String SQL_END = ";";
    
    private static final String COMMENT_PREFIX = "/*";
    
    private static final String COMMENT_SUFFIX = "*/";
    
    private static final String EXCLUDED_CHARACTERS = "[]'\"";
    
    private static final BigInteger INTEGER_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    
    private static final BigInteger INTEGER_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    
    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    
    /**
     * Get exactly number value and type.
     *
     * @param value string to be converted
     * @param radix radix
     * @return exactly number value and type
     */
    public static Number getExactlyNumber(final String value, final int radix) {
        try {
            return getExactlyNumber(new BigInteger(value, radix));
        } catch (final NumberFormatException ex) {
            return new BigDecimal(value);
        }
    }
    
    /**
     * Get exactly number.
     *
     * @param value to be converted value
     * @return converted value
     */
    public static Number getExactlyNumber(final BigInteger value) {
        if (value.compareTo(INTEGER_MIN) >= 0 && value.compareTo(INTEGER_MAX) <= 0) {
            return value.intValue();
        }
        if (value.compareTo(LONG_MIN) >= 0 && value.compareTo(LONG_MAX) <= 0) {
            return value.longValue();
        }
        return value;
    }
    
    /**
     * Get exactly value for SQL expression.
     *
     * <p>remove special char for SQL expression</p>
     *
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExactlyValue(final String value) {
        return null == value ? null : tryGetRealContentInBackticks(CharMatcher.anyOf(EXCLUDED_CHARACTERS).removeFrom(value));
    }
    
    /**
     * Get exactly value for SQL expression.
     *
     * <p>remove special char for SQL expression</p>
     *
     * @param value SQL expression
     * @param reservedCharacters characters to be reserved
     * @return exactly SQL expression
     */
    public static String getExactlyValue(final String value, final String reservedCharacters) {
        if (null == value) {
            return null;
        }
        String toBeExcludedCharacters = CharMatcher.anyOf(reservedCharacters).removeFrom(EXCLUDED_CHARACTERS);
        return CharMatcher.anyOf(toBeExcludedCharacters).removeFrom(value);
    }
    
    /**
     * Try get exactly value for backticks string.
     *
     * <p>try get content containing backticks exactly value</p>
     *
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String tryGetRealContentInBackticks(final String value) {
        if (null == value) {
            return null;
        }
        if (value.startsWith(BACKTICK) && value.endsWith(BACKTICK)) {
            return getRealContentInBackticks(value);
        }
        return value;
    }
    
    /**
     * Get exactly content in backticks.
     *
     * @param value SQL expression
     * @return exactly content in backticks
     */
    public static String getRealContentInBackticks(final String value) {
        if (null == value) {
            return null;
        }
        int startIndex = 1;
        int stopIndex = value.length() - 1;
        StringBuilder exactlyTableName = new StringBuilder();
        while (startIndex < stopIndex) {
            if (value.charAt(startIndex) == '`' && (startIndex + 1 >= stopIndex || value.charAt(startIndex + 1) != '`')) {
                return value;
            } else if (value.charAt(startIndex) == '`' && value.charAt(startIndex + 1) == '`') {
                startIndex++;
            }
            exactlyTableName.append(value.charAt(startIndex));
            startIndex++;
        }
        return 0 == exactlyTableName.length() ? value : exactlyTableName.toString();
    }
    
    /**
     * Get exactly SQL expression.
     *
     * <p>remove space for SQL expression</p>
     *
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExactlyExpression(final String value) {
        return Strings.isNullOrEmpty(value) ? value : CharMatcher.anyOf(" ").removeFrom(value);
    }
    
    /**
     * Get exactly SQL expression without outside parentheses.
     *
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExpressionWithoutOutsideParentheses(final String value) {
        int parenthesesOffset = getParenthesesOffset(value);
        if (0 == parenthesesOffset) {
            return value;
        }
        String result = value.substring(parenthesesOffset, value.length() - parenthesesOffset);
        return isValidParenthesis(result) ? result : value;
    }
    
    private static int getParenthesesOffset(final String value) {
        int left = 0;
        if (Strings.isNullOrEmpty(value)) {
            return left;
        }
        
        int right = value.length() - 1;
        while (Paren.PARENTHESES.getLeftParen() == value.charAt(left) && Paren.PARENTHESES.getRightParen() == value.charAt(right)) {
            left++;
            right--;
        }
        return left;
    }
    
    private static boolean isValidParenthesis(final String text) {
        int count = 0;
        for (char each : text.toCharArray()) {
            if (Paren.PARENTHESES.getLeftParen() == each) {
                count++;
            } else if (Paren.PARENTHESES.getRightParen() == each) {
                if (count == 0) {
                    return false;
                }
                count--;
            }
        }
        return count == 0;
    }
    
    /**
     * Get subquery from tableSegment.
     *
     * @param tableSegment TableSegment
     * @return exactly SubqueryTableSegment list
     */
    public static List<SubqueryTableSegment> getSubqueryTableSegmentFromTableSegment(final TableSegment tableSegment) {
        List<SubqueryTableSegment> result = new LinkedList<>();
        if (tableSegment instanceof SubqueryTableSegment) {
            result.add((SubqueryTableSegment) tableSegment);
        }
        if (tableSegment instanceof JoinTableSegment) {
            result.addAll(getSubqueryTableSegmentFromJoinTableSegment((JoinTableSegment) tableSegment));
        }
        return result;
    }
    
    private static List<SubqueryTableSegment> getSubqueryTableSegmentFromJoinTableSegment(final JoinTableSegment joinTableSegment) {
        List<SubqueryTableSegment> result = new LinkedList<>();
        if (joinTableSegment.getLeft() instanceof SubqueryTableSegment) {
            result.add((SubqueryTableSegment) joinTableSegment.getLeft());
        } else if (joinTableSegment.getLeft() instanceof JoinTableSegment) {
            result.addAll(getSubqueryTableSegmentFromJoinTableSegment((JoinTableSegment) joinTableSegment.getLeft()));
        }
        if (joinTableSegment.getRight() instanceof SubqueryTableSegment) {
            result.add((SubqueryTableSegment) joinTableSegment.getRight());
        } else if (joinTableSegment.getRight() instanceof JoinTableSegment) {
            result.addAll(getSubqueryTableSegmentFromJoinTableSegment((JoinTableSegment) joinTableSegment.getRight()));
        }
        return result;
    }
    
    /**
     * Create literal expression.
     *
     * @param astNode AST node
     * @param startIndex start index
     * @param stopIndex stop index
     * @param text text
     * @return literal expression segment
     */
    public static ExpressionSegment createLiteralExpression(final ASTNode astNode, final int startIndex, final int stopIndex, final String text) {
        if (astNode instanceof StringLiteralValue) {
            return new LiteralExpressionSegment(startIndex, stopIndex, ((StringLiteralValue) astNode).getValue());
        }
        if (astNode instanceof NumberLiteralValue) {
            return new LiteralExpressionSegment(startIndex, stopIndex, ((NumberLiteralValue) astNode).getValue());
        }
        if (astNode instanceof BooleanLiteralValue) {
            return new LiteralExpressionSegment(startIndex, stopIndex, ((BooleanLiteralValue) astNode).getValue());
        }
        if (astNode instanceof NullLiteralValue) {
            return new LiteralExpressionSegment(startIndex, stopIndex, null);
        }
        if (astNode instanceof TemporalLiteralValue) {
            return new LiteralExpressionSegment(startIndex, stopIndex, ((TemporalLiteralValue) astNode).getValue());
        }
        if (astNode instanceof OtherLiteralValue) {
            return new CommonExpressionSegment(startIndex, stopIndex, ((OtherLiteralValue) astNode).getValue());
        }
        return new CommonExpressionSegment(startIndex, stopIndex, text);
    }
    
    /**
     * Trim the semicolon of SQL.
     *
     * @param sql SQL to be trim
     * @return SQL without semicolon
     */
    public static String trimSemicolon(final String sql) {
        return sql.endsWith(SQL_END) ? sql.substring(0, sql.length() - 1) : sql;
    }
    
    /**
     * Trim the comment of SQL.
     *
     * @param sql to be trimmed SQL
     * @return trimmed SQL
     */
    public static String trimComment(final String sql) {
        String result = sql;
        if (sql.startsWith(COMMENT_PREFIX)) {
            result = result.substring(sql.indexOf(COMMENT_SUFFIX) + 2);
        }
        if (sql.endsWith(SQL_END)) {
            result = result.substring(0, result.length() - 1);
        }
        return result.trim();
    }
}

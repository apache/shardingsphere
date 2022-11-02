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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.constant.Paren;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLRepairTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SQL utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLUtil {
    
    private static final String SQL_END = ";";
    
    private static final String COMMENT_PREFIX = "/*";
    
    private static final String COMMENT_SUFFIX = "*/";
    
    private static final String EXCLUDED_CHARACTERS = "[]`'\"";
    
    private static final Pattern SINGLE_CHARACTER_PATTERN = Pattern.compile("^_|([^\\\\])_");
    
    private static final Pattern SINGLE_CHARACTER_ESCAPE_PATTERN = Pattern.compile("\\\\_");
    
    private static final Pattern ANY_CHARACTER_PATTERN = Pattern.compile("^%|([^\\\\])%");
    
    private static final Pattern ANY_CHARACTER_ESCAPE_PATTERN = Pattern.compile("\\\\%");
    
    /**
     * Get exactly number value and type.
     *
     * @param value string to be converted
     * @param radix radix
     * @return exactly number value and type
     */
    public static Number getExactlyNumber(final String value, final int radix) {
        try {
            return getBigInteger(value, radix);
        } catch (final NumberFormatException ex) {
            return new BigDecimal(value);
        }
    }
    
    private static Number getBigInteger(final String value, final int radix) {
        BigInteger result = new BigInteger(value, radix);
        if (result.compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) <= 0) {
            return result.intValue();
        }
        if (result.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) <= 0) {
            return result.longValue();
        }
        return result;
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
        return null == value ? null : CharMatcher.anyOf(EXCLUDED_CHARACTERS).removeFrom(value);
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
        return 0 == parenthesesOffset ? value : value.substring(parenthesesOffset, value.length() - parenthesesOffset);
    }
    
    private static int getParenthesesOffset(final String value) {
        int result = 0;
        if (Strings.isNullOrEmpty(value)) {
            return result;
        }
        while (Paren.PARENTHESES.getLeftParen() == value.charAt(result)) {
            result++;
        }
        return result;
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
     * Determine whether SQL is read-only.
     *
     * @param sqlStatement SQL statement
     * @return true if read-only, otherwise false
     */
    public static boolean isReadOnly(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DMLStatement) {
            return isReadOnly((DMLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DALStatement) {
            return isReadOnly((DALStatement) sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return false;
        }
        if (sqlStatement instanceof DCLStatement) {
            return false;
        }
        throw new UnsupportedSQLOperationException(String.format("Unsupported SQL Type `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static boolean isReadOnly(final DMLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return true;
        }
        if (sqlStatement instanceof UpdateStatement || sqlStatement instanceof DeleteStatement || sqlStatement instanceof InsertStatement) {
            return false;
        }
        throw new UnsupportedSQLOperationException(String.format("Unsupported SQL Type `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static boolean isReadOnly(final DALStatement sqlStatement) {
        return !(sqlStatement instanceof SetStatement
                | sqlStatement instanceof MySQLUseStatement
                | sqlStatement instanceof MySQLUninstallPluginStatement
                | sqlStatement instanceof MySQLResetStatement
                | sqlStatement instanceof MySQLRepairTableStatement
                | sqlStatement instanceof MySQLOptimizeTableStatement
                | sqlStatement instanceof MySQLLoadIndexInfoStatement
                | sqlStatement instanceof MySQLKillStatement
                | sqlStatement instanceof MySQLInstallPluginStatement
                | sqlStatement instanceof MySQLFlushStatement
                | sqlStatement instanceof MySQLChecksumTableStatement
                | sqlStatement instanceof MySQLCacheIndexStatement);
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
     * @param sql SQL to be trim
     * @return remove comment from SQL
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
    
    /**
     * Convert like pattern to regex.
     * 
     * @param pattern like pattern
     * @return regex
     */
    public static String convertLikePatternToRegex(final String pattern) {
        String result = pattern;
        if (pattern.contains("_")) {
            result = SINGLE_CHARACTER_PATTERN.matcher(result).replaceAll("$1.");
            result = SINGLE_CHARACTER_ESCAPE_PATTERN.matcher(result).replaceAll("_");
        }
        if (pattern.contains("%")) {
            result = ANY_CHARACTER_PATTERN.matcher(result).replaceAll("$1.*");
            result = ANY_CHARACTER_ESCAPE_PATTERN.matcher(result).replaceAll("%");
        }
        return result;
    }
}

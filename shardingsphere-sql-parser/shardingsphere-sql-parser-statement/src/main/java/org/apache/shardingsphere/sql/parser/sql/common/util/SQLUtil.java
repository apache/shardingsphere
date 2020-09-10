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
import org.apache.shardingsphere.sql.parser.sql.common.constant.Paren;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerDenyUserStatement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLUtil {
    
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
        return null == value ? null : CharMatcher.anyOf("[]`'\"").removeFrom(value);
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
     * @param tableSegment TableSegment.
     * @return exactly SubqueryTableSegment list.
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
     
     /*
     * Determine whether SQL is read-only.
     *
     * @param sqlStatement SQL statement
     * @return true if read-only, otherwise false
     */
    public static boolean isReadOnly(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DMLStatement) {
            return isReadOnly((DMLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return isReadOnly((DDLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DCLStatement) {
            return isReadOnly((DCLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DALStatement) {
            return isReadOnly((DALStatement) sqlStatement);
        }
        throw new UnsupportedOperationException(String.format("Unsupported SQL Type `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static boolean isReadOnly(final DMLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return true;
        } else if (sqlStatement instanceof UpdateStatement
                | sqlStatement instanceof DeleteStatement
                | sqlStatement instanceof InsertStatement) {
            return false;
        }
        throw new UnsupportedOperationException(String.format("Unsupported SQL Type `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static boolean isReadOnly(final DDLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement
                | sqlStatement instanceof AlterTableStatement
                | sqlStatement instanceof DropTableStatement
                | sqlStatement instanceof CreateIndexStatement
                | sqlStatement instanceof AlterIndexStatement
                | sqlStatement instanceof DropIndexStatement
                | sqlStatement instanceof TruncateStatement
                | sqlStatement instanceof AlterTableStatement) {
            return false;
        }
        return false;
    }
    
    private static boolean isReadOnly(final DCLStatement sqlStatement) {
        if (sqlStatement instanceof GrantStatement
                | sqlStatement instanceof RevokeStatement
                | sqlStatement instanceof SQLServerDenyUserStatement) {
            return false;
        }
        return false;
    }
    
    private static boolean isReadOnly(final DALStatement sqlStatement) {
        if (sqlStatement instanceof SetStatement
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
                | sqlStatement instanceof MySQLCacheIndexStatement) {
            return false;
        }
        return true;
    }
}

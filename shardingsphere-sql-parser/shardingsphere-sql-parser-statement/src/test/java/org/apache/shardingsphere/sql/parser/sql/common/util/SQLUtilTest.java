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

import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowMasterStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropDimensionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerRevertStatement;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public final class SQLUtilTest {
    
    @Test
    public void assertGetExactlyNumberForInteger() {
        assertThat(SQLUtil.getExactlyNumber("100000", 10), is(100000));
        assertThat(SQLUtil.getExactlyNumber("100000", 16), is(1048576));
        assertThat(SQLUtil.getExactlyNumber(String.valueOf(Integer.MIN_VALUE), 10), is(Integer.MIN_VALUE));
        assertThat(SQLUtil.getExactlyNumber(String.valueOf(Integer.MAX_VALUE), 10), is(Integer.MAX_VALUE));
    }
    
    @Test
    public void assertGetExactlyNumberForLong() {
        assertThat(SQLUtil.getExactlyNumber("100000000000", 10), is(100000000000L));
        assertThat(SQLUtil.getExactlyNumber("100000000000", 16), is(17592186044416L));
        assertThat(SQLUtil.getExactlyNumber(String.valueOf(Long.MIN_VALUE), 10), is(Long.MIN_VALUE));
        assertThat(SQLUtil.getExactlyNumber(String.valueOf(Long.MAX_VALUE), 10), is(Long.MAX_VALUE));
    }
    
    @Test
    public void assertGetExactlyNumberForBigInteger() {
        assertThat(SQLUtil.getExactlyNumber("10000000000000000000", 10), is(new BigInteger("10000000000000000000")));
        assertThat(SQLUtil.getExactlyNumber("10000000000000000000", 16), is(new BigInteger("75557863725914323419136")));
        assertThat(SQLUtil.getExactlyNumber(String.valueOf(Long.MIN_VALUE + 1), 10), is(Long.MIN_VALUE + 1));
        assertThat(SQLUtil.getExactlyNumber(String.valueOf(Long.MAX_VALUE - 1), 10), is(Long.MAX_VALUE - 1));
    }
    
    @Test
    public void assertGetExactlyNumberForBigDecimal() {
        assertThat(SQLUtil.getExactlyNumber("1.1", 10), is(new BigDecimal("1.1")));
    }
    
    @Test
    public void assertGetExactlyValue() {
        assertThat(SQLUtil.getExactlyValue("`xxx`"), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("[xxx]"), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("\"xxx\""), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("'xxx'"), is("xxx"));
    }
    
    @Test
    public void assertGetExactlyValueWithReservedCharacters() {
        assertThat(SQLUtil.getExactlyValue("`xxx`", "`"), is("`xxx`"));
        assertThat(SQLUtil.getExactlyValue("[xxx]", "[]"), is("[xxx]"));
        assertThat(SQLUtil.getExactlyValue("\"xxx\"", "\""), is("\"xxx\""));
        assertThat(SQLUtil.getExactlyValue("'xxx'", "'"), is("'xxx'"));
    }
    
    @Test
    public void assertGetExactlyValueUsingNull() {
        assertNull(SQLUtil.getExactlyValue(null));
    }
    
    @Test
    public void assertGetExactlyExpressionUsingAndReturningNull() {
        assertNull(SQLUtil.getExactlyExpression(null));
    }
    
    @Test
    public void assertGetExactlyExpressionUsingAndReturningEmptyString() {
        assertThat(SQLUtil.getExactlyExpression(""), is(""));
    }
    
    @Test
    public void assertGetExactlyExpression() {
        assertThat(SQLUtil.getExactlyExpression("((a + b*c))"), is("((a+b*c))"));
    }
    
    @Test
    public void assertGetExpressionWithoutOutsideParentheses() {
        assertThat(SQLUtil.getExpressionWithoutOutsideParentheses("((a + b*c))"), is("a + b*c"));
        assertThat(SQLUtil.getExpressionWithoutOutsideParentheses(""), is(""));
    }
    
    @Test
    public void assertConvertLikePatternToRegexWhenEndWithPattern() {
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding_'"), is("SHOW DATABASES LIKE 'sharding.'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding%'"), is("SHOW DATABASES LIKE 'sharding.*'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding%_'"), is("SHOW DATABASES LIKE 'sharding.*.'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding\\_'"), is("SHOW DATABASES LIKE 'sharding_'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding\\%'"), is("SHOW DATABASES LIKE 'sharding%'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding\\%\\_'"), is("SHOW DATABASES LIKE 'sharding%_'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding_\\_'"), is("SHOW DATABASES LIKE 'sharding._'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding%\\%'"), is("SHOW DATABASES LIKE 'sharding.*%'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding_\\%'"), is("SHOW DATABASES LIKE 'sharding.%'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding\\_%'"), is("SHOW DATABASES LIKE 'sharding_.*'"));
    }
    
    @Test
    public void assertConvertLikePatternToRegexWhenStartWithPattern() {
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '_sharding'"), is("SHOW DATABASES LIKE '.sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '%sharding'"), is("SHOW DATABASES LIKE '.*sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '%_sharding'"), is("SHOW DATABASES LIKE '.*.sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '\\_sharding'"), is("SHOW DATABASES LIKE '_sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '\\%sharding'"), is("SHOW DATABASES LIKE '%sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '\\%\\_sharding'"), is("SHOW DATABASES LIKE '%_sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '_\\_sharding'"), is("SHOW DATABASES LIKE '._sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '%\\%sharding'"), is("SHOW DATABASES LIKE '.*%sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '_\\%sharding'"), is("SHOW DATABASES LIKE '.%sharding'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE '\\_%sharding'"), is("SHOW DATABASES LIKE '_.*sharding'"));
    }
    
    @Test
    public void assertConvertLikePatternToRegexWhenContainsPattern() {
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding_db'"), is("SHOW DATABASES LIKE 'sharding.db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding%db'"), is("SHOW DATABASES LIKE 'sharding.*db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding%_db'"), is("SHOW DATABASES LIKE 'sharding.*.db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding\\_db'"), is("SHOW DATABASES LIKE 'sharding_db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding\\%db'"), is("SHOW DATABASES LIKE 'sharding%db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding\\%\\_db'"), is("SHOW DATABASES LIKE 'sharding%_db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding_\\_db'"), is("SHOW DATABASES LIKE 'sharding._db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding%\\%db'"), is("SHOW DATABASES LIKE 'sharding.*%db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding_\\%db'"), is("SHOW DATABASES LIKE 'sharding.%db'"));
        assertThat(SQLUtil.convertLikePatternToRegex("SHOW DATABASES LIKE 'sharding\\_%db'"), is("SHOW DATABASES LIKE 'sharding_.*db'"));
    }
    
    @Test
    public void assertIsReadOnly() {
        assertFalse(SQLUtil.isReadOnly(new SQLServerRevertStatement()));
        assertFalse(SQLUtil.isReadOnly(new OracleDropDimensionStatement()));
        assertFalse(SQLUtil.isReadOnly(new MySQLUpdateStatement()));
        assertFalse(SQLUtil.isReadOnly(new MySQLDeleteStatement()));
        assertFalse(SQLUtil.isReadOnly(new MySQLInsertStatement()));
        assertTrue(SQLUtil.isReadOnly(new MySQLSelectStatement()));
        assertTrue(SQLUtil.isReadOnly(new MySQLShowMasterStatusStatement()));
    }
    
    @Test
    public void assertTrimSemiColon() {
        assertThat(SQLUtil.trimSemicolon("SHOW DATABASES;"), is("SHOW DATABASES"));
        assertThat(SQLUtil.trimSemicolon("SHOW DATABASES"), is("SHOW DATABASES"));
    }
    
    @Test
    public void assertTrimComment() {
        assertThat(SQLUtil.trimComment("/* This is a comment */ SHOW DATABASES"), is("SHOW DATABASES"));
        assertThat(SQLUtil.trimComment("/* This is a query with a semicolon */ SHOW DATABASES;"), is("SHOW DATABASES"));
        assertThat(SQLUtil.trimComment("/* This is a query with spaces */    SHOW DATABASES   "), is("SHOW DATABASES"));
    }
}

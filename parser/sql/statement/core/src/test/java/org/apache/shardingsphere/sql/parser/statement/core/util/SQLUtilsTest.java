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

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.TemporalLiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.DateTimeLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.TemporalLiteralValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class SQLUtilsTest {
    
    @Test
    void assertGetExactlyNumberForInteger() {
        assertThat(SQLUtils.getExactlyNumber("100000", 10), is(100000));
        assertThat(SQLUtils.getExactlyNumber("100000", 16), is(1048576));
        assertThat(SQLUtils.getExactlyNumber(String.valueOf(Integer.MIN_VALUE), 10), is(Integer.MIN_VALUE));
        assertThat(SQLUtils.getExactlyNumber(String.valueOf(Integer.MAX_VALUE), 10), is(Integer.MAX_VALUE));
    }
    
    @Test
    void assertGetExactlyNumberForLong() {
        assertThat(SQLUtils.getExactlyNumber("100000000000", 10), is(100000000000L));
        assertThat(SQLUtils.getExactlyNumber("100000000000", 16), is(17592186044416L));
        assertThat(SQLUtils.getExactlyNumber(String.valueOf(Long.MIN_VALUE), 10), is(Long.MIN_VALUE));
        assertThat(SQLUtils.getExactlyNumber(String.valueOf(Long.MAX_VALUE), 10), is(Long.MAX_VALUE));
    }
    
    @Test
    void assertGetExactlyNumberForBigInteger() {
        assertThat(SQLUtils.getExactlyNumber("10000000000000000000", 10), is(new BigInteger("10000000000000000000")));
        assertThat(SQLUtils.getExactlyNumber("10000000000000000000", 16), is(new BigInteger("75557863725914323419136")));
        assertThat(SQLUtils.getExactlyNumber(String.valueOf(Long.MIN_VALUE + 1L), 10), is(Long.MIN_VALUE + 1L));
        assertThat(SQLUtils.getExactlyNumber(String.valueOf(Long.MAX_VALUE - 1L), 10), is(Long.MAX_VALUE - 1L));
    }
    
    @Test
    void assertGetExactlyNumberForBigDecimal() {
        assertThat(SQLUtils.getExactlyNumber("1.1", 10), is(new BigDecimal("1.1")));
    }
    
    @Test
    void assertGetExactlyValue() {
        assertThat(SQLUtils.getExactlyValue("`xxx`"), is("xxx"));
        assertThat(SQLUtils.getExactlyValue("[xxx]"), is("xxx"));
        assertThat(SQLUtils.getExactlyValue("\"xxx\""), is("xxx"));
        assertThat(SQLUtils.getExactlyValue("'xxx'"), is("xxx"));
        assertThat(SQLUtils.getExactlyValue("`[xxx`"), is("xxx"));
        assertThat(SQLUtils.getExactlyValue("```[xxx```"), is("`xxx`"));
    }
    
    @Test
    void assertGetExactlyValueWithReservedCharacters() {
        assertThat(SQLUtils.getExactlyValue("`xxx`", "`"), is("`xxx`"));
        assertThat(SQLUtils.getExactlyValue("[xxx]", "[]"), is("[xxx]"));
        assertThat(SQLUtils.getExactlyValue("\"xxx\"", "\""), is("\"xxx\""));
        assertThat(SQLUtils.getExactlyValue("'xxx'", "'"), is("'xxx'"));
    }
    
    @Test
    void assertGetExactlyValueUsingNull() {
        assertNull(SQLUtils.getExactlyValue(null));
    }
    
    @Test
    void assertGetExactlyExpressionUsingAndReturningNull() {
        assertNull(SQLUtils.getExactlyExpression(null));
    }
    
    @Test
    void assertGetExactlyExpressionUsingAndReturningEmptyString() {
        assertThat(SQLUtils.getExactlyExpression(""), is(""));
    }
    
    @Test
    void assertGetExactlyExpression() {
        assertThat(SQLUtils.getExactlyExpression("((a + b*c))"), is("((a+b*c))"));
    }
    
    @Test
    void assertCreateLiteralExpressionForTemporalLiteral() {
        TemporalLiteralExpressionSegment actual = (TemporalLiteralExpressionSegment) SQLUtils.createLiteralExpression(
                new TemporalLiteralValue("DATE", "'2024-03-01'"), 0, 16, "DATE '2024-03-01'");
        assertThat(actual.getLiterals(), is("2024-03-01"));
        assertThat(actual.getText(), is("DATE '2024-03-01'"));
    }
    
    @Test
    void assertCreateLiteralExpressionForDateTimeLiteral() {
        TemporalLiteralExpressionSegment actual = (TemporalLiteralExpressionSegment) SQLUtils.createLiteralExpression(
                new DateTimeLiteralValue("DATE", "'2024-03-01'", false), 0, 16, "DATE '2024-03-01'");
        assertThat(actual.getLiterals(), is("2024-03-01"));
        assertThat(actual.getText(), is("DATE '2024-03-01'"));
    }
    
    @Test
    void assertCreateLiteralExpressionForBraceDateTimeLiteral() {
        TemporalLiteralExpressionSegment actual = (TemporalLiteralExpressionSegment) SQLUtils.createLiteralExpression(
                new DateTimeLiteralValue("ts", "'2024-03-01 10:00:00'", true), 0, 25, "{ts '2024-03-01 10:00:00'}");
        assertThat(actual.getLiterals(), is("2024-03-01 10:00:00"));
        assertThat(actual.getText(), is("{ts '2024-03-01 10:00:00'}"));
    }
    
    @Test
    void assertGetExpressionWithoutOutsideParentheses() {
        assertThat(SQLUtils.getExpressionWithoutOutsideParentheses("((a + b*c))"), is("a + b*c"));
        assertThat(SQLUtils.getExpressionWithoutOutsideParentheses(""), is(""));
    }
    
    @Test
    void assertTrimSemiColon() {
        assertThat(SQLUtils.trimSemicolon("SHOW DATABASES;"), is("SHOW DATABASES"));
        assertThat(SQLUtils.trimSemicolon("SHOW DATABASES"), is("SHOW DATABASES"));
    }
    
    @Test
    void assertTrimComment() {
        assertThat(SQLUtils.trimComment("/* This is a comment */ SHOW DATABASES"), is("SHOW DATABASES"));
        assertThat(SQLUtils.trimComment("/* This is a query with a semicolon */ SHOW DATABASES;"), is("SHOW DATABASES"));
        assertThat(SQLUtils.trimComment("/* This is a query with spaces */    SHOW DATABASES   "), is("SHOW DATABASES"));
    }
    
    @Test
    void assertTryGetRealContentInBackticks() {
        assertThat(SQLUtils.tryGetRealContentInBackticks("`"), is("`"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("``"), is("``"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("```"), is("```"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("t_order"), is("t_order"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("````"), is("`"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("`t_order`"), is("t_order"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("```t_order`"), is("`t_order"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("```````t_order```````"), is("```t_order```"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("``t_order`"), is("``t_order`"));
        assertThat(SQLUtils.tryGetRealContentInBackticks("`````t_o``r``d``e``r```"), is("``t_o`r`d`e`r`"));
    }
}

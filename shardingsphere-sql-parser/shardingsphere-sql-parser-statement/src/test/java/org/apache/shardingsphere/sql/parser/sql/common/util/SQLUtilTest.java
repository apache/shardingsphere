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

package org.apache.shardingsphere.sql.parser.sql.util;

import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

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
}

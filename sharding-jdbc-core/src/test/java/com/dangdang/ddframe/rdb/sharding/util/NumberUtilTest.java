/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.util;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class NumberUtilTest {
    
    @Test
    public void assertRoundHalfUpWithInteger() {
        assertThat(NumberUtil.roundHalfUp(1), is(1));
        assertThat(NumberUtil.roundHalfUp(2), is(2));
    }
    
    @Test
    public void assertRoundHalfUpWithDouble() {
        assertThat(NumberUtil.roundHalfUp(1.499d), is(1));
        assertThat(NumberUtil.roundHalfUp(1.5d), is(2));
    }
    
    @Test
    public void assertRoundHalfUpWithFloat() {
        assertThat(NumberUtil.roundHalfUp(1.499f), is(1));
        assertThat(NumberUtil.roundHalfUp(1.5f), is(2));
    }
    
    @Test
    public void assertRoundHalfUpWithString() {
        assertThat(NumberUtil.roundHalfUp("1.499"), is(1));
        assertThat(NumberUtil.roundHalfUp("1.5"), is(2));
    }
    
    @Test(expected = ShardingJdbcException.class)
    public void assertRoundHalfUpWithInvalidType() {
        NumberUtil.roundHalfUp(new Object());
    }
    
    @Test
    public void assertGetExactlyNumberForInteger() {
        assertThat(NumberUtil.getExactlyNumber("100000", 10), is((Number) 100000));
        assertThat(NumberUtil.getExactlyNumber("100000", 16), is((Number) 1048576));
        assertThat(NumberUtil.getExactlyNumber(String.valueOf(Integer.MIN_VALUE), 10), is((Number) Integer.MIN_VALUE));
        assertThat(NumberUtil.getExactlyNumber(String.valueOf(Integer.MAX_VALUE), 10), is((Number) Integer.MAX_VALUE));
    }
    
    @Test
    public void assertGetExactlyNumberForLong() {
        assertThat(NumberUtil.getExactlyNumber("100000000000", 10), is((Number) 100000000000L));
        assertThat(NumberUtil.getExactlyNumber("100000000000", 16), is((Number) 17592186044416L));
        assertThat(NumberUtil.getExactlyNumber(String.valueOf(Long.MIN_VALUE), 10), is((Number) Long.MIN_VALUE));
        assertThat(NumberUtil.getExactlyNumber(String.valueOf(Long.MAX_VALUE), 10), is((Number) Long.MAX_VALUE));
    }
    
    @Test
    public void assertGetExactlyNumberForBigInteger() {
        assertThat(NumberUtil.getExactlyNumber("10000000000000000000", 10), is((Number) new BigInteger("10000000000000000000")));
        assertThat(NumberUtil.getExactlyNumber("10000000000000000000", 16), is((Number) new BigInteger("75557863725914323419136")));
        assertThat(NumberUtil.getExactlyNumber(String.valueOf(Long.MIN_VALUE + 1), 10), is((Number) (Long.MIN_VALUE + 1)));
        assertThat(NumberUtil.getExactlyNumber(String.valueOf(Long.MAX_VALUE - 1), 10), is((Number) (Long.MAX_VALUE - 1)));
    }
}

/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.shardingjdbc.jdbc.core.resultset;

import io.shardingsphere.core.exception.ShardingException;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ResultSetUtilTest {
    @Test
    public void assertConvertValue() {
        Object object = new Object();
        assertThat((String) ResultSetUtil.convertValue(object, String.class), is(object.toString()));
        assertThat((String) ResultSetUtil.convertValue("1", int.class), is("1"));
    }
    
    @Test
    public void assertConvertNumberValueSuccess() {
        assertThat((String) ResultSetUtil.convertValue("1", String.class), is("1"));
        assertThat((byte) ResultSetUtil.convertValue((byte) 1, byte.class), is((byte) 1));
        assertThat((short) ResultSetUtil.convertValue((short) 1, short.class), is((short) 1));
        assertThat((int) ResultSetUtil.convertValue(new BigDecimal("1"), int.class), is(1));
        assertThat((long) ResultSetUtil.convertValue(new BigDecimal("1"), long.class), is(1L));
        assertThat((double) ResultSetUtil.convertValue(new BigDecimal("1"), double.class), is(1d));
        assertThat((float) ResultSetUtil.convertValue(new BigDecimal("1"), float.class), is(1f));
        assertThat((BigDecimal) ResultSetUtil.convertValue(new BigDecimal("1"), BigDecimal.class), is(new BigDecimal("1")));
        assertThat((BigDecimal) ResultSetUtil.convertValue((short) 1, BigDecimal.class), is(new BigDecimal("1")));
    
        assertThat((Date) ResultSetUtil.convertValue(new Date(0L), Date.class), is(new Date(0L)));
        assertThat(ResultSetUtil.convertValue((short) 1, Object.class), is((Object) Short.valueOf("1")));
        assertThat(ResultSetUtil.convertValue((short) 1, String.class), is((Object) "1"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertConvertNumberValueError() {
        ResultSetUtil.convertValue(1, Date.class);
    }
    
    @Test
    public void assertConvertNullValue() {
        assertThat(ResultSetUtil.convertValue(null, boolean.class), is((Object) false));
        assertThat(ResultSetUtil.convertValue(null, byte.class), is((Object) (byte) 0));
        assertThat(ResultSetUtil.convertValue(null, short.class), is((Object) (short) 0));
        assertThat(ResultSetUtil.convertValue(null, int.class), is((Object) 0));
        assertThat(ResultSetUtil.convertValue(null, long.class), is((Object) 0L));
        assertThat(ResultSetUtil.convertValue(null, double.class), is((Object) 0D));
        assertThat(ResultSetUtil.convertValue(null, float.class), is((Object) 0F));
        assertThat(ResultSetUtil.convertValue(null, String.class), is((Object) null));
        assertThat(ResultSetUtil.convertValue(null, Object.class), is((Object) null));
        assertThat(ResultSetUtil.convertValue(null, BigDecimal.class), is((Object) null));
        assertThat(ResultSetUtil.convertValue(null, Date.class), is((Object) null));
    }
    
    @Test
    public void assertConvertDateValueSuccess() {
        Date now = new Date();
        assertThat((Date) ResultSetUtil.convertValue(now, Date.class), is(now));
        assertThat((java.sql.Date) ResultSetUtil.convertValue(now, java.sql.Date.class), is(now));
        assertThat((Time) ResultSetUtil.convertValue(now, Time.class), is(now));
        assertThat((Timestamp) ResultSetUtil.convertValue(now, Timestamp.class), is(new Timestamp(now.getTime())));
    }
    
    @Test(expected = ShardingException.class)
    public void assertConvertDateValueError() {
        ResultSetUtil.convertValue(new Date(), int.class);
    }
}

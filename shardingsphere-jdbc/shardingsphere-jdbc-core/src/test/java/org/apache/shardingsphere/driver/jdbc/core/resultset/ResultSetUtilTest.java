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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ResultSetUtilTest {
    @Test
    public void assertConvertValue() {
        Object object = new Object();
        assertThat(ResultSetUtil.convertValue(object, String.class), is(object.toString()));
        assertThat(ResultSetUtil.convertValue("1", int.class), is("1"));
    }
    
    @Test
    public void assertConvertLocalDateTime() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LocalDateTime dateTime = (LocalDateTime) ResultSetUtil.convertValue(timestamp, LocalDateTime.class);
        assertNotNull(dateTime);
        assertThat(dateTime.toString(), is(timestamp.toLocalDateTime().toString()));
    }
    
    @Test
    public void assertConvertNumberValueSuccess() {
        assertThat(ResultSetUtil.convertValue("1", String.class), is("1"));
        assertThat(ResultSetUtil.convertValue(1, boolean.class), is(true));
        assertThat(ResultSetUtil.convertValue((byte) 1, byte.class), is((byte) 1));
        assertThat(ResultSetUtil.convertValue((short) 1, short.class), is((short) 1));
        assertThat(ResultSetUtil.convertValue(new BigDecimal("1"), int.class), is(1));
        assertThat(ResultSetUtil.convertValue(new BigDecimal("1"), long.class), is(1L));
        assertThat(ResultSetUtil.convertValue(new BigDecimal("1"), double.class), is(1.0d));
        assertThat(ResultSetUtil.convertValue(new BigDecimal("1"), float.class), is(1.0f));
        assertThat(ResultSetUtil.convertValue(new BigDecimal("1"), BigDecimal.class), is(new BigDecimal("1")));
        assertThat(ResultSetUtil.convertValue((short) 1, BigDecimal.class), is(new BigDecimal("1")));
        assertThat(ResultSetUtil.convertValue(new Date(0L), Date.class), is(new Date(0L)));
        assertThat(ResultSetUtil.convertValue((short) 1, Object.class), is(Short.valueOf("1")));
        assertThat(ResultSetUtil.convertValue((short) 1, String.class), is("1"));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertConvertNumberValueError() {
        ResultSetUtil.convertValue(1, Date.class);
    }
    
    @Test
    public void assertConvertNullValue() {
        assertThat(ResultSetUtil.convertValue(null, boolean.class), is(false));
        assertThat(ResultSetUtil.convertValue(null, byte.class), is((byte) 0));
        assertThat(ResultSetUtil.convertValue(null, short.class), is((short) 0));
        assertThat(ResultSetUtil.convertValue(null, int.class), is(0));
        assertThat(ResultSetUtil.convertValue(null, long.class), is(0L));
        assertThat(ResultSetUtil.convertValue(null, double.class), is(0.0D));
        assertThat(ResultSetUtil.convertValue(null, float.class), is(0.0F));
        assertThat(ResultSetUtil.convertValue(null, String.class), is((Object) null));
        assertThat(ResultSetUtil.convertValue(null, Object.class), is((Object) null));
        assertThat(ResultSetUtil.convertValue(null, BigDecimal.class), is((Object) null));
        assertThat(ResultSetUtil.convertValue(null, Date.class), is((Object) null));
    }
    
    @Test
    public void assertConvertDateValueSuccess() {
        Date now = new Date();
        assertThat(ResultSetUtil.convertValue(now, Date.class), is(now));
        assertThat(ResultSetUtil.convertValue(now, java.sql.Date.class), is(now));
        assertThat(ResultSetUtil.convertValue(now, Time.class), is(now));
        assertThat(ResultSetUtil.convertValue(now, Timestamp.class), is(new Timestamp(now.getTime())));
        assertThat(ResultSetUtil.convertValue(now, String.class), is(now.toString()));
    }
    
    @Test
    public void assertConvertByteArrayValueSuccess() {
        byte[] bytesValue = {};
        assertThat(ResultSetUtil.convertValue(bytesValue, byte.class), is(bytesValue));
        assertThat(ResultSetUtil.convertValue(new byte[]{1}, byte.class), is((byte) 1));
        assertThat(ResultSetUtil.convertValue(Shorts.toByteArray((short) 1), short.class), is((short) 1));
        assertThat(ResultSetUtil.convertValue(Ints.toByteArray(1), int.class), is(1));
        assertThat(ResultSetUtil.convertValue(Longs.toByteArray(1L), long.class), is(1L));
        assertThat(ResultSetUtil.convertValue(Longs.toByteArray(1L), double.class), is(1.0d));
        assertThat(ResultSetUtil.convertValue(Longs.toByteArray(1L), float.class), is(1.0f));
        assertThat(ResultSetUtil.convertValue(Longs.toByteArray(1L), BigDecimal.class), is(new BigDecimal("1")));
    }
    
    @SneakyThrows(MalformedURLException.class)
    @Test
    public void assertConvertURLValue() {
        String urlString = "http://apache.org";
        URL url = (URL) ResultSetUtil.convertValue(urlString, URL.class);
        assertNotNull(url);
        assertThat(url, is(new URL(urlString)));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertConvertURLValueError() {
        String urlString = "no-exist:apache.org";
        ResultSetUtil.convertValue(urlString, URL.class);
    }
    
    @Test
    public void assertConvertBigDecimalValue() {
        BigDecimal bigDecimal = (BigDecimal) ResultSetUtil.convertBigDecimalValue("12", false, 0);
        assertThat(bigDecimal, is(BigDecimal.valueOf(12)));
    }
    
    @Test
    public void assertConvertBigDecimalValueNull() {
        BigDecimal bigDecimal = (BigDecimal) ResultSetUtil.convertBigDecimalValue(null, false, 0);
        assertNull(bigDecimal);
    }
    
    @Test
    public void assertConvertBigDecimalValueWithScale() {
        BigDecimal bigDecimal = (BigDecimal) ResultSetUtil.convertBigDecimalValue("12.243", true, 2);
        assertThat(bigDecimal, is(BigDecimal.valueOf(12.24)));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertConvertBigDecimalValueError() {
        ResultSetUtil.convertBigDecimalValue(new Date(), true, 2);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertConvertDateValueError() {
        ResultSetUtil.convertValue(new Date(), int.class);
    }
}

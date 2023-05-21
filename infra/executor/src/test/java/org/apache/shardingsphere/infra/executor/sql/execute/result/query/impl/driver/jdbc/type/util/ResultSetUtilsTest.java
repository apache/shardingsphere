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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.exception.UnsupportedDataTypeConversionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultSetUtilsTest {
    
    @Test
    void assertConvertValue() throws SQLException {
        Object object = new Object();
        assertThat(ResultSetUtils.convertValue(object, String.class), is(object.toString()));
    }
    
    @Test
    void assertConvertLocalDateTimeValue() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.of(2021, Month.DECEMBER, 23, 19, 30);
        assertThat(ResultSetUtils.convertValue(localDateTime, Timestamp.class), is(Timestamp.valueOf(localDateTime)));
    }
    
    @Test
    void assertConvertTimestampValue() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.of(2021, Month.DECEMBER, 23, 19, 30);
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        assertThat(ResultSetUtils.convertValue(timestamp, LocalDateTime.class), is(localDateTime));
        assertThat(ResultSetUtils.convertValue(timestamp, LocalDate.class), is(LocalDate.of(2021, Month.DECEMBER, 23)));
        assertThat(ResultSetUtils.convertValue(timestamp, LocalTime.class), is(LocalTime.of(19, 30)));
    }
    
    @Test
    void assertConvertBooleanValue() throws SQLException {
        assertFalse((boolean) ResultSetUtils.convertValue("-2", boolean.class));
        assertTrue((boolean) ResultSetUtils.convertValue("1", boolean.class));
    }
    
    @Test
    void assertConvertNumberValueSuccess() throws SQLException {
        assertThat(ResultSetUtils.convertValue("1", String.class), is("1"));
        assertTrue((boolean) ResultSetUtils.convertValue(1, boolean.class));
        assertThat(ResultSetUtils.convertValue((byte) 1, byte.class), is((byte) 1));
        assertThat(ResultSetUtils.convertValue((short) 1, short.class), is((short) 1));
        assertThat(ResultSetUtils.convertValue(new BigDecimal("1"), int.class), is(1));
        assertThat(ResultSetUtils.convertValue(new BigDecimal("1"), long.class), is(1L));
        assertThat(ResultSetUtils.convertValue(new BigDecimal("1"), double.class), is(1.0D));
        assertThat(ResultSetUtils.convertValue(new BigDecimal("1"), float.class), is(1.0F));
        assertThat(ResultSetUtils.convertValue(new BigDecimal("1"), BigDecimal.class), is(new BigDecimal("1")));
        assertThat(ResultSetUtils.convertValue((short) 1, BigDecimal.class), is(new BigDecimal("1")));
        assertThat(ResultSetUtils.convertValue(new Date(0L), Date.class), is(new Date(0L)));
        assertThat(ResultSetUtils.convertValue((short) 1, Object.class), is(Short.valueOf("1")));
        assertThat(ResultSetUtils.convertValue((short) 1, String.class), is("1"));
        assertThat(ResultSetUtils.convertValue(1, Byte.class), is(Byte.valueOf("1")));
        assertThat(ResultSetUtils.convertValue(1, Short.class), is(Short.valueOf("1")));
        assertThat(ResultSetUtils.convertValue(1, Long.class), is(Long.valueOf("1")));
        assertThat(ResultSetUtils.convertValue(1, Double.class), is(Double.valueOf("1")));
        assertThat(ResultSetUtils.convertValue(1, Float.class), is(Float.valueOf("1")));
    }
    
    @Test
    void assertConvertNumberValueError() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> ResultSetUtils.convertValue(1, Date.class));
    }
    
    @Test
    void assertConvertNullValue() throws SQLException {
        assertFalse((boolean) ResultSetUtils.convertValue(null, boolean.class));
        assertThat(ResultSetUtils.convertValue(null, byte.class), is((byte) 0));
        assertThat(ResultSetUtils.convertValue(null, short.class), is((short) 0));
        assertThat(ResultSetUtils.convertValue(null, int.class), is(0));
        assertThat(ResultSetUtils.convertValue(null, long.class), is(0L));
        assertThat(ResultSetUtils.convertValue(null, double.class), is(0.0D));
        assertThat(ResultSetUtils.convertValue(null, float.class), is(0.0F));
        assertThat(ResultSetUtils.convertValue(null, String.class), is((Object) null));
        assertThat(ResultSetUtils.convertValue(null, Object.class), is((Object) null));
        assertThat(ResultSetUtils.convertValue(null, BigDecimal.class), is((Object) null));
        assertThat(ResultSetUtils.convertValue(null, Date.class), is((Object) null));
    }
    
    @Test
    void assertConvertNullType() {
        assertThrows(SQLException.class, () -> ResultSetUtils.convertValue(null, null));
    }
    
    @Test
    void assertConvertDateValueSuccess() throws SQLException {
        Date now = new Date();
        assertThat(ResultSetUtils.convertValue(now, Date.class), is(now));
        assertThat(ResultSetUtils.convertValue(now, java.sql.Date.class), is(now));
        assertThat(ResultSetUtils.convertValue(now, Time.class), is(now));
        assertThat(ResultSetUtils.convertValue(now, Timestamp.class), is(new Timestamp(now.getTime())));
        assertThat(ResultSetUtils.convertValue(now, String.class), is(now.toString()));
    }
    
    @Test
    void assertConvertByteArrayValueSuccess() throws SQLException {
        byte[] bytesValue = {};
        assertThat(ResultSetUtils.convertValue(bytesValue, byte.class), is(bytesValue));
        assertThat(ResultSetUtils.convertValue(new byte[]{1}, byte.class), is((byte) 1));
        assertThat(ResultSetUtils.convertValue(Shorts.toByteArray((short) 1), short.class), is((short) 1));
        assertThat(ResultSetUtils.convertValue(Ints.toByteArray(1), int.class), is(1));
        assertThat(ResultSetUtils.convertValue(Longs.toByteArray(1L), long.class), is(1L));
        assertThat(ResultSetUtils.convertValue(Longs.toByteArray(1L), double.class), is(1.0D));
        assertThat(ResultSetUtils.convertValue(Longs.toByteArray(1L), float.class), is(1.0F));
        assertThat(ResultSetUtils.convertValue(Longs.toByteArray(1L), BigDecimal.class), is(new BigDecimal("1")));
    }
    
    @SneakyThrows(MalformedURLException.class)
    @Test
    void assertConvertURLValue() throws SQLException {
        String urlString = "https://shardingsphere.apache.org/";
        URL url = (URL) ResultSetUtils.convertValue(urlString, URL.class);
        assertThat(url, is(new URL(urlString)));
    }
    
    @Test
    void assertConvertURLValueError() {
        String urlString = "no-exist:shardingsphere.apache.org/";
        assertThrows(UnsupportedDataTypeConversionException.class, () -> ResultSetUtils.convertValue(urlString, URL.class));
    }
    
    @Test
    void assertConvertBigDecimalValue() {
        BigDecimal bigDecimal = (BigDecimal) ResultSetUtils.convertBigDecimalValue("12", false, 0);
        assertThat(bigDecimal, is(BigDecimal.valueOf(12)));
    }
    
    @Test
    void assertConvertBigDecimalValueNull() {
        BigDecimal bigDecimal = (BigDecimal) ResultSetUtils.convertBigDecimalValue(null, false, 0);
        assertNull(bigDecimal);
    }
    
    @Test
    void assertConvertBigDecimalValueWithScale() {
        BigDecimal bigDecimal = (BigDecimal) ResultSetUtils.convertBigDecimalValue("12.243", true, 2);
        assertThat(bigDecimal, is(BigDecimal.valueOf(12.24)));
    }
    
    @Test
    void assertConvertBigDecimalValueError() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> ResultSetUtils.convertBigDecimalValue(new Date(), true, 2));
    }
    
    @Test
    void assertConvertDateValueError() {
        assertThrows(UnsupportedDataTypeConversionException.class, () -> ResultSetUtils.convertValue(new Date(), int.class));
    }
}

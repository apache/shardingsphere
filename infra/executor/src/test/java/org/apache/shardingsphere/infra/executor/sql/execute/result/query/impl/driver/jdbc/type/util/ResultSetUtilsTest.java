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
import org.apache.shardingsphere.infra.exception.kernel.data.UnsupportedDataTypeConversionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultSetUtilsTest {
    
    @Test
    void assertConvertNullType() {
        assertThrows(SQLException.class, () -> ResultSetUtils.convertValue(null, null));
    }
    
    @Test
    void assertConvertObjectValue() throws SQLException {
        Object object = new Object();
        assertThat(ResultSetUtils.convertValue(object, String.class), is(object.toString()));
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
    void assertConvertLocalDateTimeValue() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.of(2021, Month.DECEMBER, 23, 19, 30);
        assertThat(ResultSetUtils.convertValue(localDateTime, Object.class), is(localDateTime));
        assertThat(ResultSetUtils.convertValue(localDateTime, Timestamp.class), is(Timestamp.valueOf(localDateTime)));
        assertThat(ResultSetUtils.convertValue(localDateTime, String.class), is("2021-12-23T19:30"));
    }
    
    @Test
    void assertConvertTimestampValue() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.of(2021, Month.DECEMBER, 23, 19, 30);
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        assertThat(ResultSetUtils.convertValue(timestamp, LocalDateTime.class), is(localDateTime));
        assertThat(ResultSetUtils.convertValue(timestamp, LocalDate.class), is(LocalDate.of(2021, Month.DECEMBER, 23)));
        assertThat(ResultSetUtils.convertValue(timestamp, LocalTime.class), is(LocalTime.of(19, 30)));
        assertThat(ResultSetUtils.convertValue(timestamp, OffsetDateTime.class), isA(OffsetDateTime.class));
        assertThat(ResultSetUtils.convertValue(timestamp, String.class), isA(String.class));
        assertThat(ResultSetUtils.convertValue(timestamp, Object.class), is(timestamp));
    }
    
    @Test
    void assertConvertURLValue() throws SQLException, MalformedURLException {
        assertThat(ResultSetUtils.convertValue("https://shardingsphere.apache.org/", URL.class), is(new URL("https://shardingsphere.apache.org/")));
        assertThrows(UnsupportedDataTypeConversionException.class, () -> ResultSetUtils.convertValue("no-exist:shardingsphere.apache.org/", URL.class));
    }
    
    @Test
    void assertConvertNumberValue() throws SQLException {
        assertThat(ResultSetUtils.convertValue("1", String.class), is("1"));
        assertTrue((boolean) ResultSetUtils.convertValue(-1, boolean.class));
        assertTrue((boolean) ResultSetUtils.convertValue(1, boolean.class));
        assertFalse((boolean) ResultSetUtils.convertValue(-2, boolean.class));
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
        assertThrows(UnsupportedDataTypeConversionException.class, () -> ResultSetUtils.convertValue(1, Date.class));
    }
    
    @Test
    void assertConvertDateValue() throws SQLException {
        Date now = new Date();
        assertThat(ResultSetUtils.convertValue(now, Date.class), is(now));
        assertThat(ResultSetUtils.convertValue(now, java.sql.Date.class), is(now));
        assertThat(ResultSetUtils.convertValue(now, Time.class), is(now));
        assertThat(ResultSetUtils.convertValue(now, Timestamp.class), is(new Timestamp(now.getTime())));
        assertThat(ResultSetUtils.convertValue(now, String.class), is(now.toString()));
        assertThrows(UnsupportedDataTypeConversionException.class, () -> ResultSetUtils.convertValue(new Date(), int.class));
    }
    
    @Test
    void assertConvertByteArrayValueSuccess() throws SQLException {
        byte[] bytesValue = {};
        assertThat(ResultSetUtils.convertValue(bytesValue, byte.class), is(bytesValue));
        assertThat(ResultSetUtils.convertValue(new byte[]{(byte) 1}, byte.class), is((byte) 1));
        assertThat(ResultSetUtils.convertValue(Shorts.toByteArray((short) 1), short.class), is((short) 1));
        assertThat(ResultSetUtils.convertValue(Ints.toByteArray(1), int.class), is(1));
        assertThat(ResultSetUtils.convertValue(Longs.toByteArray(1L), long.class), is(1L));
        assertThat(ResultSetUtils.convertValue(Longs.toByteArray(1L), double.class), is(1.0D));
        assertThat(ResultSetUtils.convertValue(Longs.toByteArray(1L), float.class), is(1.0F));
        assertThat(ResultSetUtils.convertValue(Longs.toByteArray(1L), BigDecimal.class), is(new BigDecimal("1")));
    }
    
    @Test
    void assertConvertBooleanValue() throws SQLException {
        assertTrue((boolean) ResultSetUtils.convertValue(true, boolean.class));
        assertFalse((boolean) ResultSetUtils.convertValue("", boolean.class));
        assertFalse((boolean) ResultSetUtils.convertValue("-2", boolean.class));
        assertTrue((boolean) ResultSetUtils.convertValue("1", boolean.class));
        assertTrue((boolean) ResultSetUtils.convertValue("t", boolean.class));
        assertTrue((boolean) ResultSetUtils.convertValue("y", boolean.class));
    }
    
    @Test
    void assertConvertValueWithMismatchedValueAndConvertType() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> ResultSetUtils.convertValue(new ResultSetUtilsTest(), Date.class));
    }
    
    @Test
    void assertConvertBigDecimalValue() {
        assertThat(ResultSetUtils.convertBigDecimalValue(12, false, 0), is(BigDecimal.valueOf(12L)));
        assertThat(ResultSetUtils.convertBigDecimalValue("12", false, 0), is(BigDecimal.valueOf(12L)));
        assertThat(ResultSetUtils.convertBigDecimalValue(BigDecimal.valueOf(12), false, 0), is(BigDecimal.valueOf(12)));
        assertNull(ResultSetUtils.convertBigDecimalValue(null, false, 0));
        assertThat(ResultSetUtils.convertBigDecimalValue("12.243", true, 2), is(BigDecimal.valueOf(12.24)));
        assertThrows(UnsupportedDataTypeConversionException.class, () -> ResultSetUtils.convertBigDecimalValue(new Date(), true, 2));
    }
    
    @Test
    void assertConvertDateValueToLocalDate() throws SQLException {
        Date date = new Date(1609459200000L);
        LocalDate result = (LocalDate) ResultSetUtils.convertValue(date, LocalDate.class);
        assertThat(result, isA(LocalDate.class));
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        assertThat(result, is(sqlDate.toLocalDate()));
    }
    
    @Test
    void assertConvertDateValueToLocalDateWithDifferentTimestamps() throws SQLException {
        Date epochDate = new Date(0L);
        LocalDate epochResult = (LocalDate) ResultSetUtils.convertValue(epochDate, LocalDate.class);
        assertThat(epochResult, is(LocalDate.of(1970, 1, 1)));
        Date christmasDate = new Date(1703462400000L);
        LocalDate christmasResult = (LocalDate) ResultSetUtils.convertValue(christmasDate, LocalDate.class);
        assertThat(christmasResult, is(new java.sql.Date(christmasDate.getTime()).toLocalDate()));
    }
    
    @Test
    void assertConvertDateValueToLocalDateWithCurrentDate() throws SQLException {
        Date now = new Date();
        LocalDate result = (LocalDate) ResultSetUtils.convertValue(now, LocalDate.class);
        java.sql.Date sqlDate = new java.sql.Date(now.getTime());
        assertThat(result, is(sqlDate.toLocalDate()));
    }
}

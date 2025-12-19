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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import com.google.protobuf.BoolValue;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Empty;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import org.junit.jupiter.api.Test;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UseOfObsoleteDateTimeApi")
class ColumnValueConvertUtilsTest {
    
    @Test
    void assertConvertNullToEmpty() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(null);
        assertThat(actual, isA(Empty.class));
    }
    
    @Test
    void assertConvertIntegerToInt32Value() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(1);
        assertThat(((Int32Value) actual).getValue(), is(1));
    }
    
    @Test
    void assertConvertShortToInt32Value() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage((short) 2);
        assertThat(((Int32Value) actual).getValue(), is(2));
    }
    
    @Test
    void assertConvertByteToInt32Value() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage((byte) 3);
        assertThat(((Int32Value) actual).getValue(), is(3));
    }
    
    @Test
    void assertConvertLongToInt64Value() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(4L);
        assertThat(((Int64Value) actual).getValue(), is(4L));
    }
    
    @Test
    void assertConvertBigIntegerToStringValue() {
        BigInteger expectedBigInteger = new BigInteger("1234");
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(expectedBigInteger);
        assertThat(new BigInteger(((StringValue) actual).getValue()), is(expectedBigInteger));
    }
    
    @Test
    void assertConvertFloatToFloatValue() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(1.5F);
        assertThat(((FloatValue) actual).getValue(), is(1.5F));
    }
    
    @Test
    void assertConvertDoubleToDoubleValue() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(2.5D);
        assertThat(((DoubleValue) actual).getValue(), is(2.5D));
    }
    
    @Test
    void assertConvertBigDecimalToStringValue() {
        BigDecimal expectedBigDecimal = new BigDecimal("1000.01");
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(expectedBigDecimal);
        assertThat(((StringValue) actual).getValue(), is(expectedBigDecimal.toString()));
    }
    
    @Test
    void assertConvertStringToStringValue() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage("abcd");
        assertThat(((StringValue) actual).getValue(), is("abcd"));
    }
    
    @Test
    void assertConvertBooleanToBoolValue() {
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(true);
        assertTrue(((BoolValue) actual).getValue());
    }
    
    @Test
    void assertConvertBytesToBytesValue() {
        byte[] expectedBytes = "123456".getBytes(StandardCharsets.UTF_8);
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(expectedBytes);
        assertThat(((BytesValue) actual).getValue().toByteArray(), is(expectedBytes));
    }
    
    @Test
    void assertConvertTimeToInt64Value() {
        Time time = Time.valueOf(LocalTime.of(1, 2, 3));
        int nanos = new Timestamp(time.getTime()).getNanos();
        Int64Value actual = (Int64Value) ColumnValueConvertUtils.convertToProtobufMessage(time);
        assertThat(LocalTime.ofNanoOfDay(actual.getValue()), is(LocalTime.of(1, 2, 3, nanos)));
    }
    
    @Test
    void assertConvertSqlDateToInt64Value() {
        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.of(2020, 1, 2));
        Int64Value actual = (Int64Value) ColumnValueConvertUtils.convertToProtobufMessage(sqlDate);
        assertThat(actual.getValue(), is(sqlDate.toLocalDate().toEpochDay()));
    }
    
    @Test
    void assertConvertUtilDateToTimestampMessage() {
        Date utilDate = new Date(1_600_000_000_123L);
        com.google.protobuf.Timestamp actual = (com.google.protobuf.Timestamp) ColumnValueConvertUtils.convertToProtobufMessage(utilDate);
        assertThat(actual.getSeconds(), is(utilDate.getTime() / 1000L));
        assertThat(actual.getNanos(), is((int) ((utilDate.getTime() % 1000L) * 1_000_000L)));
    }
    
    @Test
    void assertConvertLocalDateTimeToTimestampMessage() {
        LocalDateTime localDateTime = LocalDateTime.of(2021, 5, 6, 7, 8, 9, 123000000);
        com.google.protobuf.Timestamp actual = (com.google.protobuf.Timestamp) ColumnValueConvertUtils.convertToProtobufMessage(localDateTime);
        Timestamp expectedTimestamp = Timestamp.valueOf(localDateTime);
        assertThat(actual.getSeconds(), is(expectedTimestamp.getTime() / 1000L));
        assertThat(actual.getNanos(), is(expectedTimestamp.getNanos()));
    }
    
    @Test
    void assertConvertLocalDateToInt64Value() {
        LocalDate localDate = LocalDate.of(2022, 3, 4);
        Int64Value actual = (Int64Value) ColumnValueConvertUtils.convertToProtobufMessage(localDate);
        assertThat(actual.getValue(), is(localDate.toEpochDay()));
    }
    
    @Test
    void assertConvertLocalTimeToInt64Value() {
        LocalTime localTime = LocalTime.of(5, 6, 7, 8);
        Int64Value actual = (Int64Value) ColumnValueConvertUtils.convertToProtobufMessage(localTime);
        assertThat(actual.getValue(), is(localTime.toNanoOfDay()));
    }
    
    @Test
    void assertConvertOffsetDateTimeToTimestampMessage() {
        OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5, 600000000), ZoneOffset.ofHours(1));
        com.google.protobuf.Timestamp actual = (com.google.protobuf.Timestamp) ColumnValueConvertUtils.convertToProtobufMessage(offsetDateTime);
        Timestamp expectedTimestamp = Timestamp.valueOf(offsetDateTime.toLocalDateTime());
        assertThat(actual.getSeconds(), is(expectedTimestamp.getTime() / 1000L));
        assertThat(actual.getNanos(), is(expectedTimestamp.getNanos()));
    }
    
    @Test
    void assertConvertOffsetTimeToInt64Value() {
        OffsetTime offsetTime = OffsetTime.of(1, 2, 3, 4, ZoneOffset.ofHours(-2));
        Int64Value actual = (Int64Value) ColumnValueConvertUtils.convertToProtobufMessage(offsetTime);
        assertThat(actual.getValue(), is(offsetTime.toLocalTime().toNanoOfDay()));
    }
    
    @Test
    void assertConvertZonedDateTimeToTimestampMessage() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.of(2022, 7, 8, 9, 10, 11, 120000000), ZoneId.of("UTC"));
        com.google.protobuf.Timestamp actual = (com.google.protobuf.Timestamp) ColumnValueConvertUtils.convertToProtobufMessage(zonedDateTime);
        Timestamp expectedTimestamp = Timestamp.valueOf(zonedDateTime.toLocalDateTime());
        assertThat(actual.getSeconds(), is(expectedTimestamp.getTime() / 1000L));
        assertThat(actual.getNanos(), is(expectedTimestamp.getNanos()));
    }
    
    @Test
    void assertConvertInstantToTimestampMessage() {
        Instant instant = Instant.ofEpochSecond(1_700_000_000L, 123_000_000L);
        com.google.protobuf.Timestamp actual = (com.google.protobuf.Timestamp) ColumnValueConvertUtils.convertToProtobufMessage(instant);
        assertThat(actual.getSeconds(), is(instant.getEpochSecond()));
        assertThat(actual.getNanos(), is(instant.getNano()));
    }
    
    @Test
    void assertConvertClobToStringValue() throws SQLException {
        SerialClob clob = new SerialClob("clob_value".toCharArray());
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(clob);
        assertThat(((StringValue) actual).getValue(), is("clob_value"));
    }
    
    @Test
    void assertConvertBlobToBytesValue() throws SQLException {
        byte[] expectedBlobBytes = "blob_value".getBytes(StandardCharsets.UTF_8);
        SerialBlob blob = new SerialBlob(expectedBlobBytes);
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(blob);
        assertThat(((BytesValue) actual).getValue().toByteArray(), is(expectedBlobBytes));
    }
    
    @Test
    void assertConvertCustomObjectToStringValue() {
        Object customObject = new Object() {
            
            @Override
            public String toString() {
                return "custom_object";
            }
        };
        Message actual = ColumnValueConvertUtils.convertToProtobufMessage(customObject);
        assertThat(((StringValue) actual).getValue(), is("custom_object"));
    }
}

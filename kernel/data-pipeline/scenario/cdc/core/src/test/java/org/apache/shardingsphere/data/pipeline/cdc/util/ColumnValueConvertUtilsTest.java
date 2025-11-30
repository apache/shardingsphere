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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnValueConvertUtilsTest {
    
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    void assertConvertToProtobufMessage() {
        Message actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(null);
        assertThat(actualMessage, isA(Empty.class));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(1);
        assertThat(actualMessage, isA(Int32Value.class));
        assertThat(((Int32Value) actualMessage).getValue(), is(1));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage((byte) 1);
        assertThat(actualMessage, isA(Int32Value.class));
        assertThat(((Int32Value) actualMessage).getValue(), is(1));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage((short) 1);
        assertThat(actualMessage, isA(Int32Value.class));
        assertThat(((Int32Value) actualMessage).getValue(), is(1));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(1L);
        assertThat(actualMessage, isA(Int64Value.class));
        assertThat(((Int64Value) actualMessage).getValue(), is(1L));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(new BigInteger("1234"));
        assertThat(actualMessage, isA(StringValue.class));
        assertThat(new BigInteger(((StringValue) actualMessage).getValue()), is(new BigInteger("1234")));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(1.0F);
        assertThat(actualMessage, isA(FloatValue.class));
        assertThat(((FloatValue) actualMessage).getValue(), is(1.0F));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(1.23);
        assertThat(actualMessage, isA(DoubleValue.class));
        assertThat(((DoubleValue) actualMessage).getValue(), is(1.23));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(new BigDecimal("100"));
        assertThat(actualMessage, isA(StringValue.class));
        assertThat(((StringValue) actualMessage).getValue(), is("100"));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage("abcd");
        assertThat(actualMessage, isA(StringValue.class));
        assertThat(((StringValue) actualMessage).getValue(), is("abcd"));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(true);
        assertThat(actualMessage, isA(BoolValue.class));
        assertTrue(((BoolValue) actualMessage).getValue());
        Timestamp now = new Timestamp(System.currentTimeMillis());
        long epochSecond = now.toInstant().getEpochSecond();
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(now.toLocalDateTime());
        assertThat(actualMessage, isA(com.google.protobuf.Timestamp.class));
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getSeconds(), is(epochSecond));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(now);
        assertThat(actualMessage, isA(com.google.protobuf.Timestamp.class));
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getSeconds(), is(epochSecond));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(new Date(now.getTime()));
        assertThat(actualMessage, isA(com.google.protobuf.Timestamp.class));
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getSeconds(), is(epochSecond));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(now.toInstant());
        assertThat(actualMessage, isA(com.google.protobuf.Timestamp.class));
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getNanos(), is(now.toInstant().getNano()));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(now.toLocalDateTime().toLocalTime());
        assertThat(actualMessage, isA(Int64Value.class));
        assertThat(((Int64Value) actualMessage).getValue(), is(now.toLocalDateTime().toLocalTime().toNanoOfDay()));
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage("123456".getBytes());
        assertThat(actualMessage, isA(BytesValue.class));
        assertThat(((BytesValue) actualMessage).getValue().toByteArray(), is("123456".getBytes()));
        OffsetTime offsetTime = OffsetTime.now();
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(offsetTime);
        assertThat(actualMessage, isA(Int64Value.class));
        assertThat(((Int64Value) actualMessage).getValue(), is(offsetTime.toLocalTime().toNanoOfDay()));
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        actualMessage = ColumnValueConvertUtils.convertToProtobufMessage(offsetDateTime);
        assertThat(actualMessage, isA(com.google.protobuf.Timestamp.class));
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getSeconds(), is(offsetDateTime.toEpochSecond()));
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getNanos(), is(offsetDateTime.getNano()));
    }
    
    @Test
    void assertTimeConvert() {
        Time time = new Time(-3600L * 1000L - 1234L);
        int nanos = new Timestamp(time.getTime()).getNanos();
        Int64Value actualMessage = (Int64Value) ColumnValueConvertUtils.convertToProtobufMessage(time);
        assertThat(LocalTime.ofNanoOfDay(actualMessage.getValue()), is(time.toLocalTime().withNano(nanos)));
    }
}

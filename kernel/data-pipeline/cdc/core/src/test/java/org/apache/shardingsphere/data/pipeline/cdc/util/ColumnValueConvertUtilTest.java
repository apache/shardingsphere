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
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.BigDecimalValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.BigIntegerValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.LocalTimeValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.NullValue;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ColumnValueConvertUtilTest {
    
    @Test
    public void assertConvertToProtobufMessage() {
        Message actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(null);
        assertTrue(actualMessage instanceof NullValue);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(1);
        assertTrue(actualMessage instanceof Int32Value);
        assertThat(((Int32Value) actualMessage).getValue(), is(1));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage((byte) 1);
        assertTrue(actualMessage instanceof Int32Value);
        assertThat(((Int32Value) actualMessage).getValue(), is(1));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage((short) 1);
        assertTrue(actualMessage instanceof Int32Value);
        assertThat(((Int32Value) actualMessage).getValue(), is(1));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(1L);
        assertTrue(actualMessage instanceof Int64Value);
        assertThat(((Int64Value) actualMessage).getValue(), is(1L));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(new BigInteger("1234"));
        assertTrue(actualMessage instanceof BigIntegerValue);
        assertThat(new BigInteger(((BigIntegerValue) actualMessage).getValue().toByteArray()), is(new BigInteger("1234")));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(1.0F);
        assertTrue(actualMessage instanceof FloatValue);
        assertThat(((FloatValue) actualMessage).getValue(), is(1.0F));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(1.23);
        assertTrue(actualMessage instanceof DoubleValue);
        assertThat(((DoubleValue) actualMessage).getValue(), is(1.23));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(new BigDecimal("100"));
        assertTrue(actualMessage instanceof BigDecimalValue);
        assertThat(((BigDecimalValue) actualMessage).getValue(), is("100"));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage("abcd");
        assertTrue(actualMessage instanceof StringValue);
        assertThat(((StringValue) actualMessage).getValue(), is("abcd"));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(true);
        assertTrue(actualMessage instanceof BoolValue);
        assertTrue(((BoolValue) actualMessage).getValue());
        Timestamp now = new Timestamp(System.currentTimeMillis());
        long epochSecond = now.toInstant().getEpochSecond();
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(now.toLocalDateTime());
        assertTrue(actualMessage instanceof com.google.protobuf.Timestamp);
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getSeconds(), is(epochSecond));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(now);
        assertTrue(actualMessage instanceof com.google.protobuf.Timestamp);
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getSeconds(), is(epochSecond));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(new Date(now.getTime()));
        assertTrue(actualMessage instanceof com.google.protobuf.Timestamp);
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getSeconds(), is(epochSecond));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(now.toInstant());
        assertTrue(actualMessage instanceof com.google.protobuf.Timestamp);
        assertThat(((com.google.protobuf.Timestamp) actualMessage).getNanos(), is(now.toInstant().getNano()));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(now.toLocalDateTime().toLocalTime());
        assertTrue(actualMessage instanceof LocalTimeValue);
        assertThat(((LocalTimeValue) actualMessage).getValue(), is(now.toLocalDateTime().toLocalTime().toString()));
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage("123456".getBytes());
        assertTrue(actualMessage instanceof BytesValue);
        assertThat(((BytesValue) actualMessage).getValue().toByteArray(), is("123456".getBytes()));
    }
}

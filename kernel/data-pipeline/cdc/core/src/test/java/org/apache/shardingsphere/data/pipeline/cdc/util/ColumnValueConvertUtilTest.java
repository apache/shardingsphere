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
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.NullValue;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public final class ColumnValueConvertUtilTest {
    
    @Test
    public void assertConvertToProtobufMessage() {
        Message actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(null);
        assertTrue(actualMessage instanceof NullValue);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(1);
        assertTrue(actualMessage instanceof Int32Value);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage((byte) 1);
        assertTrue(actualMessage instanceof Int32Value);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage((short) 1);
        assertTrue(actualMessage instanceof Int32Value);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(1L);
        assertTrue(actualMessage instanceof Int64Value);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(new BigInteger("1"));
        assertTrue(actualMessage instanceof BigIntegerValue);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(1.0F);
        assertTrue(actualMessage instanceof FloatValue);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(1.0);
        assertTrue(actualMessage instanceof DoubleValue);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(new BigDecimal("100"));
        assertTrue(actualMessage instanceof BigDecimalValue);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage("100");
        assertTrue(actualMessage instanceof StringValue);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(true);
        assertTrue(actualMessage instanceof BoolValue);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(LocalDateTime.now());
        assertTrue(actualMessage instanceof com.google.protobuf.Timestamp);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(new Timestamp(System.currentTimeMillis()));
        assertTrue(actualMessage instanceof com.google.protobuf.Timestamp);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(new Date());
        assertTrue(actualMessage instanceof com.google.protobuf.Timestamp);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(Instant.now());
        assertTrue(actualMessage instanceof com.google.protobuf.Timestamp);
        actualMessage = ColumnValueConvertUtil.convertToProtobufMessage(new byte[10]);
        assertTrue(actualMessage instanceof BytesValue);
    }
}

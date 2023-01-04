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

package org.apache.shardingsphere.data.pipeline.cdc.client.util;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.JsonFormat;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.BigDecimalValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.BigIntegerValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.BlobValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.ClobValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.LocalTimeValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.NullValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;

/**
 * Any value convert.
 */
public final class AnyValueConvert {
    
    /**
     * Convert any to object.
     *
     * @param any any
     * @return object
     * @throws InvalidProtocolBufferException invalid protocol buffer exception
     */
    public static Object convertToObject(final Any any) throws InvalidProtocolBufferException {
        if (null == any || any.is(NullValue.class)) {
            return null;
        }
        if (any.is(StringValue.class)) {
            return any.unpack(StringValue.class).getValue();
        }
        if (any.is(Int32Value.class)) {
            return any.unpack(Int32Value.class).getValue();
        }
        if (any.is(Int64Value.class)) {
            return any.unpack(Int64Value.class).getValue();
        }
        if (any.is(Int64Value.class)) {
            return any.unpack(Int64Value.class).getValue();
        }
        if (any.is(BigIntegerValue.class)) {
            return new BigInteger(any.unpack(BigIntegerValue.class).getValue().toByteArray());
        }
        if (any.is(FloatValue.class)) {
            return any.unpack(FloatValue.class).getValue();
        }
        if (any.is(DoubleValue.class)) {
            return any.unpack(DoubleValue.class).getValue();
        }
        if (any.is(BigDecimalValue.class)) {
            return new BigDecimal(any.unpack(BigDecimalValue.class).getValue());
        }
        if (any.is(BoolValue.class)) {
            return any.unpack(BoolValue.class).getValue();
        }
        if (any.is(BytesValue.class)) {
            return any.unpack(BytesValue.class).getValue().toByteArray();
        }
        if (any.is(com.google.protobuf.Timestamp.class)) {
            return converProtobufTimestamp(any.unpack(com.google.protobuf.Timestamp.class));
        }
        if (any.is(LocalTimeValue.class)) {
            return LocalTime.parse(any.unpack(LocalTimeValue.class).getValue());
        }
        if (any.is(ClobValue.class)) {
            return any.unpack(ClobValue.class).getValue();
        }
        if (any.is(BlobValue.class)) {
            return any.unpack(BlobValue.class).getValue().toByteArray();
        }
        return JsonFormat.printer().includingDefaultValueFields().print(any);
    }
    
    private static Timestamp converProtobufTimestamp(final com.google.protobuf.Timestamp timestamp) {
        return new Timestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()).toEpochMilli());
    }
}

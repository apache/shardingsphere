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
import com.google.protobuf.Empty;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.util.JsonFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

/**
 * Protobuf any value converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ProtobufAnyValueConverter {
    
    /**
     * Convert any to object.
     *
     * @param any any
     * @return object
     * @throws InvalidProtocolBufferException invalid protocol buffer exception
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public static Object convertToObject(final Any any) throws InvalidProtocolBufferException {
        if (null == any || any.is(Empty.class)) {
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
        if (any.is(UInt32Value.class)) {
            return any.unpack(UInt32Value.class).getValue();
        }
        if (any.is(UInt64Value.class)) {
            return any.unpack(UInt64Value.class).getValue();
        }
        if (any.is(FloatValue.class)) {
            return any.unpack(FloatValue.class).getValue();
        }
        if (any.is(DoubleValue.class)) {
            return any.unpack(DoubleValue.class).getValue();
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
        if (any.is(Struct.class)) {
            return JsonFormat.printer().print(any.unpack(Struct.class));
        }
        // TODO can't use JsonFormat, might change the original value without error prompt. there need to cover more types,
        log.error("not support unpack value={}", any);
        throw new UnsupportedOperationException(String.format("not support unpack the type %s", any.getTypeUrl()));
    }
    
    private static Timestamp converProtobufTimestamp(final com.google.protobuf.Timestamp timestamp) {
        Timestamp result = new Timestamp(timestamp.getSeconds() * 1000L);
        result.setNanos(timestamp.getNanos());
        return result;
    }
}

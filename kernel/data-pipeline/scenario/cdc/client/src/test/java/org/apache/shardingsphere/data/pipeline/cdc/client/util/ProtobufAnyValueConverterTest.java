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
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Empty;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtobufAnyValueConverterTest {
    
    @Test
    void assertConvertToObject() throws InvalidProtocolBufferException {
        Object actual = ProtobufAnyValueConverter.convertToObject(Any.pack(Int32Value.of(123)));
        assertThat(actual, is(123));
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(Int64Value.of(Long.MAX_VALUE)));
        assertThat(actual, is(Long.MAX_VALUE));
        OffsetDateTime now = OffsetDateTime.now();
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(com.google.protobuf.Timestamp.newBuilder().setSeconds(now.toEpochSecond()).setNanos(now.getNano()).build()));
        assertThat(actual, is(Timestamp.valueOf(now.toLocalDateTime())));
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(FloatValue.of(1.23F)));
        assertThat(actual, is(1.23F));
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(DoubleValue.of(4.56D)));
        assertThat(actual, is(4.56D));
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(StringValue.of("Hello")));
        assertThat(actual, is("Hello"));
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(BoolValue.of(true)));
        assertTrue((Boolean) actual);
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(BytesValue.of(ByteString.copyFrom(new byte[]{1, 2, 3}))));
        assertThat(actual, is(new byte[]{1, 2, 3}));
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(UInt64Value.of(101010L)));
        assertThat(actual, is(101010L));
        actual = ProtobufAnyValueConverter.convertToObject(Any.pack(Empty.getDefaultInstance()));
        assertNull(actual);
        actual = Struct.newBuilder().putFields("str", Value.newBuilder().setStringValue("ABC defg").build())
                .putFields("null", Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
                .putFields("number", Value.newBuilder().setNumberValue(123.45D).build())
                .putFields("list", Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setNumberValue(1)).build()).build()).build();
        Builder expected = Struct.newBuilder();
        JsonFormat.parser().merge((String) ProtobufAnyValueConverter.convertToObject(Any.pack((Struct) actual)), expected);
        assertThat(actual.toString(), is(expected.toString()));
    }
}

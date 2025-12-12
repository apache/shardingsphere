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
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Timestamp;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProtobufAnyValueConverterTest {
    
    @Test
    void assertConvertUnsupportedTypeThrowsUnsupportedOperationException() {
        Any any = Any.pack(Value.newBuilder().setStringValue("unsupported").build());
        UnsupportedOperationException actual = assertThrows(UnsupportedOperationException.class, () -> ProtobufAnyValueConverter.convertToObject(any));
        assertThat(actual.getMessage(), is(String.format("not support unpack the type %s", any.getTypeUrl())));
    }
    
    @ParameterizedTest
    @MethodSource("supportedValues")
    void assertConvertToObject(final Any any, final Object expected) throws InvalidProtocolBufferException {
        Object actual = ProtobufAnyValueConverter.convertToObject(any);
        if (expected instanceof Struct) {
            Builder actualStruct = Struct.newBuilder();
            JsonFormat.parser().merge((String) actual, actualStruct);
            assertThat(actualStruct.build().toString(), is(expected.toString()));
        } else {
            assertThat(actual, is(expected));
        }
    }
    
    private static Stream<Arguments> supportedValues() {
        long seconds = 1577830861L;
        int nanos = 123456789;
        Timestamp expectedTimestamp = new Timestamp(seconds * 1000);
        expectedTimestamp.setNanos(nanos);
        Struct struct = Struct.newBuilder().putFields("str", Value.newBuilder().setStringValue("ABC defg").build())
                .putFields("null", Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
                .putFields("number", Value.newBuilder().setNumberValue(123.45D).build())
                .putFields("list", Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setNumberValue(1)).build()).build()).build();
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(Any.pack(Int32Value.of(123)), 123),
                Arguments.of(Any.pack(Int64Value.of(Long.MAX_VALUE)), Long.MAX_VALUE),
                Arguments.of(Any.pack(UInt32Value.of(7)), 7),
                Arguments.of(Any.pack(com.google.protobuf.Timestamp.newBuilder().setSeconds(seconds).setNanos(nanos).build()), expectedTimestamp),
                Arguments.of(Any.pack(FloatValue.of(1.23F)), 1.23F),
                Arguments.of(Any.pack(DoubleValue.of(4.56D)), 4.56D),
                Arguments.of(Any.pack(StringValue.of("Hello")), "Hello"),
                Arguments.of(Any.pack(BoolValue.of(true)), true),
                Arguments.of(Any.pack(BytesValue.of(ByteString.copyFrom(new byte[]{1, 2, 3}))), new byte[]{1, 2, 3}),
                Arguments.of(Any.pack(UInt64Value.of(101010L)), 101010L),
                Arguments.of(Any.pack(Empty.getDefaultInstance()), null),
                Arguments.of(Any.pack(struct), struct)
        );
    }
}

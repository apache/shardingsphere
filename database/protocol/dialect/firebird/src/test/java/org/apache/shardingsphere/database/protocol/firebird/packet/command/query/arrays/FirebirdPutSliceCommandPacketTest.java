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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdPutSliceCommandPacketTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sliceCases")
    void assertReadPacket(final String name, final byte[] descriptor, final int nativeLength, final byte[] data) {
        ByteBuf input = createRequest(descriptor, nativeLength, data);
        try {
            input.writeInt(FirebirdCommandPacketType.COMMIT.getValue());
            new FirebirdPutSliceCommandPacket(new FirebirdPacketPayload(input, StandardCharsets.UTF_8));
            assertThat(input.readInt(), is(FirebirdCommandPacketType.COMMIT.getValue()));
        } finally {
            input.release();
        }
    }
    
    private static Stream<Arguments> sliceCases() {
        return Stream.of(
                Arguments.of("empty slice", new byte[]{1, 6, 1, BlrConstants.blr_long, 0, (byte) 255}, 0, new byte[0]),
                Arguments.of("integer value", new byte[]{1, 6, 1, BlrConstants.blr_long, 0, (byte) 255}, 4, new byte[]{0, 0, 0, 12}),
                Arguments.of("two SMALLINT values with relation and field IDs", new byte[]{1, 3, 1, 0, 6, 1, BlrConstants.blr_short, 0, 5, 2, 0, (byte) 255}, 4,
                        new byte[]{0, 0, 0, 1, -1, -1, -1, -2}),
                Arguments.of("two padded CHAR values with relation and field names", new byte[]{1, 2, 1, 'R', 4, 1, 'F', 6, 1, BlrConstants.blr_text, 3, 0, (byte) 255}, 6,
                        new byte[]{'a', 'b', 'c', 0, 'd', 'e', 'f', 0}),
                Arguments.of("four padded CHAR values", new byte[]{1, 6, 1, BlrConstants.blr_text, 3, 0, (byte) 255}, 12, new byte[16]),
                Arguments.of("CHAR with character set", new byte[]{1, 6, 1, BlrConstants.blr_text2, 4, 0, 3, 0, (byte) 255}, 3, new byte[4]),
                Arguments.of("VARCHAR values of different lengths", new byte[]{1, 6, 1, BlrConstants.blr_varying, 5, 0, (byte) 255}, 14,
                        new byte[]{0, 0, 0, 1, 'a', 0, 0, 0, 0, 0, 0, 5, 'b', 'c', 'd', 'e', 'f', 0, 0, 0}),
                Arguments.of("VARCHAR value with a character set", new byte[]{1, 6, 1, BlrConstants.blr_varying2, 4, 0, 5, 0, (byte) 255}, 7,
                        new byte[]{0, 0, 0, 1, 'a', 0, 0, 0}),
                Arguments.of("CSTRING without a character set", new byte[]{1, 6, 1, BlrConstants.blr_cstring, 5, 0, (byte) 255}, 5, new byte[]{0, 0, 0, 1, 'a', 0, 0, 0}),
                Arguments.of("CSTRING with a character set", new byte[]{1, 6, 1, BlrConstants.blr_cstring2, 4, 0, 5, 0, (byte) 255}, 5, new byte[]{0, 0, 0, 1, 'a', 0, 0, 0}),
                Arguments.of("short decimal float", new byte[]{1, 6, 1, BlrConstants.blr_dec64, (byte) 255}, 8, new byte[8]),
                Arguments.of("long decimal float", new byte[]{1, 6, 1, BlrConstants.blr_dec128, (byte) 255}, 16, new byte[16]),
                Arguments.of("64-bit integer", new byte[]{1, 6, 1, BlrConstants.blr_int64, 0, (byte) 255}, 8, new byte[8]),
                Arguments.of("128-bit integer", new byte[]{1, 6, 1, BlrConstants.blr_int128, 0, (byte) 255}, 16, new byte[16]),
                Arguments.of("quad", new byte[]{1, 6, 1, BlrConstants.blr_quad, 0, (byte) 255}, 8, new byte[8]),
                Arguments.of("float", new byte[]{1, 6, 1, BlrConstants.blr_float, (byte) 255}, 4, new byte[4]),
                Arguments.of("double", new byte[]{1, 6, 1, BlrConstants.blr_double, (byte) 255}, 8, new byte[8]),
                Arguments.of("legacy double", new byte[]{1, 6, 1, BlrConstants.blr_d_float, (byte) 255}, 8, new byte[8]),
                Arguments.of("date", new byte[]{1, 6, 1, BlrConstants.blr_sql_date, (byte) 255}, 4, new byte[4]),
                Arguments.of("time", new byte[]{1, 6, 1, BlrConstants.blr_sql_time, (byte) 255}, 4, new byte[4]),
                Arguments.of("timestamp", new byte[]{1, 6, 1, BlrConstants.blr_timestamp, (byte) 255}, 8, new byte[8]),
                Arguments.of("time with time zone", new byte[]{1, 6, 1, BlrConstants.blr_sql_time_tz, (byte) 255}, 8, new byte[8]),
                Arguments.of("timestamp with time zone", new byte[]{1, 6, 1, BlrConstants.blr_timestamp_tz, (byte) 255}, 12, new byte[12]),
                Arguments.of("extended time with time zone", new byte[]{1, 6, 1, BlrConstants.blr_ex_time_tz, (byte) 255}, 8, new byte[12]),
                Arguments.of("extended timestamp with time zone", new byte[]{1, 6, 1, BlrConstants.blr_ex_timestamp_tz, (byte) 255}, 12, new byte[16]),
                Arguments.of("boolean padded to four bytes", new byte[]{1, 6, 1, BlrConstants.blr_bool, (byte) 255}, 1, new byte[]{1, 0, 0, 0}),
                Arguments.of("two boolean values", new byte[]{1, 6, 1, BlrConstants.blr_bool, (byte) 255}, 2, new byte[]{1, 0, 0, 0, 0, 0, 0, 0}));
    }
    
    @Test
    void assertReadPacketWithIncompleteVariableData() {
        ByteBuf input = createRequest(new byte[]{1, 6, 1, BlrConstants.blr_varying, 5, 0, (byte) 255}, 7, new byte[]{0, 0, 0, 1});
        try {
            FirebirdPacketPayload payload = new FirebirdPacketPayload(input, StandardCharsets.UTF_8);
            assertThrows(IndexOutOfBoundsException.class, () -> new FirebirdPutSliceCommandPacket(payload));
        } finally {
            input.release();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSlices")
    void assertReadPacketWithInvalidInput(final String name, final byte[] descriptor, final int nativeLength) {
        ByteBuf input = createRequest(descriptor, nativeLength, new byte[0]);
        try {
            FirebirdPacketPayload payload = new FirebirdPacketPayload(input, StandardCharsets.UTF_8);
            assertThrows(IllegalArgumentException.class, () -> new FirebirdPutSliceCommandPacket(payload));
        } finally {
            input.release();
        }
    }
    
    private static Stream<Arguments> invalidSlices() {
        return Stream.of(
                Arguments.of("negative native length", new byte[]{1, 6, 1, BlrConstants.blr_long, 0, (byte) 255}, -1),
                Arguments.of("invalid SDL version", new byte[]{2, 6, 1, BlrConstants.blr_long, 0, (byte) 255}, 4),
                Arguments.of("descriptor without element", new byte[]{1}, 4),
                Arguments.of("missing descriptor", new byte[]{1, (byte) 255}, 4),
                Arguments.of("invalid element count", new byte[]{1, 6, 2, BlrConstants.blr_long, 0, (byte) 255}, 4),
                Arguments.of("unsupported element type", new byte[]{1, 6, 1, 127, (byte) 255}, 4),
                Arguments.of("zero declared text width", new byte[]{1, 6, 1, BlrConstants.blr_text, 0, 0, (byte) 255}, 1),
                Arguments.of("zero native VARCHAR width", new byte[]{1, 6, 1, BlrConstants.blr_varying, -2, -1, (byte) 255}, 1));
    }
    
    @Test
    void assertGetLength() {
        ByteBuf input = createRequest(new byte[]{1, 6, 1, BlrConstants.blr_long, 0, (byte) 255}, 4, new byte[]{0, 0, 0, 12});
        try {
            assertThat(FirebirdPutSliceCommandPacket.getLength(new FirebirdPacketPayload(input, StandardCharsets.UTF_8)), is(48));
            assertThat(input.readerIndex(), is(Integer.BYTES));
        } finally {
            input.release();
        }
    }
    
    @Test
    void assertGetLengthWithSmallintValues() {
        ByteBuf input = createRequest(new byte[]{1, 6, 1, BlrConstants.blr_short, 0, (byte) 255}, 4, new byte[]{0, 0, 0, 1, 0, 0, 0, 2});
        try {
            assertThat(FirebirdPutSliceCommandPacket.getLength(new FirebirdPacketPayload(input, StandardCharsets.UTF_8)), is(52));
        } finally {
            input.release();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sliceCases")
    void assertGetLengthForSliceTypes(final String name, final byte[] descriptor, final int nativeLength, final byte[] data) {
        ByteBuf input = createRequest(descriptor, nativeLength, data);
        try {
            int expected = input.readableBytes();
            input.writeInt(FirebirdCommandPacketType.COMMIT.getValue());
            assertThat(FirebirdPutSliceCommandPacket.getLength(new FirebirdPacketPayload(input, StandardCharsets.UTF_8)), is(expected));
            assertThat(input.readerIndex(), is(Integer.BYTES));
        } finally {
            input.release();
        }
    }
    
    @Test
    void assertGetLengthWithIncompleteFixedData() {
        ByteBuf input = createRequest(new byte[]{1, 6, 1, BlrConstants.blr_short, 0, (byte) 255}, 4, new byte[0]);
        try {
            assertThat(FirebirdPutSliceCommandPacket.getLength(new FirebirdPacketPayload(input, StandardCharsets.UTF_8)), is(52));
            assertThat(input.readerIndex(), is(Integer.BYTES));
        } finally {
            input.release();
        }
    }
    
    @Test
    void assertGetLengthWithIncompleteVariableData() {
        ByteBuf input = createRequest(new byte[]{1, 6, 1, BlrConstants.blr_varying, 5, 0, (byte) 255}, 7, new byte[]{0, 0, 0, 1});
        try {
            FirebirdPacketPayload payload = new FirebirdPacketPayload(input, StandardCharsets.UTF_8);
            assertThrows(IndexOutOfBoundsException.class, () -> FirebirdPutSliceCommandPacket.getLength(payload));
            assertThat(input.readerIndex(), is(Integer.BYTES));
        } finally {
            input.release();
        }
    }
    
    private static ByteBuf createRequest(final byte[] descriptor, final int nativeLength, final byte[] data) {
        return Unpooled.buffer().writeInt(0).writeInt(FirebirdCommandPacketType.PUT_SLICE.getValue()).writeInt(1).writeLong(2L).writeInt(nativeLength)
                .writeInt(descriptor.length).writeBytes(descriptor).writeZero((4 - descriptor.length) & 3)
                .writeInt(4).writeInt(7).writeInt(nativeLength).writeBytes(data).skipBytes(Integer.BYTES);
    }
}

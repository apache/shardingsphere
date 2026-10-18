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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdSliceResponsePacketTest {
    
    private static final int SLICE_HEADER_LENGTH = 12;
    
    private static final int DUMMY_RESPONSE_LENGTH = 4;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteArguments")
    void assertWrite(final String name, final int sliceLength, final FirebirdBinaryColumnType columnType, final Collection<Object> elements, final int elementLength, final byte[] expectedBytes) {
        ByteBuf byteBuf = Unpooled.buffer();
        PacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdSliceResponsePacket(sliceLength, columnType, elements, elementLength).write(payload);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(expectedBytes));
    }
    
    private static Stream<Arguments> assertWriteArguments() {
        return Stream.of(
                Arguments.of("empty_slice", 0, FirebirdBinaryColumnType.SHORT, Collections.emptyList(), 2, new byte[]{0, 0, 0, 60, 0, 0, 0, 0, 0, 0, 0, 0}),
                Arguments.of("smallint_slice", 2, FirebirdBinaryColumnType.SHORT, Collections.singletonList(1), 2, new byte[]{0, 0, 0, 60, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 1}),
                Arguments.of("int4_slice", 8, FirebirdBinaryColumnType.LONG, Arrays.asList(1, 2), 4, new byte[]{0, 0, 0, 60, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 1, 0, 0, 0, 2}),
                Arguments.of("text_slice", 1, FirebirdBinaryColumnType.TEXT, Collections.singletonList("A"), 1, new byte[]{0, 0, 0, 60, 0, 0, 0, 1, 0, 0, 0, 1, 65, 0, 0, 0}),
                Arguments.of("padded_text_slice", 4, FirebirdBinaryColumnType.TEXT, Arrays.asList("A", "B"), 2, new byte[]{0, 0, 0, 60, 0, 0, 0, 4, 0, 0, 0, 4, 65, 32, 0, 0, 66, 32, 0, 0}),
                Arguments.of("binary_element_slice", 1, FirebirdBinaryColumnType.TEXT, Collections.singletonList(new byte[]{(byte) 0xFF}), 1,
                        new byte[]{0, 0, 0, 60, 0, 0, 0, 1, 0, 0, 0, 1, (byte) 0xFF, 0, 0, 0}),
                Arguments.of("int128_slice", 16, FirebirdBinaryColumnType.INT128, Collections.singletonList(BigInteger.valueOf(-1L)), 16,
                        new byte[]{0, 0, 0, 60, 0, 0, 0, 16, 0, 0, 0, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}),
                Arguments.of("time_tz_slice", 6, FirebirdBinaryColumnType.TIME_TZ, Collections.singletonList(OffsetTime.parse("01:02:03+02:00")), 6,
                        new byte[]{0, 0, 0, 60, 0, 0, 0, 6, 0, 0, 0, 6, 0x31, 0x6d, 0x0b, (byte) 0xB0, 0, 0, 0x06, 0x17}));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertDecFloatWriteArguments")
    void assertDecFloatElementsUseTheirXdrLengths(final String name, final FirebirdBinaryColumnType columnType, final BigDecimal value, final byte[] expectedElementBytes) {
        ByteBuf byteBuf = Unpooled.buffer();
        PacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdSliceResponsePacket(expectedElementBytes.length, columnType, Collections.singletonList(value), expectedElementBytes.length).write(payload);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual.length, is(SLICE_HEADER_LENGTH + expectedElementBytes.length));
        assertThat(Arrays.copyOfRange(actual, SLICE_HEADER_LENGTH, actual.length), is(expectedElementBytes));
    }
    
    private static Stream<Arguments> assertDecFloatWriteArguments() {
        BigDecimal dec16Value = new BigDecimal("1.234567890123456");
        BigDecimal dec34Value = new BigDecimal("1.234567890123456789012345678901234");
        return Stream.of(
                Arguments.of("dec16_slice", FirebirdBinaryColumnType.DEC16, dec16Value, Decimal64.valueOf(dec16Value).toBytes()),
                Arguments.of("dec34_slice", FirebirdBinaryColumnType.DEC34, dec34Value, Decimal128.valueOf(dec34Value).toBytes()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertAffectedTypesKeepsNextPacketBoundaryArguments")
    void assertAffectedTypesKeepsNextPacketBoundary(final String name, final int sliceLength, final FirebirdBinaryColumnType columnType, final Object element,
                                                    final int elementLength, final byte[] expectedElementBytes) {
        ByteBuf byteBuf = Unpooled.buffer();
        PacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdSliceResponsePacket(sliceLength, columnType, Collections.singletonList(element), elementLength).write(payload);
        new FirebirdDummyResponsePacket().write(payload);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual.length, is(SLICE_HEADER_LENGTH + expectedElementBytes.length + DUMMY_RESPONSE_LENGTH));
        assertThat(Arrays.copyOfRange(actual, SLICE_HEADER_LENGTH, SLICE_HEADER_LENGTH + expectedElementBytes.length), is(expectedElementBytes));
        assertThat(Arrays.copyOfRange(actual, actual.length - DUMMY_RESPONSE_LENGTH, actual.length), is(new byte[]{0, 0, 0, 71}));
    }
    
    private static Stream<Arguments> assertAffectedTypesKeepsNextPacketBoundaryArguments() {
        BigDecimal value = new BigDecimal("1.5");
        return Stream.of(
                Arguments.of("int128", 16, FirebirdBinaryColumnType.INT128, BigInteger.valueOf(-2L), 16, repeatedSignBytes(16, (byte) -2)),
                Arguments.of("time_tz", 6, FirebirdBinaryColumnType.TIME_TZ, OffsetTime.parse("01:02:03+02:00"), 6, new byte[]{0x31, 0x6d, 0x0b, (byte) 0xB0, 0, 0, 0x06, 0x17}),
                Arguments.of("d_float", 8, FirebirdBinaryColumnType.D_FLOAT, 1.5D, 8, new byte[]{0x3F, (byte) 0xF8, 0, 0, 0, 0, 0, 0}),
                Arguments.of("quad", 8, FirebirdBinaryColumnType.QUAD, 5L, 8, new byte[]{0, 0, 0, 0, 0, 0, 0, 5}),
                Arguments.of("dec16", 8, FirebirdBinaryColumnType.DEC16, value, 8, Decimal64.valueOf(value).toBytes()),
                Arguments.of("dec34", 16, FirebirdBinaryColumnType.DEC34, value, 16, Decimal128.valueOf(value).toBytes()),
                Arguments.of("binary_element", 1, FirebirdBinaryColumnType.TEXT, new byte[]{(byte) 0xFF}, 1, new byte[]{(byte) 0xFF, 0, 0, 0}));
    }
    
    private static byte[] repeatedSignBytes(final int length, final byte lastByte) {
        byte[] result = new byte[length];
        Arrays.fill(result, (byte) -1);
        result[length - 1] = lastByte;
        return result;
    }
    
    @Test
    void assertWriteKeepsNextPacketBoundary() {
        ByteBuf byteBuf = Unpooled.buffer();
        PacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdSliceResponsePacket(2, FirebirdBinaryColumnType.SHORT, Collections.singletonList(1), 2).write(payload);
        new FirebirdDummyResponsePacket().write(payload);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(new byte[]{0, 0, 0, 60, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 71}));
    }
    
    @Test
    void assertTextWriteKeepsNextPacketBoundary() {
        ByteBuf byteBuf = Unpooled.buffer();
        PacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdSliceResponsePacket(1, FirebirdBinaryColumnType.TEXT, Collections.singletonList("A"), 1).write(payload);
        new FirebirdDummyResponsePacket().write(payload);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(new byte[]{0, 0, 0, 60, 0, 0, 0, 1, 0, 0, 0, 1, 65, 0, 0, 0, 0, 0, 0, 71}));
    }
}

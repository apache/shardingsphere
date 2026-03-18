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

package org.apache.shardingsphere.database.protocol.postgresql.payload;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.postgresql.packet.ByteBufTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLPacketPayloadTest {
    
    @Test
    void assertReadInt1() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBufWithBytes(new byte[]{(byte) 200}), StandardCharsets.UTF_8);
        assertThat(payload.readInt1(), is(200));
    }
    
    @Test
    void assertWriteInt1() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        int expectedInt1 = 200;
        payload.writeInt1(expectedInt1);
        assertThat((int) byteBuf.readUnsignedByte(), is(expectedInt1));
    }
    
    @Test
    void assertReadInt2() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        byteBuf.writeShort(65535);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(payload.readInt2(), is(65535));
    }
    
    @Test
    void assertWriteInt2() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        int expectedInt2 = 32768;
        payload.writeInt2(expectedInt2);
        assertThat(byteBuf.readUnsignedShort(), is(expectedInt2));
    }
    
    @Test
    void assertReadInt4() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        byteBuf.writeInt(Integer.MAX_VALUE);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        int actualInt4 = payload.readInt4();
        assertThat(actualInt4, is(Integer.MAX_VALUE));
    }
    
    @Test
    void assertWriteInt4() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        int expectedInt4 = Integer.MIN_VALUE;
        payload.writeInt4(expectedInt4);
        int actualInt4 = byteBuf.readInt();
        assertThat(actualInt4, is(expectedInt4));
    }
    
    @Test
    void assertReadInt8() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        byteBuf.writeLong(Long.MAX_VALUE);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(payload.readInt8(), is(Long.MAX_VALUE));
    }
    
    @Test
    void assertWriteInt8() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        long expectedInt8 = Long.MIN_VALUE;
        payload.writeInt8(expectedInt8);
        long actualInt8 = byteBuf.readLong();
        assertThat(actualInt8, is(expectedInt8));
    }
    
    @Test
    void assertWriteBytes() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        payload.writeBytes(new byte[]{1, 2, 3});
        int actualByte1 = byteBuf.readUnsignedByte();
        int actualByte2 = byteBuf.readUnsignedByte();
        int actualByte3 = byteBuf.readUnsignedByte();
        assertThat(actualByte1, is(1));
        assertThat(actualByte2, is(2));
        assertThat(actualByte3, is(3));
    }
    
    @Test
    void assertBytesBeforeZero() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBufWithBytes(new byte[]{117, 115, 101, 114, 0}), StandardCharsets.UTF_8);
        assertThat(payload.bytesBeforeZero(), is(4));
    }
    
    @Test
    void assertReadStringNul() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBufWithBytes(new byte[]{117, 115, 101, 114, 0}), StandardCharsets.UTF_8);
        assertThat(payload.readStringNul(), is("user"));
    }
    
    @Test
    void assertWriteStringNul() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        String expectedString = "user";
        payload.writeStringNul(expectedString);
        assertThat(byteBuf.readCharSequence(expectedString.length(), StandardCharsets.UTF_8).toString(), is(expectedString));
        assertThat((int) byteBuf.readUnsignedByte(), is(0));
    }
    
    @Test
    void assertWriteStringEOF() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        String expectedString = "user";
        payload.writeStringEOF(expectedString);
        assertThat(byteBuf.readCharSequence(expectedString.length(), StandardCharsets.UTF_8).toString(), is(expectedString));
    }
    
    @Test
    void assertSkipReserved() {
        ByteBuf byteBuf = createByteBufWithBytes(new byte[]{1, 2, 3, 4});
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        payload.skipReserved(4);
        assertThat(byteBuf.readableBytes(), is(0));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("hasCompletePacketArguments")
    void assertHasCompletePacket(final String name, final byte[] packetData, final boolean expectedHasCompletePacket) {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(createByteBufWithBytes(packetData), StandardCharsets.UTF_8);
        boolean actualHasCompletePacket = payload.hasCompletePacket();
        assertThat(actualHasCompletePacket, is(expectedHasCompletePacket));
    }
    
    private ByteBuf createByteBufWithBytes(final byte[] bytes) {
        ByteBuf result = ByteBufTestUtils.createByteBuf(16, 128);
        result.writeBytes(bytes);
        return result;
    }
    
    private static Stream<Arguments> hasCompletePacketArguments() {
        return Stream.of(
                Arguments.of("readable bytes less than header length", new byte[]{1, 0, 0, 0}, false),
                Arguments.of("readable bytes smaller than declared packet length", new byte[]{1, 0, 0, 0, 6, 9}, false),
                Arguments.of("readable bytes match declared packet length", new byte[]{1, 0, 0, 0, 4}, true));
    }
}

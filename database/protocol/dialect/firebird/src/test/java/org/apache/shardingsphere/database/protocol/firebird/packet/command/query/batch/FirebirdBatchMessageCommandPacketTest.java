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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class FirebirdBatchMessageCommandPacketTest {
    
    @Test
    void assertGetLengthWithColumnDescriptors() {
        ByteBuf packet = Unpooled.buffer().writeInt(0).writeInt(42).writeInt(1).writeByte(0).writeZero(3).writeInt(100);
        int actual = FirebirdBatchMessageCommandPacket.getLength(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8),
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)));
        assertThat(actual, is(20));
    }
    
    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("octetsArguments")
    void assertReadParameterValuesWithOctets(final FirebirdBinaryColumnType type, final String name, final byte[] expected) {
        ByteBuf data = Unpooled.buffer().writeInt(0).writeInt(42).writeInt(1).writeInt(0);
        if (FirebirdBinaryColumnType.VARYING == type) {
            data.writeInt(expected.length);
        }
        data.writeBytes(expected).writeZero((4 - expected.length % 4) % 4).writeInt(123);
        FirebirdBatchMessageCommandPacket packet = new FirebirdBatchMessageCommandPacket(new FirebirdPacketPayload(data, StandardCharsets.UTF_8));
        List<List<Object>> actual = packet.readParameterValues(Arrays.asList(new FirebirdBatchColumnDescriptor(type, 5, 1, 0),
                new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 8)));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).get(0), is(expected));
        assertThat(actual.get(0).get(1), is(123));
    }
    
    private static Stream<Arguments> octetsArguments() {
        return Stream.of(
                Arguments.of(FirebirdBinaryColumnType.VARYING, "invalid_utf8", new byte[]{1, 2, (byte) 0xFF}),
                Arguments.of(FirebirdBinaryColumnType.VARYING, "empty", new byte[0]),
                Arguments.of(FirebirdBinaryColumnType.TEXT, "fixed_length", new byte[]{0, (byte) 0x80, (byte) 0xFF, 0, 1}));
    }
    
    @Test
    void assertReadParameterValuesWithNullOctets() {
        ByteBuf data = Unpooled.buffer().writeInt(0).writeInt(42).writeInt(2).writeInt(0x01000000).writeInt(0).writeInt(3).writeBytes(new byte[]{1, 2, (byte) 0xFF}).writeByte(0);
        FirebirdBatchMessageCommandPacket packet = new FirebirdBatchMessageCommandPacket(new FirebirdPacketPayload(data, StandardCharsets.UTF_8));
        List<List<Object>> actual = packet.readParameterValues(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.VARYING, 5, 1, 0)));
        assertThat(actual.size(), is(2));
        assertNull(actual.get(0).get(0));
        assertThat(actual.get(1).get(0), is(new byte[]{1, 2, (byte) 0xFF}));
    }
}

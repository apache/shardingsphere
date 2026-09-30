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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdSliceResponsePacketTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteArguments")
    void assertWrite(final String name, final int sliceLength, final byte[] sliceData, final int expectedPadding) {
        ByteBuf byteBuf = Unpooled.buffer();
        new FirebirdSliceResponsePacket(sliceLength, sliceData).write(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8));
        assertThat(byteBuf.readInt(), is(FirebirdCommandPacketType.SLICE.getValue()));
        assertThat(byteBuf.readInt(), is(sliceLength));
        assertThat(byteBuf.readInt(), is(sliceData.length));
        assertThat(byteBuf.readInt(), is(sliceData.length));
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(expectedPadding == 0 ? sliceData : pad(sliceData, expectedPadding)));
    }
    
    @Test
    void assertGetters() {
        byte[] sliceData = new byte[]{1, 2, 3};
        FirebirdSliceResponsePacket packet = new FirebirdSliceResponsePacket(3, sliceData);
        assertThat(packet.getSliceLength(), is(3));
        assertThat(packet.getSliceData(), is(sliceData));
    }
    
    private static byte[] pad(final byte[] value, final int padding) {
        byte[] result = new byte[value.length + padding];
        System.arraycopy(value, 0, result, 0, value.length);
        return result;
    }
    
    private static Stream<Arguments> assertWriteArguments() {
        return Stream.of(
                Arguments.of("empty_slice", 0, new byte[0], 0),
                Arguments.of("aligned_slice", 4, new byte[]{1, 2, 3, 4}, 0),
                Arguments.of("unaligned_slice", 3, new byte[]{1, 2, 3}, 1));
    }
}

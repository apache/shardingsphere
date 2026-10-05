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

class FirebirdSliceResponsePacketTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteArguments")
    void assertWrite(final String name, final int sliceLength, final FirebirdBinaryColumnType columnType, final List<Object> elements, final byte[] expectedBytes) {
        ByteBuf byteBuf = Unpooled.buffer();
        PacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdSliceResponsePacket(sliceLength, columnType, elements).write(payload);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(expectedBytes));
    }
    
    private static Stream<Arguments> assertWriteArguments() {
        return Stream.of(Arguments.of("empty_slice", 0, FirebirdBinaryColumnType.SHORT, Collections.emptyList(), new byte[]{0, 0, 0, 60, 0, 0, 0, 0, 0, 0, 0, 0}),
                Arguments.of("smallint_slice", 2, FirebirdBinaryColumnType.SHORT, Collections.singletonList(1), new byte[]{0, 0, 0, 60, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 1}),
                Arguments.of("int4_slice", 8, FirebirdBinaryColumnType.LONG, Arrays.asList(1, 2), new byte[]{0, 0, 0, 60, 0, 0, 0, 8, 0, 0, 0, 8, 0, 0, 0, 1, 0, 0, 0, 2}));
    }
    
    @Test
    void assertWriteKeepsNextPacketBoundary() {
        ByteBuf byteBuf = Unpooled.buffer();
        PacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdSliceResponsePacket(2, FirebirdBinaryColumnType.SHORT, Collections.singletonList(1)).write(payload);
        new FirebirdDummyResponsePacket().write(payload);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(new byte[]{0, 0, 0, 60, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 71}));
    }
}

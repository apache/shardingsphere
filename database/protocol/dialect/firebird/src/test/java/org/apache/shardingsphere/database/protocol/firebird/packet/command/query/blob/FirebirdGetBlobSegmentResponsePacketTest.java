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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebirdGetBlobSegmentResponsePacketTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteArguments")
    void assertWrite(final String name,
                     final byte[] segment, final int expectedLength, final int expectedWriteBytesCount, final int expectedGetByteBufCount, final int expectedPad, final int expectedPadCallCount) {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        ByteBuf byteBuf = mock(ByteBuf.class);
        if (expectedGetByteBufCount > 0) {
            when(payload.getByteBuf()).thenReturn(byteBuf);
        }
        new FirebirdGetBlobSegmentResponsePacket(segment).write(payload);
        verify(payload).writeInt2LE(expectedLength);
        verify(payload, times(expectedWriteBytesCount)).writeBytes(segment);
        verify(payload, times(expectedGetByteBufCount)).getByteBuf();
        verify(byteBuf, times(expectedPadCallCount)).writeZero(expectedPad);
    }
    
    private static Stream<Arguments> assertWriteArguments() {
        return Stream.of(
                Arguments.of("empty_segment", new byte[0], 0, 0, 0, 0, 0),
                Arguments.of("aligned_segment", new byte[]{1, 2, 3, 4}, 4, 1, 0, 0, 0),
                Arguments.of("unaligned_segment", new byte[]{1, 2, 3}, 3, 1, 1, 1, 1));
    }
}

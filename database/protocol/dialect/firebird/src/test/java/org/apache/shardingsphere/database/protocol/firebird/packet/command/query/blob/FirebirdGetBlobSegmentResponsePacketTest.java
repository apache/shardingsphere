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

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class FirebirdGetBlobSegmentResponsePacketTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteArguments")
    void assertWrite(final String name, final byte[] segment, final int expectedWriteBytesCount) {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        new FirebirdGetBlobSegmentResponsePacket(segment).write(payload);
        verify(payload).writeInt2LE(segment.length);
        verify(payload, times(expectedWriteBytesCount)).writeBytes(segment);
        verifyNoMoreInteractions(payload);
    }
    
    private static Stream<Arguments> assertWriteArguments() {
        return Stream.of(
                Arguments.of("empty_segment", new byte[0], 0),
                Arguments.of("aligned_segment", new byte[]{1, 2, 3, 4}, 1),
                Arguments.of("unaligned_segment_without_padding", new byte[]{1, 2, 3}, 1));
    }
}

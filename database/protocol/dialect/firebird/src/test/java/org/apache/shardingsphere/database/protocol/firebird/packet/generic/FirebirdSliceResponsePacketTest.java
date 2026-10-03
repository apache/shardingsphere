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
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
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
    void assertWrite(final String name, final int sliceLength, final byte[] sliceData, final byte[] expectedBytes) {
        ByteBuf byteBuf = Unpooled.buffer();
        new FirebirdSliceResponsePacket(sliceLength, sliceData).write(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8));
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(expectedBytes));
    }
    
    private static Stream<Arguments> assertWriteArguments() {
        return Stream.of(Arguments.of("empty_slice", 0, new byte[0], new byte[]{0, 0, 0, 60, 0, 0, 0, 0, 0, 0, 0, 0}),
                Arguments.of("aligned_slice", 4, new byte[]{1, 2, 3, 4}, new byte[]{0, 0, 0, 60, 0, 0, 0, 4, 0, 0, 0, 4, 1, 2, 3, 4}),
                Arguments.of("unaligned_slice", 3, new byte[]{1, 2, 3}, new byte[]{0, 0, 0, 60, 0, 0, 0, 3, 0, 0, 0, 3, 1, 2, 3, 0}));
    }
}

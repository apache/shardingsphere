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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdStringBinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertRead() {
        when(payload.readString()).thenReturn("foo");
        assertThat(new FirebirdStringBinaryProtocolValue().read(payload), is("foo"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeArguments")
    void assertWrite(final String name, final Object value, final byte[] expected) {
        ByteBuf actualBuffer = Unpooled.buffer();
        FirebirdPacketPayload actualPayload = new FirebirdPacketPayload(actualBuffer, StandardCharsets.UTF_8);
        new FirebirdStringBinaryProtocolValue().write(actualPayload, value);
        byte[] actual = new byte[actualBuffer.writerIndex()];
        actualBuffer.getBytes(0, actual);
        assertArrayEquals(expected, actual);
    }
    
    @Test
    void assertGetLength() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readerIndex()).thenReturn(2);
        when(payload.getBufferLength(2)).thenReturn(6);
        assertThat(new FirebirdStringBinaryProtocolValue().getLength(payload), is(6));
    }
    
    private static Stream<Arguments> writeArguments() {
        return Stream.of(
                Arguments.of("byte array", new byte[]{1}, new byte[]{0, 0, 0, 1, 1, 0, 0, 0}),
                Arguments.of("string", "bar", new byte[]{0, 0, 0, 3, 98, 97, 114, 0}),
                Arguments.of("null", null, new byte[]{0, 0, 0, 0}));
    }
}

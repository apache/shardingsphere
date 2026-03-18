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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdInt16BinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private ByteBuf result;
    
    @Test
    void assertRead() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readSlice(16)).thenReturn(result);
        assertThat(new FirebirdInt16BinaryProtocolValue().read(payload), is(result));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeArguments")
    void assertWrite(final String name, final Object value, final byte[] expected) {
        ByteBuf actualBuffer = Unpooled.buffer();
        FirebirdPacketPayload actualPayload = new FirebirdPacketPayload(actualBuffer, StandardCharsets.UTF_8);
        new FirebirdInt16BinaryProtocolValue().write(actualPayload, value);
        byte[] actual = new byte[16];
        actualBuffer.getBytes(0, actual);
        assertArrayEquals(expected, actual);
    }
    
    @Test
    void assertGetLength() {
        assertThat(new FirebirdInt16BinaryProtocolValue().getLength(payload), is(16));
    }
    
    private static Stream<Arguments> writeArguments() {
        return Stream.of(
                Arguments.of("big decimal", BigDecimal.valueOf(10L), new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10}),
                Arguments.of("integer", 513, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1}),
                Arguments.of("big integer", BigInteger.valueOf(66051L), new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3}),
                Arguments.of("long", 4294967298L, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2}));
    }
}

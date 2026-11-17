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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class FirebirdBatchMessageCommandPacketTest {
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"42, 42", "0, 0", "32767, 32767", "32768, 32768", "-32768, 32768", "-536, 65000", "-1, 65535"})
    void assertConstructor(final int wireHandle, final int expectedHandle) {
        ByteBuf buffer = Unpooled.buffer().writeInt(0).writeInt(wireHandle).writeInt(0);
        try {
            FirebirdBatchMessageCommandPacket actual = new FirebirdBatchMessageCommandPacket(new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8));
            assertThat(actual.getStatementHandle(), is(expectedHandle));
        } finally {
            buffer.release();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"32767, 32767", "-32768, 32768", "-536, 65000", "65000, 65000"})
    void assertGetLengthWithNativeHandle(final int wireHandle, final int expectedHandle) {
        FirebirdBatchRegistry registry = mock(FirebirdBatchRegistry.class);
        when(registry.getMessageFormat(anyInt(), anyInt())).thenReturn(null);
        ByteBuf buffer = Unpooled.buffer().writeInt(0).writeInt(wireHandle).writeInt(0);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(registry);
            when(registry.getMessageFormat(1, expectedHandle)).thenReturn(Collections.emptyList());
            int actual = FirebirdBatchMessageCommandPacket.getLength(new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8), 1);
            assertThat(actual, is(12));
        } finally {
            buffer.release();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(ints = {0, -1, 65535})
    void assertGetLengthWithInvalidObject(final int wireHandle) {
        FirebirdBatchRegistry registry = mock(FirebirdBatchRegistry.class);
        when(registry.getMessageFormat(anyInt(), anyInt())).thenReturn(null);
        ByteBuf buffer = Unpooled.buffer().writeInt(0).writeInt(wireHandle).writeInt(0);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(registry);
            assertThrows(InvalidBatchHandleException.class, () -> FirebirdBatchMessageCommandPacket.getLength(new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8), 1));
        } finally {
            buffer.release();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(ints = {0, 8, 11})
    void assertGetLengthWithIncompleteRegistryHeader(final int length) {
        ByteBuf buffer = Unpooled.buffer().writeZero(2 + length);
        buffer.readerIndex(2);
        try {
            int actual = FirebirdBatchMessageCommandPacket.getLength(new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8), 1);
            assertThat(actual, is(-1));
            assertThat(buffer.readerIndex(), is(2));
        } finally {
            buffer.release();
        }
    }
    
    @Test
    void assertGetLengthWithColumnDescriptors() {
        ByteBuf packet = Unpooled.buffer().writeInt(0).writeInt(42).writeInt(1).writeByte(0).writeZero(3).writeInt(100);
        int actual = FirebirdBatchMessageCommandPacket.getLength(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8),
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)));
        assertThat(actual, is(20));
    }
    
    @Test
    void assertGetLengthWithIncompleteHeader() {
        ByteBuf packet = Unpooled.buffer().writeZero(11);
        packet.readerIndex(2);
        int actual = FirebirdBatchMessageCommandPacket.getLength(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8), Collections.emptyList());
        assertThat(actual, is(-1));
        assertThat(packet.readerIndex(), is(2));
    }
    
    @Test
    void assertGetLengthWithNonzeroReaderIndex() {
        ByteBuf packet = Unpooled.buffer().writeZero(3).writeInt(0).writeInt(42).writeInt(1).writeByte(0).writeZero(3).writeInt(100);
        packet.readerIndex(3);
        int actual = FirebirdBatchMessageCommandPacket.getLength(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8),
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)));
        assertThat(actual, is(20));
        assertThat(packet.readerIndex(), is(23));
    }
}

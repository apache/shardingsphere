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
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdBlobBinaryProtocolValueTest {
    
    @AfterEach
    void clearStore() {
        getStore().clear();
    }
    
    @Test
    void assertGetBlobContent() {
        getStore().put(1L, new byte[]{1, 2});
        assertArrayEquals(new byte[]{1, 2}, FirebirdBlobBinaryProtocolValue.getBlobContent(1L));
    }
    
    @Test
    void assertRemoveBlobContent() {
        getStore().put(2L, new byte[]{3});
        FirebirdBlobBinaryProtocolValue.removeBlobContent(2L);
        assertNull(getStore().get(2L));
    }
    
    @Test
    void assertRead() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(3);
        byteBuf.writeBytes(new byte[]{65, 66, 67});
        byteBuf.writeByte(0);
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(new FirebirdBlobBinaryProtocolValue().read(payload), is("ABC"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeRegisterArguments")
    void assertWriteWithRegister(final String name, final Object value, final byte[] expected) {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdBlobBinaryProtocolValue().write(payload, value);
        long blobId = byteBuf.getLong(0);
        assertTrue(blobId > 0);
        assertArrayEquals(expected, getStore().get(blobId));
    }
    
    @Test
    void assertWriteWithNull() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdBlobBinaryProtocolValue().write(payload, null);
        assertThat(byteBuf.getLong(0), is(0L));
    }
    
    @Test
    void assertWriteWithBlobId() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdBlobBinaryProtocolValue().write(payload, 7L);
        assertThat(byteBuf.getLong(0), is(7L));
    }
    
    @Test
    void assertWriteWithBlob() throws SQLException, IOException {
        Blob blob = mock(Blob.class);
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenReturn(0, 2, -1);
        when(blob.getBinaryStream()).thenReturn(inputStream);
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdBlobBinaryProtocolValue().write(payload, blob);
        long blobId = byteBuf.getLong(0);
        assertTrue(blobId > 0);
        assertArrayEquals(new byte[]{0, 0}, getStore().get(blobId));
    }
    
    @Test
    void assertWriteWithBlobSQLException() throws SQLException {
        Blob blob = mock(Blob.class);
        when(blob.getBinaryStream()).thenThrow(new SQLException("failed"));
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new FirebirdBlobBinaryProtocolValue().write(payload, blob));
        assertThat(actual.getMessage(), is("Failed to read java.sql.Blob stream"));
    }
    
    @Test
    void assertWriteWithBlobIOException() throws SQLException, IOException {
        Blob blob = mock(Blob.class);
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenThrow(new IOException("failed"));
        when(blob.getBinaryStream()).thenReturn(inputStream);
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new FirebirdBlobBinaryProtocolValue().write(payload, blob));
        assertThat(actual.getMessage(), is("Failed to read java.sql.Blob content"));
    }
    
    @Test
    void assertWriteWithClob() throws SQLException {
        Clob clob = mock(Clob.class);
        when(clob.length()).thenReturn(3L);
        when(clob.getSubString(1L, 3)).thenReturn("xyz");
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdBlobBinaryProtocolValue().write(payload, clob);
        long blobId = byteBuf.getLong(0);
        assertTrue(blobId > 0);
        assertArrayEquals(new byte[]{120, 121, 122}, getStore().get(blobId));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private static Map<Long, byte[]> getStore() {
        return (Map<Long, byte[]>) Plugins.getMemberAccessor().get(FirebirdBlobBinaryProtocolValue.class.getDeclaredField("STORE"), FirebirdBlobBinaryProtocolValue.class);
    }
    
    @Test
    void assertWriteWithClobSQLException() throws SQLException {
        Clob clob = mock(Clob.class);
        when(clob.length()).thenThrow(new SQLException("failed"));
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new FirebirdBlobBinaryProtocolValue().write(payload, clob));
        assertThat(actual.getMessage(), is("Failed to read java.sql.Clob"));
    }
    
    @Test
    void assertGetLength() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(new FirebirdBlobBinaryProtocolValue().getLength(payload), is(8));
    }
    
    private static Stream<Arguments> writeRegisterArguments() {
        return Stream.of(
                Arguments.of("byte array", new byte[]{1, 2}, new byte[]{1, 2}),
                Arguments.of("string", "bar", new byte[]{98, 97, 114}),
                Arguments.of("string builder", new StringBuilder("baz"), new byte[]{98, 97, 122}));
    }
}

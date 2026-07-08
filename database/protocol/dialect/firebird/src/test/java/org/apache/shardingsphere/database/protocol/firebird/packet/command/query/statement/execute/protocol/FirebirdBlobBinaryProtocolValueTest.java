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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
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
    
    private static final int CONNECTION_ID = 1;
    
    @AfterEach
    void tearDown() {
        FirebirdBlobBinaryProtocolValue.unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertGetBlobContentWithUnknownConnection() {
        assertNull(FirebirdBlobBinaryProtocolValue.getBlobContent(99, 1L));
    }
    
    @Test
    void assertUnregisterConnectionDropsContents() {
        long blobId = writeAndGetBlobId(new byte[]{3});
        FirebirdBlobBinaryProtocolValue.unregisterConnection(CONNECTION_ID);
        assertNull(FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobId));
    }
    
    @Test
    void assertContentIsScopedByConnection() {
        long blobId = writeAndGetBlobId(new byte[]{1, 2});
        assertArrayEquals(new byte[]{1, 2}, FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobId));
        assertNull(FirebirdBlobBinaryProtocolValue.getBlobContent(2, blobId));
    }
    
    @Test
    void assertRead() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 3, 65, 66, 67, 0});
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(new FirebirdBlobBinaryProtocolValue().read(payload), is("ABC"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeRegisterArguments")
    void assertWriteWithRegister(final String name, final Object value, final byte[] expected) {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = createPayload(byteBuf);
        new FirebirdBlobBinaryProtocolValue().write(payload, value);
        long blobId = byteBuf.getLong(0);
        assertTrue(blobId > 0);
        assertArrayEquals(expected, FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobId));
    }
    
    @Test
    void assertWriteWithNull() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = createPayload(byteBuf);
        new FirebirdBlobBinaryProtocolValue().write(payload, null);
        assertThat(byteBuf.getLong(0), is(0L));
    }
    
    @Test
    void assertWriteWithBlobId() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = createPayload(byteBuf);
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
        FirebirdPacketPayload payload = createPayload(byteBuf);
        new FirebirdBlobBinaryProtocolValue().write(payload, blob);
        long blobId = byteBuf.getLong(0);
        assertTrue(blobId > 0);
        assertArrayEquals(new byte[]{0, 0}, FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobId));
    }
    
    @Test
    void assertWriteWithBlobSQLException() throws SQLException {
        Blob blob = mock(Blob.class);
        when(blob.getBinaryStream()).thenThrow(new SQLException("failed"));
        FirebirdPacketPayload payload = createPayload(Unpooled.buffer());
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new FirebirdBlobBinaryProtocolValue().write(payload, blob));
        assertThat(actual.getMessage(), is("Failed to read java.sql.Blob stream"));
    }
    
    @Test
    void assertWriteWithBlobIOException() throws SQLException, IOException {
        Blob blob = mock(Blob.class);
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenThrow(new IOException("failed"));
        when(blob.getBinaryStream()).thenReturn(inputStream);
        FirebirdPacketPayload payload = createPayload(Unpooled.buffer());
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new FirebirdBlobBinaryProtocolValue().write(payload, blob));
        assertThat(actual.getMessage(), is("Failed to read java.sql.Blob content"));
    }
    
    @Test
    void assertWriteWithClob() throws SQLException {
        Clob clob = mock(Clob.class);
        when(clob.length()).thenReturn(3L);
        when(clob.getSubString(1L, 3)).thenReturn("xyz");
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = createPayload(byteBuf);
        new FirebirdBlobBinaryProtocolValue().write(payload, clob);
        long blobId = byteBuf.getLong(0);
        assertTrue(blobId > 0);
        assertArrayEquals(new byte[]{120, 121, 122}, FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobId));
    }
    
    @Test
    void assertWriteWithClobSQLException() throws SQLException {
        Clob clob = mock(Clob.class);
        when(clob.length()).thenThrow(new SQLException("failed"));
        FirebirdPacketPayload payload = createPayload(Unpooled.buffer());
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new FirebirdBlobBinaryProtocolValue().write(payload, clob));
        assertThat(actual.getMessage(), is("Failed to read java.sql.Clob"));
    }
    
    @Test
    void assertGetLength() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[0]);
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(new FirebirdBlobBinaryProtocolValue().getLength(payload), is(8));
    }
    
    private long writeAndGetBlobId(final byte[] content) {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = createPayload(byteBuf);
        new FirebirdBlobBinaryProtocolValue().write(payload, content);
        return byteBuf.getLong(0);
    }
    
    private FirebirdPacketPayload createPayload(final ByteBuf byteBuf) {
        FirebirdPacketPayload result = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        result.setConnectionId(CONNECTION_ID);
        return result;
    }
    
    private static Stream<Arguments> writeRegisterArguments() {
        return Stream.of(
                Arguments.of("byte array", new byte[]{1, 2}, new byte[]{1, 2}),
                Arguments.of("string", "bar", new byte[]{98, 97, 114}),
                Arguments.of("string builder", new StringBuilder("baz"), new byte[]{98, 97, 122}));
    }
}
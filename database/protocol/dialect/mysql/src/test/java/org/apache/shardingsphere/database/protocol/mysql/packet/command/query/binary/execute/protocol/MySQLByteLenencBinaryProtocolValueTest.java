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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MySQLByteLenencBinaryProtocolValueTest {
    
    @Test
    void assertRead() {
        byte[] input = {0x0d, 0x0a, 0x33, 0x18, 0x01, 0x4a, 0x08, 0x0a, (byte) 0x9a, 0x01, 0x18, 0x01, 0x4a, 0x6f};
        byte[] expected = {0x0a, 0x33, 0x18, 0x01, 0x4a, 0x08, 0x0a, (byte) 0x9a, 0x01, 0x18, 0x01, 0x4a, 0x6f};
        ByteBuf byteBuf = Unpooled.wrappedBuffer(input);
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        byte[] actual = (byte[]) new MySQLByteLenencBinaryProtocolValue().read(payload, false);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertWrite() {
        byte[] input = {0x0a, 0x33, 0x18, 0x01, 0x4a, 0x08, 0x0a, (byte) 0x9a, 0x01, 0x18, 0x01, 0x4a, 0x6f};
        byte[] expected = {0x0d, 0x0a, 0x33, 0x18, 0x01, 0x4a, 0x08, 0x0a, (byte) 0x9a, 0x01, 0x18, 0x01, 0x4a, 0x6f};
        ByteBuf actual = Unpooled.wrappedBuffer(new byte[expected.length]).writerIndex(0);
        MySQLPacketPayload payload = new MySQLPacketPayload(actual, StandardCharsets.UTF_8);
        new MySQLByteLenencBinaryProtocolValue().write(payload, input);
        assertThat(ByteBufUtil.getBytes(actual), is(expected));
    }
    
    @Test
    void assertWriteString() {
        ByteBuf byteBuf = Unpooled.buffer();
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new MySQLByteLenencBinaryProtocolValue().write(payload, "value");
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringLenenc(), is("value"));
    }
    
    @Test
    void assertWriteClob() throws SQLException, IOException {
        String expected = "ASCII|中文|🙂|é|𝄞";
        Reader reader = spy(new StringReader(expected));
        Clob clob = mock(Clob.class);
        when(clob.getCharacterStream()).thenReturn(reader);
        ByteBuf byteBuf = Unpooled.buffer();
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new MySQLByteLenencBinaryProtocolValue().write(payload, clob);
        assertThat(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8).readStringLenenc(), is(expected));
        verify(reader).close();
    }
    
    @Test
    void assertWriteClobWithIOException() throws SQLException, IOException {
        IOException expectedCause = new IOException("read error");
        Reader reader = spy(new StringReader("") {
            
            @Override
            public int read(final char[] chars, final int offset, final int length) throws IOException {
                throw expectedCause;
            }
        });
        Clob clob = mock(Clob.class);
        when(clob.getCharacterStream()).thenReturn(reader);
        ByteBuf byteBuf = Unpooled.buffer();
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        UnknownSQLException actual = assertThrows(UnknownSQLException.class, () -> new MySQLByteLenencBinaryProtocolValue().write(payload, clob));
        assertThat(actual.getCause(), is(expectedCause));
        assertThat(byteBuf.writerIndex(), is(0));
        verify(reader).close();
    }
    
    @Test
    void assertWriteClobWithSQLException() throws SQLException {
        SQLException expectedCause = new SQLException("sql error");
        Clob clob = mock(Clob.class);
        when(clob.getCharacterStream()).thenThrow(expectedCause);
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        UnknownSQLException actual = assertThrows(UnknownSQLException.class, () -> new MySQLByteLenencBinaryProtocolValue().write(payload, clob));
        assertThat(actual.getCause(), is(expectedCause));
    }
}

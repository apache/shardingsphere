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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query;

import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLDataRowPacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Mock
    private SQLXML sqlxml;
    
    @Before
    public void setup() {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
    }
    
    @Test
    public void assertWriteWithNull() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(null));
        actual.write(payload);
        verify(payload).writeInt4(0xFFFFFFFF);
    }
    
    @Test
    public void assertWriteWithBytes() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(new byte[]{'a'}));
        actual.write(payload);
        verify(payload).writeInt4(new byte[]{'a'}.length);
        verify(payload).writeBytes(new byte[]{'a'});
    }
    
    @Test
    public void assertWriteWithSQLXML() throws SQLException {
        when(sqlxml.getString()).thenReturn("value");
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(sqlxml));
        actual.write(payload);
        byte[] valueBytes = "value".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(valueBytes.length);
        verify(payload).writeBytes(valueBytes);
    }
    
    @Test
    public void assertWriteWithString() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList("value"));
        assertThat(actual.getData(), is(Collections.singletonList("value")));
        actual.write(payload);
        byte[] valueBytes = "value".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(valueBytes.length);
        verify(payload).writeBytes(valueBytes);
    }
    
    @Test(expected = RuntimeException.class)
    public void assertWriteWithSQLXML4Error() throws SQLException {
        when(sqlxml.getString()).thenThrow(new SQLException("mock"));
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(sqlxml));
        actual.write(payload);
        verify(payload, times(0)).writeStringEOF(any());
    }
    
    @Test
    public void assertWriteBinaryNull() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(new BinaryCell(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4, null)));
        actual.write(payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(0xFFFFFFFF);
    }
    
    @Test
    public void assertWriteBinaryInt4() {
        final int value = 12345678;
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(new BinaryCell(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4, value)));
        actual.write(payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(4);
        verify(payload).writeInt4(value);
    }
    
    @Test
    public void assertGetIdentifier() {
        assertThat(new PostgreSQLDataRowPacket(Collections.emptyList()).getIdentifier(), is(PostgreSQLMessagePacketType.DATA_ROW));
    }
}

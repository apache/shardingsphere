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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLDataRowPacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Mock
    private SQLXML sqlxml;
    
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
    @SneakyThrows
    public void assertWriteWithSQLXML() {
        when(sqlxml.getString()).thenReturn("string");
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(sqlxml));
        actual.write(payload);
        verify(payload).writeInt4("string".getBytes().length);
        verify(payload).writeStringEOF("string");
    }
    
    @Test
    public void assertWriteWithString() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList("str"));
        assertThat(actual.getData(), is(Collections.singletonList("str")));
        actual.write(payload);
        verify(payload).writeInt4("str".getBytes().length);
        verify(payload).writeStringEOF("str");
    }
    
    @Test
    @SneakyThrows
    public void assertWriteWithSQLXML4Error() {
        when(sqlxml.getString()).thenThrow(new SQLException("mock"));
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(sqlxml));
        actual.write(payload);
    }
}

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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdPrepareStatementPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertPrepareStatementPacket() {
        doNothing().when(payload).skipReserved(anyInt());
        when(payload.readInt4()).thenReturn(1, 2, 3, 10);
        when(payload.readString()).thenReturn("SELECT 1");
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true, true, false);
        when(byteBuf.readByte()).thenReturn((byte) FirebirdSQLInfoPacketType.STMT_TYPE.getCode(), (byte) FirebirdSQLInfoPacketType.DESCRIBE_VARS.getCode());
        FirebirdPrepareStatementPacket packet = new FirebirdPrepareStatementPacket(payload);
        verify(payload).skipReserved(4);
        assertThat(packet.getTransactionId(), is(1));
        assertThat(packet.getStatementId(), is(2));
        assertThat(packet.getSqlDialect(), is(3));
        assertThat(packet.getSQL(), is("SELECT 1"));
        assertTrue(packet.isValidStatementHandle());
        assertTrue(packet.nextItem());
        assertThat(packet.getCurrentItem(), is(FirebirdSQLInfoPacketType.STMT_TYPE));
        assertTrue(packet.nextItem());
        assertThat(packet.getCurrentItem(), is(FirebirdSQLInfoPacketType.DESCRIBE_VARS));
        assertFalse(packet.nextItem());
    }
    
    @Test
    void assertIsValidStatementHandleWhenInvalid() {
        doNothing().when(payload).skipReserved(anyInt());
        when(payload.readInt4()).thenReturn(1, 0xFFFF, 3, 10);
        when(payload.readString()).thenReturn("SELECT 1");
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(false);
        FirebirdPrepareStatementPacket packet = new FirebirdPrepareStatementPacket(payload);
        assertFalse(packet.isValidStatementHandle());
    }
    
    @Test
    void assertGetLength() {
        when(payload.getBufferLength(16)).thenReturn(12);
        when(payload.getBufferLength(28)).thenReturn(8);
        assertThat(FirebirdPrepareStatementPacket.getLength(payload), is(40));
        verify(payload).getBufferLength(16);
        verify(payload).getBufferLength(28);
    }
}

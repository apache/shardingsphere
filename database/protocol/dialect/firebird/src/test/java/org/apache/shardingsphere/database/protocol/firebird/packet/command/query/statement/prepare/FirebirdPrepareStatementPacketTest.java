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
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdPrepareStatementPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertConstructor() {
        FirebirdPrepareStatementPacket packet = createPacket(2, Arrays.asList(FirebirdSQLInfoPacketType.STMT_TYPE, FirebirdSQLInfoPacketType.DESCRIBE_VARS));
        verify(payload).skipReserved(4);
        assertThat(packet.getTransactionId(), is(1));
        assertThat(packet.getStatementId(), is(2));
        assertThat(packet.getSqlDialect(), is(3));
        assertThat(packet.getInfoItems(), is(Arrays.asList(FirebirdSQLInfoPacketType.STMT_TYPE, FirebirdSQLInfoPacketType.DESCRIBE_VARS)));
        assertThat(packet.getMaxLength(), is(10));
    }
    
    @Test
    void assertIsValidStatementHandle() {
        assertTrue(createPacket(2, Collections.emptyList()).isValidStatementHandle());
    }
    
    @Test
    void assertIsValidStatementHandleWhenInvalid() {
        assertFalse(createPacket(0xFFFF, Collections.emptyList()).isValidStatementHandle());
    }
    
    @Test
    void assertNextItem() {
        assertTrue(createPacket(2, Collections.singletonList(FirebirdSQLInfoPacketType.STMT_TYPE)).nextItem());
    }
    
    @Test
    void assertNextItemWhenNoItem() {
        assertFalse(createPacket(2, Collections.emptyList()).nextItem());
    }
    
    @Test
    void assertGetCurrentItem() throws ReflectiveOperationException {
        FirebirdPrepareStatementPacket packet = createPacket(2, Collections.singletonList(FirebirdSQLInfoPacketType.STMT_TYPE));
        Plugins.getMemberAccessor().set(FirebirdPrepareStatementPacket.class.getDeclaredField("currentItemIdx"), packet, 0);
        assertThat(packet.getCurrentItem(), is(FirebirdSQLInfoPacketType.STMT_TYPE));
    }
    
    @Test
    void assertGetSQL() {
        assertThat(createPacket(2, Collections.emptyList()).getSQL(), is("SELECT 1"));
    }
    
    @Test
    void assertWrite() {
        FirebirdPacketPayload writePayload = mock(FirebirdPacketPayload.class);
        createPacket(2, Collections.emptyList()).write((PacketPayload) writePayload);
        verifyNoInteractions(writePayload);
    }
    
    @Test
    void assertGetLength() {
        when(payload.getBufferLength(16)).thenReturn(12);
        when(payload.getBufferLength(28)).thenReturn(8);
        assertThat(FirebirdPrepareStatementPacket.getLength(payload), is(40));
        verify(payload).getBufferLength(16);
        verify(payload).getBufferLength(28);
    }
    
    private FirebirdPrepareStatementPacket createPacket(final int statementId, final List<FirebirdSQLInfoPacketType> infoItems) {
        when(payload.readInt4()).thenReturn(1, statementId, 3, 10);
        when(payload.readString()).thenReturn("SELECT 1");
        when(payload.readBuffer()).thenReturn(byteBuf);
        int[] idx = new int[1];
        when(byteBuf.isReadable()).thenAnswer(invocation -> idx[0] < infoItems.size());
        if (!infoItems.isEmpty()) {
            when(byteBuf.readByte()).thenAnswer(invocation -> (byte) infoItems.get(idx[0]++).getCode());
        }
        return new FirebirdPrepareStatementPacket(payload);
    }
}

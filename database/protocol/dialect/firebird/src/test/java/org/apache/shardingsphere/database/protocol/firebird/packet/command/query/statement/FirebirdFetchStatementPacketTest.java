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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdFetchStatementPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertFetchStatementPacket() {
        when(payload.readInt4()).thenReturn(3, 7, 10);
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 9, (short) 0, (short) BlrConstants.blr_long, (short) BlrConstants.blr_short, (short) BlrConstants.blr_end);
        when(byteBuf.skipBytes(anyInt())).thenReturn(byteBuf);
        FirebirdFetchStatementPacket packet = new FirebirdFetchStatementPacket(payload);
        verify(payload).skipReserved(4);
        assertThat(packet.getStatementId(), is(3));
        assertThat(packet.getMessage(), is(7));
        assertThat(packet.getFetchSize(), is(10));
        assertThat(packet.getParameterTypes().size(), is(2));
        assertThat(packet.getParameterTypes().get(0), is(FirebirdBinaryColumnType.LONG));
        assertThat(packet.getParameterTypes().get(1), is(FirebirdBinaryColumnType.SHORT));
    }
    
    @Test
    void assertGetLength() {
        when(payload.getBufferLength(8)).thenReturn(20);
        assertThat(FirebirdFetchStatementPacket.getLength(payload), is(36));
        verify(payload).getBufferLength(8);
    }
}

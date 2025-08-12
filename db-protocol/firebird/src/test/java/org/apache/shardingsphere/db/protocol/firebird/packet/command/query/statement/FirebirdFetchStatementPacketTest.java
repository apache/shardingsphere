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

package org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebirdFetchStatementPacketTest {
    
    @Test
    void assertFetchStatementPacket() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(payload.readInt4()).thenReturn(3, 7, 10);
        ByteBuf blr = mock(ByteBuf.class);
        when(payload.readBuffer()).thenReturn(blr);
        when(blr.isReadable()).thenReturn(true);
        when(blr.readUnsignedByte()).thenReturn((short) 9, (short) 0, (short) BlrConstants.blr_long, (short) BlrConstants.blr_short, (short) BlrConstants.blr_end);
        when(blr.skipBytes(anyInt())).thenReturn(blr);
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
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(payload.getBufferLength(8)).thenReturn(20);
        assertThat(FirebirdFetchStatementPacket.getLength(payload), is(36));
        verify(payload).getBufferLength(8);
    }
}

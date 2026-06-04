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
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebirdBatchCreateCommandPacketTest {
    
    @Test
    void assertConstructor() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        ByteBuf blrBuffer = mock(ByteBuf.class);
        ByteBuf batchParametersBuffer = mock(ByteBuf.class);
        when(payload.readInt4()).thenReturn(7);
        when(payload.readBuffer()).thenReturn(blrBuffer, batchParametersBuffer);
        when(payload.readInt4Unsigned()).thenReturn(12L);
        when(blrBuffer.isReadable()).thenReturn(true);
        FirebirdBatchCreateCommandPacket actualPacket = new FirebirdBatchCreateCommandPacket(payload);
        assertThat(actualPacket.getStatementHandle(), is(7));
        assertThat(actualPacket.getBatchBlr(), is(blrBuffer));
        assertThat(actualPacket.getBatchMessageLength(), is(12L));
        assertThat(actualPacket.getBatchParametersBuffer(), is(batchParametersBuffer));
        verify(payload).skipReserved(4);
    }
    
    @Test
    void assertConstructorWhenBatchBlrMissing() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        ByteBuf blrBuffer = mock(ByteBuf.class);
        when(payload.readBuffer()).thenReturn(blrBuffer);
        when(blrBuffer.isReadable()).thenReturn(false);
        assertThrows(FirebirdProtocolException.class, () -> new FirebirdBatchCreateCommandPacket(payload));
        verify(payload).skipReserved(4);
    }
    
    @Test
    void assertGetLength() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(payload.getBufferLength(8)).thenReturn(5);
        when(payload.getBufferLength(17)).thenReturn(9);
        assertThat(FirebirdBatchCreateCommandPacket.getLength(payload), is(26));
    }
}

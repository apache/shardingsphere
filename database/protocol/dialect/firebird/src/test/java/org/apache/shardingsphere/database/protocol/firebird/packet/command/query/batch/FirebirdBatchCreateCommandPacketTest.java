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
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebirdBatchCreateCommandPacketTest {
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"7, 7", "0, 0", "32767, 32767", "32768, 32768", "-32768, 32768", "-536, 65000", "-1, 65535"})
    void assertConstructor(final int wireHandle, final int expectedHandle) {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        ByteBuf blrBuffer = mock(ByteBuf.class);
        ByteBuf batchParametersBuffer = mock(ByteBuf.class);
        when(payload.readInt4()).thenReturn(wireHandle);
        when(payload.readBuffer()).thenReturn(blrBuffer, batchParametersBuffer);
        when(payload.readInt4Unsigned()).thenReturn(12L);
        when(blrBuffer.isReadable()).thenReturn(true);
        FirebirdBatchCreateCommandPacket actual = new FirebirdBatchCreateCommandPacket(payload);
        assertThat(actual.getStatementHandle(), is(expectedHandle));
        assertThat(actual.getBatchBlr(), is(blrBuffer));
        assertThat(actual.getBatchMessageLength(), is(12L));
        assertThat(actual.getBatchParametersBuffer(), is(batchParametersBuffer));
        verify(payload).skipReserved(4);
    }
    
    @Test
    void assertConstructorWhenBatchBlrMissing() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        ByteBuf blrBuffer = mock(ByteBuf.class);
        when(payload.readBuffer()).thenReturn(blrBuffer);
        when(blrBuffer.isReadable()).thenReturn(false);
        assertThrows(DatabaseProtocolException.class, () -> new FirebirdBatchCreateCommandPacket(payload));
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

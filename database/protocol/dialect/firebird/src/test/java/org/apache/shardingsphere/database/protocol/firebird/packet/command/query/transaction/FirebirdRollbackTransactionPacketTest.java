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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction;

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdRollbackTransactionPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"456, 456", "0, 0", "32767, 32767", "32768, 32768", "-32768, 32768", "-536, 65000", "-1, 65535"})
    void assertReadTransactionId(final int wireHandle, final int expectedHandle) {
        when(payload.readInt4()).thenReturn(wireHandle);
        FirebirdRollbackTransactionPacket actual = new FirebirdRollbackTransactionPacket(payload);
        verify(payload).skipReserved(4);
        assertThat(actual.getTransactionId(), is(expectedHandle));
    }
    
    @Test
    void assertGetLength() {
        assertThat(FirebirdRollbackTransactionPacket.getLength(), is(8));
    }
    
    @Test
    void assertWrite() {
        FirebirdRollbackTransactionPacket packet = new FirebirdRollbackTransactionPacket(payload);
        FirebirdPacketPayload writePayload = mock(FirebirdPacketPayload.class);
        packet.write((PacketPayload) writePayload);
        verifyNoInteractions(writePayload);
    }
}

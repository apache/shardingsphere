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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute;

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdExecuteImmediatePacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertConstructor() {
        when(payload.readInt4()).thenReturn(1);
        when(payload.readString()).thenReturn("SET TRANSACTION");
        FirebirdExecuteImmediatePacket actual = new FirebirdExecuteImmediatePacket(payload);
        verify(payload).skipReserved(4);
        verify(payload).skipReserved(8);
        assertThat(actual.getTransactionId(), is(1));
        assertThat(actual.getSQL(), is("SET TRANSACTION"));
    }
    
    @Test
    void assertGetLength() {
        when(payload.getBufferLength(16)).thenReturn(20);
        when(payload.getBufferLength(36)).thenReturn(4);
        assertThat(FirebirdExecuteImmediatePacket.getLength(payload), is(44));
    }
}

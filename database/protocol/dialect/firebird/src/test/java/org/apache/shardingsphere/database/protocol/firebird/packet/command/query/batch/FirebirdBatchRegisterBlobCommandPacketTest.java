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

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebirdBatchRegisterBlobCommandPacketTest {
    
    @Test
    void assertConstructor() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(payload.readInt4()).thenReturn(7);
        when(payload.readInt8()).thenReturn(0x123456789ABCDEFL, 0xFEDCBA9876543210L);
        FirebirdBatchRegisterBlobCommandPacket actual = new FirebirdBatchRegisterBlobCommandPacket(payload);
        assertThat(actual.getStatementHandle(), is(7));
        assertThat(actual.getExistingBlobId(), is(0x123456789ABCDEFL));
        assertThat(actual.getBatchBlobId(), is(0xFEDCBA9876543210L));
        verify(payload).skipReserved(4);
    }
    
    @Test
    void assertGetLength() {
        assertThat(FirebirdBatchRegisterBlobCommandPacket.getLength(), is(24));
    }
}

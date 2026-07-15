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

class FirebirdBatchExecuteCommandPacketTest {
    
    @Test
    void assertConstructor() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(payload.readInt4()).thenReturn(7, 9);
        FirebirdBatchExecuteCommandPacket actualPacket = new FirebirdBatchExecuteCommandPacket(payload);
        assertThat(actualPacket.getStatementHandle(), is(7));
        assertThat(actualPacket.getTransactionHandle(), is(9));
        verify(payload).skipReserved(4);
    }
    
    @Test
    void assertGetLength() {
        assertThat(FirebirdBatchExecuteCommandPacket.getLength(), is(12));
    }
}

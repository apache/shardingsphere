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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebirdBatchExecuteCommandPacketTest {
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"7, 7, 9, 9", "32767, 32767, 0, 0", "0, 0, -32768, 32768", "-32768, 32768, -536, 65000", "32768, 32768, 65000, 65000", "-1, 65535, -1, 65535"})
    void assertConstructor(final int wireStatementHandle, final int expectedStatementHandle, final int wireTransactionHandle, final int expectedTransactionHandle) {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(payload.readInt4()).thenReturn(wireStatementHandle, wireTransactionHandle);
        FirebirdBatchExecuteCommandPacket actual = new FirebirdBatchExecuteCommandPacket(payload);
        assertThat(actual.getStatementHandle(), is(expectedStatementHandle));
        assertThat(actual.getTransactionHandle(), is(expectedTransactionHandle));
        verify(payload).skipReserved(4);
    }
    
    @Test
    void assertGetLength() {
        assertThat(FirebirdBatchExecuteCommandPacket.getLength(), is(12));
    }
}

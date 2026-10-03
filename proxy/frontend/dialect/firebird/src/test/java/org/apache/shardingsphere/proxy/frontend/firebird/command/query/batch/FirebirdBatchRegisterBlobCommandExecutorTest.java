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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchWithoutBlobsException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.RepeatedBatchBlobIdException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegisterBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(FirebirdBatchRegistry.class)
class FirebirdBatchRegisterBlobCommandExecutorTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdBatchRegisterBlobCommandPacket packet;
    
    @Mock
    private FirebirdBatchRegistry batchRegistry;
    
    @Mock
    private FirebirdBatchStatement batchStatement;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionId()).thenReturn(9);
        when(packet.getStatementHandle()).thenReturn(7);
        when(FirebirdBatchRegistry.getInstance()).thenReturn(batchRegistry);
    }
    
    @Test
    void assertExecute() {
        when(batchRegistry.getBatchStatement(9, 7)).thenReturn(batchStatement);
        when(batchStatement.hasBatchBlobColumn()).thenReturn(true);
        Map<Long, Long> blobIds = new HashMap<>(2);
        when(batchStatement.getBlobIds()).thenReturn(blobIds);
        when(packet.getExistingBlobId()).thenReturn(2L);
        when(packet.getBatchBlobId()).thenReturn(1L);
        Collection<DatabasePacket> actual = new FirebirdBatchRegisterBlobCommandExecutor(packet, connectionSession).execute();
        assertThat(blobIds, is(Collections.singletonMap(1L, 2L)));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(0));
        verify(batchRegistry).getBatchStatement(9, 7);
    }
    
    @Test
    void assertExecuteWithDuplicateBlob() {
        when(batchRegistry.getBatchStatement(9, 7)).thenReturn(batchStatement);
        when(batchStatement.hasBatchBlobColumn()).thenReturn(true);
        Map<Long, Long> blobIds = new HashMap<>(2);
        blobIds.put(1L, 2L);
        when(batchStatement.getBlobIds()).thenReturn(blobIds);
        when(packet.getExistingBlobId()).thenReturn(3L);
        when(packet.getBatchBlobId()).thenReturn(1L);
        RepeatedBatchBlobIdException actual = assertThrows(RepeatedBatchBlobIdException.class, () -> new FirebirdBatchRegisterBlobCommandExecutor(packet, connectionSession).execute());
        assertThat(actual.getBatchBlobId(), is(1L));
        assertThat(blobIds, is(Collections.singletonMap(1L, 2L)));
    }
    
    @Test
    void assertExecuteWithoutBlobColumns() {
        when(batchRegistry.getBatchStatement(9, 7)).thenReturn(batchStatement);
        BatchWithoutBlobsException actual = assertThrows(BatchWithoutBlobsException.class, () -> new FirebirdBatchRegisterBlobCommandExecutor(packet, connectionSession).execute());
        assertThat(actual.getStatementHandle(), is(7));
        verify(batchStatement, never()).getBlobIds();
    }
    
    @Test
    void assertExecuteWithUnknownBatch() {
        assertThrows(InvalidBatchHandleException.class, () -> new FirebirdBatchRegisterBlobCommandExecutor(packet, connectionSession).execute());
    }
}

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

package org.apache.shardingsphere.proxy.frontend.firebird.resource;

import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBlobBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchStatementManager;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        FirebirdTransactionIdGenerator.class, FirebirdStatementIdGenerator.class, FirebirdBlobIdGenerator.class, FirebirdBlobHandleGenerator.class,
        FirebirdBlobWriteCache.class, FirebirdBlobReadCache.class, FirebirdConnectionProtocolVersion.class, FirebirdFetchStatementCache.class,
        FirebirdBatchStatementManager.class
})
class FirebirdConnectionResourceManagerTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock
    private FirebirdTransactionIdGenerator transactionIdGenerator;
    
    @Mock
    private FirebirdStatementIdGenerator statementIdGenerator;
    
    @Mock
    private FirebirdBlobIdGenerator blobIdGenerator;
    
    @Mock
    private FirebirdBlobHandleGenerator blobHandleGenerator;
    
    @Mock
    private FirebirdBlobWriteCache blobWriteCache;
    
    @Mock
    private FirebirdBlobReadCache blobReadCache;
    
    @Mock
    private FirebirdConnectionProtocolVersion connectionProtocolVersion;
    
    @Mock
    private FirebirdFetchStatementCache fetchStatementCache;
    
    @Mock
    private FirebirdBatchStatementManager batchStatementManager;
    
    @BeforeEach
    void setUp() {
        when(FirebirdTransactionIdGenerator.getInstance()).thenReturn(transactionIdGenerator);
        when(FirebirdStatementIdGenerator.getInstance()).thenReturn(statementIdGenerator);
        when(FirebirdBlobIdGenerator.getInstance()).thenReturn(blobIdGenerator);
        when(FirebirdBlobHandleGenerator.getInstance()).thenReturn(blobHandleGenerator);
        when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
        when(FirebirdBlobReadCache.getInstance()).thenReturn(blobReadCache);
        when(FirebirdConnectionProtocolVersion.getInstance()).thenReturn(connectionProtocolVersion);
        when(FirebirdFetchStatementCache.getInstance()).thenReturn(fetchStatementCache);
        when(FirebirdBatchStatementManager.getInstance()).thenReturn(batchStatementManager);
    }
    
    @Test
    void assertRegisterConnection() {
        FirebirdConnectionResourceManager.getInstance().registerConnection(CONNECTION_ID);
        InOrder inOrder = inOrder(transactionIdGenerator, statementIdGenerator, blobIdGenerator, blobHandleGenerator, blobWriteCache, blobReadCache,
                fetchStatementCache, batchStatementManager);
        inOrder.verify(transactionIdGenerator).registerConnection(CONNECTION_ID);
        inOrder.verify(statementIdGenerator).registerConnection(CONNECTION_ID);
        inOrder.verify(blobIdGenerator).registerConnection(CONNECTION_ID);
        inOrder.verify(blobHandleGenerator).registerConnection(CONNECTION_ID);
        inOrder.verify(blobWriteCache).registerConnection(CONNECTION_ID);
        inOrder.verify(blobReadCache).registerConnection(CONNECTION_ID);
        inOrder.verify(fetchStatementCache).registerConnection(CONNECTION_ID);
        inOrder.verify(batchStatementManager).registerConnection(CONNECTION_ID);
    }
    
    @Test
    void assertUnregisterConnection() {
        byte[] expectedBlobContent = new byte[]{1};
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(payload.getConnectionId()).thenReturn(CONNECTION_ID);
        new FirebirdBlobBinaryProtocolValue().write(payload, expectedBlobContent);
        ArgumentCaptor<Long> blobIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(payload).writeInt8(blobIdCaptor.capture());
        assertArrayEquals(expectedBlobContent, FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobIdCaptor.getValue()));
        FirebirdConnectionResourceManager.getInstance().unregisterConnection(CONNECTION_ID);
        InOrder inOrder = inOrder(statementIdGenerator, transactionIdGenerator, blobIdGenerator, blobHandleGenerator, blobWriteCache, blobReadCache,
                connectionProtocolVersion, fetchStatementCache, batchStatementManager);
        inOrder.verify(statementIdGenerator).unregisterConnection(CONNECTION_ID);
        inOrder.verify(transactionIdGenerator).unregisterConnection(CONNECTION_ID);
        inOrder.verify(blobIdGenerator).unregisterConnection(CONNECTION_ID);
        inOrder.verify(blobHandleGenerator).unregisterConnection(CONNECTION_ID);
        inOrder.verify(blobWriteCache).unregisterConnection(CONNECTION_ID);
        inOrder.verify(blobReadCache).unregisterConnection(CONNECTION_ID);
        inOrder.verify(connectionProtocolVersion).unsetProtocolVersion(CONNECTION_ID);
        inOrder.verify(fetchStatementCache).unregisterConnection(CONNECTION_ID);
        inOrder.verify(batchStatementManager).unregisterConnection(CONNECTION_ID);
        assertNull(FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobIdCaptor.getValue()));
    }
}

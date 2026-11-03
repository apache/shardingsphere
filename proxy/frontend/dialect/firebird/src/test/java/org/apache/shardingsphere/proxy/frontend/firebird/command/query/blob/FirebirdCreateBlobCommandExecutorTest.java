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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCreateBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdCreateBlobCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock
    private FirebirdCreateBlobCommandPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        FirebirdBlobIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        lenient().when(packet.getTransactionId()).thenReturn(1);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() {
        FirebirdCreateBlobCommandExecutor executor = new FirebirdCreateBlobCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) response).getHandle(), is(3));
        assertThat(((FirebirdGenericResponsePacket) response).getId(), is(1L));
    }
    
    @Test
    void assertExecuteWithStreamBlob() {
        when(packet.isStreamBlob()).thenReturn(true);
        FirebirdCreateBlobCommandExecutor executor = new FirebirdCreateBlobCommandExecutor(packet, connectionSession);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) executor.execute().iterator().next();
        assertTrue(FirebirdBlobWriteCache.getInstance().isStreamBlob(CONNECTION_ID, 1, actual.getId()));
    }
    
    @Test
    void assertSeparateTransactionsCanAllocateTheSameBlobId() {
        when(packet.getTransactionId()).thenReturn(1, 2);
        FirebirdCreateBlobCommandExecutor executor = new FirebirdCreateBlobCommandExecutor(packet, connectionSession);
        FirebirdGenericResponsePacket actualFirst = (FirebirdGenericResponsePacket) executor.execute().iterator().next();
        FirebirdGenericResponsePacket actualSecond = (FirebirdGenericResponsePacket) executor.execute().iterator().next();
        assertThat(actualFirst.getId(), is(1L));
        assertThat(actualSecond.getId(), is(1L));
    }
    
    @Test
    void assertExecuteWithInvalidTransaction() {
        when(packet.getTransactionId()).thenReturn(32766, 1);
        FirebirdCreateBlobCommandExecutor executor = new FirebirdCreateBlobCommandExecutor(packet, connectionSession);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) executor.execute().iterator().next();
        assertThat(actual.getStatusVector().getSegments().size(), is(1));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_trans_handle));
        assertTrue(actual.getStatusVector().getSegments().get(0).getArguments().isEmpty());
        FirebirdGenericResponsePacket actualNext = (FirebirdGenericResponsePacket) executor.execute().iterator().next();
        assertThat(actualNext.getHandle(), is(3));
        assertThat(actualNext.getId(), is(1L));
    }
}

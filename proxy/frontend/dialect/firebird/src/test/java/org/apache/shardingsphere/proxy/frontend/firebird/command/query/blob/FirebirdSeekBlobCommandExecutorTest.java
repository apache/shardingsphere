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

import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrTypeException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdSeekBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdSeekBlobCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int BLOB_HANDLE = 7;
    
    private static final int SEEK_MODE_FROM_TAIL = 2;
    
    @Mock
    private FirebirdSeekBlobCommandPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithStreamBlob() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3, 4}, true);
        when(packet.getBlobHandle()).thenReturn(BLOB_HANDLE);
        when(packet.getOffset()).thenReturn(3);
        Collection<DatabasePacket> actualPackets = new FirebirdSeekBlobCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actualPackets.iterator().next()).getId(), is(3L));
        assertThat(FirebirdBlobReadCache.getInstance().readSegment(CONNECTION_ID, BLOB_HANDLE, 4).get().getData(), is(new byte[]{4}));
    }
    
    @Test
    void assertExecuteWithOffsetBeyondBlobContent() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2}, true);
        when(packet.getBlobHandle()).thenReturn(BLOB_HANDLE);
        when(packet.getOffset()).thenReturn(9);
        Collection<DatabasePacket> actualPackets = new FirebirdSeekBlobCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actualPackets.iterator().next()).getId(), is(2L));
    }
    
    @Test
    void assertExecuteWithDeferredPlaceholderHandle() {
        int blobHandle = FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, blobHandle, new byte[]{1, 2, 3}, true);
        when(packet.getBlobHandle()).thenReturn(0xFFFF);
        when(packet.getSeekMode()).thenReturn(SEEK_MODE_FROM_TAIL);
        when(packet.getOffset()).thenReturn(-1);
        Collection<DatabasePacket> actualPackets = new FirebirdSeekBlobCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actualPackets.iterator().next()).getId(), is(2L));
    }
    
    @Test
    void assertExecuteWithFullyReadBlob() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2}, true);
        FirebirdBlobReadCache.getInstance().readSegment(CONNECTION_ID, BLOB_HANDLE, 2);
        when(packet.getBlobHandle()).thenReturn(BLOB_HANDLE);
        Collection<DatabasePacket> actualPackets = new FirebirdSeekBlobCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actualPackets.iterator().next()).getId(), is(0L));
        assertThat(FirebirdBlobReadCache.getInstance().readSegment(CONNECTION_ID, BLOB_HANDLE, 2).get().getData(), is(new byte[]{1, 2}));
    }
    
    @Test
    void assertExecuteWithSegmentedBlob() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2}, false);
        when(packet.getBlobHandle()).thenReturn(BLOB_HANDLE);
        FirebirdSeekBlobCommandExecutor executor = new FirebirdSeekBlobCommandExecutor(packet, connectionSession);
        InvalidSegstrTypeException actual = assertThrows(InvalidSegstrTypeException.class, executor::execute);
        assertThat(actual.getBlobHandle(), is(BLOB_HANDLE));
    }
    
    @Test
    void assertExecuteWithUnknownHandle() {
        when(packet.getBlobHandle()).thenReturn(BLOB_HANDLE);
        FirebirdSeekBlobCommandExecutor executor = new FirebirdSeekBlobCommandExecutor(packet, connectionSession);
        InvalidSegstrHandleException actual = assertThrows(InvalidSegstrHandleException.class, executor::execute);
        assertThat(actual.getBlobHandle(), is(BLOB_HANDLE));
    }
}

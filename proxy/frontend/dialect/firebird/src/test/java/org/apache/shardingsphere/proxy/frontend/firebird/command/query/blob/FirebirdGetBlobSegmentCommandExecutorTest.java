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

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentResponsePacket;
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
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdGetBlobSegmentCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int BLOB_HANDLE = 7;
    
    @Mock
    private FirebirdGetBlobSegmentCommandPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getBlobHandle()).thenReturn(BLOB_HANDLE);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithDeferredPlaceholderHandleExpectsLastOpenedBlob() {
        int blobHandle = FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, blobHandle, new byte[]{7, 8, 9});
        when(packet.getBlobHandle()).thenReturn(0xFFFF);
        when(packet.getSegmentLength()).thenReturn(16386);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualPackets.iterator().next();
        assertThat(actualGenericPacket.getHandle(), is(0));
        assertThat(getResponseSegment(actualGenericPacket), is(new byte[]{7, 8, 9}));
    }
    
    @Test
    void assertExecuteWithUnknownBlobExpectsEof() {
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualPackets.iterator().next();
        assertThat(actualGenericPacket.getHandle(), is(2));
        assertNull(actualGenericPacket.getData());
    }
    
    @Test
    void assertExecuteWithZeroRequestedLengthExpectsPartial() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{9, 8});
        when(packet.getSegmentLength()).thenReturn(0);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualPackets.iterator().next();
        assertThat(actualGenericPacket.getHandle(), is(1));
        assertThat(getResponseSegment(actualGenericPacket).length, is(0));
        byte[] actualRemainingSegment = FirebirdBlobReadCache.getInstance().getSegment(CONNECTION_ID, BLOB_HANDLE).orElse(new byte[0]);
        assertThat(actualRemainingSegment, is(new byte[]{9, 8}));
    }
    
    @Test
    void assertExecuteWithPartialSegmentLeftExpectsPartial() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3});
        when(packet.getSegmentLength()).thenReturn(4);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualPackets.iterator().next();
        assertThat(actualGenericPacket.getHandle(), is(1));
        assertThat(actualGenericPacket.getData(), isA(FirebirdGetBlobSegmentResponsePacket.class));
        assertThat(getResponseSegment(actualGenericPacket), is(new byte[]{1, 2}));
        byte[] actualRemainingSegment = FirebirdBlobReadCache.getInstance().getSegment(CONNECTION_ID, BLOB_HANDLE).orElse(new byte[0]);
        assertThat(actualRemainingSegment, is(new byte[]{3}));
    }
    
    @Test
    void assertExecuteWithFullyConsumedSegmentExpectsComplete() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{4, 5});
        when(packet.getSegmentLength()).thenReturn(4);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualPackets.iterator().next();
        assertThat(actualGenericPacket.getHandle(), is(0));
        assertThat(getResponseSegment(actualGenericPacket), is(new byte[]{4, 5}));
        assertFalse(FirebirdBlobReadCache.getInstance().getSegment(CONNECTION_ID, BLOB_HANDLE).isPresent());
    }
    
    @Test
    void assertExecuteWithRequestedLengthOverMaxSegmentDataLengthExpectsCappedSegment() {
        byte[] content = new byte[0xFFFF + 1];
        content[0xFFFF] = 42;
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, content);
        when(packet.getSegmentLength()).thenReturn(0xFFFF + 100);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualPackets.iterator().next();
        assertThat(actualGenericPacket.getHandle(), is(1));
        assertThat(getResponseSegment(actualGenericPacket).length, is(0xFFFF));
        byte[] actualRemainingSegment = FirebirdBlobReadCache.getInstance().getSegment(CONNECTION_ID, BLOB_HANDLE).orElse(new byte[0]);
        assertThat(actualRemainingSegment, is(new byte[]{42}));
    }
    
    @Test
    void assertExecuteWithOtherConnectionBlobExpectsEof() {
        FirebirdBlobReadCache.getInstance().registerConnection(2);
        FirebirdBlobReadCache.getInstance().registerBlob(2, BLOB_HANDLE, new byte[]{1, 2, 3});
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        FirebirdGenericResponsePacket actualGenericPacket = (FirebirdGenericResponsePacket) actualPackets.iterator().next();
        assertThat(actualGenericPacket.getHandle(), is(2));
        assertThat(FirebirdBlobReadCache.getInstance().getSegment(2, BLOB_HANDLE).orElse(new byte[0]), is(new byte[]{1, 2, 3}));
        FirebirdBlobReadCache.getInstance().unregisterConnection(2);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getResponseSegment(final FirebirdGenericResponsePacket responsePacket) {
        return (byte[]) Plugins.getMemberAccessor().get(FirebirdGetBlobSegmentResponsePacket.class.getDeclaredField("segment"), responsePacket.getData());
    }
}

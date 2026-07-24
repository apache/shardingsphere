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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdBlobRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdGetBlobSegmentCommandExecutor;
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
    void setup() {
        FirebirdBlobRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getBlobHandle()).thenReturn(BLOB_HANDLE);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithMissingBlobState() {
        when(packet.getSegmentLength()).thenReturn(3);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) response;
        assertThat(actualResponse.getHandle(), is(2));
        assertThat(getResponseSegment(actualResponse).length, is(0));
    }
    
    @Test
    void assertExecuteWithSegmentRemaining() {
        FirebirdBlobRegistry.getInstance().openBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3});
        when(packet.getSegmentLength()).thenReturn(2);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) response;
        assertThat(actualResponse.getHandle(), is(0));
        byte[] actualSegment = getResponseSegment(actualResponse);
        assertThat(actualSegment, is(new byte[]{1, 2}));
    }
    
    @Test
    void assertExecuteWithEndOfBlob() {
        FirebirdBlobRegistry.getInstance().openBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{4, 5});
        when(packet.getSegmentLength()).thenReturn(4);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) response;
        assertThat(actualResponse.getHandle(), is(2));
        byte[] actualSegment = getResponseSegment(actualResponse);
        assertThat(actualSegment, is(new byte[]{4, 5}));
    }
    
    @Test
    void assertExecuteWithDeferredHandle() {
        FirebirdBlobRegistry.getInstance().openBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3});
        FirebirdBlobRegistry.getInstance().setLastBlobHandle(CONNECTION_ID, BLOB_HANDLE);
        when(packet.getBlobHandle()).thenReturn(0xFFFF);
        when(packet.getSegmentLength()).thenReturn(2);
        FirebirdGetBlobSegmentCommandExecutor executor = new FirebirdGetBlobSegmentCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) response;
        assertThat(actualResponse.getHandle(), is(0));
        assertThat(getResponseSegment(actualResponse), is(new byte[]{1, 2}));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private byte[] getResponseSegment(final FirebirdGenericResponsePacket responsePacket) {
        return (byte[]) Plugins.getMemberAccessor().get(FirebirdGetBlobSegmentResponsePacket.class.getDeclaredField("segment"), responsePacket.getData());
    }
}

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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBlobInfoExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int BLOB_HANDLE = 2;
    
    @Mock
    private FirebirdInfoPacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getHandle()).thenReturn(BLOB_HANDLE);
        when(packet.getInfoItems()).thenReturn(Collections.emptyList());
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithReadBlob() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3});
        Collection<DatabasePacket> actual = new FirebirdBlobInfoExecutor(packet, connectionSession).execute();
        assertThat(getBlobLength(actual), is(3));
    }
    
    @Test
    void assertExecuteWithPartiallyReadBlob() {
        FirebirdBlobReadCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3});
        FirebirdBlobReadCache.getInstance().readSegment(CONNECTION_ID, BLOB_HANDLE, 1);
        Collection<DatabasePacket> actual = new FirebirdBlobInfoExecutor(packet, connectionSession).execute();
        assertThat(getBlobLength(actual), is(2));
    }
    
    @Test
    void assertExecuteWithWriteBlob() {
        FirebirdBlobWriteCache.getInstance().registerBlob(CONNECTION_ID, BLOB_HANDLE, 3L);
        FirebirdBlobWriteCache.getInstance().appendSegment(CONNECTION_ID, BLOB_HANDLE, new byte[]{1, 2, 3, 4});
        Collection<DatabasePacket> actual = new FirebirdBlobInfoExecutor(packet, connectionSession).execute();
        assertThat(getBlobLength(actual), is(4));
    }
    
    @Test
    void assertExecuteWithMissingBlob() {
        Collection<DatabasePacket> actual = new FirebirdBlobInfoExecutor(packet, connectionSession).execute();
        assertThat(getBlobLength(actual), is(0));
    }
    
    private int getBlobLength(final Collection<DatabasePacket> actual) {
        FirebirdGenericResponsePacket actualResponsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        return ((FirebirdBlobInfoReturnPacket) actualResponsePacket.getData()).getBlobLength();
    }
}

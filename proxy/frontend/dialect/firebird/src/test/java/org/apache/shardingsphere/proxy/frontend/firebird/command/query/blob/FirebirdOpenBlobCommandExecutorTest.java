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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrIdException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCloseBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCreateBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdOpenBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdPutBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBlobBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache.BlobSegment;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdOpenBlobCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock
    private FirebirdOpenBlobCommandPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        FirebirdStatementIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobBinaryProtocolValue.unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() {
        long blobId = registerBlobContent(new byte[]{1, 2});
        when(packet.getBlobId()).thenReturn(blobId);
        FirebirdOpenBlobCommandExecutor executor = new FirebirdOpenBlobCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) response).getHandle(), is(1));
        assertThat(((FirebirdGenericResponsePacket) response).getId(), is(blobId));
    }
    
    @Test
    void assertExecuteWithUnknownBlobId() {
        when(packet.getBlobId()).thenReturn(99L);
        FirebirdOpenBlobCommandExecutor executor = new FirebirdOpenBlobCommandExecutor(packet, connectionSession);
        assertThrows(InvalidSegstrIdException.class, executor::execute);
    }
    
    @Test
    void assertExecuteWithZeroBlobId() {
        when(packet.getBlobId()).thenReturn(0L);
        FirebirdOpenBlobCommandExecutor executor = new FirebirdOpenBlobCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) response).getHandle(), is(1));
        assertThat(((FirebirdGenericResponsePacket) response).getId(), is(0L));
    }
    
    @Test
    void assertExecuteAfterCreatedBlobIsClosed() {
        FirebirdGenericResponsePacket createdResponse = createBlobWithSegment(new byte[]{1, 2, 3});
        FirebirdCloseBlobCommandPacket closePacket = mock(FirebirdCloseBlobCommandPacket.class);
        when(closePacket.getBlobHandle()).thenReturn(createdResponse.getHandle());
        new FirebirdCloseBlobCommandExecutor(closePacket, connectionSession).execute();
        when(packet.getBlobId()).thenReturn(createdResponse.getId());
        Collection<DatabasePacket> actual = new FirebirdOpenBlobCommandExecutor(packet, connectionSession).execute();
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(actualResponse.getId(), is(createdResponse.getId()));
        Optional<BlobSegment> actualSegment = FirebirdBlobReadCache.getInstance().readSegment(CONNECTION_ID, actualResponse.getHandle(), 3);
        assertTrue(actualSegment.isPresent());
        assertThat(actualSegment.get().getData(), is(new byte[]{1, 2, 3}));
    }
    
    @Test
    void assertExecuteWhenCreatedBlobIsNotClosed() {
        FirebirdGenericResponsePacket createdResponse = createBlobWithSegment(new byte[]{1, 2, 3});
        when(packet.getBlobId()).thenReturn(createdResponse.getId());
        FirebirdOpenBlobCommandExecutor executor = new FirebirdOpenBlobCommandExecutor(packet, connectionSession);
        assertThrows(InvalidSegstrIdException.class, executor::execute);
    }
    
    @Test
    void assertExecuteWithUnknownResultBlobId() {
        when(packet.getBlobId()).thenReturn(-99L);
        FirebirdOpenBlobCommandExecutor executor = new FirebirdOpenBlobCommandExecutor(packet, connectionSession);
        assertThrows(InvalidSegstrIdException.class, executor::execute);
    }
    
    private FirebirdGenericResponsePacket createBlobWithSegment(final byte[] segment) {
        FirebirdGenericResponsePacket result = (FirebirdGenericResponsePacket) new FirebirdCreateBlobCommandExecutor(
                mock(FirebirdCreateBlobCommandPacket.class), connectionSession).execute().iterator().next();
        FirebirdPutBlobSegmentCommandPacket putPacket = mock(FirebirdPutBlobSegmentCommandPacket.class);
        when(putPacket.getBlobHandle()).thenReturn(result.getHandle());
        when(putPacket.getSegment()).thenReturn(segment);
        new FirebirdPutBlobSegmentCommandExecutor(putPacket, connectionSession).execute();
        return result;
    }
    
    private long registerBlobContent(final byte[] content) {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        payload.setConnectionId(CONNECTION_ID);
        new FirebirdBlobBinaryProtocolValue().write(payload, content);
        return byteBuf.getLong(0);
    }
}

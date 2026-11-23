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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdSingleHandleCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCreateAndOpenBlobCommandPacket;
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
class FirebirdCreateAndOpenBlobCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock
    private FirebirdCreateAndOpenBlobCommandPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        FirebirdBlobIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobReadCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobBinaryProtocolValue.unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithCreateBlob() {
        FirebirdCreateAndOpenBlobCommandExecutor executor = new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.CREATE_BLOB);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) response).getHandle(), is(1));
        assertThat(((FirebirdGenericResponsePacket) response).getId(), is(1L));
    }
    
    @Test
    void assertExecuteWithCreateBlob2AndStreamType() {
        when(packet.isStreamBlob()).thenReturn(true);
        FirebirdCreateAndOpenBlobCommandExecutor executor = new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.CREATE_BLOB2);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) executor.execute().iterator().next();
        assertTrue(FirebirdBlobWriteCache.getInstance().isStreamBlob(CONNECTION_ID, actual.getId()));
    }
    
    @Test
    void assertExecuteWithOpenBlob() {
        long blobId = registerBlobContent(new byte[]{1, 2});
        when(packet.getBlobId()).thenReturn(blobId);
        FirebirdCreateAndOpenBlobCommandExecutor executor = new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB);
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
        FirebirdCreateAndOpenBlobCommandExecutor executor = new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB);
        assertThrows(InvalidSegstrIdException.class, executor::execute);
    }
    
    @Test
    void assertExecuteWithZeroBlobId() {
        FirebirdCreateAndOpenBlobCommandExecutor executor = new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        DatabasePacket response = actual.iterator().next();
        assertThat(response, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) response).getHandle(), is(1));
        assertThat(((FirebirdGenericResponsePacket) response).getId(), is(0L));
    }
    
    @Test
    void assertExecuteAfterCreatedBlobIsClosed() {
        FirebirdGenericResponsePacket createdResponse = createBlobWithSegment(new byte[]{1, 2, 3}, false);
        FirebirdSingleHandleCommandPacket closePacket = mock(FirebirdSingleHandleCommandPacket.class);
        when(closePacket.getObjectHandle()).thenReturn(createdResponse.getHandle());
        new FirebirdCancelAndCloseBlobCommandExecutor(closePacket, connectionSession, FirebirdCommandPacketType.CLOSE_BLOB).execute();
        when(packet.getBlobId()).thenReturn(createdResponse.getId());
        Collection<DatabasePacket> actual = new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB2).execute();
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(actualResponse.getId(), is(createdResponse.getId()));
        Optional<BlobSegment> actualSegment = FirebirdBlobReadCache.getInstance().readSegment(CONNECTION_ID, actualResponse.getHandle(), 3);
        assertTrue(actualSegment.isPresent());
        assertThat(actualSegment.get().getData(), is(new byte[]{1, 2, 3}));
    }
    
    @Test
    void assertExecuteWhenCreatedBlobIsNotClosed() {
        FirebirdGenericResponsePacket createdResponse = createBlobWithSegment(new byte[]{1, 2, 3}, false);
        when(packet.getBlobId()).thenReturn(createdResponse.getId());
        FirebirdCreateAndOpenBlobCommandExecutor executor = new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB);
        assertThrows(InvalidSegstrIdException.class, executor::execute);
    }
    
    @Test
    void assertExecuteWithResultBlobId() {
        when(packet.getBlobId()).thenReturn(registerBlobContent(new byte[]{1, 2}));
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB2)
                .execute().iterator().next();
        assertThat(FirebirdBlobReadCache.getInstance().isStreamBlob(CONNECTION_ID, actual.getHandle()), is(Optional.of(true)));
    }
    
    @Test
    void assertExecuteWithZeroBlobIdOpensSegmentedBlob() {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB)
                .execute().iterator().next();
        assertThat(FirebirdBlobReadCache.getInstance().isStreamBlob(CONNECTION_ID, actual.getHandle()), is(Optional.of(false)));
    }
    
    @Test
    void assertExecuteRestoresCreatedStreamBlobType() {
        FirebirdGenericResponsePacket createdResponse = createBlobWithSegment(new byte[]{1, 2, 3}, true);
        FirebirdSingleHandleCommandPacket closePacket = mock(FirebirdSingleHandleCommandPacket.class);
        when(closePacket.getObjectHandle()).thenReturn(createdResponse.getHandle());
        new FirebirdCancelAndCloseBlobCommandExecutor(closePacket, connectionSession, FirebirdCommandPacketType.CLOSE_BLOB).execute();
        when(packet.getBlobId()).thenReturn(createdResponse.getId());
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB2)
                .execute().iterator().next();
        assertThat(FirebirdBlobReadCache.getInstance().isStreamBlob(CONNECTION_ID, actual.getHandle()), is(Optional.of(true)));
    }
    
    @Test
    void assertExecuteWithUnknownResultBlobId() {
        when(packet.getBlobId()).thenReturn(-99L);
        FirebirdCreateAndOpenBlobCommandExecutor executor = new FirebirdCreateAndOpenBlobCommandExecutor(packet, connectionSession, FirebirdCommandPacketType.OPEN_BLOB);
        assertThrows(InvalidSegstrIdException.class, executor::execute);
    }
    
    private FirebirdGenericResponsePacket createBlobWithSegment(final byte[] segment, final boolean streamBlob) {
        FirebirdCreateAndOpenBlobCommandPacket createPacket = mock(FirebirdCreateAndOpenBlobCommandPacket.class);
        when(createPacket.isStreamBlob()).thenReturn(streamBlob);
        FirebirdGenericResponsePacket result = (FirebirdGenericResponsePacket) new FirebirdCreateAndOpenBlobCommandExecutor(createPacket, connectionSession, FirebirdCommandPacketType.CREATE_BLOB)
                .execute().iterator().next();
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

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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchWithoutBlobsException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchBlobPolicyException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchBlobStreamCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBatchBlobStreamCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 2;
    
    private final FirebirdBatchStatementManager manager = FirebirdBatchStatementManager.getInstance();
    
    @Mock
    private FirebirdBatchBlobStreamCommandPacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        manager.registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
    }
    
    @AfterEach
    void tearDown() {
        manager.unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, createBlobFields(), 64L, false, false, true);
        ByteBuf data = Unpooled.buffer().writeLong(1L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3});
        when(packet.getData()).thenReturn(data);
        when(packet.getLength()).thenReturn(20L);
        Collection<DatabasePacket> actual = new FirebirdBatchBlobStreamCommandExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        FirebirdBatchStatement actualBatchStatement = manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertThat(actualBatchStatement.getBlobStreamSize(), is(20L));
        assertThat(actualBatchStatement.getBlobBuffer().size(), is(1));
    }
    
    private List<FirebirdBatchColumnDescriptor> createBlobFields() {
        ByteBuf blr = Unpooled.buffer().writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(2)
                .writeByte(BlrConstants.blr_blob2).writeZero(4).writeByte(BlrConstants.blr_short).writeByte(0).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        List<FirebirdBatchColumnDescriptor> result = FirebirdParseBatchBlr.parse(blr, blr.readableBytes()).getFields();
        blr.release();
        return result;
    }
    
    @Test
    void assertExecuteWithCumulativeBufferSizeExceeded() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, createBlobFields(), 16L, false, false, true);
        when(packet.getData()).thenReturn(Unpooled.buffer().writeLong(1L).writeInt(0).writeInt(0), Unpooled.buffer().writeLong(2L).writeInt(0).writeInt(0));
        when(packet.getLength()).thenReturn(16L);
        FirebirdBatchBlobStreamCommandExecutor executor = new FirebirdBatchBlobStreamCommandExecutor(packet, connectionSession);
        executor.execute();
        assertThrows(BatchTooBigException.class, executor::execute);
        FirebirdBatchStatement actualBatchStatement = manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertThat(actualBatchStatement.getBlobStreamSize(), is(16L));
        assertThat(actualBatchStatement.getBlobBuffer().size(), is(1));
    }
    
    @Test
    void assertExecuteWithUnknownBatch() {
        assertThrows(InvalidBatchHandleException.class, () -> new FirebirdBatchBlobStreamCommandExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertExecuteWithoutBlobPolicy() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, createBlobFields(), 64L, false, false, false);
        when(packet.getLength()).thenReturn(16L);
        assertThrows(InvalidBatchBlobPolicyException.class, () -> new FirebirdBatchBlobStreamCommandExecutor(packet, connectionSession).execute());
        FirebirdBatchStatement actualBatchStatement = manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertTrue(actualBatchStatement.getBlobBuffer().isEmpty());
        assertThat(actualBatchStatement.getBlobStreamSize(), is(0L));
    }
    
    @Test
    void assertExecuteWithEmptyStreamWithoutBlobPolicy() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID,
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 64L, false, false, false);
        when(packet.getLength()).thenReturn(0L);
        Collection<DatabasePacket> actual = new FirebirdBatchBlobStreamCommandExecutor(packet, connectionSession).execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        FirebirdBatchStatement actualBatchStatement = manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertTrue(actualBatchStatement.getBlobBuffer().isEmpty());
        assertThat(actualBatchStatement.getBlobStreamSize(), is(0L));
    }
    
    @Test
    void assertExecuteWithNonemptyStreamWithoutBlobFields() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID,
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 64L, false, false, true);
        when(packet.getLength()).thenReturn(16L);
        assertThrows(BatchWithoutBlobsException.class, () -> new FirebirdBatchBlobStreamCommandExecutor(packet, connectionSession).execute());
        assertTrue(manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID).getBlobBuffer().isEmpty());
    }
    
    @Test
    void assertExecuteWithoutBlobFieldsAndPolicy() {
        manager.registerBatchStatement(CONNECTION_ID, STATEMENT_ID,
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 64L, false, false, false);
        when(packet.getLength()).thenReturn(16L);
        assertThrows(BatchWithoutBlobsException.class, () -> new FirebirdBatchBlobStreamCommandExecutor(packet, connectionSession).execute());
        assertTrue(manager.getBatchStatement(CONNECTION_ID, STATEMENT_ID).getBlobBuffer().isEmpty());
    }
}

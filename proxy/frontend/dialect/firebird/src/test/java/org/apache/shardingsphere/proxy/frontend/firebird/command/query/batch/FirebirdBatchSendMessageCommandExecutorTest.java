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
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSendMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBatchSendMessageCommandExecutorTest {
    
    private static final int CONNECTION_ID = 3;
    
    private static final int STATEMENT_ID = 33;
    
    @Mock
    private FirebirdBatchSendMessageCommandPacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock
    private FirebirdBatchRegistry batchRegistry;
    
    @Mock
    private FirebirdBatchStatement batchStatement;
    
    @Test
    void assertExecute() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getDataLength()).thenReturn(8);
        when(batchStatement.getBufferSize()).thenReturn(16L);
        when(packet.readParameterValues(any())).thenReturn(Collections.singletonList(Collections.singletonList("foo")));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            Collection<DatabasePacket> actual = new FirebirdBatchSendMessageCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.size(), is(1));
            assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
            verify(batchStatement).addParameterValues(Collections.singletonList("foo"));
            verify(batchStatement).addSize(8);
        }
    }
    
    @Test
    void assertExecuteWhenBatchStatementMissing() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(null);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            assertThrows(FirebirdProtocolException.class, () -> new FirebirdBatchSendMessageCommandExecutor(packet, connectionSession).execute());
        }
    }
    
    @Test
    void assertExecuteWhenBatchTooBig() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getDataLength()).thenReturn(9);
        when(batchStatement.getAccumulatedSize()).thenReturn(8L);
        when(batchStatement.getBufferSize()).thenReturn(16L);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            assertThrows(BatchTooBigException.class, () -> new FirebirdBatchSendMessageCommandExecutor(packet, connectionSession).execute());
            verify(batchStatement, never()).addSize(9);
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteDoesNotStoreRejectedCoalescedBatchMessage() throws SQLException {
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        when(context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        when(context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get()).thenReturn(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(STATEMENT_ID,
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 8L);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try {
            List<Object> out = new LinkedList<>();
            new FirebirdPacketCodecEngine().decode(context, buildBatchMessages(), out);
            assertThat(out.size(), is(2));
            new FirebirdBatchSendMessageCommandExecutor(createBatchSendMessagePacket((ByteBuf) out.get(0)), connectionSession).execute();
            assertThat(batchStatement.getParameterValues().size(), is(1));
            assertThat(batchStatement.getParameterValues().get(0), is(Collections.singletonList(100)));
            assertThat(batchStatement.getAccumulatedSize(), is(8L));
            assertThrows(BatchTooBigException.class, () -> new FirebirdBatchSendMessageCommandExecutor(createBatchSendMessagePacket((ByteBuf) out.get(1)), connectionSession).execute());
            assertTrue(batchStatement.getParameterValues().isEmpty());
            assertThat(batchStatement.getAccumulatedSize(), is(0L));
        } finally {
            FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    private FirebirdBatchSendMessageCommandPacket createBatchSendMessagePacket(final ByteBuf byteBuf) {
        return new FirebirdBatchSendMessageCommandPacket(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8));
    }
    
    private ByteBuf buildBatchMessages() {
        ByteBuf result = Unpooled.buffer();
        result.writeBytes(buildBatchMessage(100));
        result.writeBytes(buildBatchMessage(200));
        return result;
    }
    
    private ByteBuf buildBatchMessage(final int value) {
        return Unpooled.buffer()
                .writeInt(FirebirdCommandPacketType.BATCH_MSG.getValue())
                .writeInt(STATEMENT_ID)
                .writeInt(1)
                .writeByte(0)
                .writeZero(3)
                .writeInt(value);
    }
}

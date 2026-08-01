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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBatchCommandPacketTest {
    
    private static final int CONNECTION_ID = 66;
    
    private static final int STATEMENT_ID = 7;
    
    @Test
    void assertBatchExecuteCommandPacket() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(4).writeInt(21).writeInt(31), StandardCharsets.UTF_8);
        FirebirdBatchExecuteCommandPacket packet = new FirebirdBatchExecuteCommandPacket(payload);
        assertThat(packet.getStatementHandle(), is(21));
        assertThat(packet.getTransactionHandle(), is(31));
        assertThat(FirebirdBatchExecuteCommandPacket.getLength(), is(12));
    }
    
    @Test
    void assertBatchCancelCommandPacket() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(4).writeInt(9), StandardCharsets.UTF_8);
        FirebirdBatchCancelCommandPacket packet = new FirebirdBatchCancelCommandPacket(payload);
        assertThat(packet.getStatementHandle(), is(9));
        assertThat(FirebirdBatchCancelCommandPacket.getLength(), is(8));
    }
    
    @Test
    void assertBatchSendMessageGetLengthWhenHeaderIncomplete() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(8), StandardCharsets.UTF_8);
        assertThat(FirebirdBatchMessageCommandPacket.getLength(payload, 1), is(-1));
    }
    
    @Test
    void assertBatchSendMessageGetLengthWithoutBatchStatement() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer().writeZero(4).writeInt(1).writeInt(1), StandardCharsets.UTF_8);
        assertThrows(FirebirdProtocolException.class, () -> FirebirdBatchMessageCommandPacket.getLength(payload, CONNECTION_ID));
    }
    
    @Test
    void assertBatchSendMessageGetLengthChecksDataSizeOnly() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(STATEMENT_ID, Collections.singletonList(longDescriptor()), 8L);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try {
            assertThat(FirebirdBatchMessageCommandPacket.getLength(createBatchMessagePayload(), CONNECTION_ID), is(20));
            assertTrue(batchStatement.getParameterValues().isEmpty());
        } finally {
            FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertBatchSendMessageGetLengthIgnoresBatchBufferSize() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(STATEMENT_ID, Collections.singletonList(longDescriptor()), 8L);
        batchStatement.addSize(1L);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try {
            assertThat(FirebirdBatchMessageCommandPacket.getLength(createBatchMessagePayload(), CONNECTION_ID), is(20));
            assertTrue(batchStatement.getParameterValues().isEmpty());
        } finally {
            FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertReadText() {
        ByteBuf data = Unpooled.buffer().writeByte(0).writeZero(3);
        data.writeCharSequence("abc", StandardCharsets.UTF_8);
        data.writeByte(0);
        assertThat(createBatchSendMessagePacket(data)
                .readParameterValues(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.TEXT, 3, 4, 0))),
                is(Collections.singletonList(Collections.singletonList("abc"))));
    }
    
    @Test
    void assertReadVarying() {
        ByteBuf data = Unpooled.buffer().writeByte(0).writeZero(3).writeInt(3);
        data.writeCharSequence("abc", StandardCharsets.UTF_8);
        data.writeByte(0);
        assertThat(createBatchSendMessagePacket(data)
                .readParameterValues(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.VARYING, 5, 4, 0))),
                is(Collections.singletonList(Collections.singletonList("abc"))));
    }
    
    @Test
    void assertReadMixedTypes() {
        ByteBuf data = Unpooled.buffer().writeByte(0).writeZero(3);
        data.writeCharSequence("abc", StandardCharsets.UTF_8);
        data.writeByte(0).writeInt(3);
        data.writeCharSequence("def", StandardCharsets.UTF_8);
        data.writeByte(0).writeInt(42);
        List<FirebirdBatchColumnDescriptor> descriptors = Arrays.asList(
                new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.TEXT, 3, 4, 0),
                new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.VARYING, 5, 4, 6), longDescriptor());
        assertThat(createBatchSendMessagePacket(data).readParameterValues(descriptors),
                is(Collections.singletonList(Arrays.asList("abc", "def", 42))));
    }
    
    private FirebirdPacketPayload createBatchMessagePayload() {
        return new FirebirdPacketPayload(Unpooled.buffer()
                .writeZero(4)
                .writeInt(STATEMENT_ID)
                .writeInt(1)
                .writeByte(0)
                .writeZero(3)
                .writeInt(1), StandardCharsets.UTF_8);
    }
    
    private FirebirdBatchMessageCommandPacket createBatchSendMessagePacket(final ByteBuf data) {
        return new FirebirdBatchMessageCommandPacket(new FirebirdPacketPayload(Unpooled.buffer()
                .writeZero(4).writeInt(STATEMENT_ID).writeInt(1).writeBytes(data), StandardCharsets.UTF_8));
    }
    
    private FirebirdBatchColumnDescriptor longDescriptor() {
        return new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0);
    }
}

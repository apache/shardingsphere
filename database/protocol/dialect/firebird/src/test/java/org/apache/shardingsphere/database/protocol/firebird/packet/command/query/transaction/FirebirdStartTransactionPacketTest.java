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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBuffer;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type.FirebirdTransactionParameterBufferType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class FirebirdStartTransactionPacketTest {
    
    @Test
    void assertIsAutoCommit() {
        assertTrue(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.AUTOCOMMIT)).isAutoCommit());
        assertFalse(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.READ)).isAutoCommit());
    }
    
    @Test
    void assertIsReadOnly() {
        assertTrue(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.READ)).isReadOnly());
        assertFalse(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.AUTOCOMMIT)).isReadOnly());
    }
    
    @Test
    void assertIsolationLevelReadCommitted() {
        assertThat(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.READ_COMMITTED)).getIsolationLevel(), is(TransactionIsolationLevel.READ_COMMITTED));
    }
    
    @Test
    void assertIsolationLevelRepeatableRead() {
        assertThat(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.CONCURRENCY)).getIsolationLevel(), is(TransactionIsolationLevel.REPEATABLE_READ));
    }
    
    @Test
    void assertIsolationLevelSerializable() {
        FirebirdStartTransactionPacket packet = new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.CONSISTENCY));
        getParameterBuffer(packet).put(FirebirdTransactionParameterBufferType.CONCURRENCY, false);
        assertThat(packet.getIsolationLevel(), is(TransactionIsolationLevel.SERIALIZABLE));
    }
    
    @Test
    void assertIsolationLevelNone() {
        FirebirdStartTransactionPacket packet = new FirebirdStartTransactionPacket(createPayload(1));
        Map<FirebirdTransactionParameterBufferType, Object> parameterBuffer = getParameterBuffer(packet);
        parameterBuffer.put(FirebirdTransactionParameterBufferType.CONCURRENCY, false);
        parameterBuffer.put(FirebirdTransactionParameterBufferType.CONSISTENCY, false);
        assertThat(packet.getIsolationLevel(), is(TransactionIsolationLevel.NONE));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<FirebirdTransactionParameterBufferType, Object> getParameterBuffer(final FirebirdStartTransactionPacket packet) {
        FirebirdParameterBuffer tpb = (FirebirdParameterBuffer) Plugins.getMemberAccessor().get(FirebirdStartTransactionPacket.class.getDeclaredField("tpb"), packet);
        return (Map<FirebirdTransactionParameterBufferType, Object>) Plugins.getMemberAccessor().get(FirebirdParameterBuffer.class.getDeclaredField("parameterBuffer"), tpb);
    }
    
    @Test
    void assertWrite() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class, RETURNS_DEEP_STUBS);
        assertDoesNotThrow(() -> new FirebirdStartTransactionPacket(payload).write(payload));
    }
    
    @Test
    void assertGetLength() {
        assertThat(FirebirdStartTransactionPacket.getLength(createPayload(1)), is(16));
    }
    
    private FirebirdPacketPayload createPayload(final int handle, final FirebirdTransactionParameterBufferType... types) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(0);
        byteBuf.writeInt(handle);
        ByteBuf tpbBuffer = Unpooled.buffer();
        tpbBuffer.writeByte(1);
        for (FirebirdTransactionParameterBufferType each : types) {
            tpbBuffer.writeByte(each.getCode());
        }
        int tpbLength = tpbBuffer.readableBytes();
        byteBuf.writeInt(tpbLength);
        byteBuf.writeBytes(tpbBuffer);
        byteBuf.writeZero((4 - tpbLength) & 3);
        return new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }
}

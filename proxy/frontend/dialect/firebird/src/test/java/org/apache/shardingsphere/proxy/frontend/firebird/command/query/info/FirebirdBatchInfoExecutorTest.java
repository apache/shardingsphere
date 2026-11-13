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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.batch.FirebirdBatchInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchStatementManager;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(FirebirdBatchStatementManager.class)
class FirebirdBatchInfoExecutorTest {
    
    @Mock
    private FirebirdInfoPacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdBatchStatementManager batchStatementManager;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(packet.getHandle()).thenReturn(7);
        when(FirebirdBatchStatementManager.getInstance()).thenReturn(batchStatementManager);
    }
    
    @Test
    void assertExecute() {
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(7, Collections.emptyList(), 1024L);
        when(batchStatementManager.getBatchStatement(1, 7)).thenReturn(batchStatement);
        when(packet.getInfoItems()).thenReturn(Collections.singletonList(FirebirdBatchInfoPacketType.BUFFER_BYTES_SIZE));
        when(packet.getMaxLength()).thenReturn(64);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdBatchInfoExecutor(packet, connectionSession).execute().iterator().next();
        ByteBuf actualData = Unpooled.buffer();
        actual.getData().write(new FirebirdPacketPayload(actualData, StandardCharsets.UTF_8));
        assertThat(ByteBufUtil.hexDump(actualData), is("0a04000004000001"));
    }
    
    @Test
    void assertExecuteWhenBatchNotFound() {
        assertThrows(InvalidBatchHandleException.class, () -> new FirebirdBatchInfoExecutor(packet, connectionSession).execute());
    }
}

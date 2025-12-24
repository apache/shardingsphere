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

package org.apache.shardingsphere.database.protocol.opengauss.packet.command;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLAggregatedCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenGaussCommandPacketFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PostgreSQLPacketPayload payload;
    
    @Test
    void assertNewOpenGaussComBatchBindPacket() {
        when(payload.getByteBuf()).thenReturn(mock(ByteBuf.class));
        assertThat(OpenGaussCommandPacketFactory.newInstance(OpenGaussCommandPacketType.BATCH_BIND_COMMAND, payload), isA(PostgreSQLAggregatedCommandPacket.class));
    }
    
    @Test
    void assertNewPostgreSQLPacket() {
        assertThat(OpenGaussCommandPacketFactory.newInstance(mock(PostgreSQLCommandPacketType.class), payload), isA(PostgreSQLCommandPacket.class));
    }
}

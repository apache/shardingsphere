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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLAggregatedCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLComTerminationPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLCommandPacketFactoryTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    public void assertNewInstanceWithQueryComPacket() {
        when(payload.getByteBuf()).thenReturn(mock(ByteBuf.class));
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.SIMPLE_QUERY, payload), instanceOf(PostgreSQLComQueryPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithParseComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.PARSE_COMMAND, payload), instanceOf(PostgreSQLAggregatedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithBindComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.BIND_COMMAND, payload), instanceOf(PostgreSQLAggregatedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithDescribeComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.DESCRIBE_COMMAND, payload), instanceOf(PostgreSQLAggregatedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithExecuteComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.EXECUTE_COMMAND, payload), instanceOf(PostgreSQLAggregatedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithSyncComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.SYNC_COMMAND, payload), instanceOf(PostgreSQLAggregatedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithCloseComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.CLOSE_COMMAND, payload), instanceOf(PostgreSQLAggregatedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithTerminationComPacket() {
        when(payload.getByteBuf()).thenReturn(mock(ByteBuf.class));
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.TERMINATE, payload), instanceOf(PostgreSQLComTerminationPacket.class));
    }
}

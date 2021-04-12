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

import org.apache.shardingsphere.db.protocol.postgresql.packet.command.admin.PostgreSQLUnsupportedCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.sync.PostgreSQLComSyncPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLComTerminationPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLCommandPacketFactoryTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    public void assertNewInstanceWithQueryComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.QUERY, payload, 1), instanceOf(PostgreSQLComQueryPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithParseComPacket() {
        when(payload.readInt4()).thenReturn(1);
        when(payload.readStringNul()).thenReturn("stat-id");
        when(payload.readStringNul()).thenReturn("SELECT * FROM t_order");
        when(payload.readInt2()).thenReturn(0);
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.PARSE, payload, 1), instanceOf(PostgreSQLComParsePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithBindComPacket() {
        when(payload.readInt4()).thenReturn(1);
        when(payload.readStringNul()).thenReturn("stat-id");
        when(payload.readStringNul()).thenReturn("SELECT * FROM t_order");
        when(payload.readInt2()).thenReturn(0);
        BinaryStatementRegistry.getInstance().register(1);
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.BIND, payload, 1), instanceOf(PostgreSQLComBindPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithDescribeComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.DESCRIBE, payload, 1), instanceOf(PostgreSQLComDescribePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithExecuteComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.EXECUTE, payload, 1), instanceOf(PostgreSQLComExecutePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithSyncComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.SYNC, payload, 1), instanceOf(PostgreSQLComSyncPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithTerminationComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.TERMINATE, payload, 1), instanceOf(PostgreSQLComTerminationPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithUnsupportedComPacket() {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.PORTAL_SUSPENDED, payload, 1), instanceOf(PostgreSQLUnsupportedCommandPacket.class));
    }
}

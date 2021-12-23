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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.describe;

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLParameterDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLPortal;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComDescribeExecutorTest {
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Mock
    private PostgreSQLComDescribePacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    public void assertDescribePortal() {
        when(packet.getType()).thenReturn('P');
        when(packet.getName()).thenReturn("P_1");
        PostgreSQLPortal portal = mock(PostgreSQLPortal.class);
        PostgreSQLRowDescriptionPacket expected = mock(PostgreSQLRowDescriptionPacket.class);
        when(portal.describe()).thenReturn(expected);
        when(connectionContext.getPortal("P_1")).thenReturn(portal);
        Collection<DatabasePacket<?>> actual = new PostgreSQLComDescribeExecutor(connectionContext, packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expected));
    }
    
    @Test
    public void assertDescribePreparedStatement() {
        when(packet.getType()).thenReturn('S');
        when(packet.getName()).thenReturn("S_1");
        when(connectionSession.getConnectionId()).thenReturn(1);
        PostgreSQLPreparedStatementRegistry.getInstance().register(1);
        PostgreSQLPreparedStatementRegistry.getInstance().register(1, "S_1", "", new PostgreSQLSelectStatement(), Collections.singletonList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4));
        Collection<DatabasePacket<?>> actual = new PostgreSQLComDescribeExecutor(connectionContext, packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actual.iterator().next();
        assertThat(actualParameterDescription, instanceOf(PostgreSQLParameterDescriptionPacket.class));
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(1);
        verify(mockPayload).writeInt4(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4.getValue());
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertDescribeUnknownType() {
        new PostgreSQLComDescribeExecutor(connectionContext, packet, connectionSession).execute();
    }
}

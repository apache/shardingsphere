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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.execute;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.execute.PostgreSQLPortalSuspendedPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.PostgreSQLPortal;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.tcl.PostgreSQLCommitStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComExecuteExecutorTest {
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Mock
    private PostgreSQLComExecutePacket packet;
    
    @Mock
    private PostgreSQLPortal portal;
    
    @Mock
    private QueryCommandExecutor queryCommandExecutor;
    
    @Mock
    private PostgreSQLPacket postgreSQLPacket;
    
    @Mock
    private PostgreSQLDataRowPacket dataRowPacket;
    
    @Before
    public void setup() {
        when(packet.getPortal()).thenReturn("");
        when(connectionContext.getPortal(anyString())).thenReturn(portal);
    }
    
    @Test
    public void assertExecuteQuery() throws SQLException {
        when(connectionContext.getPendingExecutors()).thenReturn(new ArrayList<>(Collections.singletonList(queryCommandExecutor)));
        when(queryCommandExecutor.execute()).thenReturn(Collections.singletonList(postgreSQLPacket));
        when(portal.getSqlStatement()).thenReturn(mock(PostgreSQLSelectStatement.class));
        when(portal.next()).thenReturn(true, false);
        when(portal.nextPacket()).thenReturn(dataRowPacket);
        PostgreSQLComExecuteExecutor actual = new PostgreSQLComExecuteExecutor(connectionContext, packet);
        Collection<DatabasePacket<?>> actualPackets = actual.execute();
        assertThat(actualPackets.size(), is(3));
        Iterator<DatabasePacket<?>> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), is(postgreSQLPacket));
        assertThat(actualPacketsIterator.next(), is(dataRowPacket));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    public void assertExecuteUpdate() throws SQLException {
        when(portal.getSqlStatement()).thenReturn(mock(EmptyStatement.class));
        when(portal.next()).thenReturn(false);
        Collection<DatabasePacket<?>> actual = new PostgreSQLComExecuteExecutor(connectionContext, packet).execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLEmptyQueryResponsePacket.class)));
    }
    
    @Test
    public void assertExecuteQueryAndPortalSuspended() throws SQLException {
        when(packet.getPortal()).thenReturn("C_1");
        when(connectionContext.getPortal("C_1")).thenReturn(portal);
        when(packet.getMaxRows()).thenReturn(1);
        when(portal.next()).thenReturn(true, false);
        when(portal.nextPacket()).thenReturn(dataRowPacket);
        PostgreSQLComExecuteExecutor executor = new PostgreSQLComExecuteExecutor(connectionContext, packet);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(2));
        Iterator<DatabasePacket<?>> actualPackets = actual.iterator();
        assertThat(actualPackets.next(), is(dataRowPacket));
        assertThat(actualPackets.next(), is(instanceOf(PostgreSQLPortalSuspendedPacket.class)));
    }
    
    @Test
    public void assertCloseAndPortalSuspended() throws SQLException {
        when(packet.getPortal()).thenReturn("");
        when(packet.getMaxRows()).thenReturn(1);
        when(connectionContext.getPortal("")).thenReturn(portal);
        PostgreSQLComExecuteExecutor executor = new PostgreSQLComExecuteExecutor(connectionContext, packet);
        setDataRows(executor, 1);
        executor.close();
        verify(portal).suspend();
    }
    
    @SneakyThrows
    private void setDataRows(final PostgreSQLComExecuteExecutor target, final long value) {
        Field field = PostgreSQLComExecuteExecutor.class.getDeclaredField("dataRows");
        field.setAccessible(true);
        field.setLong(target, value);
    }
    
    @Test
    public void assertCloseAndPortalClosed() throws SQLException {
        when(connectionContext.getPortal("")).thenReturn(portal);
        PostgreSQLComExecuteExecutor executor = new PostgreSQLComExecuteExecutor(connectionContext, packet);
        executor.close();
        verify(connectionContext).closePortal("");
    }
    
    @Test
    public void assertCloseAllPortals() throws SQLException {
        when(portal.getSqlStatement()).thenReturn(mock(PostgreSQLCommitStatement.class));
        when(connectionContext.getPortal("")).thenReturn(portal);
        PostgreSQLComExecuteExecutor executor = new PostgreSQLComExecuteExecutor(connectionContext, packet);
        executor.close();
        verify(connectionContext).closeAllPortals();
    }
}

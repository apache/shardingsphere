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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.execute;

import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLComExecuteExecutorTest {
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private PostgreSQLComExecutePacket packet;
    
    @Mock
    private Portal portal;
    
    @InjectMocks
    private PostgreSQLComExecuteExecutor executor;
    
    @BeforeEach
    void setup() {
        when(packet.getPortal()).thenReturn("");
        when(portalContext.get(anyString())).thenReturn(portal);
    }
    
    @Test
    void assertExecute() throws SQLException {
        PostgreSQLPacket expectedPacket = mock(PostgreSQLPacket.class);
        when(portal.execute(anyInt())).thenReturn(Collections.singletonList(expectedPacket));
        List<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), is(expectedPacket));
    }
    
    @Test
    void assertCloseExecutorWhenPortalIsNotAnyTclStatement() throws SQLException {
        when(portal.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        executor.close();
        verify(portalContext, never()).closeAll();
    }
    
    @Test
    void assertCloseExecutorWhenPortalCommitStatement() throws SQLException {
        when(portal.getSqlStatement()).thenReturn(mock(CommitStatement.class));
        executor.close();
        verify(portalContext).closeAll();
    }
    
    @Test
    void assertCloseExecutorWhenPortalRollbackStatement() throws SQLException {
        when(portal.getSqlStatement()).thenReturn(mock(RollbackStatement.class));
        executor.close();
        verify(portalContext).closeAll();
    }
}

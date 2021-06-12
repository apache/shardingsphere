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

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComExecuteExecutorTest {
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Mock
    private QueryCommandExecutor queryCommandExecutor;
    
    @Mock
    private PostgreSQLPacket postgreSQLPacket;
    
    @Mock
    private PostgreSQLDataRowPacket dataRowPacket;
    
    @Test
    public void assertExecuteQuery() throws SQLException {
        when(connectionContext.getPendingExecutors()).thenReturn(new ArrayList<>(Collections.singletonList(queryCommandExecutor)));
        when(queryCommandExecutor.execute()).thenReturn(Collections.singletonList(postgreSQLPacket));
        when(queryCommandExecutor.next()).thenReturn(true, false);
        when((PostgreSQLDataRowPacket) queryCommandExecutor.getQueryRowPacket()).thenReturn(dataRowPacket);
        PostgreSQLComExecuteExecutor actual = new PostgreSQLComExecuteExecutor(connectionContext);
        Collection<DatabasePacket<?>> actualPackets = actual.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), is(postgreSQLPacket));
        assertTrue(actual.next());
        assertThat(actual.getQueryRowPacket(), is(dataRowPacket));
        assertTrue(actual.next());
        assertThat(actual.getQueryRowPacket(), is(instanceOf(PostgreSQLCommandCompletePacket.class)));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertExecuteUpdate() throws SQLException {
        when(connectionContext.getSqlStatement()).thenReturn(Optional.of(mock(EmptyStatement.class)));
        PostgreSQLComExecuteExecutor actual = new PostgreSQLComExecuteExecutor(connectionContext);
        assertTrue(actual.next());
        assertThat(actual.getQueryRowPacket(), is(instanceOf(PostgreSQLEmptyQueryResponsePacket.class)));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertResponseType() {
        ResponseType actual = new PostgreSQLComExecuteExecutor(connectionContext).getResponseType();
        assertThat(actual, is(ResponseType.QUERY));
    }
}

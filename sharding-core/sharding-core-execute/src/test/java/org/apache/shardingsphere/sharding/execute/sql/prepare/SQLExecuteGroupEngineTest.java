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

package org.apache.shardingsphere.sharding.execute.sql.prepare;

import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.connection.ExecutionConnection;
import org.apache.shardingsphere.underlying.executor.connection.StatementOption;
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SQLExecuteGroupEngineTest {
    
    private SQLExecuteGroupEngine sqlExecuteGroupEngine;
    
    @Test
    public void assertGetExecuteUnitGroupForOneShardMemoryStrictly() throws SQLException {
        sqlExecuteGroupEngine = new SQLExecuteGroupEngine(true, 2);
        Collection<InputGroup<StatementExecuteUnit>> actual = sqlExecuteGroupEngine.getExecuteUnitGroups(
                mockExecutionConnection(1, ConnectionMode.MEMORY_STRICTLY), mockShardRouteUnit(1, 1), new StatementOption(true));
        assertThat(actual.size(), is(1));
        for (InputGroup<StatementExecuteUnit> each : actual) {
            assertThat(each.getInputs().size(), is(1));
        }
    }
    
    @Test
    public void assertGetExecuteUnitGroupForMultiShardConnectionStrictly() throws SQLException {
        sqlExecuteGroupEngine = new SQLExecuteGroupEngine(true, 1);
        Collection<InputGroup<StatementExecuteUnit>> actual = sqlExecuteGroupEngine.getExecuteUnitGroups(
                mockExecutionConnection(1, ConnectionMode.CONNECTION_STRICTLY), mockShardRouteUnit(10, 2), new StatementOption(true));
        assertThat(actual.size(), is(10));
        for (InputGroup<StatementExecuteUnit> each : actual) {
            assertThat(each.getInputs().size(), is(2));
        }
    }
    
    private ExecutionConnection mockExecutionConnection(final int size, final ConnectionMode connectionMode) throws SQLException {
        List<Connection> connections = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            connections.add(mock(Connection.class));
        }
        ExecutionConnection result = mock(ExecutionConnection.class);
        when(result.getConnections(anyString(), eq(size), eq(connectionMode))).thenReturn(connections);
        return result;
    }
    
    private Collection<ExecutionUnit> mockShardRouteUnit(final int shardCount, final int sizePerShard) {
        Collection<ExecutionUnit> result = new ArrayList<>(shardCount * sizePerShard);
        for (int i = 0; i < shardCount; i++) {
            result.addAll(mockOneShard("ds_" + i, sizePerShard));
        }
        return result;
    }
    
    private Collection<ExecutionUnit> mockOneShard(final String dsName, final int size) {
        Collection<ExecutionUnit> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(new ExecutionUnit(dsName, mock(SQLUnit.class)));
        }
        return result;
    }
    
}

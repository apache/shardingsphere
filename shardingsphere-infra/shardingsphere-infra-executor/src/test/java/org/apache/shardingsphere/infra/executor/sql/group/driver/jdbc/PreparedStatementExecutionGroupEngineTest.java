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

package org.apache.shardingsphere.infra.executor.sql.group.driver.jdbc;

import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.connection.JDBCExecutionConnection;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PreparedStatementExecutionGroupEngineTest {
    
    private PreparedStatementExecutionGroupEngine groupEngine;
    
    @Test
    public void assertGetExecuteUnitGroupForOneShardMemoryStrictly() throws SQLException {
        groupEngine = new PreparedStatementExecutionGroupEngine(
                2, mockExecutionConnection(1, ConnectionMode.MEMORY_STRICTLY), new StatementOption(true), Collections.singletonList(mock(ShardingSphereRule.class)));
        Collection<ExecutionGroup<StatementExecuteUnit>> actual = groupEngine.group(mock(RouteContext.class), mockShardRouteUnit(1, 1));
        assertThat(actual.size(), is(1));
        for (ExecutionGroup<StatementExecuteUnit> each : actual) {
            assertThat(each.getInputs().size(), is(1));
        }
    }
    
    @Test
    public void assertGetExecuteUnitGroupForMultiShardConnectionStrictly() throws SQLException {
        groupEngine = new PreparedStatementExecutionGroupEngine(
                1, mockExecutionConnection(1, ConnectionMode.CONNECTION_STRICTLY), new StatementOption(true), Collections.singletonList(mock(ShardingSphereRule.class)));
        Collection<ExecutionGroup<StatementExecuteUnit>> actual = groupEngine.group(mock(RouteContext.class), mockShardRouteUnit(10, 2));
        assertThat(actual.size(), is(10));
        for (ExecutionGroup<StatementExecuteUnit> each : actual) {
            assertThat(each.getInputs().size(), is(2));
        }
    }
    
    private JDBCExecutionConnection mockExecutionConnection(final int size, final ConnectionMode connectionMode) throws SQLException {
        List<Connection> connections = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            connections.add(mock(Connection.class));
        }
        JDBCExecutionConnection result = mock(JDBCExecutionConnection.class);
        when(result.getConnections(anyString(), eq(size), eq(connectionMode))).thenReturn(connections);
        return result;
    }
    
    private Collection<ExecutionUnit> mockShardRouteUnit(final int shardCount, final int sizePerShard) {
        Collection<ExecutionUnit> result = new ArrayList<>(shardCount * sizePerShard);
        for (int i = 0; i < shardCount; i++) {
            result.addAll(mockOneShard(String.format("ds_%s", i), sizePerShard));
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

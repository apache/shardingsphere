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

package org.apache.shardingsphere.core.execute.sql.prepare;

import org.apache.shardingsphere.core.constant.ConnectionMode;
import org.apache.shardingsphere.core.execute.ShardingExecuteGroup;
import org.apache.shardingsphere.core.execute.StatementExecuteUnit;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
public final class SQLExecutePrepareTemplateTest {
    
    private SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    @Mock
    private SQLExecutePrepareCallback callback;
    
    @Test
    public void assertGetExecuteUnitGroupForOneShardMemoryStrictly() throws SQLException {
        mockConnections(callback, ConnectionMode.MEMORY_STRICTLY, 1);
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(2);
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> actual = sqlExecutePrepareTemplate.getExecuteUnitGroups(mockShardRouteUnit(1, 1), callback);
        assertThat(actual.size(), is(1));
        for (ShardingExecuteGroup<StatementExecuteUnit> each : actual) {
            assertThat(each.getInputs().size(), is(1));
        }
    }
    
    @Test
    public void assertGetExecuteUnitGroupForMultiShardConnectionStrictly() throws SQLException {
        mockConnections(callback, ConnectionMode.CONNECTION_STRICTLY, 1);
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(1);
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> actual = sqlExecutePrepareTemplate.getExecuteUnitGroups(mockShardRouteUnit(10, 2), callback);
        assertThat(actual.size(), is(10));
        for (ShardingExecuteGroup<StatementExecuteUnit> each : actual) {
            assertThat(each.getInputs().size(), is(2));
        }
    }
    
    private void mockConnections(final SQLExecutePrepareCallback callback, final ConnectionMode connectionMode, final int size) throws SQLException {
        List<Connection> connections = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            connections.add(mock(Connection.class));
        }
        when(callback.getConnections(eq(connectionMode), anyString(), eq(size))).thenReturn(connections);
    }
    
    private Collection<RouteUnit> mockShardRouteUnit(final int shardCount, final int sizePerShard) {
        Collection<RouteUnit> result = new ArrayList<>(shardCount * sizePerShard);
        for (int i = 0; i < shardCount; i++) {
            result.addAll(mockOneShard("ds_" + i, sizePerShard));
        }
        return result;
    }
    
    private Collection<RouteUnit> mockOneShard(final String dsName, final int size) {
        Collection<RouteUnit> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(new RouteUnit(dsName, mock(SQLUnit.class)));
        }
        return result;
    }
    
}

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

package org.apache.shardingsphere.distsql.handler.ral.queryable.computenode;

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodeInfoStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowComputeNodeInfoExecutorTest {
    
    private final ShowComputeNodeInfoExecutor executor = (ShowComputeNodeInfoExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowComputeNodeInfoStatement.class);
    
    @Test
    void assertGetColumnNames() {
        assertThat(executor.getColumnNames(mock(ShowComputeNodeInfoStatement.class)),
                is(Arrays.asList("instance_id", "host", "port", "status", "mode_type", "worker_id", "labels", "version")));
    }
    
    @Test
    void assertExecuteWithProxyInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext computeNodeInstanceContext = createProxyInstanceContext();
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodeInfoStatement.class), contextManager);
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("foo"));
        assertThat(row.getCell(2), is("127.0.0.1"));
        assertThat(row.getCell(3), is("3308"));
        assertThat(row.getCell(4), is("OK"));
        assertThat(row.getCell(5), is("Standalone"));
        assertThat(row.getCell(6), is("-1"));
        assertThat(row.getCell(7), is(""));
        assertThat(row.getCell(8), is("foo_version"));
    }
    
    private ComputeNodeInstanceContext createProxyInstanceContext() {
        ComputeNodeInstanceContext result = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo", "127.0.0.1@3308", "foo_version"));
        when(result.getInstance()).thenReturn(computeNodeInstance);
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Standalone", mock(PersistRepositoryConfiguration.class)));
        return result;
    }
    
    @Test
    void assertExecuteWithJdbcInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new JDBCInstanceMetaData("jdbc_instance", "192.168.0.1", "jdbc_version", "logic_db"));
        computeNodeInstance.setWorkerId(1);
        ComputeNodeInstanceContext computeNodeInstanceContext =
                new ComputeNodeInstanceContext(computeNodeInstance, new ModeConfiguration("Cluster", mock(PersistRepositoryConfiguration.class)), new EventBusContext());
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodeInfoStatement.class), contextManager);
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("jdbc_instance"));
        assertThat(row.getCell(2), is("192.168.0.1"));
        assertThat(row.getCell(3), is("-1"));
        assertThat(row.getCell(4), is("OK"));
        assertThat(row.getCell(5), is("Cluster"));
        assertThat(row.getCell(6), is("1"));
        assertThat(row.getCell(7), is(""));
        assertThat(row.getCell(8), is("jdbc_version"));
    }
}

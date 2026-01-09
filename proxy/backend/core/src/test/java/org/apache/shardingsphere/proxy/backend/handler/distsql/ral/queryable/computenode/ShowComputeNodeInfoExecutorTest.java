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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.computenode;

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodeInfoStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowComputeNodeInfoExecutorTest {
    
    private final ShowComputeNodeInfoExecutor executor = (ShowComputeNodeInfoExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowComputeNodeInfoStatement.class);
    
    @Test
    void assertGetColumnNames() {
        ShowComputeNodeInfoStatement sqlStatement = mock(ShowComputeNodeInfoStatement.class);
        assertThat(executor.getColumnNames(sqlStatement), is(Arrays.asList("instance_id", "host", "port", "status", "mode_type", "worker_id", "labels", "version")));
    }
    
    @Test
    void assertExecute() {
        ContextManager contextManager = mock(ContextManager.class);
        ComputeNodeInstanceContext computeNodeInstanceContext = createInstanceContext();
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodeInfoStatement.class), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("foo"));
        assertThat(row.getCell(2), is("127.0.0.1"));
        assertThat(row.getCell(3), is("3309"));
        assertThat(row.getCell(4), is("OK"));
        assertThat(row.getCell(5), is("Standalone"));
        assertThat(row.getCell(6), is("0"));
        assertThat(row.getCell(7), is(""));
        assertThat(row.getCell(8), is("foo_version"));
    }
    
    @Test
    void assertExecuteWithJdbcInstance() {
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new JDBCInstanceMetaData("jdbc_instance", "10.0.0.1", "jdbc_version", "logic_db"));
        computeNodeInstance.setWorkerId(5);
        computeNodeInstance.getLabels().add("blue");
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(new ComputeNodeInstanceContext(computeNodeInstance, new ModeConfiguration("Cluster", null), new EventBusContext()));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodeInfoStatement.class), contextManager);
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("jdbc_instance"));
        assertThat(row.getCell(2), is("10.0.0.1"));
        assertThat(row.getCell(3), is("-1"));
        assertThat(row.getCell(4), is("OK"));
        assertThat(row.getCell(5), is("Cluster"));
        assertThat(row.getCell(6), is("5"));
        assertThat(row.getCell(7), is("blue"));
        assertThat(row.getCell(8), is("jdbc_version"));
    }
    
    private ComputeNodeInstanceContext createInstanceContext() {
        ComputeNodeInstanceContext result = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getInstance().getMetaData()).thenReturn(new ProxyInstanceMetaData("foo", "127.0.0.1@3309", "foo_version"));
        when(result.getInstance().getState()).thenReturn(new InstanceStateContext());
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("H2", new Properties())));
        when(result.getInstance().getWorkerId()).thenReturn(0);
        return result;
    }
}

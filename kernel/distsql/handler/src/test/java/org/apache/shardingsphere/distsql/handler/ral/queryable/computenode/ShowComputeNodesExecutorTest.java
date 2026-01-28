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
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodesStatement;
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
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowComputeNodesExecutorTest {
    
    private final ShowComputeNodesExecutor executor = (ShowComputeNodesExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowComputeNodesStatement.class);
    
    @Test
    void assertGetColumnNames() {
        assertThat(executor.getColumnNames(mock(ShowComputeNodesStatement.class)),
                is(Arrays.asList("instance_id", "instance_type", "host", "port", "status", "mode_type", "worker_id", "labels", "version", "database_name")));
    }
    
    @Test
    void assertExecuteWithStandaloneMode() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo", "127.0.0.1@3308", "foo_version"));
        ComputeNodeInstanceContext computeNodeInstanceContext = createStandaloneInstanceContext(computeNodeInstance);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        when(contextManager.getPersistServiceFacade().getModeFacade().getComputeNodeService().loadAllInstances()).thenReturn(Collections.singleton(computeNodeInstance));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodesStatement.class), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("foo"));
        assertThat(row.getCell(2), is("PROXY"));
        assertThat(row.getCell(3), is("127.0.0.1"));
        assertThat(row.getCell(4), is("3308"));
        assertThat(row.getCell(5), is("OK"));
        assertThat(row.getCell(6), is("Standalone"));
        assertThat(row.getCell(7), is("-1"));
        assertThat(row.getCell(8), is(""));
        assertThat(row.getCell(9), is("foo_version"));
        assertThat(row.getCell(10), is(""));
    }
    
    private ComputeNodeInstanceContext createStandaloneInstanceContext(final ComputeNodeInstance computeNodeInstance) {
        ComputeNodeInstanceContext result = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getInstance()).thenReturn(computeNodeInstance);
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Standalone", mock(PersistRepositoryConfiguration.class)));
        return result;
    }
    
    @Test
    void assertExecuteWithClusterMode() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo", "127.0.0.1@3309", "foo_version"));
        computeNodeInstance.setWorkerId(1);
        ComputeNodeInstanceContext computeNodeInstanceContext = createClusterInstanceContext(computeNodeInstance);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        when(contextManager.getPersistServiceFacade().getModeFacade().getComputeNodeService().loadAllInstances()).thenReturn(Collections.singleton(computeNodeInstance));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodesStatement.class), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("foo"));
        assertThat(row.getCell(2), is("PROXY"));
        assertThat(row.getCell(3), is("127.0.0.1"));
        assertThat(row.getCell(4), is("3309"));
        assertThat(row.getCell(5), is("OK"));
        assertThat(row.getCell(6), is("Cluster"));
        assertThat(row.getCell(7), is("1"));
        assertThat(row.getCell(8), is(""));
        assertThat(row.getCell(9), is("foo_version"));
        assertThat(row.getCell(10), is(""));
    }
    
    private ComputeNodeInstanceContext createClusterInstanceContext(final ComputeNodeInstance computeNodeInstance) {
        ComputeNodeInstanceContext result = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Cluster", mock(PersistRepositoryConfiguration.class)));
        when(result.getClusterInstanceRegistry().getAllClusterInstances()).thenReturn(Collections.singleton(computeNodeInstance));
        return result;
    }
    
    @Test
    void assertExecuteWithJdbcInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new JDBCInstanceMetaData("jdbc_instance", "192.168.0.1", "jdbc_version", "logic_db"));
        computeNodeInstance.setWorkerId(2);
        computeNodeInstance.getLabels().add("prod");
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(computeNodeInstance, new ModeConfiguration("Cluster", mock()), new EventBusContext());
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        when(contextManager.getPersistServiceFacade().getModeFacade().getComputeNodeService().loadAllInstances()).thenReturn(Collections.singleton(computeNodeInstance));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodesStatement.class), contextManager);
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("jdbc_instance"));
        assertThat(row.getCell(2), is("JDBC"));
        assertThat(row.getCell(3), is("192.168.0.1"));
        assertThat(row.getCell(4), is("-1"));
        assertThat(row.getCell(5), is("OK"));
        assertThat(row.getCell(6), is("Cluster"));
        assertThat(row.getCell(7), is("2"));
        assertThat(row.getCell(8), is("prod"));
        assertThat(row.getCell(9), is("jdbc_version"));
        assertThat(row.getCell(10), is("logic_db"));
    }
}

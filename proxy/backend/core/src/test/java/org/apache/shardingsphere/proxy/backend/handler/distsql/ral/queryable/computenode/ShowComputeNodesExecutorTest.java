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

import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodesStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.persist.facade.ClusterPersistServiceFacade;
import org.apache.shardingsphere.mode.manager.standalone.persist.facade.StandalonePersistServiceFacade;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowComputeNodesExecutorTest {
    
    @Test
    void assertExecuteWithStandaloneMode() {
        ShowComputeNodesExecutor executor = new ShowComputeNodesExecutor();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext computeNodeInstanceContext = createStandaloneInstanceContext();
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        when(metaDataContextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        StandalonePersistServiceFacade standalonePersistServiceFacade = new StandalonePersistServiceFacade(metaDataContextManager);
        when(contextManager.getPersistServiceFacade().getModeFacade()).thenReturn(standalonePersistServiceFacade);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowComputeNodesStatement.class), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("foo"));
        assertThat(row.getCell(2), is("PROXY"));
        assertThat(row.getCell(3), is("127.0.0.1"));
        assertThat(row.getCell(4), is("3308"));
        assertThat(row.getCell(5), is("OK"));
        assertThat(row.getCell(6), is("Standalone"));
        assertThat(row.getCell(7), is("0"));
        assertThat(row.getCell(8), is(""));
        assertThat(row.getCell(9), is("foo_version"));
        assertThat(row.getCell(10), is(""));
    }
    
    private ComputeNodeInstanceContext createStandaloneInstanceContext() {
        ComputeNodeInstanceContext result = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getInstance().getMetaData()).thenReturn(new ProxyInstanceMetaData("foo", "127.0.0.1@3308", "foo_version"));
        when(result.getInstance().getState()).thenReturn(new InstanceStateContext());
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("H2", new Properties())));
        when(result.getInstance().getWorkerId()).thenReturn(0);
        return result;
    }
    
    @Test
    void assertExecuteWithClusterMode() {
        ShowComputeNodesExecutor executor = new ShowComputeNodesExecutor();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext computeNodeInstanceContext = createClusterInstanceContext(contextManager);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
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
    
    private ComputeNodeInstanceContext createClusterInstanceContext(final ContextManager contextManager) {
        ComputeNodeInstanceContext result = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Cluster", mock(PersistRepositoryConfiguration.class)));
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class, RETURNS_DEEP_STUBS);
        when(computeNodeInstance.getMetaData()).thenReturn(new ProxyInstanceMetaData("foo", "127.0.0.1@3309", "foo_version"));
        when(computeNodeInstance.getState()).thenReturn(new InstanceStateContext());
        when(computeNodeInstance.getWorkerId()).thenReturn(1);
        when(result.getClusterInstanceRegistry().getAllClusterInstances()).thenReturn(Collections.singleton(computeNodeInstance));
        ClusterPersistServiceFacade clusterPersistServiceFacade = mock(ClusterPersistServiceFacade.class, RETURNS_DEEP_STUBS);
        when(clusterPersistServiceFacade.getComputeNodeService().loadAllInstances()).thenReturn(Collections.singleton(computeNodeInstance));
        when(contextManager.getPersistServiceFacade().getModeFacade()).thenReturn(clusterPersistServiceFacade);
        return result;
    }
}

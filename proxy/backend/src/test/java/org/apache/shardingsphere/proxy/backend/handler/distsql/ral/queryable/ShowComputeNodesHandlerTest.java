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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodesStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowComputeNodesHandlerTest extends ProxyContextRestorer {
    
    private final ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
    
    @Test
    public void assertExecuteWithStandaloneMode() throws SQLException {
        InstanceContext instanceContext = createStandaloneInstanceContext();
        when(contextManager.getInstanceContext()).thenReturn(instanceContext);
        ShowComputeNodesHandler handler = new ShowComputeNodesHandler();
        handler.init(new ShowComputeNodesStatement(), null);
        ProxyContext.init(contextManager);
        handler.execute();
        handler.next();
        List<Object> data = handler.getRowData().getData();
        assertThat(data.size(), is(7));
        assertThat(data.get(0), is("127.0.0.1@3308"));
        assertThat(data.get(1), is("127.0.0.1"));
        assertThat(data.get(2), is(3308));
        assertThat(data.get(3), is("OK"));
        assertThat(data.get(4), is("Standalone"));
        assertThat(data.get(5), is(0L));
        assertThat(data.get(6), is(""));
    }
    
    @Test
    public void assertExecuteWithClusterMode() throws SQLException {
        InstanceContext instanceContext = createClusterInstanceContext();
        when(contextManager.getInstanceContext()).thenReturn(instanceContext);
        ShowComputeNodesHandler handler = new ShowComputeNodesHandler();
        handler.init(new ShowComputeNodesStatement(), null);
        ProxyContext.init(contextManager);
        handler.execute();
        handler.next();
        List<Object> data = handler.getRowData().getData();
        assertThat(data.size(), is(7));
        assertThat(data.get(0), is("127.0.0.1@3309"));
        assertThat(data.get(1), is("127.0.0.1"));
        assertThat(data.get(2), is(3309));
        assertThat(data.get(3), is("OK"));
        assertThat(data.get(4), is("Cluster"));
        assertThat(data.get(5), is(1L));
        assertThat(data.get(6), is(""));
    }
    
    private InstanceContext createStandaloneInstanceContext() {
        InstanceContext result = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getInstance().getMetaData()).thenReturn(new ProxyInstanceMetaData("127.0.0.1@3308", "127.0.0.1@3308"));
        when(result.getInstance().getState()).thenReturn(new StateContext());
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("H2", new Properties())));
        when(result.getInstance().getWorkerId()).thenReturn(0L);
        return result;
    }
    
    private InstanceContext createClusterInstanceContext() {
        InstanceContext result = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Cluster", mock(PersistRepositoryConfiguration.class)));
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class, RETURNS_DEEP_STUBS);
        when(computeNodeInstance.getMetaData()).thenReturn(new ProxyInstanceMetaData("127.0.0.1@3309", "127.0.0.1@3309"));
        when(computeNodeInstance.getState()).thenReturn(new StateContext());
        when(computeNodeInstance.getWorkerId()).thenReturn(1L);
        when(result.getAllClusterInstances()).thenReturn(Collections.singleton(computeNodeInstance));
        return result;
    }
}

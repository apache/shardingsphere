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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show;

import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowInstanceModeExecutor;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowInstanceModeExecutorTest {
    
    @Test
    public void assertExecutor() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        Optional<MetaDataPersistService> metaDataPersistService = Optional.of(createMetaDataPersistService());
        when(contextManager.getMetaDataContexts().getMetaDataPersistService()).thenReturn(metaDataPersistService);
        InstanceContext instanceContext = createInstanceContext();
        when(contextManager.getInstanceContext()).thenReturn(instanceContext);
        ShowInstanceModeExecutor executor = new ShowInstanceModeExecutor();
        ProxyContext.getInstance().init(contextManager);
        executor.execute();
        executor.next();
        QueryResponseRow queryResponseRow = executor.getQueryResponseRow();
        ArrayList<Object> data = new ArrayList<>(queryResponseRow.getData());
        assertThat(data.size(), is(4));
        assertThat(data.get(0), is("127.0.0.1@3309"));
        assertThat(data.get(1), is("Cluster"));
        assertThat(data.get(2), is("ZooKeeper"));
        assertThat(data.get(3), is("key=value"));
    }
    
    private MetaDataPersistService createMetaDataPersistService() {
        MetaDataPersistService result = mock(MetaDataPersistService.class);
        PersistRepository repository = mock(ClusterPersistRepository.class, RETURNS_DEEP_STUBS);
        when(result.getRepository()).thenReturn(repository);
        when(repository.getType()).thenReturn("ZooKeeper");
        when(repository.getProps()).thenReturn(createProperties("key", "value"));
        return result;
    }
    
    private InstanceContext createInstanceContext() {
        InstanceContext result = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getInstance().getInstanceDefinition().getInstanceId().getId()).thenReturn("127.0.0.1@3309");
        return result;
    }
    
    private Properties createProperties(final String key, final String value) {
        Properties result = new Properties();
        result.put(key, value);
        return result;
    }
}

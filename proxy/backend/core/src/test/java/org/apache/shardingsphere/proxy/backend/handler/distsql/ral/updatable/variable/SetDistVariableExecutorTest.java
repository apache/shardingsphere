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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.variable;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.config.global.PropertiesPersistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetDistVariableExecutorTest {
    
    private final SetDistVariableExecutor executor = new SetDistVariableExecutor();
    
    @Test
    void assertExecuteWithConfigurationKey() {
        SetDistVariableStatement statement = new SetDistVariableStatement("proxy_frontend_flush_threshold", "1024");
        ContextManager contextManager = mockContextManager();
        executor.executeUpdate(statement, contextManager);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD), is(1024));
    }
    
    @Test
    void assertExecuteWithTemporaryConfigurationKey() {
        SetDistVariableStatement statement = new SetDistVariableStatement("proxy_meta_data_collector_enabled", "false");
        ContextManager contextManager = mockContextManager();
        executor.executeUpdate(statement, contextManager);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED), is(false));
    }
    
    @Test
    void assertExecuteWithTypedSPI() {
        SetDistVariableStatement statement = new SetDistVariableStatement("proxy_frontend_database_protocol_type", "Fixture");
        ContextManager contextManager = mockContextManager();
        executor.executeUpdate(statement, contextManager);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getProps().getProperty("proxy-frontend-database-protocol-type"), is("FIXTURE"));
        assertThat(((DatabaseType) contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE)).getType(), is("FIXTURE"));
    }
    
    private ContextManager mockContextManager() {
        MetaDataPersistFacade metaDataPersistFacade = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistFacade.getPropsService()).thenReturn(mock(PropertiesPersistService.class));
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), new EventBusContext());
        computeNodeInstanceContext.init(mock(WorkerIdGenerator.class));
        return new ContextManager(new MetaDataContexts(new ShardingSphereMetaData(), new ShardingSphereStatistics()), computeNodeInstanceContext, mock(), mock());
    }
}

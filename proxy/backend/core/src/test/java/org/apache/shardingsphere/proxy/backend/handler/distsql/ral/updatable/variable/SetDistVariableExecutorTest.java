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
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.kernel.syntax.InvalidVariableValueException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UnsupportedVariableException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.config.global.PropertiesPersistService;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SetDistVariableExecutorTest {
    
    private final SetDistVariableExecutor executor = (SetDistVariableExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, SetDistVariableStatement.class);
    
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
    
    @Test
    void assertExecuteWithUnsupportedVariable() {
        assertThrows(UnsupportedVariableException.class, () -> executor.executeUpdate(new SetDistVariableStatement("unknown", "1"), mockContextManager()));
    }
    
    @Test
    void assertExecuteWithInvalidValue() {
        assertThrows(InvalidVariableValueException.class, () -> executor.executeUpdate(new SetDistVariableStatement("proxy_frontend_flush_threshold", "invalid"), mockContextManager()));
    }
    
    @Test
    void assertExecuteWithCronVariable() {
        ContextManager contextManager = mockContextManager();
        SetDistVariableStatement statement = new SetDistVariableStatement("proxy_meta_data_collector_cron", "0 0/5 * * * ?");
        executor.executeUpdate(statement, contextManager);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getProps().getProperty("proxy-meta-data-collector-cron"), is("0 0/5 * * * ?"));
    }
    
    @Test
    void assertExecuteWithInvalidCronVariable() {
        assertThrows(InvalidVariableValueException.class, () -> executor.executeUpdate(new SetDistVariableStatement("proxy_meta_data_collector_cron", "invalid"), mockContextManager()));
    }
    
    private ContextManager mockContextManager() {
        MetaDataPersistFacade metaDataPersistFacade = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistFacade.getPropsService()).thenReturn(mock(PropertiesPersistService.class));
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), new EventBusContext());
        computeNodeInstanceContext.init(mock(WorkerIdGenerator.class));
        return new ContextManager(new MetaDataContexts(new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties())), new ShardingSphereStatistics()), computeNodeInstanceContext, mock(), mock());
    }
}

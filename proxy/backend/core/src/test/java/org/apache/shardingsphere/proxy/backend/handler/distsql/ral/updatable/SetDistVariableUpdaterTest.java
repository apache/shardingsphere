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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.LoggerLevel;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.standalone.StandaloneModeContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.InvalidValueException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class SetDistVariableUpdaterTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertExecuteWithConfigurationKey() {
        SetDistVariableStatement statement = new SetDistVariableStatement("proxy_frontend_flush_threshold", "1024");
        SetDistVariableUpdater updater = new SetDistVariableUpdater();
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        updater.executeUpdate(connectionSession, statement);
        Object actualValue = contextManager.getMetaDataContexts().getMetaData().getProps().getProps().get("proxy-frontend-flush-threshold");
        assertThat(actualValue.toString(), is("1024"));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD), is(1024));
    }
    
    @Test
    void assertExecuteWithTemporaryConfigurationKey() {
        SetDistVariableStatement statement = new SetDistVariableStatement("proxy_meta_data_collector_enabled", "false");
        SetDistVariableUpdater updater = new SetDistVariableUpdater();
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        updater.executeUpdate(connectionSession, statement);
        Object actualValue = contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getProps().get("proxy-meta-data-collector-enabled");
        assertThat(actualValue.toString(), is("false"));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED), is(false));
    }
    
    @Test
    void assertExecuteWithSystemLogLevel() {
        SetDistVariableStatement statement = new SetDistVariableStatement("system_log_level", "debug");
        SetDistVariableUpdater updater = new SetDistVariableUpdater();
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        updater.executeUpdate(connectionSession, statement);
        Object actualValue = contextManager.getMetaDataContexts().getMetaData().getProps().getProps().get("system-log-level");
        assertThat(actualValue.toString(), is("DEBUG"));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL), is(LoggerLevel.DEBUG));
    }
    
    @Test
    void assertExecuteWithWrongSystemLogLevel() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        SetDistVariableStatement statement = new SetDistVariableStatement("system_log_level", "invalid");
        SetDistVariableUpdater updater = new SetDistVariableUpdater();
        assertThrows(InvalidValueException.class, () -> updater.executeUpdate(connectionSession, statement));
    }
    
    private ContextManager mockContextManager() {
        StandaloneModeContextManager standaloneModeContextManager = new StandaloneModeContextManager();
        ContextManager result = new ContextManager(new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData()),
                new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(WorkerIdGenerator.class),
                        new ModeConfiguration("Standalone", null), standaloneModeContextManager, mock(LockContext.class), new EventBusContext()));
        standaloneModeContextManager.setContextManagerAware(result);
        return result;
    }
}

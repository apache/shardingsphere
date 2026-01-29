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

package org.apache.shardingsphere.distsql.handler.ral.updatable.variable;

import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UnsupportedVariableException;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetDistVariableExecutorTest {
    
    private final SetDistVariableExecutor executor = new SetDistVariableExecutor();
    
    @Test
    void assertSetConfigVariable() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE.toString())));
        when(contextManager.getMetaDataContexts().getMetaData().getProps()).thenReturn(props);
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(new Properties()));
        MetaDataManagerPersistService metaDataManagerService = mock(MetaDataManagerPersistService.class);
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerService);
        executor.executeUpdate(new SetDistVariableStatement("SQL_SHOW", "true"), contextManager);
        ArgumentCaptor<Properties> propsCaptor = ArgumentCaptor.forClass(Properties.class);
        verify(metaDataManagerService).alterProperties(propsCaptor.capture());
        assertThat(propsCaptor.getValue().get(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.TRUE));
    }
    
    @Test
    void assertSetTemporaryVariable() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps())
                .thenReturn(new TemporaryConfigurationProperties(
                        PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED.getKey(), Boolean.FALSE.toString()))));
        MetaDataManagerPersistService metaDataManagerService = mock(MetaDataManagerPersistService.class);
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerService);
        executor.executeUpdate(new SetDistVariableStatement("PROXY_META_DATA_COLLECTOR_ENABLED", "true"), contextManager);
        ArgumentCaptor<Properties> propsCaptor = ArgumentCaptor.forClass(Properties.class);
        verify(metaDataManagerService).alterProperties(propsCaptor.capture());
        assertThat(propsCaptor.getValue().get(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED.getKey()), is(Boolean.TRUE));
    }
    
    @Test
    void assertSetInvalidVariable() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        assertThrows(UnsupportedVariableException.class, () -> executor.executeUpdate(new SetDistVariableStatement("invalid", "true"), contextManager));
    }
}

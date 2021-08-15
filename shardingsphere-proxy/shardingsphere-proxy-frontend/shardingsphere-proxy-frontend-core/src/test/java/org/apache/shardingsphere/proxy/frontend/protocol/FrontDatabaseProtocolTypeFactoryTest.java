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

package org.apache.shardingsphere.proxy.frontend.protocol;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class FrontDatabaseProtocolTypeFactoryTest {
    
    @Test
    public void assertGetDatabaseTypeWhenThrowShardingSphereConfigurationException() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(DistMetaDataPersistService.class),
                Collections.emptyMap(), mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizeContextFactory.class));
        setContextManager(metaDataContexts);
        assertTrue(metaDataContexts.getMetaDataMap().isEmpty());
        assertThat(FrontDatabaseProtocolTypeFactory.getDatabaseType().getName(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeInstanceOfMySQLDatabaseTypeFromMetaDataContextsSchemaName() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(DistMetaDataPersistService.class),
                mockMetaDataMap(), mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizeContextFactory.class));
        setContextManager(metaDataContexts);
        assertFalse(metaDataContexts.getMetaDataMap().isEmpty());
        String configuredDatabaseType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        assertTrue(configuredDatabaseType.isEmpty());
        assertTrue(metaDataContexts.getAllSchemaNames().contains(DefaultSchema.LOGIC_NAME));
        DatabaseType databaseType = FrontDatabaseProtocolTypeFactory.getDatabaseType();
        assertThat(databaseType, instanceOf(DatabaseType.class));
        assertThat(databaseType.getName(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeOfPostgreSQLDatabaseTypeFromMetaDataContextsProps() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "PostgreSQL");
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(DistMetaDataPersistService.class),
                mockMetaDataMap(), mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(props), mock(OptimizeContextFactory.class));
        setContextManager(metaDataContexts);
        assertFalse(metaDataContexts.getMetaDataMap().isEmpty());
        String configuredDatabaseType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        assertThat(configuredDatabaseType, is("PostgreSQL"));
        assertTrue(metaDataContexts.getAllSchemaNames().contains(DefaultSchema.LOGIC_NAME));
        DatabaseType databaseType = FrontDatabaseProtocolTypeFactory.getDatabaseType();
        assertThat(databaseType, instanceOf(DatabaseType.class));
        assertThat(databaseType.getName(), is("PostgreSQL"));
        assertThat(metaDataContexts.getMetaData(DefaultSchema.LOGIC_NAME).getResource().getDatabaseType(), instanceOf(MySQLDatabaseType.class));
    }
    
    private Map<String, ShardingSphereMetaData> mockMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setContextManager(final MetaDataContexts metaDataContexts) {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
}

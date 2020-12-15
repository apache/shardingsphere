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
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
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
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertGetDatabaseTypeWhenThrowShardingSphereConfigurationException() {
        StandardMetaDataContexts metaDataContexts = new StandardMetaDataContexts(
                Collections.emptyMap(), mock(ExecutorEngine.class), mock(Authentication.class), new ConfigurationProperties(new Properties()));
        setMetaDataContexts(metaDataContexts);
        assertTrue(metaDataContexts.getMetaDataMap().isEmpty());
        FrontDatabaseProtocolTypeFactory.getDatabaseType();
    }
    
    @Test
    public void assertGetDatabaseTypeInstanceOfMySQLDatabaseTypeFromMetaDataContextsSchemaName() {
        StandardMetaDataContexts metaDataContexts = new StandardMetaDataContexts(
                mockMetaDataMap(), mock(ExecutorEngine.class), mock(Authentication.class), new ConfigurationProperties(new Properties()));
        setMetaDataContexts(metaDataContexts);
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
        StandardMetaDataContexts metaDataContexts = new StandardMetaDataContexts(mockMetaDataMap(), mock(ExecutorEngine.class), mock(Authentication.class), new ConfigurationProperties(props));
        setMetaDataContexts(metaDataContexts);
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
    private void setMetaDataContexts(final StandardMetaDataContexts metaDataContexts) {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), metaDataContexts);
    }
}

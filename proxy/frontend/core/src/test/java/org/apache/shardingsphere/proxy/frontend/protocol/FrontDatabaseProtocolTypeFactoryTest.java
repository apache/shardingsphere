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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.ProxyContextRestorer;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class FrontDatabaseProtocolTypeFactoryTest extends ProxyContextRestorer {
    
    @Test
    public void assertGetDatabaseTypeWhenThrowShardingSphereConfigurationException() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(Collections.emptyMap(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        ProxyContext.init(new ContextManager(metaDataContexts, mock(InstanceContext.class)));
        assertTrue(metaDataContexts.getMetaData().getDatabases().isEmpty());
        assertThat(FrontDatabaseProtocolTypeFactory.getDatabaseType().getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeInstanceOfMySQLDatabaseTypeFromMetaDataContextsSchemaName() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(mockDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        ProxyContext.init(new ContextManager(metaDataContexts, mock(InstanceContext.class)));
        assertFalse(metaDataContexts.getMetaData().getDatabases().isEmpty());
        String configuredDatabaseType = metaDataContexts.getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        assertTrue(configuredDatabaseType.isEmpty());
        assertTrue(metaDataContexts.getMetaData().containsDatabase(DefaultDatabase.LOGIC_NAME));
        DatabaseType databaseType = FrontDatabaseProtocolTypeFactory.getDatabaseType();
        assertThat(databaseType, instanceOf(DatabaseType.class));
        assertThat(databaseType.getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeOfPostgreSQLDatabaseTypeFromMetaDataContextsProps() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(mockDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(createProperties())), new ShardingSphereData());
        ProxyContext.init(new ContextManager(metaDataContexts, mock(InstanceContext.class)));
        assertFalse(metaDataContexts.getMetaData().getDatabases().isEmpty());
        String configuredDatabaseType = metaDataContexts.getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        assertThat(configuredDatabaseType, is("PostgreSQL"));
        assertTrue(metaDataContexts.getMetaData().containsDatabase(DefaultDatabase.LOGIC_NAME));
        DatabaseType databaseType = FrontDatabaseProtocolTypeFactory.getDatabaseType();
        assertThat(databaseType, instanceOf(DatabaseType.class));
        assertThat(databaseType.getType(), is("PostgreSQL"));
        assertThat(metaDataContexts.getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME).getResourceMetaData().getDatabaseType(), instanceOf(MySQLDatabaseType.class));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "PostgreSQL");
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put(DefaultDatabase.LOGIC_NAME, database);
        return result;
    }
}

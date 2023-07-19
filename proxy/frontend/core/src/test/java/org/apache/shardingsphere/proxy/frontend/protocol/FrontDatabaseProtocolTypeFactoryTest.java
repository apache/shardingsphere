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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class FrontDatabaseProtocolTypeFactoryTest {
    
    @Test
    void assertGetDatabaseTypeWhenThrowShardingSphereConfigurationException() {
        ContextManager contextManager = mockContextManager(Collections.emptyMap(), new Properties());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThat(FrontDatabaseProtocolTypeFactory.getDatabaseType().getType(), is("MySQL"));
    }
    
    @Test
    void assertGetDatabaseTypeInstanceOfMySQLDatabaseTypeFromMetaDataContextsSchemaName() {
        ContextManager contextManager = mockContextManager(mockDatabases(), new Properties());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        DatabaseType databaseType = FrontDatabaseProtocolTypeFactory.getDatabaseType();
        assertThat(databaseType, instanceOf(DatabaseType.class));
        assertThat(databaseType.getType(), is("MySQL"));
    }
    
    @Test
    void assertGetDatabaseTypeOfPostgreSQLDatabaseTypeFromMetaDataContextsProps() {
        ContextManager contextManager = mockContextManager(mockDatabases(),
                PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "PostgreSQL")));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        DatabaseType databaseType = FrontDatabaseProtocolTypeFactory.getDatabaseType();
        assertThat(databaseType, instanceOf(DatabaseType.class));
        assertThat(databaseType.getType(), is("PostgreSQL"));
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        return Collections.singletonMap("foo_db", database);
    }
    
    private ContextManager mockContextManager(final Map<String, ShardingSphereDatabase> databases, final Properties props) {
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                mock(MetaDataPersistService.class), new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class),
                        mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(props)));
        return new ContextManager(metaDataContexts, mock(InstanceContext.class));
    }
}

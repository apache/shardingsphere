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

package org.apache.shardingsphere.driver.governance.internal.schema;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.governance.core.common.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.DataSourceConverter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.context.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JDBCGovernanceSchemaContextsTest {
    
    @Mock
    private GovernanceFacade facade;
    
    private JDBCGovernanceSchemaContexts schemaContexts;
    
    @Before
    public void setUp() {
        schemaContexts = new JDBCGovernanceSchemaContexts(new StandardSchemaContexts(new LinkedHashMap<>(), new Authentication(),
                new ConfigurationProperties(new Properties()), new H2DatabaseType()), facade);
    }
    
    private Map<String, SchemaContext> getSchemaContextMap() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getDataSources()).thenReturn(getDataSources());
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        SchemaContext result = new SchemaContext(DefaultSchema.LOGIC_NAME, schema, runtimeContext);
        return Collections.singletonMap(DefaultSchema.LOGIC_NAME, result);
    }
    
    private Map<String, DataSource> getDataSources() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setUsername("sa");
        result.setPassword("");
        return Collections.singletonMap("db0", result);
    }
    
    @Test
    public void assertRenewDataSourceChangedEvent() throws Exception {
        schemaContexts.getSchemaContexts().putAll(getSchemaContextMap());
        DataSourceChangedEvent event = new DataSourceChangedEvent(DefaultSchema.LOGIC_NAME, getDataSourceConfigurations());
        schemaContexts.renew(event);
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setUsername("sa");
        result.setPassword("");
        return DataSourceConverter.getDataSourceConfigurationMap(Collections.singletonMap("db0", result));
    }
}

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

package org.apache.shardingsphere.mode.metadata;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.global.PropertiesPersistService;
import org.apache.shardingsphere.test.fixture.rule.MockedRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetaDataContextsFactoryTest {
    
    @Mock
    private MetaDataPersistService mockMetadataPersistService;
    
    @Mock
    private InstanceContext mockInstanceContext;
    
    @Mock
    private ComputeNodeInstance mockComputeNodeInstance;
    
    @Mock
    private JDBCInstanceMetaData mockJDBCInstanceMetadata;
    
    @Mock
    private DataSourceGeneratedDatabaseConfiguration dataSourceGeneratedDatabaseConfiguration;
    
    @Mock
    private DatabaseRulePersistService mockDatabaseRulePersistService;
    
    @Mock
    private GlobalRulePersistService mockGlobalRulePersistService;
    
    @Mock
    private PropertiesPersistService mockPropertiesPersistService;
    
    @Mock
    private DatabaseMetaDataPersistService mockDatabaseMetaDataPersistService;
    
    private final Properties mockProperties = new Properties();
    
    @Test
    public void createFactoryWithJDBCInstanceMetadata() throws Exception {
        initJDBCInstanceMock();
        initCommonMocks();
        
        ContextManagerBuilderParameter parameter = getContextManagerBuilderParameter();
        MetaDataContexts actualResponse = MetaDataContextsFactory.create(mockMetadataPersistService, parameter, mockInstanceContext);
        
        assertNotNull(actualResponse);
        assertEquals(mockMetadataPersistService, actualResponse.getPersistService());
    }
    
    @Test
    public void createFactoryWithNonJDBCInstanceMetadata() throws Exception {
        initNonJDBCInstanceMock();
        initCommonMocks();
        
        ContextManagerBuilderParameter parameter = getContextManagerBuilderParameter();
        
        MetaDataContexts actualResponse = MetaDataContextsFactory.create(mockMetadataPersistService, parameter, mockInstanceContext);
        
        assertNotNull(actualResponse);
        assertEquals(mockMetadataPersistService, actualResponse.getPersistService());
    }
    
    private void initJDBCInstanceMock() {
        when(mockComputeNodeInstance.getMetaData()).thenReturn(mockJDBCInstanceMetadata);
        when(mockInstanceContext.getInstance()).thenReturn(mockComputeNodeInstance);
    }
    
    private void initNonJDBCInstanceMock() {
        when(mockComputeNodeInstance.getMetaData()).thenReturn(null);
        when(mockInstanceContext.getInstance()).thenReturn(mockComputeNodeInstance);
        
        Set<String> mockDatabaseNames = new HashSet<>();
        mockDatabaseNames.add("h2");
        
        when(mockDatabaseMetaDataPersistService.loadAllDatabaseNames()).thenReturn(mockDatabaseNames);
        when(mockMetadataPersistService.getDatabaseMetaDataService()).thenReturn(mockDatabaseMetaDataPersistService);
    }
    
    private ContextManagerBuilderParameter getContextManagerBuilderParameter() {
        Map<String, DatabaseConfiguration> mockDatabaseConfigs = new HashMap<>();
        mockDatabaseConfigs.put("h2", dataSourceGeneratedDatabaseConfiguration);
        
        return new ContextManagerBuilderParameter(null, mockDatabaseConfigs, new ArrayList<>(), mockProperties, new ArrayList<>(), null);
    }
    
    private void initCommonMocks() {
        Map<String, DataSource> mockEffectiveDataSources = new HashMap<>();
        mockEffectiveDataSources.put("hikari", getHikariDataSource());
        
        when(mockMetadataPersistService.getEffectiveDataSources(eq("h2"), Mockito.anyMap())).thenReturn(mockEffectiveDataSources);
        when(mockMetadataPersistService.getDatabaseRulePersistService()).thenReturn(mockDatabaseRulePersistService);
        
        List<RuleConfiguration> dbRuleConfigurations = new ArrayList<>();
        dbRuleConfigurations.add(new MockedRuleConfiguration("h2RuleConfig"));
        
        when(mockDatabaseRulePersistService.load("h2")).thenReturn(dbRuleConfigurations);
        when(mockMetadataPersistService.getGlobalRuleService()).thenReturn(mockGlobalRulePersistService);
        
        List<RuleConfiguration> globalRuleConfigurations = new ArrayList<>();
        globalRuleConfigurations.add(new MockedRuleConfiguration("globalRuleConfig"));
        
        when(mockGlobalRulePersistService.load()).thenReturn(globalRuleConfigurations);
        when(mockMetadataPersistService.getPropsService()).thenReturn(mockPropertiesPersistService);
        when(mockPropertiesPersistService.load()).thenReturn(mockProperties);
        when(mockMetadataPersistService.getDatabaseMetaDataService()).thenReturn(mockDatabaseMetaDataPersistService);
        
        Map<String, ShardingSphereSchema> schemas = new HashMap<>();
        when(mockDatabaseMetaDataPersistService.load(eq("h2"))).thenReturn(schemas);
    }
    
    private HikariDataSource getHikariDataSource() {
        Properties props = new Properties();
        props.setProperty("jdbcUrl", "jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        props.setProperty("username", "sa");
        props.setProperty("password", "");
        
        HikariConfig hikariConfig = new HikariConfig(props);
        return new HikariDataSource(hikariConfig);
    }
}

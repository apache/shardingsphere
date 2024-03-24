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

package org.apache.shardingsphere.metadata.persist;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.fixture.FixtureRule;
import org.apache.shardingsphere.metadata.persist.fixture.YamlDataNodeGlobalRuleConfigurationFixture;
import org.apache.shardingsphere.metadata.persist.fixture.YamlDataNodeRuleConfigurationFixture;
import org.apache.shardingsphere.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.metadata.persist.service.config.database.datasource.DataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.rule.DatabaseRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.PropertiesPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaDataPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private DataSourceUnitPersistService dataSourceService;
    
    @Mock
    private DatabaseRulePersistService databaseRulePersistService;
    
    @Mock
    private DatabaseMetaDataPersistService databaseMetaDataService;
    
    @Mock
    private DataSourceUnitPersistService dataSourceUnitService;
    
    private MetaDataPersistService metaDataPersistService;
    
    private static final String DEFAULT_VERSION = "0";
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        GlobalRulePersistService globalRuleService = new GlobalRulePersistService(repository);
        PropertiesPersistService propsService = new PropertiesPersistService(repository);
        metaDataPersistService = new MetaDataPersistService(repository);
        setField("dataSourceUnitService", dataSourceService);
        setField("databaseRulePersistService", databaseRulePersistService);
        setField("globalRuleService", globalRuleService);
        setField("propsService", propsService);
        setField("databaseMetaDataService", databaseMetaDataService);
        setField("dataSourceUnitService", dataSourceUnitService);
    }
    
    private void setField(final String name, final Object value) throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(metaDataPersistService.getClass().getDeclaredField(name), metaDataPersistService, value);
    }
    
    @Test
    void assertLoadDataSourceConfigurations() {
        assertTrue(metaDataPersistService.loadDataSourceConfigurations("foo_db").isEmpty());
    }
    
    @Test
    void testPersistGlobalRuleConfiguration() {
        Collection<RuleConfiguration> expectRuleConfigs = buildRuleConfigs();
        when(repository.getDirectly(anyString())).thenReturn("0")
                .thenReturn("");
        Properties props = new Properties();
        when(repository.getChildrenKeys(any())).thenReturn(Collections.singletonList("0"))
                .thenReturn(Collections.emptyList());
        
        metaDataPersistService.persistGlobalRuleConfiguration(expectRuleConfigs, props);
        
        // Assert
        verify(repository, times(3)).persist(anyString(), anyString());
        verify(repository).persist(GlobalNode.getPropsActiveVersionNode(), DEFAULT_VERSION);
        verify(repository).persist(GlobalNode.getPropsVersionNode(DEFAULT_VERSION), YamlEngine.marshal(props));
    }
    
    @Test
    void testPersistConfigurations() throws SQLException {
        String databaseName = "test_database";
        DataSource datasource = mockDataSource();
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo_db", datasource),
                Collections.singletonList(new YamlDataNodeRuleConfigurationFixture("foo", "foo_val")));
        Map<String, DataSource> dataSourceMap = Maps.newHashMap();
        dataSourceMap.put("mysql", datasource);
        metaDataPersistService.persistConfigurations(databaseName, databaseConfig, dataSourceMap, Collections.singletonList(new FixtureRule()));
    }
    
    private Collection<RuleConfiguration> buildRuleConfigs() {
        YamlDataNodeGlobalRuleConfigurationFixture ruleConfigurationFixture = new YamlDataNodeGlobalRuleConfigurationFixture();
        ruleConfigurationFixture.setKey("foo");
        ruleConfigurationFixture.setValue("foo_value");
        return Collections.singletonList(ruleConfigurationFixture);
    }
    
    private DataSource mockDataSource() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        return new MockedDataSource(connection);
    }
    
}

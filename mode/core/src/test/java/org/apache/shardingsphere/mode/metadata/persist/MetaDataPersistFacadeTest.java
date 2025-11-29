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

package org.apache.shardingsphere.mode.metadata.persist;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.global.PropertiesPersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.DatabaseMetaDataPersistFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({TypedSPILoader.class, DataSourcePoolPropertiesCreator.class, GenericSchemaManager.class})
class MetaDataPersistFacadeTest {
    
    @Mock
    private DataSourceUnitPersistService dataSourceUnitService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseMetaDataPersistFacade databaseMetaDataFacade;
    
    @Mock
    private DatabaseRulePersistService databaseRuleService;
    
    @Mock
    private GlobalRulePersistService globalRuleService;
    
    @Mock
    private PropertiesPersistService propsService;
    
    private MetaDataPersistFacade metaDataPersistFacade;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        metaDataPersistFacade = new MetaDataPersistFacade(mock(PersistRepository.class));
        Plugins.getMemberAccessor().set(MetaDataPersistFacade.class.getDeclaredField("dataSourceUnitService"), metaDataPersistFacade, dataSourceUnitService);
        Plugins.getMemberAccessor().set(MetaDataPersistFacade.class.getDeclaredField("databaseMetaDataFacade"), metaDataPersistFacade, databaseMetaDataFacade);
        Plugins.getMemberAccessor().set(MetaDataPersistFacade.class.getDeclaredField("databaseRuleService"), metaDataPersistFacade, databaseRuleService);
        Plugins.getMemberAccessor().set(MetaDataPersistFacade.class.getDeclaredField("globalRuleService"), metaDataPersistFacade, globalRuleService);
        Plugins.getMemberAccessor().set(MetaDataPersistFacade.class.getDeclaredField("propsService"), metaDataPersistFacade, propsService);
    }
    
    @Test
    void assertPersistGlobalRuleConfiguration() {
        Collection<RuleConfiguration> globalRuleConfigs = Collections.singleton(mock(RuleConfiguration.class));
        Properties props = new Properties();
        metaDataPersistFacade.persistGlobalRuleConfiguration(globalRuleConfigs, props);
        verify(globalRuleService).persist(globalRuleConfigs);
        verify(propsService).persist(props);
    }
    
    @Test
    void assertPersistConfigurationsWithEmptyDatabase() {
        metaDataPersistFacade.persistConfigurations("foo_db", mock(DatabaseConfiguration.class), Collections.emptyMap(), Collections.emptyList());
        verify(databaseMetaDataFacade.getDatabase()).add("foo_db");
    }
    
    @Test
    void assertPersistConfigurationsWithDatabaseRuleConfigurations() {
        DatabaseConfiguration databaseConfig = mock(DatabaseConfiguration.class, RETURNS_DEEP_STUBS);
        when(databaseConfig.getStorageUnits()).thenReturn(Collections.emptyMap());
        when(databaseConfig.getRuleConfigurations().isEmpty()).thenReturn(false);
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass())).thenReturn(Optional.of(mock(RuleConfigurationDecorator.class)));
        metaDataPersistFacade.persistConfigurations("foo_db", databaseConfig, Collections.emptyMap(), Collections.singleton(rule));
        verify(dataSourceUnitService).persist("foo_db", Collections.emptyMap());
        verify(databaseRuleService).persist(eq("foo_db"), any());
    }
    
    @Test
    void assertPersistConfigurationsWithDataSourcePoolProperties() {
        DatabaseConfiguration databaseConfig = mock(DatabaseConfiguration.class, RETURNS_DEEP_STUBS);
        when(databaseConfig.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class, RETURNS_DEEP_STUBS)));
        metaDataPersistFacade.persistConfigurations("foo_db", databaseConfig, Collections.emptyMap(), Collections.emptyList());
        verify(dataSourceUnitService).persist(eq("foo_db"), any());
        verify(databaseRuleService).persist("foo_db", Collections.emptyList());
    }
    
    @Test
    void assertLoadDataSourceConfigurations() {
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class);
        when(dataSourceUnitService.load("foo_db")).thenReturn(Collections.singletonMap("foo_ds", dataSourcePoolProps));
        DataSourceConfiguration dataSourceConfig = mock(DataSourceConfiguration.class);
        when(DataSourcePoolPropertiesCreator.createConfiguration(dataSourcePoolProps)).thenReturn(dataSourceConfig);
        Map<String, DataSourceConfiguration> actual = metaDataPersistFacade.loadDataSourceConfigurations("foo_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_ds"), is(dataSourceConfig));
    }
}

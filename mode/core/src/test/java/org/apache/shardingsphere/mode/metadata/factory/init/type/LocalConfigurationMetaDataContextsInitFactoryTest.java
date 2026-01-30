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

package org.apache.shardingsphere.mode.metadata.factory.init.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ShardingSphereDatabasesFactory.class, GlobalRulesBuilder.class, ShardingSphereStatisticsFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class LocalConfigurationMetaDataContextsInitFactoryTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    void assertCreateWithPersistSchemasEnabled() throws SQLException {
        Map<String, DatabaseConfiguration> databaseConfigs = Collections.singletonMap("foo_db", mock(DatabaseConfiguration.class));
        Map<String, StorageUnit> storageUnits = Collections.singletonMap("foo_ds", mock(StorageUnit.class, RETURNS_DEEP_STUBS));
        ShardingSphereDatabase database = createDatabase("foo_db",
                Collections.singleton(new ShardingSphereSchema("empty_schema", mock(DatabaseType.class))), storageUnits, Collections.singletonList(mock(ShardingSphereRule.class)));
        ShardingSphereStatistics statistics = createStatistics("foo_db", "empty_schema");
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(ShardingSphereDatabasesFactory.create(eq(databaseConfigs), any(ConfigurationProperties.class), eq(instanceContext), any(DatabaseType.class)))
                .thenReturn(Collections.singletonList(database));
        when(GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.emptyList());
        when(ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(statistics);
        Properties props = PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED.getKey(), Boolean.TRUE.toString()));
        try (
                MockedConstruction<MetaDataPersistFacade> persistFacadeMocked = mockConstruction(MetaDataPersistFacade.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                        (mock, context) -> when(mock.getStatisticsService().load(any())).thenReturn(new ShardingSphereStatistics()))) {
            LocalConfigurationMetaDataContextsInitFactory factory = new LocalConfigurationMetaDataContextsInitFactory(repository, instanceContext, props);
            MetaDataContexts actual = factory.create(createContextManagerBuilderParameter(databaseConfigs, new Properties()));
            assertThat(actual, is(notNullValue()));
            MetaDataPersistFacade persistFacade = persistFacadeMocked.constructed().get(0);
            verify(persistFacade).persistGlobalRuleConfiguration(Collections.emptyList(), new Properties());
            verify(persistFacade).persistConfigurations(eq("foo_db"), eq(databaseConfigs.get("foo_db")),
                    argThat(dataSources -> storageUnits.get("foo_ds").getDataSource().equals(dataSources.get("foo_ds"))), anyCollection());
            verify(persistFacade.getDatabaseMetaDataFacade().getSchema()).add("foo_db", "empty_schema");
            verify(persistFacade.getDatabaseMetaDataFacade().getTable()).persist(eq("foo_db"), eq("empty_schema"), anyCollection());
            verify(persistFacade.getStatisticsService()).persist(database, "empty_schema", statistics.getDatabaseStatistics("foo_db").getSchemaStatistics("empty_schema"));
        }
    }
    
    @Test
    void assertCreateWithPersistSchemasDisabled() throws SQLException {
        Map<String, DatabaseConfiguration> databaseConfigs = Collections.singletonMap("foo_db", mock(DatabaseConfiguration.class));
        ShardingSphereSchema schema = new ShardingSphereSchema("non_empty_schema", mock(DatabaseType.class),
                Collections.singleton(new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList());
        ShardingSphereDatabase database = createDatabase("foo_db", Collections.singleton(schema), Collections.emptyMap(), Collections.emptyList());
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(ShardingSphereDatabasesFactory.create(eq(databaseConfigs), any(ConfigurationProperties.class), eq(instanceContext), any(DatabaseType.class)))
                .thenReturn(Collections.singletonList(database));
        when(GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.emptyList());
        when(ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(new ShardingSphereStatistics());
        Properties props = PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED.getKey(), Boolean.FALSE.toString()));
        try (
                MockedConstruction<MetaDataPersistFacade> persistFacadeMocked = mockConstruction(MetaDataPersistFacade.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                        (mock, context) -> when(mock.getStatisticsService().load(any())).thenReturn(new ShardingSphereStatistics()))) {
            LocalConfigurationMetaDataContextsInitFactory factory = new LocalConfigurationMetaDataContextsInitFactory(repository, instanceContext, props);
            factory.create(createContextManagerBuilderParameter(databaseConfigs, new Properties()));
            MetaDataPersistFacade persistFacade = persistFacadeMocked.constructed().get(0);
            verify(persistFacade).persistGlobalRuleConfiguration(Collections.emptyList(), new Properties());
            verify(persistFacade).persistConfigurations(eq("foo_db"), eq(databaseConfigs.get("foo_db")), anyMap(), anyCollection());
            verify(persistFacade.getDatabaseMetaDataFacade().getSchema(), never()).add(anyString(), anyString());
            verify(persistFacade.getDatabaseMetaDataFacade().getTable(), never()).persist(anyString(), anyString(), anyCollection());
            verify(persistFacade.getStatisticsService(), never()).persist(any(), anyString(), any());
            assertFalse(schema.isEmpty());
        }
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName, final Collection<ShardingSphereSchema> schemas,
                                                  final Map<String, StorageUnit> storageUnits, final Collection<ShardingSphereRule> rules) {
        return new ShardingSphereDatabase(databaseName, mock(DatabaseType.class), new ResourceMetaData(Collections.emptyMap(), storageUnits), new RuleMetaData(rules), schemas);
    }
    
    private ShardingSphereStatistics createStatistics(final String databaseName, final String schemaName) {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        databaseStatistics.putSchemaStatistics(schemaName, new SchemaStatistics());
        result.putDatabaseStatistics(databaseName, databaseStatistics);
        return result;
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter(final Map<String, DatabaseConfiguration> databaseConfigs, final Properties props) {
        return new ContextManagerBuilderParameter(null, databaseConfigs, Collections.emptyMap(), Collections.emptyList(), props, Collections.emptyList(), null);
    }
}

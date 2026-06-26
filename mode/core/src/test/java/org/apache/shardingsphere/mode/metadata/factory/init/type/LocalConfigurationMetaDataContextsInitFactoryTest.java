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
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ShardingSphereDatabasesFactory.class, GlobalRulesBuilder.class, ShardingSphereStatisticsFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class LocalConfigurationMetaDataContextsInitFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
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
        try (
                MockedConstruction<MetaDataPersistFacade> persistFacadeMocked = mockConstruction(MetaDataPersistFacade.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                        (mock, context) -> when(mock.getStatisticsService().load(any())).thenReturn(new ShardingSphereStatistics()))) {
            LocalConfigurationMetaDataContextsInitFactory factory = new LocalConfigurationMetaDataContextsInitFactory(repository, instanceContext);
            assertNotNull(factory.create(createContextManagerBuilderParameter(databaseConfigs, new Properties())));
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
    void assertCreateWithGlobalDataSources() throws SQLException {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSource> globalDataSources = Collections.singletonMap("foo_ds", dataSource);
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(ShardingSphereDatabasesFactory.create(anyMap(), any(ConfigurationProperties.class), eq(instanceContext), eq(databaseType))).thenReturn(Collections.emptyList());
        when(GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.emptyList());
        when(ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(new ShardingSphereStatistics());
        try (
                MockedStatic<DatabaseTypeEngine> databaseTypeEngineMocked = mockStatic(DatabaseTypeEngine.class);
                MockedConstruction<MetaDataPersistFacade> ignored = mockConstruction(MetaDataPersistFacade.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS))) {
            databaseTypeEngineMocked.when(() -> DatabaseTypeEngine.getProtocolType(anyMap(), any(ConfigurationProperties.class))).thenReturn(databaseType);
            LocalConfigurationMetaDataContextsInitFactory factory = new LocalConfigurationMetaDataContextsInitFactory(repository, instanceContext);
            MetaDataContexts actual = factory.create(new ContextManagerBuilderParameter(null, Collections.emptyMap(), globalDataSources, Collections.emptyList(), new Properties(),
                    Collections.emptyList(), null));
            assertThat(actual.getMetaData().getGlobalResourceMetaData().getStorageUnits().get("foo_ds").getStorageType(), is(databaseType));
        }
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName, final Collection<ShardingSphereSchema> schemas,
                                                  final Map<String, StorageUnit> storageUnits, final Collection<ShardingSphereRule> rules) {
        return new ShardingSphereDatabase(databaseName, mock(DatabaseType.class), new ResourceMetaData(Collections.emptyMap(), storageUnits), new RuleMetaData(rules), schemas,
                new ConfigurationProperties(new Properties()));
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

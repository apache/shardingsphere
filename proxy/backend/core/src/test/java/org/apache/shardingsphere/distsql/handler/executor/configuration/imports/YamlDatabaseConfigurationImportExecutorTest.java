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

package org.apache.shardingsphere.distsql.handler.executor.configuration.imports;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.validator.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterMetaDataManagerPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.retry.RetryExecutor;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({DataSourcePoolPropertiesValidator.class, GenericSchemaBuilder.class})
class YamlDatabaseConfigurationImportExecutorTest {
    
    private final DatabaseType protocolType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "MySQL")));
    
    private final Map<String, DataSourcePoolProperties> dataSourceProps = createDataSourceProperties();
    
    private ContextManager contextManager;
    
    private MetaDataContexts metaDataContexts;
    
    private MetaDataPersistFacade metaDataPersistFacade;
    
    private DataSourceUnitPersistService dataSourceUnitService;
    
    @BeforeEach
    void setUp() throws SQLException {
        contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        metaDataContexts = createMetaDataContexts(Collections.emptyList());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class);
        when(metaDataContextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        metaDataPersistFacade = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        dataSourceUnitService = metaDataPersistFacade.getDataSourceUnitService();
        when(metaDataContextManager.getMetaDataPersistFacade()).thenReturn(metaDataPersistFacade);
        ClusterMetaDataManagerPersistService registrationService = spy(new ClusterMetaDataManagerPersistService(metaDataContextManager, mock(ClusterPersistRepository.class)));
        doNothing().when(registrationService).createDatabase("foo_db");
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(registrationService);
        DatabaseRulePersistService databaseRulePersistService = metaDataPersistFacade.getDatabaseRuleService();
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseRuleService()).thenReturn(databaseRulePersistService);
        when(DataSourcePoolPropertiesValidator.validate(anyMap(), anyCollection())).thenReturn(Collections.emptyMap());
        when(GenericSchemaBuilder.build(any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenReturn(Collections.emptyMap());
    }
    
    private MetaDataContexts createMetaDataContexts(final Collection<ShardingSphereDatabase> databases) {
        return new MetaDataContexts(new ShardingSphereMetaData(databases, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), props, protocolType), new ShardingSphereStatistics());
    }
    
    @Test
    void assertImportDatabaseConfigurationNormally() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createRuleConfiguration();
        doAnswer(invocation -> {
            metaDataContexts.update(createRegisteredMetaDataContexts(dataSourceProps));
            return null;
        }).when(dataSourceUnitService).persist("foo_db", dataSourceProps);
        new YamlDatabaseConfigurationImportExecutor(contextManager).importDatabaseConfiguration("foo_db", dataSourceProps, Collections.singleton(ruleConfig));
        ShardingSphereDatabase actual = metaDataContexts.getMetaData().getDatabase("foo_db");
        assertThat(actual.getResourceMetaData().getStorageUnits().keySet(), containsInAnyOrder("foo_ds_0", "foo_ds_1"));
        assertThat(actual.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration(), is(ruleConfig));
        verify(metaDataPersistFacade.getDatabaseRuleService()).persist(eq("foo_db"), argThat(configs -> 1 == configs.size() && configs.contains(ruleConfig)));
    }
    
    private ReadwriteSplittingRuleConfiguration createRuleConfiguration() {
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(
                new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "foo_ds_0", Collections.singletonList("foo_ds_1"), "")), Collections.emptyMap());
    }
    
    private MetaDataContexts createRegisteredMetaDataContexts(final Map<String, DataSourcePoolProperties> registeredProps) {
        Map<String, DataSource> dataSources = new LinkedHashMap<>(registeredProps.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : registeredProps.entrySet()) {
            MockedDataSource dataSource = new MockedDataSource();
            dataSource.setUrl(entry.getValue().getConnectionPropertySynonyms().getStandardProperties().get("url").toString());
            dataSources.put(entry.getKey(), dataSource);
        }
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", protocolType, new ResourceMetaData(dataSources),
                new RuleMetaData(Collections.emptyList()), Collections.emptyList(), props);
        return createMetaDataContexts(Collections.singleton(database));
    }
    
    @Test
    void assertImportDatabaseConfigurationWithDelayedStorageUnitNotification() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createRuleConfiguration();
        MetaDataContexts pendingMetaDataContexts = createRegisteredMetaDataContexts(Collections.singletonMap("foo_ds_0", dataSourceProps.get("foo_ds_0")));
        MetaDataContexts completeMetaDataContexts = createRegisteredMetaDataContexts(dataSourceProps);
        AtomicBoolean registrationStarted = new AtomicBoolean();
        AtomicBoolean notificationsDelivered = new AtomicBoolean();
        doAnswer(invocation -> {
            registrationStarted.set(true);
            return null;
        }).when(dataSourceUnitService).persist("foo_db", dataSourceProps);
        doAnswer(invocation -> {
            if (registrationStarted.get() && !notificationsDelivered.get()
                    && metaDataContexts.getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnits().keySet().containsAll(dataSourceProps.keySet())) {
                metaDataContexts.update(pendingMetaDataContexts);
                notificationsDelivered.set(true);
            }
            return metaDataContexts;
        }).when(contextManager).getMetaDataContexts();
        try (MockedConstruction<RetryExecutor> ignored = mockConstruction(RetryExecutor.class, (retry, context) -> when(retry.execute(any(), any())).thenAnswer(invocation -> {
            Predicate<Object> predicate = invocation.getArgument(0);
            Object arg = invocation.getArgument(1);
            if (predicate.test(arg)) {
                return true;
            }
            metaDataContexts.update(pendingMetaDataContexts);
            metaDataContexts.update(completeMetaDataContexts);
            notificationsDelivered.set(true);
            return predicate.test(arg);
        }))) {
            new YamlDatabaseConfigurationImportExecutor(contextManager).importDatabaseConfiguration("foo_db", dataSourceProps, Collections.singleton(ruleConfig));
        }
        ShardingSphereDatabase actual = metaDataContexts.getMetaData().getDatabase("foo_db");
        assertTrue(notificationsDelivered.get());
        assertThat(actual.getResourceMetaData().getStorageUnits().keySet(), containsInAnyOrder("foo_ds_0", "foo_ds_1"));
        assertThat(actual.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration(), is(ruleConfig));
        verify(metaDataPersistFacade.getDatabaseRuleService()).persist(eq("foo_db"), argThat(configs -> 1 == configs.size() && configs.contains(ruleConfig)));
    }
    
    private Map<String, DataSourcePoolProperties> createDataSourceProperties() {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(2, 1F);
        for (String each : Arrays.asList("foo_ds_0", "foo_ds_1")) {
            Map<String, Object> poolProps = new LinkedHashMap<>(3, 1F);
            poolProps.put("url", "jdbc:mock://127.0.0.1/" + each);
            poolProps.put("username", "root");
            poolProps.put("password", "root");
            result.put(each, new DataSourcePoolProperties(MockedDataSource.class.getName(), poolProps));
        }
        return result;
    }
}

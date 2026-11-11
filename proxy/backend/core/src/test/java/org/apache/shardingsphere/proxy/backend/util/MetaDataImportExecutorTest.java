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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.handler.executor.configuration.export.model.ExportedMetaData;
import org.apache.shardingsphere.distsql.handler.executor.configuration.imports.MetaDataImportExecutor;
import org.apache.shardingsphere.distsql.handler.executor.configuration.imports.YamlDatabaseConfigurationImportExecutor;
import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.YamlDataSourceConfiguration;
import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.YamlDatabaseConfiguration;
import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.YamlExportedServerConfiguration;
import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.swapper.YamlDatabaseConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaDataImportExecutorTest {
    
    @Mock
    private YamlDatabaseConfigurationImportExecutor databaseConfigImportExecutor;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Test
    void assertImportClusterConfigurationsWithoutServerConfiguration() {
        ExportedMetaData exportedMetaData = createExportedMetaData();
        MetaDataImportExecutor executor = new MetaDataImportExecutor(contextManager);
        setField(executor, "databaseConfigImportExecutor", databaseConfigImportExecutor);
        YamlDatabaseConfiguration databaseConfig = new YamlDatabaseConfiguration();
        databaseConfig.setDatabaseName("logic_db");
        try (MockedStatic<YamlEngine> mockedStatic = mockStatic(YamlEngine.class)) {
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlExportedServerConfiguration.class))).thenReturn(null);
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlDatabaseConfiguration.class))).thenReturn(databaseConfig);
            executor.importClusterConfigurations(exportedMetaData);
        }
        verify(databaseConfigImportExecutor).importDatabaseConfiguration("logic_db", Collections.emptyMap(), Collections.emptyList());
    }
    
    @Test
    void assertImportClusterConfigurations() {
        MetaDataManagerPersistService metaDataManagerService = mock(MetaDataManagerPersistService.class);
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerService);
        MetaDataImportExecutor executor = new MetaDataImportExecutor(contextManager);
        setField(executor, "databaseConfigImportExecutor", databaseConfigImportExecutor);
        YamlRuleConfigurationSwapperEngine swapperEngine = mock(YamlRuleConfigurationSwapperEngine.class);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        when(swapperEngine.swapToRuleConfigurations(anyCollection())).thenReturn(Collections.singleton(ruleConfig));
        setField(executor, "ruleConfigSwapperEngine", swapperEngine);
        Properties props = new Properties();
        YamlExportedServerConfiguration serverConfig = new YamlExportedServerConfiguration();
        serverConfig.setProps(props);
        YamlDatabaseConfiguration firstYamlConfig = new YamlDatabaseConfiguration();
        firstYamlConfig.setDatabaseName("foo_db");
        firstYamlConfig.setDataSources(Collections.singletonMap("foo_ds", new YamlDataSourceConfiguration()));
        firstYamlConfig.setRules(Collections.singletonList(mock(YamlRuleConfiguration.class)));
        YamlDatabaseConfiguration secondYamlConfig = new YamlDatabaseConfiguration();
        secondYamlConfig.setDatabaseName("bar_db");
        secondYamlConfig.setDataSources(Collections.singletonMap("bar_ds", new YamlDataSourceConfiguration()));
        secondYamlConfig.setRules(Collections.singletonList(mock(YamlRuleConfiguration.class)));
        Map<String, DataSourcePoolProperties> firstDataSourceProps = Collections.singletonMap("foo_ds", mock(DataSourcePoolProperties.class));
        Map<String, DataSourcePoolProperties> secondDataSourceProps = Collections.singletonMap("bar_ds", mock(DataSourcePoolProperties.class));
        Collection<RuleConfiguration> firstRuleConfigs = Collections.singletonList(mock(RuleConfiguration.class));
        Collection<RuleConfiguration> secondRuleConfigs = Collections.singletonList(mock(RuleConfiguration.class));
        YamlDatabaseConfigurationSwapper databaseConfigSwapper = mock(YamlDatabaseConfigurationSwapper.class);
        setField(executor, "databaseConfigSwapper", databaseConfigSwapper);
        when(databaseConfigSwapper.swapDataSourceProperties(firstYamlConfig.getDataSources())).thenReturn(firstDataSourceProps);
        when(databaseConfigSwapper.swapDataSourceProperties(secondYamlConfig.getDataSources())).thenReturn(secondDataSourceProps);
        when(databaseConfigSwapper.swapRuleConfigurations(firstYamlConfig.getRules())).thenReturn(firstRuleConfigs);
        when(databaseConfigSwapper.swapRuleConfigurations(secondYamlConfig.getRules())).thenReturn(secondRuleConfigs);
        Map<String, String> databases = new LinkedHashMap<>(2, 1F);
        databases.put("foo_key", "foo_database_yaml");
        databases.put("bar_key", "bar_database_yaml");
        ExportedMetaData exportedMetaData = createExportedMetaData();
        exportedMetaData.setDatabases(databases);
        try (MockedStatic<YamlEngine> mockedStatic = mockStatic(YamlEngine.class)) {
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlExportedServerConfiguration.class))).thenReturn(serverConfig);
            mockedStatic.when(() -> YamlEngine.unmarshal("foo_database_yaml", YamlDatabaseConfiguration.class)).thenReturn(firstYamlConfig);
            mockedStatic.when(() -> YamlEngine.unmarshal("bar_database_yaml", YamlDatabaseConfiguration.class)).thenReturn(secondYamlConfig);
            executor.importClusterConfigurations(exportedMetaData);
        }
        InOrder inOrder = inOrder(metaDataManagerService, databaseConfigImportExecutor);
        inOrder.verify(metaDataManagerService).alterGlobalRuleConfiguration(ruleConfig);
        inOrder.verify(metaDataManagerService).alterProperties(props);
        inOrder.verify(databaseConfigImportExecutor).importDatabaseConfiguration("foo_db", firstDataSourceProps, firstRuleConfigs);
        inOrder.verify(databaseConfigImportExecutor).importDatabaseConfiguration("bar_db", secondDataSourceProps, secondRuleConfigs);
    }
    
    @Test
    void assertImportClusterConfigurationsWhenDatabaseConversionFails() {
        MetaDataImportExecutor executor = new MetaDataImportExecutor(contextManager);
        setField(executor, "databaseConfigImportExecutor", databaseConfigImportExecutor);
        YamlDatabaseConfigurationSwapper databaseConfigSwapper = mock(YamlDatabaseConfigurationSwapper.class);
        setField(executor, "databaseConfigSwapper", databaseConfigSwapper);
        YamlDatabaseConfiguration firstYamlConfig = new YamlDatabaseConfiguration();
        YamlDatabaseConfiguration secondYamlConfig = new YamlDatabaseConfiguration();
        when(databaseConfigSwapper.swapRuleConfigurations(anyCollection())).thenReturn(Collections.emptyList()).thenThrow(IllegalStateException.class);
        ExportedMetaData exportedMetaData = createExportedMetaData();
        Map<String, String> databases = new LinkedHashMap<>(2, 1F);
        databases.put("foo_db", "foo_database_yaml");
        databases.put("bar_db", "bar_database_yaml");
        exportedMetaData.setDatabases(databases);
        try (MockedStatic<YamlEngine> mockedStatic = mockStatic(YamlEngine.class)) {
            mockedStatic.when(() -> YamlEngine.unmarshal("foo_database_yaml", YamlDatabaseConfiguration.class)).thenReturn(firstYamlConfig);
            mockedStatic.when(() -> YamlEngine.unmarshal("bar_database_yaml", YamlDatabaseConfiguration.class)).thenReturn(secondYamlConfig);
            assertThrows(IllegalStateException.class, () -> executor.importClusterConfigurations(exportedMetaData));
        }
        verifyNoInteractions(contextManager, databaseConfigImportExecutor);
    }
    
    @Test
    void assertImportClusterConfigurationsWhenServerConversionFails() {
        MetaDataImportExecutor executor = new MetaDataImportExecutor(contextManager);
        setField(executor, "databaseConfigImportExecutor", databaseConfigImportExecutor);
        YamlRuleConfigurationSwapperEngine swapperEngine = mock(YamlRuleConfigurationSwapperEngine.class);
        setField(executor, "ruleConfigSwapperEngine", swapperEngine);
        when(swapperEngine.swapToRuleConfigurations(anyCollection())).thenThrow(IllegalStateException.class);
        try (MockedStatic<YamlEngine> mockedStatic = mockStatic(YamlEngine.class)) {
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlDatabaseConfiguration.class))).thenReturn(new YamlDatabaseConfiguration());
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlExportedServerConfiguration.class))).thenReturn(new YamlExportedServerConfiguration());
            assertThrows(IllegalStateException.class, () -> executor.importClusterConfigurations(createExportedMetaData()));
        }
        verifyNoInteractions(contextManager, databaseConfigImportExecutor);
    }
    
    @Test
    void assertExportedServerConfigurationYamlCompatibility() {
        Properties props = new Properties();
        props.setProperty("sql-show", "true");
        String yaml = "rules:" + System.lineSeparator() + YamlEngine.marshal(Collections.singletonList(new YamlSQLParserRuleConfiguration()))
                + System.lineSeparator() + YamlEngine.marshal(Collections.singletonMap("props", props));
        YamlExportedServerConfiguration actual = YamlEngine.unmarshal(yaml, YamlExportedServerConfiguration.class);
        YamlProxyServerConfiguration previous = YamlEngine.unmarshal(yaml, YamlProxyServerConfiguration.class);
        assertThat(actual.getProps(), is(previous.getProps()));
        assertThat(actual.getRules().size(), is(1));
        assertThat(actual.getRules().iterator().next().getClass(), is(previous.getRules().iterator().next().getClass()));
    }
    
    private ExportedMetaData createExportedMetaData() {
        ExportedMetaData result = new ExportedMetaData();
        result.setDatabases(Collections.singletonMap("logic_db", "databaseName: logic_db"));
        result.setProps("");
        result.setRules("");
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setField(final Object target, final String fieldName, final Object value) {
        Plugins.getMemberAccessor().set(MetaDataImportExecutor.class.getDeclaredField(fieldName), target, value);
    }
}

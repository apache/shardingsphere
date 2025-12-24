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
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
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
        YamlProxyDatabaseConfiguration databaseConfig = new YamlProxyDatabaseConfiguration();
        databaseConfig.setDatabaseName("logic_db");
        try (MockedStatic<YamlEngine> mockedStatic = mockStatic(YamlEngine.class)) {
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlProxyServerConfiguration.class))).thenReturn(null);
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlProxyDatabaseConfiguration.class))).thenReturn(databaseConfig);
            executor.importClusterConfigurations(exportedMetaData);
        }
        verify(databaseConfigImportExecutor).importDatabaseConfiguration(databaseConfig);
    }
    
    @Test
    void assertImportClusterConfigurations() {
        MetaDataManagerPersistService metaDataManagerService = mock(MetaDataManagerPersistService.class);
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerService);
        ExportedMetaData exportedMetaData = createExportedMetaData();
        MetaDataImportExecutor executor = new MetaDataImportExecutor(contextManager);
        setField(executor, "databaseConfigImportExecutor", databaseConfigImportExecutor);
        YamlRuleConfigurationSwapperEngine swapperEngine = mock(YamlRuleConfigurationSwapperEngine.class);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        when(swapperEngine.swapToRuleConfigurations(anyCollection())).thenReturn(Collections.singleton(ruleConfig));
        setField(executor, "ruleConfigSwapperEngine", swapperEngine);
        Properties props = new Properties();
        YamlProxyServerConfiguration serverConfig = new YamlProxyServerConfiguration();
        serverConfig.setProps(props);
        YamlProxyDatabaseConfiguration databaseConfig = new YamlProxyDatabaseConfiguration();
        databaseConfig.setDatabaseName("logic_db");
        try (MockedStatic<YamlEngine> mockedStatic = mockStatic(YamlEngine.class)) {
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlProxyServerConfiguration.class))).thenReturn(serverConfig);
            mockedStatic.when(() -> YamlEngine.unmarshal(anyString(), eq(YamlProxyDatabaseConfiguration.class))).thenReturn(databaseConfig);
            executor.importClusterConfigurations(exportedMetaData);
        }
        verify(metaDataManagerService).alterGlobalRuleConfiguration(ruleConfig);
        verify(metaDataManagerService).alterProperties(props);
        ArgumentCaptor<YamlProxyDatabaseConfiguration> captor = ArgumentCaptor.forClass(YamlProxyDatabaseConfiguration.class);
        verify(databaseConfigImportExecutor).importDatabaseConfiguration(captor.capture());
        assertThat(captor.getValue().getDatabaseName(), is("logic_db"));
    }
    
    private ExportedMetaData createExportedMetaData() {
        ExportedMetaData result = new ExportedMetaData();
        result.setDatabases(Collections.singletonMap("logic_db", "databaseName: logic_db"));
        result.setProps("");
        result.setRules("");
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setField(Object target, String fieldName, Object value) {
        Plugins.getMemberAccessor().set(MetaDataImportExecutor.class.getDeclaredField(fieldName), target, value);
    }
}

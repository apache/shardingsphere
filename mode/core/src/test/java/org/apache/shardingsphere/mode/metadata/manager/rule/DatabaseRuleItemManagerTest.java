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

package org.apache.shardingsphere.mode.metadata.manager.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import javax.validation.constraints.NotBlank;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TypedSPILoader.class)
class DatabaseRuleItemManagerTest {
    
    private static final String DATABASE_NAME = "foo_db";
    
    @Mock
    private DatabaseRuleConfigurationManager ruleConfigManager;
    
    @Test
    void assertAlterSuccess() throws SQLException {
        MetaDataPersistFacade persistFacade = mockPersistFacade();
        RuleConfiguration currentRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleConfiguration candidateRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when(processor.swapRuleItemConfiguration(any(), any())).thenReturn(new Object());
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", "type"))).thenReturn(processor);
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS), ruleConfigManager, persistFacade);
        try (MockedConstruction<YamlRuleConfigurationSwapperEngine> ignored = mockYamlSwapper(currentRuleConfig, candidateRuleConfig)) {
            manager.alter(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", new DatabaseRuleItem("type/item")));
        }
        verify(ruleConfigManager).refresh(DATABASE_NAME, candidateRuleConfig);
        verify(processor).changeRuleItemConfiguration(eq("item"), same(candidateRuleConfig), any());
        verify(processor, never()).changeRuleItemConfiguration(any(), same(currentRuleConfig), any());
    }
    
    @Test
    void assertAlterWrapsSQLException() throws SQLException {
        MetaDataPersistFacade persistFacade = mockPersistFacade();
        RuleConfiguration currentRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleConfiguration candidateRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when(processor.swapRuleItemConfiguration(any(), any())).thenReturn(new Object());
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", "type"))).thenReturn(processor);
        doThrow(SQLException.class).when(ruleConfigManager).refresh(DATABASE_NAME, candidateRuleConfig);
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS), ruleConfigManager, persistFacade);
        try (MockedConstruction<YamlRuleConfigurationSwapperEngine> ignored = mockYamlSwapper(currentRuleConfig, candidateRuleConfig)) {
            assertThrows(SQLWrapperException.class, () -> manager.alter(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", new DatabaseRuleItem("type/item"))));
        }
        verify(processor).changeRuleItemConfiguration(eq("item"), same(candidateRuleConfig), any());
        verify(processor, never()).changeRuleItemConfiguration(any(), same(currentRuleConfig), any());
    }
    
    @Test
    void assertAlterWithInvalidRuleItemConfiguration() throws SQLException {
        MetaDataPersistFacade persistFacade = mockPersistFacade();
        RuleConfiguration currentRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleConfiguration candidateRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when(processor.swapRuleItemConfiguration(any(), any())).thenReturn(new FixtureRuleItemConfiguration(""));
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", "type"))).thenReturn(processor);
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS), ruleConfigManager, persistFacade);
        try (MockedConstruction<YamlRuleConfigurationSwapperEngine> ignored = mockYamlSwapper(currentRuleConfig, candidateRuleConfig)) {
            assertThrows(InvalidRuleConfigurationException.class, () -> manager.alter(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", new DatabaseRuleItem("type/item"))));
        }
        verify(processor, never()).changeRuleItemConfiguration(any(), any(), any());
        verify(ruleConfigManager, never()).refresh(any(), any());
    }
    
    @Test
    void assertDropSuccess() throws SQLException {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().containsDatabase(DATABASE_NAME)).thenReturn(true);
        RuleConfiguration currentRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleConfiguration candidateRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", null))).thenReturn(processor);
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(metaDataContexts, ruleConfigManager, mock(MetaDataPersistFacade.class));
        try (MockedConstruction<YamlRuleConfigurationSwapperEngine> ignored = mockYamlSwapper(currentRuleConfig, candidateRuleConfig)) {
            manager.drop(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", null));
        }
        verify(processor).dropRuleItemConfiguration(null, candidateRuleConfig);
        verify(processor, never()).dropRuleItemConfiguration(any(), same(currentRuleConfig));
        verify(ruleConfigManager).refresh(DATABASE_NAME, candidateRuleConfig);
    }
    
    @Test
    void assertDropWrapsSQLException() throws SQLException {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().containsDatabase(DATABASE_NAME)).thenReturn(true);
        RuleConfiguration currentRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleConfiguration candidateRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", "type"))).thenReturn(processor);
        doThrow(new SQLException("drop")).when(ruleConfigManager).refresh(DATABASE_NAME, candidateRuleConfig);
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(metaDataContexts, ruleConfigManager, mock(MetaDataPersistFacade.class));
        try (MockedConstruction<YamlRuleConfigurationSwapperEngine> ignored = mockYamlSwapper(currentRuleConfig, candidateRuleConfig)) {
            assertThrows(SQLWrapperException.class, () -> manager.drop(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", new DatabaseRuleItem("type/item"))));
        }
        verify(processor).dropRuleItemConfiguration("item", candidateRuleConfig);
        verify(processor, never()).dropRuleItemConfiguration(any(), same(currentRuleConfig));
    }
    
    private MetaDataPersistFacade mockPersistFacade() {
        MetaDataPersistFacade result = mock(MetaDataPersistFacade.class);
        VersionPersistService versionService = mock(VersionPersistService.class);
        when(versionService.loadContent(any())).thenReturn("yaml-content");
        when(result.getVersionService()).thenReturn(versionService);
        return result;
    }
    
    private MockedConstruction<YamlRuleConfigurationSwapperEngine> mockYamlSwapper(final RuleConfiguration currentRuleConfig, final RuleConfiguration candidateRuleConfig) {
        YamlRuleConfiguration yamlRuleConfig = mock(YamlRuleConfiguration.class);
        return mockConstruction(YamlRuleConfigurationSwapperEngine.class, (mock, context) -> {
            when(mock.swapToYamlRuleConfiguration(currentRuleConfig)).thenReturn(yamlRuleConfig);
            when(mock.swapToRuleConfiguration(yamlRuleConfig)).thenReturn(candidateRuleConfig);
        });
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class FixtureRuleItemConfiguration {
        
        @NotBlank
        private final String name;
    }
}

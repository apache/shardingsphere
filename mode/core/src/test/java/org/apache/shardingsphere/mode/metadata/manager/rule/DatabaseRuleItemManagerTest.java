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

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when(processor.swapRuleItemConfiguration(any(), any())).thenReturn(new Object());
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", "type"))).thenReturn(processor);
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS), ruleConfigManager, persistFacade);
        manager.alter(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", new DatabaseRuleItem("type/item")));
        verify(ruleConfigManager).refresh(DATABASE_NAME, currentRuleConfig, true);
        verify(processor).changeRuleItemConfiguration(eq("item"), eq(currentRuleConfig), any());
    }
    
    @Test
    void assertAlterWrapsSQLException() throws SQLException {
        MetaDataPersistFacade persistFacade = mockPersistFacade();
        RuleConfiguration currentRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when(processor.swapRuleItemConfiguration(any(), any())).thenReturn(new Object());
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", "type"))).thenReturn(processor);
        doThrow(SQLException.class).when(ruleConfigManager).refresh(DATABASE_NAME, currentRuleConfig, true);
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS), ruleConfigManager, persistFacade);
        assertThrows(SQLWrapperException.class, () -> manager.alter(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", new DatabaseRuleItem("type/item"))));
        verify(processor).changeRuleItemConfiguration(eq("item"), eq(currentRuleConfig), any());
    }
    
    @Test
    void assertDropSuccess() throws SQLException {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().containsDatabase(DATABASE_NAME)).thenReturn(true);
        RuleConfiguration currentRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", null))).thenReturn(processor);
        when((TypedSPI) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, currentRuleConfig.getClass())).thenReturn(mock(DatabaseRuleConfigurationEmptyChecker.class));
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(metaDataContexts, ruleConfigManager, mock(MetaDataPersistFacade.class));
        manager.drop(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", null));
        verify(processor).dropRuleItemConfiguration(null, currentRuleConfig);
        verify(ruleConfigManager).refresh(DATABASE_NAME, currentRuleConfig, true);
    }
    
    @Test
    void assertDropWrapsSQLException() throws SQLException {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().containsDatabase(DATABASE_NAME)).thenReturn(true);
        RuleConfiguration currentRuleConfig = mock(DatabaseRuleConfiguration.class);
        RuleItemConfigurationChangedProcessor processor = mock(RuleItemConfigurationChangedProcessor.class);
        when(processor.findRuleConfiguration(any(ShardingSphereDatabase.class))).thenReturn(currentRuleConfig);
        when((TypedSPI) TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("ruleType", "type"))).thenReturn(processor);
        when((TypedSPI) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, currentRuleConfig.getClass())).thenReturn(mock(DatabaseRuleConfigurationEmptyChecker.class));
        doThrow(new SQLException("drop")).when(ruleConfigManager).refresh(DATABASE_NAME, currentRuleConfig, true);
        DatabaseRuleItemManager manager = new DatabaseRuleItemManager(metaDataContexts, ruleConfigManager, mock(MetaDataPersistFacade.class));
        assertThrows(SQLWrapperException.class, () -> manager.drop(new DatabaseRuleNodePath(DATABASE_NAME, "ruleType", new DatabaseRuleItem("type/item"))));
        verify(processor).dropRuleItemConfiguration("item", currentRuleConfig);
    }
    
    private MetaDataPersistFacade mockPersistFacade() {
        MetaDataPersistFacade result = mock(MetaDataPersistFacade.class);
        VersionPersistService versionService = mock(VersionPersistService.class);
        when(versionService.loadContent(any())).thenReturn("yaml-content");
        when(result.getVersionService()).thenReturn(versionService);
        return result;
    }
}

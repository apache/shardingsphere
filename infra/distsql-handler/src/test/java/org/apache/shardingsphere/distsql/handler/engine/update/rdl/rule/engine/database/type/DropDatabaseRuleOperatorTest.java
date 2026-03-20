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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type;

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.aware.StaticDataSourceContainedRuleAwareStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.DatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.apache.shardingsphere.mode.persist.mode.ModePersistServiceFacade;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings({"rawtypes", "unchecked"})
class DropDatabaseRuleOperatorTest {
    
    private final ContextManager contextManager = mock(ContextManager.class);
    
    private final PersistServiceFacade persistServiceFacade = mock(PersistServiceFacade.class);
    
    private final ModePersistServiceFacade modePersistServiceFacade = mock(ModePersistServiceFacade.class);
    
    private final MetaDataManagerPersistService metaDataManagerPersistService = mock(MetaDataManagerPersistService.class);
    
    private final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
    
    private final RuleMetaData ruleMetaData = mock(RuleMetaData.class);
    
    private final DatabaseRuleDropExecutor executor = mock(DatabaseRuleDropExecutor.class);
    
    private final DropDatabaseRuleOperator ruleOperator = new DropDatabaseRuleOperator(contextManager, executor);
    
    @Test
    void assertOperateWhenNoRuleItemToDrop() {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        RuleConfiguration currentRuleConfig = mock(RuleConfiguration.class);
        when(executor.hasAnyOneToBeDropped(sqlStatement)).thenReturn(false);
        ruleOperator.operate(sqlStatement, database, currentRuleConfig);
        verify(executor, never()).buildToBeDroppedRuleConfiguration(sqlStatement);
        verify(executor, never()).buildToBeAlteredRuleConfiguration(sqlStatement);
        verifyNoInteractions(metaDataManagerPersistService);
    }
    
    @Test
    void assertOperateWhenStaticDataSourceStatementWithoutAlteredRuleConfig() {
        StaticDataSourceRuleAttribute firstAttribute = mock(StaticDataSourceRuleAttribute.class);
        StaticDataSourceRuleAttribute secondAttribute = mock(StaticDataSourceRuleAttribute.class);
        when(contextManager.getPersistServiceFacade()).thenReturn(persistServiceFacade);
        when(persistServiceFacade.getModeFacade()).thenReturn(modePersistServiceFacade);
        when(modePersistServiceFacade.getMetaDataManagerService()).thenReturn(metaDataManagerPersistService);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(ruleMetaData.getAttributes(StaticDataSourceRuleAttribute.class)).thenReturn(Arrays.asList(firstAttribute, secondAttribute));
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class, withSettings().extraInterfaces(StaticDataSourceContainedRuleAwareStatement.class));
        when(((StaticDataSourceContainedRuleAwareStatement) sqlStatement).getNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(executor.hasAnyOneToBeDropped(sqlStatement)).thenReturn(true);
        RuleConfiguration toBeDroppedRuleItemConfig = mock(RuleConfiguration.class);
        when(executor.buildToBeDroppedRuleConfiguration(sqlStatement)).thenReturn(toBeDroppedRuleItemConfig);
        RuleConfiguration currentRuleConfig = mock(RuleConfiguration.class);
        when(executor.buildToBeAlteredRuleConfiguration(sqlStatement)).thenReturn(null);
        ruleOperator.operate(sqlStatement, database, currentRuleConfig);
        verify(firstAttribute).cleanStorageNodeDataSource("ds_0");
        verify(firstAttribute).cleanStorageNodeDataSource("ds_1");
        verify(secondAttribute).cleanStorageNodeDataSource("ds_0");
        verify(secondAttribute).cleanStorageNodeDataSource("ds_1");
        verify(metaDataManagerPersistService).removeRuleConfigurationItem(database, toBeDroppedRuleItemConfig);
        verify(metaDataManagerPersistService, never()).alterRuleConfiguration(database, currentRuleConfig);
        verify(metaDataManagerPersistService, never()).removeRuleConfiguration(database, currentRuleConfig, "test_rule");
    }
    
    @Test
    void assertOperateWhenAlteredRuleConfigNotEmpty() {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        when(contextManager.getPersistServiceFacade()).thenReturn(persistServiceFacade);
        when(persistServiceFacade.getModeFacade()).thenReturn(modePersistServiceFacade);
        when(modePersistServiceFacade.getMetaDataManagerService()).thenReturn(metaDataManagerPersistService);
        when(executor.hasAnyOneToBeDropped(sqlStatement)).thenReturn(true);
        RuleConfiguration toBeDroppedRuleItemConfig = mock(RuleConfiguration.class);
        when(executor.buildToBeDroppedRuleConfiguration(sqlStatement)).thenReturn(toBeDroppedRuleItemConfig);
        TestDatabaseRuleConfiguration toBeAlteredRuleConfig = new TestDatabaseRuleConfiguration();
        when(executor.buildToBeAlteredRuleConfiguration(sqlStatement)).thenReturn(toBeAlteredRuleConfig);
        DatabaseRuleConfigurationEmptyChecker emptyChecker = mock(DatabaseRuleConfigurationEmptyChecker.class);
        when(emptyChecker.isEmpty(toBeAlteredRuleConfig)).thenReturn(false);
        RuleConfiguration currentRuleConfig = mock(RuleConfiguration.class);
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, TestDatabaseRuleConfiguration.class)).thenReturn(emptyChecker);
            ruleOperator.operate(sqlStatement, database, currentRuleConfig);
        }
        verify(metaDataManagerPersistService).removeRuleConfigurationItem(database, toBeDroppedRuleItemConfig);
        verify(metaDataManagerPersistService).alterRuleConfiguration(database, toBeAlteredRuleConfig);
        verify(metaDataManagerPersistService, never()).removeRuleConfiguration(database, currentRuleConfig, "test_rule");
    }
    
    @Test
    void assertOperateWhenAlteredRuleConfigEmpty() {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        when(contextManager.getPersistServiceFacade()).thenReturn(persistServiceFacade);
        when(persistServiceFacade.getModeFacade()).thenReturn(modePersistServiceFacade);
        when(modePersistServiceFacade.getMetaDataManagerService()).thenReturn(metaDataManagerPersistService);
        when(executor.hasAnyOneToBeDropped(sqlStatement)).thenReturn(true);
        RuleConfiguration toBeDroppedRuleItemConfig = mock(RuleConfiguration.class);
        when(executor.buildToBeDroppedRuleConfiguration(sqlStatement)).thenReturn(toBeDroppedRuleItemConfig);
        TestDatabaseRuleConfiguration toBeAlteredRuleConfig = new TestDatabaseRuleConfiguration();
        when(executor.buildToBeAlteredRuleConfiguration(sqlStatement)).thenReturn(toBeAlteredRuleConfig);
        DatabaseRuleConfigurationEmptyChecker emptyChecker = mock(DatabaseRuleConfigurationEmptyChecker.class);
        when(emptyChecker.isEmpty(toBeAlteredRuleConfig)).thenReturn(true);
        RuleConfiguration currentRuleConfig = mock(RuleConfiguration.class);
        try (
                MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class);
                MockedConstruction<YamlRuleConfigurationSwapperEngine> ignored = mockConstruction(YamlRuleConfigurationSwapperEngine.class,
                        (mock, context) -> when(mock.swapToYamlRuleConfiguration(currentRuleConfig)).thenReturn(new TestYamlRuleConfiguration()))) {
            mockedStatic.when(() -> TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, TestDatabaseRuleConfiguration.class)).thenReturn(emptyChecker);
            ruleOperator.operate(sqlStatement, database, currentRuleConfig);
        }
        verify(metaDataManagerPersistService).removeRuleConfigurationItem(database, toBeDroppedRuleItemConfig);
        verify(metaDataManagerPersistService).removeRuleConfiguration(database, currentRuleConfig, "test_rule");
        verify(metaDataManagerPersistService, never()).alterRuleConfiguration(database, toBeAlteredRuleConfig);
    }
    
    private static final class TestDatabaseRuleConfiguration implements DatabaseRuleConfiguration {
    }
    
    @RuleNodeTupleEntity("test_rule")
    private static final class TestYamlRuleConfiguration implements YamlRuleConfiguration {
        
        @Override
        public Class<TestDatabaseRuleConfiguration> getRuleConfigurationType() {
            return TestDatabaseRuleConfiguration.class;
        }
    }
}

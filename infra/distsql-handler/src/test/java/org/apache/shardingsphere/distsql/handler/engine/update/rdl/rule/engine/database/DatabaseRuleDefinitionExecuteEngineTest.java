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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database;

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.DatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class DatabaseRuleDefinitionExecuteEngineTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteUpdateWithRefreshRequiredArguments")
    void assertExecuteUpdateWithRefreshRequired(final String name, final boolean dropExecutor, final boolean currentRuleExists,
                                                final boolean hasAnyOneToBeDropped, final boolean expectedNullCurrentRuleConfig) {
        ContextManager contextManager = mock(ContextManager.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        DatabaseRuleOperator operator = mock(DatabaseRuleOperator.class);
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        DatabaseRuleDropExecutor dropExecutorInstance = dropExecutor ? mock(DatabaseRuleDropExecutor.class) : null;
        DatabaseRuleDefinitionExecutor executor = null == dropExecutorInstance ? mock(DatabaseRuleDefinitionExecutor.class) : dropExecutorInstance;
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(executor.getRuleClass()).thenReturn(ShardingSphereRule.class);
        when(ruleMetaData.findSingleRule(ShardingSphereRule.class)).thenReturn(currentRuleExists ? Optional.of(rule) : Optional.empty());
        RuleConfiguration ruleConfig = null;
        if (currentRuleExists) {
            ruleConfig = expectedNullCurrentRuleConfig ? null : mock(RuleConfiguration.class);
            when(rule.getConfiguration()).thenReturn(ruleConfig);
        }
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        if (null != dropExecutorInstance) {
            when(dropExecutorInstance.hasAnyOneToBeDropped(sqlStatement)).thenReturn(hasAnyOneToBeDropped);
        }
        try (MockedStatic<DatabaseRuleOperatorFactory> operatorFactory = mockStatic(DatabaseRuleOperatorFactory.class)) {
            operatorFactory.when(() -> DatabaseRuleOperatorFactory.newInstance(contextManager, executor)).thenReturn(operator);
            new DatabaseRuleDefinitionExecuteEngine(sqlStatement, contextManager, database, executor).executeUpdate();
            operatorFactory.verify(() -> DatabaseRuleOperatorFactory.newInstance(contextManager, executor));
        }
        verify(executor).checkBeforeUpdate(sqlStatement);
        verify(operator).operate(sqlStatement, database, ruleConfig);
    }
    
    @Test
    void assertExecuteUpdateWithoutCurrentRuleForDropExecutor() {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        ContextManager contextManager = mock(ContextManager.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        DatabaseRuleDropExecutor executor = mock(DatabaseRuleDropExecutor.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(executor.getRuleClass()).thenReturn(ShardingSphereRule.class);
        when(ruleMetaData.findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.empty());
        try (MockedStatic<DatabaseRuleOperatorFactory> operatorFactory = mockStatic(DatabaseRuleOperatorFactory.class)) {
            new DatabaseRuleDefinitionExecuteEngine(sqlStatement, contextManager, database, executor).executeUpdate();
            operatorFactory.verify(() -> DatabaseRuleOperatorFactory.newInstance(contextManager, executor), never());
        }
        verify(executor).checkBeforeUpdate(sqlStatement);
        verify(executor, never()).hasAnyOneToBeDropped(sqlStatement);
    }
    
    @Test
    void assertExecuteUpdateWithoutDroppedDataForDropExecutor() {
        DatabaseRuleDefinitionStatement sqlStatement = mock(DatabaseRuleDefinitionStatement.class);
        ContextManager contextManager = mock(ContextManager.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        DatabaseRuleDropExecutor executor = mock(DatabaseRuleDropExecutor.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(executor.getRuleClass()).thenReturn(ShardingSphereRule.class);
        when(ruleMetaData.findSingleRule(ShardingSphereRule.class)).thenReturn(Optional.of(rule));
        try (MockedStatic<DatabaseRuleOperatorFactory> operatorFactory = mockStatic(DatabaseRuleOperatorFactory.class)) {
            new DatabaseRuleDefinitionExecuteEngine(sqlStatement, contextManager, database, executor).executeUpdate();
            operatorFactory.verify(() -> DatabaseRuleOperatorFactory.newInstance(contextManager, executor), never());
        }
        verify(executor).checkBeforeUpdate(sqlStatement);
        verify(executor).hasAnyOneToBeDropped(sqlStatement);
        verify(rule, never()).getConfiguration();
    }
    
    private static Stream<Arguments> assertExecuteUpdateWithRefreshRequiredArguments() {
        return Stream.of(
                Arguments.of("non-drop executor refreshes with current rule configuration", false, true, false, false),
                Arguments.of("non-drop executor refreshes without current rule configuration", false, false, false, true),
                Arguments.of("drop executor refreshes when current rule has dropped data", true, true, true, false));
    }
}

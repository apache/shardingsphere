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

package org.apache.shardingsphere.sqlfederation.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.global.GlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.statement.updatable.AlterSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderDuplicatedException;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderNotFoundException;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;
import org.apache.shardingsphere.test.it.distsql.handler.engine.update.DistSQLGlobalRuleDefinitionExecutorAssert;
import org.apache.shardingsphere.test.it.distsql.handler.engine.update.DistSQLRuleDefinitionExecutorSettings;
import org.apache.shardingsphere.test.it.distsql.handler.engine.update.DistSQLRuleDefinitionExecutorTestCaseArgumentsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereArgumentVerifyMatchers.deepEq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DistSQLRuleDefinitionExecutorSettings("cases/alter-sql-federation-rule.xml")
class AlterSQLFederationRuleExecutorTest {
    
    private final DistSQLGlobalRuleDefinitionExecutorAssert executorAssert = new DistSQLGlobalRuleDefinitionExecutorAssert(mock(SQLFederationRule.class));
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(DistSQLRuleDefinitionExecutorTestCaseArgumentsProvider.class)
    void assertExecuteUpdate(final String name, final GlobalRuleConfiguration ruleConfig,
                             final DistSQLStatement sqlStatement, final RuleConfiguration matchedRuleConfig, final Class<? extends Exception> expectedException) throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        when(provider.getType()).thenReturn("CALCITE");
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.singleton(provider));
            executorAssert.assertExecuteUpdate(ruleConfig, sqlStatement, matchedRuleConfig, expectedException);
        }
    }
    
    @Test
    void assertExplicitProviderMissingBeforePersistence() {
        assertRejectedBeforePersistence(new AlterSQLFederationRuleStatement(true, null, null, "UNKNOWN"), Collections.emptyList(), SQLFederationProviderNotFoundException.class,
                "SQL_FEDERATION-00001: SQL Federation provider 'UNKNOWN' is not installed.");
    }
    
    @Test
    void assertDefaultProviderMissingBeforePersistence() {
        assertRejectedBeforePersistence(new AlterSQLFederationRuleStatement(true, null, null), Collections.emptyList(), SQLFederationProviderNotFoundException.class,
                "SQL_FEDERATION-00001: SQL Federation provider 'CALCITE' is not installed.");
    }
    
    @Test
    void assertDuplicateProviderBeforePersistence() {
        SQLFederationProvider first = mock(SQLFederationProvider.class);
        SQLFederationProvider second = mock(SQLFederationProvider.class);
        when(first.getType()).thenReturn("CALCITE");
        when(second.getType()).thenReturn("calcite");
        assertRejectedBeforePersistence(new AlterSQLFederationRuleStatement(true, null, null), Arrays.asList(first, second), SQLFederationProviderDuplicatedException.class,
                "SQL_FEDERATION-00002: Multiple SQL Federation providers have type 'calcite'.");
    }
    
    @Test
    void assertDisabledUnknownProviderPersists() {
        SQLFederationCacheOption cacheOption = new SQLFederationCacheOption(4, 64L);
        SQLFederationRule rule = mock(SQLFederationRule.class);
        when(rule.getConfiguration()).thenReturn(new SQLFederationRuleConfiguration(false, false, cacheOption));
        AlterSQLFederationRuleExecutor executor = new AlterSQLFederationRuleExecutor();
        executor.setRule(rule);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            new GlobalRuleDefinitionExecuteEngine(new AlterSQLFederationRuleStatement(false, null, null, "UNKNOWN"), contextManager, executor).executeUpdate();
            serviceLoader.verifyNoInteractions();
        }
        MetaDataManagerPersistService persistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(persistService).alterGlobalRuleConfiguration(deepEq(new SQLFederationRuleConfiguration(false, false, cacheOption, "UNKNOWN")));
    }
    
    private void assertRejectedBeforePersistence(final AlterSQLFederationRuleStatement sqlStatement, final Collection<SQLFederationProvider> providers,
                                                 final Class<? extends Exception> expectedException, final String expectedMessage) {
        SQLFederationRule rule = mock(SQLFederationRule.class);
        when(rule.getConfiguration()).thenReturn(new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(4, 64L)));
        AlterSQLFederationRuleExecutor executor = new AlterSQLFederationRuleExecutor();
        executor.setRule(rule);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(providers);
            Exception actual = assertThrows(expectedException, () -> new GlobalRuleDefinitionExecuteEngine(sqlStatement, contextManager, executor).executeUpdate());
            assertThat(actual.getMessage(), is(expectedMessage));
        }
        MetaDataManagerPersistService persistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(persistService, never()).alterGlobalRuleConfiguration(any());
    }
}

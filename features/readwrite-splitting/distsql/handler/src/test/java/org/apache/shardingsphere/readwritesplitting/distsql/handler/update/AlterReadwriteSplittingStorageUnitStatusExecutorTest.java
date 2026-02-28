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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.InvalidStorageUnitStatusException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.QualifiedDataSourceStatePersistService;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingStorageUnitStatusStatement;
import org.apache.shardingsphere.readwritesplitting.exception.actual.ReadwriteSplittingActualDataSourceNotFoundException;
import org.apache.shardingsphere.readwritesplitting.group.ReadwriteSplittingGroup;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterReadwriteSplittingStorageUnitStatusExecutorTest {
    
    private final AlterReadwriteSplittingStorageUnitStatusExecutor executor = (AlterReadwriteSplittingStorageUnitStatusExecutor) TypedSPILoader.getService(
            DistSQLUpdateExecutor.class, AlterReadwriteSplittingStorageUnitStatusStatement.class);
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ReadwriteSplittingRule rule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
        executor.setRule(rule);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("executeUpdateFailureArguments")
    void assertExecuteUpdateWithInvalidStatus(final String name, final String configuredRuleName, final Collection<String> readDataSources,
                                              final Collection<String> disabledDataSourceNames, final boolean enable, final Class<? extends Exception> expectedException) {
        lenient().when(database.getName()).thenReturn("test_db");
        setRuleGroup(configuredRuleName, readDataSources, disabledDataSourceNames);
        assertThrows(expectedException, () -> executor.executeUpdate(new AlterReadwriteSplittingStorageUnitStatusStatement(null, "readwrite_group", "read_ds_0", enable), contextManager));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("executeUpdateSuccessArguments")
    void assertExecuteUpdate(final String name, final String statementRuleName, final String configuredRuleName,
                             final boolean enable, final Collection<String> disabledDataSourceNames, final DataSourceState expectedState) {
        when(database.getName()).thenReturn("test_db");
        QualifiedDataSourceStatePersistService qualifiedDataSourceStateService = mock(QualifiedDataSourceStatePersistService.class);
        when(contextManager.getPersistServiceFacade().getQualifiedDataSourceStateService()).thenReturn(qualifiedDataSourceStateService);
        setRuleGroup(configuredRuleName, Collections.singleton("read_ds_0"), disabledDataSourceNames);
        assertDoesNotThrow(() -> executor.executeUpdate(new AlterReadwriteSplittingStorageUnitStatusStatement(null, statementRuleName, "read_ds_0", enable), contextManager));
        verify(qualifiedDataSourceStateService).update("test_db", statementRuleName, "read_ds_0", expectedState);
    }
    
    private void setRuleGroup(final String configuredRuleName, final Collection<String> readDataSources, final Collection<String> disabledDataSourceNames) {
        if (null == configuredRuleName) {
            when(rule.getDataSourceRuleGroups()).thenReturn(Collections.emptyMap());
            return;
        }
        ReadwriteSplittingDataSourceGroupRule dataSourceGroupRule = mock(ReadwriteSplittingDataSourceGroupRule.class);
        when(dataSourceGroupRule.getName()).thenReturn(configuredRuleName);
        ReadwriteSplittingGroup readwriteSplittingGroup = mock(ReadwriteSplittingGroup.class);
        when(readwriteSplittingGroup.getReadDataSources()).thenReturn(new LinkedList<>(readDataSources));
        when(dataSourceGroupRule.getReadwriteSplittingGroup()).thenReturn(readwriteSplittingGroup);
        lenient().when(dataSourceGroupRule.getDisabledDataSourceNames()).thenReturn(Collections.unmodifiableCollection(disabledDataSourceNames));
        when(rule.getDataSourceRuleGroups()).thenReturn(Collections.singletonMap(configuredRuleName, dataSourceGroupRule));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ReadwriteSplittingRule.class));
    }
    
    private static Stream<Arguments> executeUpdateFailureArguments() {
        return Stream.of(
                Arguments.of("missing readwrite-splitting rule", null, Collections.singleton("read_ds_0"), Collections.emptySet(), true,
                        MissingRequiredRuleException.class),
                Arguments.of("missing read storage unit", "readwrite_group", Collections.singleton("read_ds_1"), Collections.emptySet(), true,
                        ReadwriteSplittingActualDataSourceNotFoundException.class),
                Arguments.of("enable storage unit that is not disabled", "readwrite_group", Collections.singleton("read_ds_0"), Collections.emptySet(), true,
                        InvalidStorageUnitStatusException.class),
                Arguments.of("disable storage unit that is already disabled", "readwrite_group", Collections.singleton("read_ds_0"), Collections.singleton("read_ds_0"), false,
                        InvalidStorageUnitStatusException.class));
    }
    
    private static Stream<Arguments> executeUpdateSuccessArguments() {
        return Stream.of(
                Arguments.of("enable storage unit", "readwrite_group", "readwrite_group", true, Collections.singleton("read_ds_0"), DataSourceState.ENABLED),
                Arguments.of("disable storage unit", "readwrite_group", "readwrite_group", false, Collections.emptySet(), DataSourceState.DISABLED),
                Arguments.of("match rule name case insensitively", "readwrite_group", "READWRITE_GROUP", true, Collections.singleton("read_ds_0"), DataSourceState.ENABLED));
    }
}

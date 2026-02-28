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

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.CreateReadwriteSplittingRuleStatement;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReadwriteSplittingRuleExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    private final CreateReadwriteSplittingRuleExecutor executor = (CreateReadwriteSplittingRuleExecutor) TypedSPILoader.getService(
            DatabaseRuleDefinitionExecutor.class, CreateReadwriteSplittingRuleStatement.class);
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckBeforeUpdate() {
        mockCheckBeforeUpdateDependencies(Collections.emptySet());
        setRule(null);
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(createSQLStatement(false, Collections.singleton(createRuleSegment("readwrite_ds_1")))));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfigurationWithoutIfNotExists() {
        setRule(createCurrentRuleConfiguration());
        ReadwriteSplittingRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(createSQLStatement(false, Collections.singleton(createRuleSegment("readwrite_ds_1"))));
        assertThat(actual.getDataSourceGroups().size(), is(1));
        assertThat(actual.getLoadBalancers().size(), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("buildToBeCreatedRuleConfigurationArguments")
    void assertBuildToBeCreatedRuleConfigurationWithIfNotExists(final String name, final ReadwriteSplittingRuleConfiguration currentRuleConfig,
                                                                final Collection<ReadwriteSplittingRuleSegment> ruleSegments, final int expectedGroupCount) {
        setRule(currentRuleConfig);
        assertThat(executor.buildToBeCreatedRuleConfiguration(createSQLStatement(true, ruleSegments)).getDataSourceGroups().size(), is(expectedGroupCount));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ReadwriteSplittingRule.class));
    }
    
    @Test
    void assertCheckBeforeUpdateWithMissingStorageUnit() {
        mockCheckBeforeUpdateDependencies(Collections.singleton("missing_ds"));
        setRule(null);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.checkBeforeUpdate(createSQLStatement(false, Collections.singleton(createRuleSegment("readwrite_ds_1")))));
    }
    
    private void mockCheckBeforeUpdateDependencies(final Collection<String> notExistedDataSources) {
        when(database.getName()).thenReturn("test_db");
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(notExistedDataSources);
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
    }
    
    private void setRule(final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        ReadwriteSplittingRule rule = null == currentRuleConfig ? null : mock(ReadwriteSplittingRule.class);
        if (null != rule) {
            lenient().when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        }
        executor.setRule(rule);
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final boolean ifNotExists, final Collection<ReadwriteSplittingRuleSegment> ruleSegments) {
        CreateReadwriteSplittingRuleStatement result = new CreateReadwriteSplittingRuleStatement(ifNotExists, new LinkedList<>(ruleSegments));
        result.buildAttributes();
        return result;
    }
    
    private static Stream<Arguments> buildToBeCreatedRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("if not exists with null rule", null, new LinkedList<>(Collections.singleton(createRuleSegment("readwrite_ds_1"))), 1),
                Arguments.of("if not exists removes duplicated rule", createCurrentRuleConfiguration(), new LinkedList<>(Collections.singleton(createRuleSegment("readwrite_ds_0"))), 0),
                Arguments.of("if not exists keeps non duplicated rule", createCurrentRuleConfiguration(), new LinkedList<>(Collections.singleton(createRuleSegment("readwrite_ds_1"))), 1));
    }
    
    private static ReadwriteSplittingRuleSegment createRuleSegment(final String ruleName) {
        return new ReadwriteSplittingRuleSegment(ruleName, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), new AlgorithmSegment("RANDOM", new Properties()));
    }
    
    private static ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration("readwrite_ds_0", "ds_write",
                Arrays.asList("read_ds_0", "read_ds_1"), "RANDOM");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceGroupConfig)), Collections.emptyMap());
    }
}

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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InUsedRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.single.rule.SingleRule;
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
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropReadwriteSplittingRuleExecutorTest {
    
    private final DropReadwriteSplittingRuleExecutor executor = (DropReadwriteSplittingRuleExecutor) TypedSPILoader.getService(
            DatabaseRuleDefinitionExecutor.class, DropReadwriteSplittingRuleStatement.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckBeforeUpdateWithoutToBeDroppedRule() {
        when(database.getName()).thenReturn("test_db");
        setRule(new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement()));
    }
    
    @Test
    void assertCheckBeforeUpdateWithIfExists() {
        DropReadwriteSplittingRuleStatement sqlStatement = new DropReadwriteSplittingRuleStatement(true, Collections.singleton("readwrite_ds"));
        sqlStatement.buildAttributes();
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdateWithInUsedRule() {
        when(database.getName()).thenReturn("test_db");
        DataSourceMapperRuleAttribute dataSourceMapperRuleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(dataSourceMapperRuleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("logic_tbl", Collections.singleton("readwrite_ds")));
        ShardingSphereRule mapperRule = mock(ShardingSphereRule.class);
        when(mapperRule.getAttributes()).thenReturn(new RuleAttributes(dataSourceMapperRuleAttribute));
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(mapperRule));
        setRule(createCurrentRuleConfiguration());
        assertThrows(InUsedRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement()));
    }
    
    @Test
    void assertCheckBeforeUpdateWithUnusedResources() {
        ReadwriteSplittingRule readwriteRule = mock(ReadwriteSplittingRule.class);
        when(readwriteRule.getAttributes()).thenReturn(new RuleAttributes());
        DataSourceMapperRuleAttribute dataSourceMapperRuleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(dataSourceMapperRuleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("logic_tbl", Collections.singleton("foo_ds")));
        ShardingSphereRule mapperRule = mock(ShardingSphereRule.class);
        when(mapperRule.getAttributes()).thenReturn(new RuleAttributes(dataSourceMapperRuleAttribute));
        ShardingSphereRule plainRule = mock(ShardingSphereRule.class);
        when(plainRule.getAttributes()).thenReturn(new RuleAttributes());
        DataNodeRuleAttribute dataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(dataNodeRuleAttribute.getAllDataNodes()).thenReturn(Collections.singletonMap("bar_tbl", Collections.singleton(new DataNode("bar_ds.tbl"))));
        ShardingSphereRule dataNodeRule = mock(ShardingSphereRule.class);
        when(dataNodeRule.getAttributes()).thenReturn(new RuleAttributes(dataNodeRuleAttribute));
        SingleRule singleRule = mock(SingleRule.class);
        when(singleRule.getAttributes()).thenReturn(new RuleAttributes());
        when(database.getRuleMetaData().getRules()).thenReturn(Arrays.asList(readwriteRule, mapperRule, plainRule, dataNodeRule, singleRule));
        setRule(createCurrentRuleConfiguration());
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(createSQLStatement()));
    }
    
    private DropReadwriteSplittingRuleStatement createSQLStatement() {
        DropReadwriteSplittingRuleStatement result = new DropReadwriteSplittingRuleStatement(false, Collections.singleton("readwrite_ds"));
        result.buildAttributes();
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("buildToBeDroppedRuleConfigurationArguments")
    void assertBuildToBeDroppedRuleConfiguration(final String name,
                                                 final ReadwriteSplittingRuleConfiguration currentRuleConfig, final int expectedDataSourceGroupCount, final int expectedLoadBalancerCount) {
        setRule(currentRuleConfig);
        ReadwriteSplittingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(createSQLStatement());
        assertThat(actual.getDataSourceGroups().size(), is(expectedDataSourceGroupCount));
        assertThat(actual.getLoadBalancers().size(), is(expectedLoadBalancerCount));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("hasAnyOneToBeDroppedArguments")
    void assertHasAnyOneToBeDropped(final String name, final Collection<String> toBeDroppedNames, final boolean expected) {
        setRule(createCurrentRuleConfiguration());
        DropReadwriteSplittingRuleStatement sqlStatement = new DropReadwriteSplittingRuleStatement(false, toBeDroppedNames);
        sqlStatement.buildAttributes();
        boolean actual = executor.hasAnyOneToBeDropped(sqlStatement);
        assertThat(actual, is(expected));
    }
    
    private void setRule(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ReadwriteSplittingRule.class));
    }
    
    private static Stream<Arguments> hasAnyOneToBeDroppedArguments() {
        return Stream.of(
                Arguments.of("contains dropped rule", Collections.singleton("readwrite_ds"), true),
                Arguments.of("without dropped rule", Collections.singleton("other_ds"), false),
                Arguments.of("intersected dropped rules", Arrays.asList("other_ds", "readwrite_ds"), true));
    }
    
    private static Stream<Arguments> buildToBeDroppedRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("drop configuration with unused load balancer", createCurrentRuleConfiguration(), 1, 1),
                Arguments.of("drop configuration without load balancer name", createCurrentRuleConfigurationWithoutLoadBalancerName(), 1, 1),
                Arguments.of("drop configuration with in-used load balancer", createMultipleCurrentRuleConfigurations(), 1, 0));
    }
    
    private static ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration("readwrite_ds",
                "", Collections.emptyList(), "readwrite_ds");
        Map<String, AlgorithmConfiguration> loadBalancers = Collections.singletonMap("readwrite_ds", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceGroupConfig)), loadBalancers);
    }
    
    private static ReadwriteSplittingRuleConfiguration createCurrentRuleConfigurationWithoutLoadBalancerName() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration("readwrite_ds", "", new LinkedList<>(), null);
        Map<String, AlgorithmConfiguration> loadBalancers = Collections.singletonMap("readwrite_ds", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceGroupConfig)), loadBalancers);
    }
    
    private static ReadwriteSplittingRuleConfiguration createMultipleCurrentRuleConfigurations() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration fooDataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_ds", "", new LinkedList<>(), "TEST");
        ReadwriteSplittingDataSourceGroupRuleConfiguration barDataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration("bar_ds", "", new LinkedList<>(), "TEST");
        Map<String, AlgorithmConfiguration> loadBalancers = Collections.singletonMap("TEST", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Arrays.asList(fooDataSourceGroupConfig, barDataSourceGroupConfig)), loadBalancers);
    }
}

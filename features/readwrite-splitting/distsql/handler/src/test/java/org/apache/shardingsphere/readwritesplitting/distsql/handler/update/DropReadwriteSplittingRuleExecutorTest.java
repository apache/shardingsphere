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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InUsedRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.RuleDefinitionException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropReadwriteSplittingRuleExecutorTest {
    
    private final DropReadwriteSplittingRuleExecutor executor = new DropReadwriteSplittingRuleExecutor();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeDroppedRule() throws RuleDefinitionException {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement()));
    }
    
    @Test
    void assertCheckSQLStatementWithIfExists() throws RuleDefinitionException {
        DropReadwriteSplittingRuleStatement sqlStatement = new DropReadwriteSplittingRuleStatement(true, Collections.singleton("readwrite_ds"));
        sqlStatement.buildAttributes();
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    @Test
    void assertCheckSQLStatementWithInUsed() throws RuleDefinitionException {
        DataSourceMapperRuleAttribute dataSourceMapperRuleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.singleton(dataSourceMapperRuleAttribute));
        DataNodeRuleAttribute dataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(dataNodeRuleAttribute.getAllDataNodes()).thenReturn(Collections.singletonMap("foo_ds", Collections.singleton(new DataNode("readwrite_ds.tbl"))));
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(dataNodeRuleAttribute));
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        executor.setDatabase(database);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(InUsedRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement()));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        ReadwriteSplittingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(createSQLStatement());
        assertThat(actual.getDataSourceGroups().size(), is(1));
        assertThat(actual.getLoadBalancers().size(), is(1));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfigurationWithInUsedLoadBalancer() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createMultipleCurrentRuleConfigurations();
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        ReadwriteSplittingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(createSQLStatement());
        assertThat(actual.getDataSourceGroups().size(), is(1));
        assertTrue(actual.getLoadBalancers().isEmpty());
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfigurationWithoutLoadBalancerName() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createCurrentRuleConfigurationWithoutLoadBalancerName();
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        ReadwriteSplittingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(createSQLStatement());
        assertThat(actual.getDataSourceGroups().size(), is(1));
        assertThat(actual.getLoadBalancers().size(), is(1));
    }
    
    private DropReadwriteSplittingRuleStatement createSQLStatement() {
        DropReadwriteSplittingRuleStatement result = new DropReadwriteSplittingRuleStatement(false, Collections.singleton("readwrite_ds"));
        result.buildAttributes();
        return result;
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration("readwrite_ds",
                "", Collections.emptyList(), "readwrite_ds");
        Map<String, AlgorithmConfiguration> loadBalancers = Collections.singletonMap("readwrite_ds", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceGroupConfig)), loadBalancers);
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfigurationWithoutLoadBalancerName() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration("readwrite_ds",
                "", new LinkedList<>(), null);
        Map<String, AlgorithmConfiguration> loadBalancers = Collections.singletonMap("readwrite_ds", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceGroupConfig)), loadBalancers);
    }
    
    private ReadwriteSplittingRuleConfiguration createMultipleCurrentRuleConfigurations() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration fooDataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo_ds", "", new LinkedList<>(), "TEST");
        ReadwriteSplittingDataSourceGroupRuleConfiguration barDataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "bar_ds", "", new LinkedList<>(), "TEST");
        Map<String, AlgorithmConfiguration> loadBalancers = Collections.singletonMap("TEST", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Arrays.asList(fooDataSourceGroupConfig, barDataSourceGroupConfig)), loadBalancers);
    }
}

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

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.RuleIdentifiers;
import org.apache.shardingsphere.infra.rule.identifier.type.datanode.DataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datasource.DataSourceMapperRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
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
    void assertCheckSQLStatementWithoutToBeDroppedRule() throws RuleDefinitionViolationException {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement()));
    }
    
    @Test
    void assertCheckSQLStatementWithIfExists() throws RuleDefinitionViolationException {
        executor.checkBeforeUpdate(new DropReadwriteSplittingRuleStatement(true, Collections.singleton("readwrite_ds")));
    }
    
    @Test
    void assertCheckSQLStatementWithInUsed() throws RuleDefinitionViolationException {
        DataSourceMapperRule dataSourceMapperRule = mock(DataSourceMapperRule.class, RETURNS_DEEP_STUBS);
        when(dataSourceMapperRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("foo_ds", Collections.singleton("readwrite_ds")));
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getRuleIdentifiers()).thenReturn(new RuleIdentifiers(dataSourceMapperRule));
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        DataNodeRule dataNodeRule = mock(DataNodeRule.class);
        when(dataNodeRule.getAllDataNodes()).thenReturn(Collections.singletonMap("foo_ds", Collections.singleton(new DataNode("readwrite_ds.tbl"))));
        when(rule.getRuleIdentifiers()).thenReturn(new RuleIdentifiers(dataNodeRule));
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        executor.setDatabase(database);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(RuleInUsedException.class, () -> executor.checkBeforeUpdate(createSQLStatement()));
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        assertTrue(executor.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig));
        assertThat(ruleConfig.getLoadBalancers().size(), is(0));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithInUsedLoadBalancer() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createMultipleCurrentRuleConfigurations();
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        assertFalse(executor.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig));
        assertThat(ruleConfig.getLoadBalancers().size(), is(1));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithoutLoadBalancerName() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createCurrentRuleConfigurationWithoutLoadBalancerName();
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        assertTrue(executor.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig));
        assertThat(ruleConfig.getLoadBalancers().size(), is(0));
    }
    
    private DropReadwriteSplittingRuleStatement createSQLStatement() {
        return new DropReadwriteSplittingRuleStatement(false, Collections.singleton("readwrite_ds"));
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds",
                "", Collections.emptyList(), "readwrite_ds");
        Map<String, AlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
        loadBalancers.put("readwrite_ds", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), loadBalancers);
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfigurationWithoutLoadBalancerName() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds",
                "", new LinkedList<>(), null);
        Map<String, AlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
        loadBalancers.put("readwrite_ds", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), loadBalancers);
    }
    
    private ReadwriteSplittingRuleConfiguration createMultipleCurrentRuleConfigurations() {
        ReadwriteSplittingDataSourceRuleConfiguration fooDataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("foo_ds",
                "", new LinkedList<>(), "TEST");
        ReadwriteSplittingDataSourceRuleConfiguration barDataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("bar_ds",
                "", new LinkedList<>(), "TEST");
        Map<String, AlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
        loadBalancers.put("TEST", new AlgorithmConfiguration("TEST", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Arrays.asList(fooDataSourceRuleConfig, barDataSourceRuleConfig)), loadBalancers);
    }
}

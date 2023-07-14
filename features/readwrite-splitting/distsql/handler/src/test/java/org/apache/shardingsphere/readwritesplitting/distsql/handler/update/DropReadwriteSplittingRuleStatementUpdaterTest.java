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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropReadwriteSplittingRuleStatementUpdaterTest {
    
    private final DropReadwriteSplittingRuleStatementUpdater updater = new DropReadwriteSplittingRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckSQLStatementWithoutCurrentRule() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement(), null));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeDroppedRule() throws RuleDefinitionViolationException {
        assertThrows(MissingRequiredRuleException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement(), new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap())));
    }
    
    @Test
    void assertCheckSQLStatementWithIfExists() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(database, new DropReadwriteSplittingRuleStatement(true, Collections.singleton("readwrite_ds")),
                new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        updater.checkSQLStatement(database, new DropReadwriteSplittingRuleStatement(true, Collections.singleton("readwrite_ds")), null);
    }
    
    @Test
    void assertCheckSQLStatementWithInUsed() throws RuleDefinitionViolationException {
        DataSourceContainedRule dataSourceContainedRule = mock(DataSourceContainedRule.class);
        when(dataSourceContainedRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("foo_ds", Collections.singleton("readwrite_ds")));
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(dataSourceContainedRule));
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        when(dataNodeContainedRule.getAllDataNodes()).thenReturn(Collections.singletonMap("foo_ds", Collections.singleton(new DataNode("readwrite_ds.tbl"))));
        when(database.getRuleMetaData().findRules(DataNodeContainedRule.class)).thenReturn(Collections.singleton(dataNodeContainedRule));
        assertThrows(RuleInUsedException.class, () -> updater.checkSQLStatement(database, createSQLStatement(), createCurrentRuleConfiguration()));
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        assertTrue(updater.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig));
        assertThat(ruleConfig.getLoadBalancers().size(), is(0));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithInUsedLoadBalancer() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createMultipleCurrentRuleConfigurations();
        assertFalse(updater.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig));
        assertThat(ruleConfig.getLoadBalancers().size(), is(1));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithoutLoadBalancerName() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createCurrentRuleConfigurationWithoutLoadBalancerName();
        assertTrue(updater.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig));
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

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

package org.apache.shardingsphere.sharding.distsql.update;

import org.apache.shardingsphere.distsql.handler.exception.algorithm.AlgorithmInUsedException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingAlgorithmExecutor;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropShardingAlgorithmExecutorTest {
    
    private final DropShardingAlgorithmExecutor executor = new DropShardingAlgorithmExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedAlgorithm() throws RuleDefinitionViolationException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredAlgorithmException.class, () -> executor.checkBeforeUpdate(createSQLStatement("t_order")));
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedAlgorithmWithIfExists() throws RuleDefinitionViolationException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        executor.checkBeforeUpdate(createSQLStatementWithIfExists("t_order"));
    }
    
    @Test
    void assertCheckSQLStatementWithBindingTableRule() throws RuleDefinitionViolationException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(AlgorithmInUsedException.class, () -> executor.checkBeforeUpdate(createSQLStatement("t_order_tb_inline")));
    }
    
    @Test
    void assertCheckSQLStatementWithBindingTableRuleWithIfExists() throws RuleDefinitionViolationException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(AlgorithmInUsedException.class, () -> executor.checkBeforeUpdate(createSQLStatementWithIfExists("t_order_tb_inline")));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        String toBeDroppedAlgorithmName = "t_test";
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(3));
        assertTrue(currentRuleConfig.getShardingAlgorithms().containsKey(toBeDroppedAlgorithmName));
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        ShardingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(createSQLStatement(toBeDroppedAlgorithmName));
        assertThat(actual.getShardingAlgorithms().size(), is(1));
        assertTrue(actual.getShardingAlgorithms().containsKey(toBeDroppedAlgorithmName));
    }
    
    private DropShardingAlgorithmStatement createSQLStatement(final String algorithmName) {
        return new DropShardingAlgorithmStatement(false, Collections.singleton(algorithmName));
    }
    
    private DropShardingAlgorithmStatement createSQLStatementWithIfExists(final String algorithmName) {
        return new DropShardingAlgorithmStatement(true, Collections.singleton(algorithmName));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("t_order", null);
        tableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("column", "t_order_db_inline"));
        tableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("column", "t_order_tb_inline"));
        result.getTables().add(tableRuleConfig);
        result.getShardingAlgorithms().put("t_order_db_inline", new AlgorithmConfiguration("standard", new Properties()));
        result.getShardingAlgorithms().put("t_order_tb_inline", new AlgorithmConfiguration("standard", new Properties()));
        result.getShardingAlgorithms().put("t_test", new AlgorithmConfiguration("standard", new Properties()));
        return result;
    }
}

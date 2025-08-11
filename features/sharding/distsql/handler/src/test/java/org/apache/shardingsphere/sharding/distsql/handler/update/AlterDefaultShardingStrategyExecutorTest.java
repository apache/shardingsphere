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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.exception.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.AlterDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterDefaultShardingStrategyExecutorTest {
    
    private final AlterDefaultShardingStrategyExecutor executor = new AlterDefaultShardingStrategyExecutor();
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("test");
        executor.setDatabase(database);
    }
    
    @Test
    void assertExecuteWithInvalidStrategyType() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        assertThrows(InvalidAlgorithmConfigurationException.class,
                () -> executor.checkBeforeUpdate(new AlterDefaultShardingStrategyStatement("TABLE", "invalidType", null, null)));
    }
    
    @Test
    void assertExecuteWithNotExist() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("not_exist_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlterDefaultShardingStrategyStatement sqlStatement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id", null);
        assertThrows(MissingRequiredAlgorithmException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteWithUnmatchedStrategy() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlterDefaultShardingStrategyStatement sqlStatement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id,user_id", null);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertAlterDefaultTableShardingStrategy() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        AlterDefaultShardingStrategyStatement sqlStatement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id", algorithm);
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) toBeAlteredRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("default_table_order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    void assertAlterDefaultDatabaseShardingStrategy() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("DATABASE", "standard", "user_id", databaseAlgorithmSegment);
        executor.checkBeforeUpdate(statement);
        ShardingRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(statement);
        StandardShardingStrategyConfiguration defaultDatabaseShardingStrategy = (StandardShardingStrategyConfiguration) actual.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is("default_database_inline"));
        assertThat(defaultDatabaseShardingStrategy.getShardingColumn(), is("user_id"));
    }
    
    @Test
    void assertAlterDefaultTableShardingStrategyWithNoneShardingStrategyType() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlterDefaultShardingStrategyStatement sqlStatement = new AlterDefaultShardingStrategyStatement("TABLE", "none", null, null);
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        NoneShardingStrategyConfiguration defaultTableShardingStrategy = (NoneShardingStrategyConfiguration) actual.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getType(), is(""));
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is(""));
    }
    
    @Test
    void assertAlterDefaultDatabaseShardingStrategyWithNoneShardingStrategyType() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlterDefaultShardingStrategyStatement sqlStatement = new AlterDefaultShardingStrategyStatement("DATABASE", "none", null, null);
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        NoneShardingStrategyConfiguration defaultDatabaseShardingStrategy = (NoneShardingStrategyConfiguration) actual.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getType(), is(""));
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is(""));
    }
}

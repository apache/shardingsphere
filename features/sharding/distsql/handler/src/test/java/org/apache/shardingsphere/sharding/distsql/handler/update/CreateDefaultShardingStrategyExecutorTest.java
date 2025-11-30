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
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.CreateDefaultShardingStrategyStatement;
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

class CreateDefaultShardingStrategyExecutorTest {
    
    private final CreateDefaultShardingStrategyExecutor executor = new CreateDefaultShardingStrategyExecutor();
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        executor.setDatabase(database);
    }
    
    @Test
    void assertExecuteWithInvalidStrategyType() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "invalidType", null, null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> executor.checkBeforeUpdate(statement));
    }
    
    @Test
    void assertExecuteWithoutAlgorithm() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", null);
        assertThrows(MissingRequiredAlgorithmException.class, () -> executor.checkBeforeUpdate(statement));
    }
    
    @Test
    void assertExecuteWithExist() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(statement));
    }
    
    @Test
    void assertExecuteWithUnmatchedStrategy() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id,user_id", null);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> executor.checkBeforeUpdate(statement));
    }
    
    @Test
    void assertCreateDefaultTableShardingStrategy() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement sqlStatement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) toBeCreatedRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("default_table_order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    void assertCreateDefaultDatabaseShardingStrategy() {
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "DATABASE", "standard", "user_id", databaseAlgorithmSegment);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(statement);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(statement);
        StandardShardingStrategyConfiguration defaultDatabaseShardingStrategy = (StandardShardingStrategyConfiguration) toBeCreatedRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is("default_database_inline"));
        assertThat(defaultDatabaseShardingStrategy.getShardingColumn(), is("user_id"));
    }
    
    @Test
    void assertCreateDefaultTableShardingStrategyWithIfNotExists() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement sqlStatement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        executor.checkBeforeUpdate(sqlStatement);
        algorithm = new AlgorithmSegment("user_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statementWithIfNotExists = new CreateDefaultShardingStrategyStatement(true, "TABLE", "standard", "order_id", algorithm);
        executor.checkBeforeUpdate(statementWithIfNotExists);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(statementWithIfNotExists);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) toBeCreatedRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("default_table_user_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    void assertCreateDefaultTableShardingStrategyWithNoneShardingStrategyType() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        CreateDefaultShardingStrategyStatement sqlStatement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "none", null, null);
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        NoneShardingStrategyConfiguration defaultTableShardingStrategy = (NoneShardingStrategyConfiguration) toBeCreatedRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getType(), is(""));
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is(""));
    }
    
    @Test
    void assertCreateDefaultDatabaseShardingStrategyWithNoneShardingStrategyType() {
        CreateDefaultShardingStrategyStatement sqlStatement = new CreateDefaultShardingStrategyStatement(false, "DATABASE", "none", null, null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        NoneShardingStrategyConfiguration defaultDatabaseShardingStrategy = (NoneShardingStrategyConfiguration) toBeCreatedRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getType(), is(""));
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is(""));
    }
}

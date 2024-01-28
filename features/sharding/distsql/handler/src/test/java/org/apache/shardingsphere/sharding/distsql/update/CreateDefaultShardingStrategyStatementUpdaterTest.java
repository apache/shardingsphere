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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateDefaultShardingStrategyExecutor;
import org.apache.shardingsphere.sharding.distsql.statement.CreateDefaultShardingStrategyStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateDefaultShardingStrategyStatementUpdaterTest {
    
    private final CreateDefaultShardingStrategyExecutor executor = new CreateDefaultShardingStrategyExecutor();
    
    @BeforeEach
    void before() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        executor.setDatabase(database);
    }
    
    @Test
    void assertExecuteWithInvalidStrategyType() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "invalidType", null, null);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> executor.checkBeforeUpdate(statement, new ShardingRuleConfiguration()));
    }
    
    @Test
    void assertExecuteWithoutAlgorithm() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", null);
        assertThrows(MissingRequiredAlgorithmException.class, () -> executor.checkBeforeUpdate(statement, null));
    }
    
    @Test
    void assertExecuteWithExist() {
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(statement, currentRuleConfig));
    }
    
    @Test
    void assertExecuteWithUnmatchedStrategy() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id,user_id", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> executor.checkBeforeUpdate(statement, currentRuleConfig));
    }
    
    @Test
    void assertCreateDefaultTableShardingStrategy() {
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        executor.checkBeforeUpdate(statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("default_table_order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    void assertCreateDefaultDatabaseShardingStrategy() {
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "DATABASE", "standard", "user_id", databaseAlgorithmSegment);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        executor.checkBeforeUpdate(statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        StandardShardingStrategyConfiguration defaultDatabaseShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is("default_database_inline"));
        assertThat(defaultDatabaseShardingStrategy.getShardingColumn(), is("user_id"));
    }
    
    @Test
    void assertCreateDefaultTableShardingStrategyWithIfNotExists() {
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        executor.checkBeforeUpdate(statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        algorithm = new AlgorithmSegment("user_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statementWithIfNotExists = new CreateDefaultShardingStrategyStatement(true, "TABLE", "standard", "order_id", algorithm);
        executor.checkBeforeUpdate(statementWithIfNotExists, currentRuleConfig);
        toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, statementWithIfNotExists);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("default_table_order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    void assertCreateDefaultDatabaseShardingStrategyWithIfNotExists() {
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "DATABASE", "standard", "user_id", databaseAlgorithmSegment);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        executor.checkBeforeUpdate(statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${order_id % 2}")));
        CreateDefaultShardingStrategyStatement statementWithIfNotExists = new CreateDefaultShardingStrategyStatement(true, "TABLE", "standard", "order_id", databaseAlgorithmSegment);
        executor.checkBeforeUpdate(statementWithIfNotExists, currentRuleConfig);
        toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, statementWithIfNotExists);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        StandardShardingStrategyConfiguration defaultDatabaseShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is("default_database_inline"));
        assertThat(defaultDatabaseShardingStrategy.getShardingColumn(), is("user_id"));
    }
    
    @Test
    void assertCreateDefaultTableShardingStrategyWithNoneShardingStrategyType() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "none", null, null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        executor.checkBeforeUpdate(statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        NoneShardingStrategyConfiguration defaultTableShardingStrategy = (NoneShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getType(), is(""));
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is(""));
    }
    
    @Test
    void assertCreateDefaultDatabaseShardingStrategyWithNoneShardingStrategyType() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "DATABASE", "none", null, null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        executor.checkBeforeUpdate(statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        executor.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        NoneShardingStrategyConfiguration defaultDatabaseShardingStrategy = (NoneShardingStrategyConfiguration) currentRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getType(), is(""));
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is(""));
    }
}

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
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterDefaultShardingStrategyStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterDefaultShardingStrategyStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterDefaultShardingStrategyStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final AlterDefaultShardingStrategyStatementUpdater updater = new AlterDefaultShardingStrategyStatementUpdater();
    
    @BeforeEach
    void before() {
        when(database.getName()).thenReturn("test");
    }
    
    @Test
    void assertExecuteWithInvalidStrategyType() {
        assertThrows(InvalidAlgorithmConfigurationException.class,
                () -> updater.checkSQLStatement(database, new AlterDefaultShardingStrategyStatement("TABLE", "invalidType", null, null), new ShardingRuleConfiguration()));
    }
    
    @Test
    void assertExecuteWithoutCurrentConfiguration() {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id", null);
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, statement, null));
    }
    
    @Test
    void assertExecuteWithNotExist() {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("not_exist_algorithm", null);
        assertThrows(MissingRequiredAlgorithmException.class, () -> updater.checkSQLStatement(database, statement, currentRuleConfig));
    }
    
    @Test
    void assertExecuteWithUnmatchedStrategy() {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id,user_id", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> updater.checkSQLStatement(database, statement, currentRuleConfig));
    }
    
    @Test
    void assertAlterDefaultTableShardingStrategy() {
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id", algorithm);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("default_table_order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    void assertAlterDefaultDatabaseShardingStrategy() {
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("DATABASE", "standard", "user_id", databaseAlgorithmSegment);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        StandardShardingStrategyConfiguration defaultDatabaseShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is("default_database_inline"));
        assertThat(defaultDatabaseShardingStrategy.getShardingColumn(), is("user_id"));
    }
    
    @Test
    void assertAlterDefaultTableShardingStrategyWithNoneShardingStrategyType() {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "none", null, null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        NoneShardingStrategyConfiguration defaultTableShardingStrategy = (NoneShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getType(), is(""));
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is(""));
    }
    
    @Test
    void assertAlterDefaultDatabaseShardingStrategyWithNoneShardingStrategyType() {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("DATABASE", "none", null, null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        NoneShardingStrategyConfiguration defaultDatabaseShardingStrategy = (NoneShardingStrategyConfiguration) currentRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getType(), is(""));
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is(""));
    }
}

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
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateDefaultShardingStrategyStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDefaultShardingStrategyStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final CreateDefaultShardingStrategyStatementUpdater updater = new CreateDefaultShardingStrategyStatementUpdater();
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("test");
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidStrategyType() {
        updater.checkSQLStatement(database, new CreateDefaultShardingStrategyStatement(false, "TABLE", "invalidType", null, null), new ShardingRuleConfiguration());
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertExecuteWithoutCurrentConfiguration() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", null);
        updater.checkSQLStatement(database, statement, null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithExist() {
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(database, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithUnmatchedStrategy() {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id,user_id", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(database, statement, currentRuleConfig);
    }
    
    @Test
    public void assertCreateDefaultTableShardingStrategy() {
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("default_table_order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    public void assertCreateDefaultDatabaseShardingStrategy() {
        AlgorithmSegment databaseAlgorithmSegment = getAutoCreativeAlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "DATABASE", "standard", "user_id", databaseAlgorithmSegment);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        StandardShardingStrategyConfiguration defaultDatabaseShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is("default_database_inline"));
        assertThat(defaultDatabaseShardingStrategy.getShardingColumn(), is("user_id"));
    }
    
    @Test
    public void assertCreateDefaultTableShardingStrategyWithIfNotExists() {
        AlgorithmSegment algorithm = new AlgorithmSegment("order_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "TABLE", "standard", "order_id", algorithm);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        algorithm = new AlgorithmSegment("user_id_algorithm", new Properties());
        CreateDefaultShardingStrategyStatement statementWithIfNotExists = new CreateDefaultShardingStrategyStatement(true, "TABLE", "standard", "order_id", algorithm);
        updater.checkSQLStatement(database, statementWithIfNotExists, currentRuleConfig);
        toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statementWithIfNotExists);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("default_table_order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    public void assertCreateDefaultDatabaseShardingStrategyWithIfNotExists() {
        AlgorithmSegment databaseAlgorithmSegment = getAutoCreativeAlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement(false, "DATABASE", "standard", "user_id", databaseAlgorithmSegment);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        databaseAlgorithmSegment = getAutoCreativeAlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${order_id % 2}")));
        CreateDefaultShardingStrategyStatement statementWithIfNotExists = new CreateDefaultShardingStrategyStatement(true, "TABLE", "standard", "order_id", databaseAlgorithmSegment);
        updater.checkSQLStatement(database, statementWithIfNotExists, currentRuleConfig);
        toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statementWithIfNotExists);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        StandardShardingStrategyConfiguration defaultDatabaseShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is("default_database_inline"));
        assertThat(defaultDatabaseShardingStrategy.getShardingColumn(), is("user_id"));
    }
    
    private AlgorithmSegment getAutoCreativeAlgorithmSegment(final String name, final Properties props) {
        return new AlgorithmSegment(name, props);
    }
}

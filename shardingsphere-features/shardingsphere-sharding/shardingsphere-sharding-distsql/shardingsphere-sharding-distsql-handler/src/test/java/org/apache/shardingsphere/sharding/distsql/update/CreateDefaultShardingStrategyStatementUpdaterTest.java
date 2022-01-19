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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateDefaultShardingStrategyStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDefaultShardingStrategyStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final CreateDefaultShardingStrategyStatementUpdater updater = new CreateDefaultShardingStrategyStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getName()).thenReturn("test");
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidStrategyType() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDefaultShardingStrategyStatement("TABLE", "invalidType", null, "order_id_algorithm", null), new ShardingRuleConfiguration());
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteWithoutCurrentConfiguration() throws DistSQLException {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement("TABLE", "standard", "order_id", "order_id_algorithm", null);
        updater.checkSQLStatement(shardingSphereMetaData, statement, null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithExist() throws DistSQLException {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement("TABLE", "standard", "order_id", "order_id_algorithm", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithUnmatchedStrategy() throws DistSQLException {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement("TABLE", "standard", "order_id,user_id", "order_id_algorithm", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test
    public void assertCreateDefaultTableShardingStrategy() throws DistSQLException {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement("TABLE", "standard", "order_id", "order_id_algorithm", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfiguration = updater.buildToBeCreatedRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfiguration);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    public void assertCreateDefaultDatabaseShardingStrategy() throws DistSQLException {
        AlgorithmSegment databaseAlgorithmSegment = getAutoCreativeAlgorithmSegment("inline", newProperties("algorithm-expression", "ds_${user_id% 2}"));
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement("DATABASE", "standard", "user_id", null, databaseAlgorithmSegment);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfiguration = updater.buildToBeCreatedRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfiguration);
        StandardShardingStrategyConfiguration defaultDatabaseShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultDatabaseShardingStrategy();
        assertThat(defaultDatabaseShardingStrategy.getShardingAlgorithmName(), is("default_database_inline"));
        assertThat(defaultDatabaseShardingStrategy.getShardingColumn(), is("user_id"));
    }
    
    private AlgorithmSegment getAutoCreativeAlgorithmSegment(final String name, final Properties props) {
        return new AlgorithmSegment(name, props);
    }
    
    private static Properties newProperties(final String key, final String value) {
        Properties result = new Properties();
        result.put(key, value);
        return result;
    }
}

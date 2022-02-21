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
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterDefaultShardingStrategyStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterDefaultShardingStrategyStatement;
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
public final class AlterDefaultShardingStrategyStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final AlterDefaultShardingStrategyStatementUpdater updater = new AlterDefaultShardingStrategyStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getName()).thenReturn("test");
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidStrategyType() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, new AlterDefaultShardingStrategyStatement("TABLE", "invalidType", null, "order_id_algorithm", null), new ShardingRuleConfiguration());
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteWithoutCurrentConfiguration() throws DistSQLException {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id", "order_id_algorithm", null);
        updater.checkSQLStatement(shardingSphereMetaData, statement, null);
    }
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertExecuteWithNotExist() throws DistSQLException {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id", "order_id_algorithm", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("not_exist_algorithm", null);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithUnmatchedStrategy() throws DistSQLException {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id,user_id", "order_id_algorithm", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test
    public void assertAlterDefaultTableShardingStrategy() throws DistSQLException {
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("TABLE", "standard", "order_id", "order_id_algorithm", null);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        currentRuleConfig.getShardingAlgorithms().put("order_id_algorithm", null);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeAlteredRuleConfiguration = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfiguration);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        assertThat(defaultTableShardingStrategy.getShardingAlgorithmName(), is("order_id_algorithm"));
        assertThat(defaultTableShardingStrategy.getShardingColumn(), is("order_id"));
    }
    
    @Test
    public void assertAlterDefaultDatabaseShardingStrategy() throws DistSQLException {
        AlgorithmSegment databaseAlgorithmSegment = getAutoCreativeAlgorithmSegment("inline", newProperties("algorithm-expression", "ds_${user_id% 2}"));
        AlterDefaultShardingStrategyStatement statement = new AlterDefaultShardingStrategyStatement("DATABASE", "standard", "user_id", null, databaseAlgorithmSegment);
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeAlteredRuleConfiguration = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfiguration);
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

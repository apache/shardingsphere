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

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateDefaultShardingStrategyStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

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
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertExecuteWithInvalidStrategyType() throws DistSQLException {
        Properties properties = new Properties();
        properties.put("inputKey", "inputValue");
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDefaultShardingStrategyStatement("TABLE", "invalidType", null, null), null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithExist() throws DistSQLException {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement("TABLE", "standard", "order_id", "order_id_algorithm");
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
    }
    
    @Test
    public void assertExecuteSuccess() throws DistSQLException {
        CreateDefaultShardingStrategyStatement statement = new CreateDefaultShardingStrategyStatement("TABLE", "standard", "order_id", "order_id_algorithm");
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderAlgorithm"));
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfiguration = updater.buildToBeCreatedRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfiguration);
        StandardShardingStrategyConfiguration defaultTableShardingStrategy = (StandardShardingStrategyConfiguration) currentRuleConfig.getDefaultTableShardingStrategy();
        Assert.assertEquals("order_id_algorithm", defaultTableShardingStrategy.getShardingAlgorithmName());
        Assert.assertEquals("order_id", defaultTableShardingStrategy.getShardingColumn());
    }
}

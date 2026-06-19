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
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ColumnKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateShardingKeyGenerateStrategyExecutorTest {
    
    private final CreateShardingKeyGenerateStrategyExecutor executor = new CreateShardingKeyGenerateStrategyExecutor();
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckBeforeUpdateWithDuplicateStrategy() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("order_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("snowflake", "t_order", "order_id"));
        executor.setRule(mockRule(currentRuleConfig));
        CreateShardingKeyGenerateStrategyStatement sqlStatement = new CreateShardingKeyGenerateStrategyStatement(false, "order_strategy",
                new ColumnKeyGenerateStrategyDefinitionSegment("t_order", "order_id", "snowflake", null));
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdateWithMissingKeyGenerator() {
        executor.setRule(mockRule(new ShardingRuleConfiguration()));
        CreateShardingKeyGenerateStrategyStatement sqlStatement = new CreateShardingKeyGenerateStrategyStatement(false, "order_strategy",
                new ColumnKeyGenerateStrategyDefinitionSegment("t_order", "order_id", "snowflake", null));
        assertThrows(UnregisteredAlgorithmException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfigurationWithAlgorithm() {
        executor.setRule(mockRule(new ShardingRuleConfiguration()));
        CreateShardingKeyGenerateStrategyStatement sqlStatement = new CreateShardingKeyGenerateStrategyStatement(false, "order_strategy",
                new ColumnKeyGenerateStrategyDefinitionSegment("t_order", "order_id", null, new AlgorithmSegment("SNOWFLAKE", new Properties())));
        ShardingRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(actual.getKeyGenerateStrategies().size(), is(1));
        assertThat(actual.getKeyGenerateStrategies().get("order_strategy").getKeyGeneratorName(), is("order_strategy_snowflake"));
        assertThat(((ColumnKeyGenerateStrategiesRuleConfiguration) actual.getKeyGenerateStrategies().get("order_strategy")).getLogicTable(), is("t_order"));
        assertThat(((ColumnKeyGenerateStrategiesRuleConfiguration) actual.getKeyGenerateStrategies().get("order_strategy")).getKeyGenerateColumn(), is("order_id"));
        assertThat(actual.getKeyGenerators().get("order_strategy_snowflake").getType(), is("SNOWFLAKE"));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfigurationWithIfNotExists() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("order_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("snowflake", "t_order", "order_id"));
        executor.setRule(mockRule(currentRuleConfig));
        CreateShardingKeyGenerateStrategyStatement sqlStatement = new CreateShardingKeyGenerateStrategyStatement(true, "order_strategy",
                new ColumnKeyGenerateStrategyDefinitionSegment("t_order", "order_id", null, new AlgorithmSegment("SNOWFLAKE", new Properties())));
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertTrue(actual.getKeyGenerateStrategies().isEmpty());
        assertTrue(actual.getKeyGenerators().isEmpty());
    }
    
    private ShardingRule mockRule(final ShardingRuleConfiguration ruleConfig) {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
}

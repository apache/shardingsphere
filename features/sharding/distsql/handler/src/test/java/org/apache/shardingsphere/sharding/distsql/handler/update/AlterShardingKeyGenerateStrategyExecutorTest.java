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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.SequenceKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ColumnKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.SequenceKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingKeyGenerateStrategyStatement;
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

class AlterShardingKeyGenerateStrategyExecutorTest {
    
    private final AlterShardingKeyGenerateStrategyExecutor executor = new AlterShardingKeyGenerateStrategyExecutor();
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckBeforeUpdateWithMissingStrategy() {
        executor.setRule(mockRule(new ShardingRuleConfiguration()));
        AlterShardingKeyGenerateStrategyStatement sqlStatement = new AlterShardingKeyGenerateStrategyStatement("order_strategy",
                new ColumnKeyGenerateStrategyDefinitionSegment("t_order", "order_id", "snowflake", null));
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdateWithMissingKeyGenerator() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("order_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("old_generator", "t_order", "order_id"));
        executor.setRule(mockRule(currentRuleConfig));
        AlterShardingKeyGenerateStrategyStatement sqlStatement = new AlterShardingKeyGenerateStrategyStatement("order_strategy",
                new ColumnKeyGenerateStrategyDefinitionSegment("t_order", "order_id", "snowflake", null));
        assertThrows(UnregisteredAlgorithmException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdateWithConflictingGeneratedKeyGenerator() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("order_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("old_generator", "t_order", "order_id"));
        currentRuleConfig.getKeyGenerators().put("order_strategy", new AlgorithmConfiguration("UUID", new Properties()));
        executor.setRule(mockRule(currentRuleConfig));
        AlterShardingKeyGenerateStrategyStatement sqlStatement = new AlterShardingKeyGenerateStrategyStatement("order_strategy",
                new ColumnKeyGenerateStrategyDefinitionSegment("t_order", "order_id", null, new AlgorithmSegment("SNOWFLAKE", new Properties())));
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertBuildToBeAlteredRuleConfigurationWithAlgorithm() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("order_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("old_generator", "t_order", "order_id"));
        executor.setRule(mockRule(currentRuleConfig));
        AlterShardingKeyGenerateStrategyStatement sqlStatement = new AlterShardingKeyGenerateStrategyStatement("order_strategy",
                new SequenceKeyGenerateStrategyDefinitionSegment("order_seq", null, new AlgorithmSegment("UUID", new Properties())));
        ShardingRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(actual.getKeyGenerateStrategies().size(), is(1));
        assertThat(actual.getKeyGenerateStrategies().get("order_strategy").getKeyGeneratorName(), is("order_strategy_uuid"));
        assertThat(((SequenceKeyGenerateStrategiesRuleConfiguration) actual.getKeyGenerateStrategies().get("order_strategy")).getKeyGenerateSequence(), is("order_seq"));
        assertThat(actual.getKeyGenerators().get("order_strategy_uuid").getType(), is("UUID"));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("order_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("old_generator", "t_order", "order_id"));
        currentRuleConfig.getKeyGenerators().put("old_generator", new AlgorithmConfiguration("UUID", new Properties()));
        currentRuleConfig.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        executor.setRule(mockRule(currentRuleConfig));
        AlterShardingKeyGenerateStrategyStatement sqlStatement = new AlterShardingKeyGenerateStrategyStatement("order_strategy",
                new ColumnKeyGenerateStrategyDefinitionSegment("t_order", "order_id", "snowflake", null));
        ShardingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(executor.buildToBeAlteredRuleConfiguration(sqlStatement));
        assertThat(actual.getKeyGenerators().size(), is(1));
        assertTrue(actual.getKeyGenerators().containsKey("old_generator"));
    }
    
    private ShardingRule mockRule(final ShardingRuleConfiguration ruleConfig) {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
}

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

import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropShardingKeyGenerateStrategyExecutorTest {
    
    private final DropShardingKeyGenerateStrategyExecutor executor = new DropShardingKeyGenerateStrategyExecutor();
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckBeforeUpdateWithMissingStrategy() {
        executor.setRule(mockRule(new ShardingRuleConfiguration()));
        DropShardingKeyGenerateStrategyStatement sqlStatement = new DropShardingKeyGenerateStrategyStatement(false, Collections.singleton("order_strategy"));
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("order_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("snowflake", "t_order", "order_id"));
        executor.setRule(mockRule(currentRuleConfig));
        DropShardingKeyGenerateStrategyStatement sqlStatement = new DropShardingKeyGenerateStrategyStatement(false, Collections.singleton("order_strategy"));
        ShardingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertThat(actual.getKeyGenerateStrategies().size(), is(1));
        assertTrue(actual.getKeyGenerateStrategies().containsKey("order_strategy"));
    }
    
    @Test
    void assertHasAnyOneToBeDropped() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerateStrategies().put("order_strategy", new ColumnKeyGenerateStrategiesRuleConfiguration("snowflake", "t_order", "order_id"));
        executor.setRule(mockRule(currentRuleConfig));
        assertTrue(executor.hasAnyOneToBeDropped(new DropShardingKeyGenerateStrategyStatement(false, Collections.singleton("order_strategy"))));
    }
    
    private ShardingRule mockRule(final ShardingRuleConfiguration ruleConfig) {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
}

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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropDefaultShardingStrategyExecutor;
import org.apache.shardingsphere.sharding.distsql.statement.DropDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropDefaultShardingStrategyExecutorTest {
    
    private final DropDefaultShardingStrategyExecutor executor = new DropDefaultShardingStrategyExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedAlgorithm() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement("table")));
    }
    
    @Test
    void assertCheckSQLStatementWithIfExists() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        executor.checkBeforeUpdate(new DropDefaultShardingStrategyStatement(true, "table"));
        executor.setRule(null);
        executor.checkBeforeUpdate(new DropDefaultShardingStrategyStatement(true, "table"));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        ShardingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(createSQLStatement("Database"));
        assertNotNull(actual.getDefaultDatabaseShardingStrategy());
        assertThat(actual.getShardingAlgorithms().size(), is(1));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfigurationWithInUsedAlgorithm() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        ShardingRuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(createSQLStatement("Table"));
        assertNull(toBeDroppedRuleConfig.getDefaultTableShardingStrategy());
        assertTrue(toBeDroppedRuleConfig.getShardingAlgorithms().isEmpty());
    }
    
    @Test
    void assertUpdateMultipleStrategies() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        ShardingRuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(createSQLStatement("Database"));
        assertNull(toBeDroppedRuleConfig.getDefaultTableShardingStrategy());
        toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(createSQLStatement("Table"));
        assertThat(toBeDroppedRuleConfig.getShardingAlgorithms().size(), is(1));
    }
    
    private DropDefaultShardingStrategyStatement createSQLStatement(final String defaultType) {
        return new DropDefaultShardingStrategyStatement(false, defaultType);
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "algorithm_name"));
        result.setShardingAlgorithms(Collections.singletonMap("algorithm_name", new AlgorithmConfiguration("INLINE", new Properties())));
        return result;
    }
}

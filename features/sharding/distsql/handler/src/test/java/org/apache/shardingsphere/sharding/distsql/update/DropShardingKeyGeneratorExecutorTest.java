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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.AlgorithmInUsedException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingKeyGeneratorExecutor;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropShardingKeyGeneratorExecutorTest {
    
    private final DropShardingKeyGeneratorExecutor executor = new DropShardingKeyGeneratorExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertExecuteWithNotExist() {
        DropShardingKeyGeneratorStatement sqlStatement = new DropShardingKeyGeneratorStatement(false, Collections.singleton("uuid_key_generator"));
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredAlgorithmException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertExecuteWithNotExistWithIfExists() {
        DropShardingKeyGeneratorStatement sqlStatement = new DropShardingKeyGeneratorStatement(true, Collections.singleton("uuid_key_generator"));
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    @Test
    void assertDropSpecifiedKeyGenerator() {
        DropShardingKeyGeneratorStatement sqlStatement = new DropShardingKeyGeneratorStatement(false, Collections.singleton("uuid_key_generator"));
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        ShardingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertThat(actual.getKeyGenerators().size(), is(1));
    }
    
    @Test
    void assertExecuteWithUsed() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerators().put("uuid_key_generator", new AlgorithmConfiguration("UUID", null));
        currentRuleConfig.getAutoTables().add(createShardingAutoTableRuleConfiguration());
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        DropShardingKeyGeneratorStatement sqlStatement = new DropShardingKeyGeneratorStatement(false, Collections.singleton("uuid_key_generator"));
        assertThrows(AlgorithmInUsedException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("auto_table", null);
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "uuid_key_generator"));
        return result;
    }
}

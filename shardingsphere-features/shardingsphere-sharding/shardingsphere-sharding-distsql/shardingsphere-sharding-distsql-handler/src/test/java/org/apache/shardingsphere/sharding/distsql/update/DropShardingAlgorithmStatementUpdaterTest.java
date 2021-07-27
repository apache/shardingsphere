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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingAlgorithmStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAlgorithmStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public final class DropShardingAlgorithmStatementUpdaterTest {
    
    private final DropShardingAlgorithmStatementUpdater updater = new DropShardingAlgorithmStatementUpdater();
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", new DropShardingAlgorithmStatement(Collections.emptyList()), null, mock(ShardingSphereResource.class));
    }
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertCheckSQLStatementWithoutExistedAlgorithm() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement("t_order"), new ShardingRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = AlgorithmInUsedException.class)
    public void assertCheckSQLStatementWithBindingTableRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement("t_order_tb_inline"), createCurrentRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        String toBeDroppedAlgorithmName = "t_test";
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(3));
        assertTrue(currentRuleConfig.getShardingAlgorithms().containsKey(toBeDroppedAlgorithmName));
        updater.updateCurrentRuleConfiguration(createSQLStatement(toBeDroppedAlgorithmName), currentRuleConfig);
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(2));
        assertFalse(currentRuleConfig.getShardingAlgorithms().containsKey(toBeDroppedAlgorithmName));
        assertTrue(currentRuleConfig.getShardingAlgorithms().containsKey("t_order_db_inline"));
    }
    
    private DropShardingAlgorithmStatement createSQLStatement(final String algorithmName) {
        return new DropShardingAlgorithmStatement(Collections.singleton(algorithmName));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfiguration = new ShardingTableRuleConfiguration("t_order");
        tableRuleConfiguration.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("column", "t_order_db_inline"));
        tableRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration("column", "t_order_tb_inline"));
        result.getTables().add(tableRuleConfiguration);
        result.getShardingAlgorithms().put("t_order_db_inline", new ShardingSphereAlgorithmConfiguration("standard", new Properties()));
        result.getShardingAlgorithms().put("t_order_tb_inline", new ShardingSphereAlgorithmConfiguration("standard", new Properties()));
        result.getShardingAlgorithms().put("t_test", new ShardingSphereAlgorithmConfiguration("standard", new Properties()));
        return result;
    }
}

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
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingAlgorithmStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAlgorithmStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DropShardingAlgorithmStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final DropShardingAlgorithmStatementUpdater updater = new DropShardingAlgorithmStatementUpdater();
    
    @Test
    void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        assertThrows(MissingRequiredAlgorithmException.class, () -> updater.checkSQLStatement(database, new DropShardingAlgorithmStatement(false, Collections.emptyList()), null));
    }
    
    @Test
    void assertCheckSQLStatementWithoutCurrentRuleWithIfExists() throws RuleDefinitionViolationException {
        DropShardingAlgorithmStatement dropShardingAlgorithmStatement = new DropShardingAlgorithmStatement(true, Collections.emptyList());
        updater.checkSQLStatement(database, dropShardingAlgorithmStatement, null);
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedAlgorithm() throws RuleDefinitionViolationException {
        assertThrows(MissingRequiredAlgorithmException.class, () -> updater.checkSQLStatement(database, createSQLStatement("t_order"), new ShardingRuleConfiguration()));
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedAlgorithmWithIfExists() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(database, createSQLStatementWithIfExists("t_order"), new ShardingRuleConfiguration());
    }
    
    @Test
    void assertCheckSQLStatementWithBindingTableRule() throws RuleDefinitionViolationException {
        assertThrows(AlgorithmInUsedException.class, () -> updater.checkSQLStatement(database, createSQLStatement("t_order_tb_inline"), createCurrentRuleConfiguration()));
    }
    
    @Test
    void assertCheckSQLStatementWithBindingTableRuleWithIfExists() throws RuleDefinitionViolationException {
        assertThrows(AlgorithmInUsedException.class, () -> updater.checkSQLStatement(database, createSQLStatementWithIfExists("t_order_tb_inline"), createCurrentRuleConfiguration()));
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        String toBeDroppedAlgorithmName = "t_test";
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(3));
        assertTrue(currentRuleConfig.getShardingAlgorithms().containsKey(toBeDroppedAlgorithmName));
        updater.updateCurrentRuleConfiguration(createSQLStatement(toBeDroppedAlgorithmName), currentRuleConfig);
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(2));
        assertFalse(currentRuleConfig.getShardingAlgorithms().containsKey(toBeDroppedAlgorithmName));
        assertTrue(currentRuleConfig.getShardingAlgorithms().containsKey("t_order_db_inline"));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithIfExists() {
        String toBeDroppedAlgorithmName = "t_test";
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(3));
        assertTrue(currentRuleConfig.getShardingAlgorithms().containsKey(toBeDroppedAlgorithmName));
        updater.updateCurrentRuleConfiguration(createSQLStatementWithIfExists(toBeDroppedAlgorithmName), currentRuleConfig);
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(2));
        assertFalse(currentRuleConfig.getShardingAlgorithms().containsKey(toBeDroppedAlgorithmName));
        assertTrue(currentRuleConfig.getShardingAlgorithms().containsKey("t_order_db_inline"));
    }
    
    private DropShardingAlgorithmStatement createSQLStatement(final String algorithmName) {
        return new DropShardingAlgorithmStatement(false, Collections.singleton(algorithmName));
    }
    
    private DropShardingAlgorithmStatement createSQLStatementWithIfExists(final String algorithmName) {
        return new DropShardingAlgorithmStatement(true, Collections.singleton(algorithmName));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("t_order", null);
        tableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("column", "t_order_db_inline"));
        tableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("column", "t_order_tb_inline"));
        result.getTables().add(tableRuleConfig);
        result.getShardingAlgorithms().put("t_order_db_inline", new AlgorithmConfiguration("standard", new Properties()));
        result.getShardingAlgorithms().put("t_order_tb_inline", new AlgorithmConfiguration("standard", new Properties()));
        result.getShardingAlgorithms().put("t_test", new AlgorithmConfiguration("standard", new Properties()));
        return result;
    }
}

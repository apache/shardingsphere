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

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropDefaultShardingStrategyStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropDefaultShardingStrategyStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DropDefaultShardingStrategyStatementUpdaterTest {
    
    private final DropDefaultShardingStrategyStatementUpdater updater = new DropDefaultShardingStrategyStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckSQLStatementWithoutCurrentRule() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, new DropDefaultShardingStrategyStatement(false, "TABLE"), null));
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedAlgorithm() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement("table"), new ShardingRuleConfiguration()));
    }
    
    @Test
    void assertCheckSQLStatementWithIfExists() {
        updater.checkSQLStatement(database, new DropDefaultShardingStrategyStatement(true, "table"), new ShardingRuleConfiguration());
        updater.checkSQLStatement(database, new DropDefaultShardingStrategyStatement(true, "table"), null);
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement("Database"), currentRuleConfig);
        assertNull(currentRuleConfig.getDefaultDatabaseShardingStrategy());
        assertTrue(currentRuleConfig.getShardingAlgorithms().isEmpty());
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithInUsedAlgorithm() {
        ShardingRuleConfiguration currentRuleConfig = createMultipleCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement("Table"), currentRuleConfig);
        assertNull(currentRuleConfig.getDefaultTableShardingStrategy());
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(1));
    }
    
    @Test
    void assertUpdateMultipleStrategies() {
        ShardingRuleConfiguration currentRuleConfig = createMultipleCurrentRuleConfiguration();
        assertFalse(updater.updateCurrentRuleConfiguration(createSQLStatement("Database"), currentRuleConfig));
        assertTrue(updater.updateCurrentRuleConfiguration(createSQLStatement("Table"), currentRuleConfig));
        assertNull(currentRuleConfig.getDefaultTableShardingStrategy());
        assertTrue(currentRuleConfig.getShardingAlgorithms().isEmpty());
    }
    
    private DropDefaultShardingStrategyStatement createSQLStatement(final String defaultType) {
        return new DropDefaultShardingStrategyStatement(false, defaultType);
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "algorithm_name"));
        Map<String, AlgorithmConfiguration> stringAlgorithms = new LinkedHashMap<>();
        stringAlgorithms.put("algorithm_name", new AlgorithmConfiguration("INLINE", new Properties()));
        result.setShardingAlgorithms(stringAlgorithms);
        return result;
    }
    
    private ShardingRuleConfiguration createMultipleCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "algorithm_name"));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "algorithm_name"));
        Map<String, AlgorithmConfiguration> stringAlgorithms = new LinkedHashMap<>();
        stringAlgorithms.put("algorithm_name", new AlgorithmConfiguration("INLINE", new Properties()));
        result.setShardingAlgorithms(stringAlgorithms);
        return result;
    }
}

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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.updater;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateTablesException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidShardingAlgorithmsException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public final class AlterShardingTableRuleStatementUpdaterTest {
    
    private final AlterShardingTableRuleStatementUpdater updater = new AlterShardingTableRuleStatementUpdater();
    
    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() {
        updater.checkSQLStatement("foo", createSQLStatement("t_order", "HASH_MOD"), null, mock(ShardingSphereResource.class));
    }
    
    @Test(expected = DuplicateTablesException.class)
    public void assertCheckSQLStatementWithDuplicateTables() {
        updater.checkSQLStatement("foo", createDuplicatedSQLStatement(), createCurrentRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutExistTable() {
        updater.checkSQLStatement("foo", createSQLStatement("invalid_table", "HASH_MOD"), createCurrentRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = InvalidShardingAlgorithmsException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredShardingAlgorithms() {
        updater.checkSQLStatement("foo", createSQLStatement("t_order", "INVALID_TYPE"), createCurrentRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        updater.updateCurrentRuleConfiguration("foo", createSQLStatement("t_order", "HASH_MOD"), createCurrentRuleConfiguration());
        // TODO assert current rule configuration
        
    }
    
    private AlterShardingTableRuleStatement createSQLStatement(final String tableName, final String shardingAlgorithmName) {
        TableRuleSegment ruleSegment = new TableRuleSegment(tableName, Arrays.asList("ds_0", "ds_1"), "order_id", new AlgorithmSegment(shardingAlgorithmName, new Properties()), null, null);
        return new AlterShardingTableRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private AlterShardingTableRuleStatement createDuplicatedSQLStatement() {
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1"), "order_id", new AlgorithmSegment("HASH_MOD", new Properties()), null, null);
        return new AlterShardingTableRuleStatement(Arrays.asList(ruleSegment, ruleSegment));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig = new ShardingAutoTableRuleConfiguration("t_order");
        shardingAutoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "test"));
        result.getAutoTables().add(shardingAutoTableRuleConfig);
        return result;
    }
}

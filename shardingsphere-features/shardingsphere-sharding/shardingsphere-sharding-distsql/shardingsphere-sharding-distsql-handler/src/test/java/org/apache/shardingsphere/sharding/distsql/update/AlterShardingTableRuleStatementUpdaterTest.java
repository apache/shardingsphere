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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShardingTableRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereResource resource;
    
    private final AlterShardingTableRuleStatementUpdater updater = new AlterShardingTableRuleStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getResource()).thenReturn(resource);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("t_order", "STANDARD_TEST"), null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicateTables() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createDuplicatedSQLStatement(), createCurrentRuleConfiguration());
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutExistTable() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("invalid_table", "STANDARD_TEST"), createCurrentRuleConfiguration());
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredShardingAlgorithms() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("t_order", "INVALID_TYPE"), createCurrentRuleConfiguration());
    }
    
    private AlterShardingTableRuleStatement createSQLStatement(final String tableName, final String shardingAlgorithmName) {
        TableRuleSegment ruleSegment = new TableRuleSegment(tableName, Arrays.asList("ds_0", "ds_1"), "order_id", new AlgorithmSegment(shardingAlgorithmName, new Properties()), null, null);
        return new AlterShardingTableRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private AlterShardingTableRuleStatement createDuplicatedSQLStatement() {
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1"), "order_id", new AlgorithmSegment("STANDARD_TEST", new Properties()), null, null);
        return new AlterShardingTableRuleStatement(Arrays.asList(ruleSegment, ruleSegment));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig = new ShardingAutoTableRuleConfiguration("t_order");
        shardingAutoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "STANDARD_TEST"));
        result.getAutoTables().add(shardingAutoTableRuleConfig);
        return result;
    }
}

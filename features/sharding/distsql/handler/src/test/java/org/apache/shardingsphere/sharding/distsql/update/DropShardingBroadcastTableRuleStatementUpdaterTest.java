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

import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingBroadcastTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBroadcastTableRulesStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingBroadcastTableRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final DropShardingBroadcastTableRuleStatementUpdater updater = new DropShardingBroadcastTableRuleStatementUpdater();
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() {
        updater.checkSQLStatement(database, createSQLStatement("t_order"), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutExistBroadcastTableRule() {
        updater.checkSQLStatement(database, createSQLStatement("t_order"), new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertCheckSQLStatementWithIfExists() {
        updater.checkSQLStatement(database, createSQLStatement(true, "t_order"), new ShardingRuleConfiguration());
        updater.checkSQLStatement(database, createSQLStatement(true, "t_order"), null);
    }
    
    @Test
    public void assertHasAnyOneToBeDropped() {
        assertFalse(updater.hasAnyOneToBeDropped(createSQLStatement(true, "t_order"), new ShardingRuleConfiguration()));
        assertFalse(updater.hasAnyOneToBeDropped(createSQLStatement(true, "t_order"), null));
        assertTrue(updater.hasAnyOneToBeDropped(createSQLStatement(true, "t_order"), createCurrentRuleConfiguration()));
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithBroadcastTableRuleAreNotTheSame() {
        updater.checkSQLStatement(database, createSQLStatement("t_order_item"), createCurrentRuleConfiguration());
    }
    
    @Test
    public void assertDropSpecifiedCurrentRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement("t_order"), currentRuleConfig);
        assertTrue(currentRuleConfig.getBroadcastTables().isEmpty());
    }
    
    @Test
    public void assertDropSpecifiedCurrentRuleConfigurationWithDifferentCase() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement("T_ORDER"), currentRuleConfig);
        assertTrue(currentRuleConfig.getBroadcastTables().isEmpty());
    }
    
    @Test
    public void assertAllCurrentRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement(null), currentRuleConfig);
        assertTrue(currentRuleConfig.getBroadcastTables().isEmpty());
    }
    
    private DropShardingBroadcastTableRulesStatement createSQLStatement(final String tableName) {
        return null == tableName ? new DropShardingBroadcastTableRulesStatement(false, Collections.emptyList())
                : new DropShardingBroadcastTableRulesStatement(false, Collections.singleton(tableName));
    }
    
    private DropShardingBroadcastTableRulesStatement createSQLStatement(final boolean ifExists, final String tableName) {
        return null == tableName
                ? new DropShardingBroadcastTableRulesStatement(false, Collections.emptyList())
                : new DropShardingBroadcastTableRulesStatement(ifExists, Collections.singleton(tableName));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order", null));
        result.getBroadcastTables().add("t_order");
        return result;
    }
}

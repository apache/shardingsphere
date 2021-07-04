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

import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingBroadcastTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBroadcastTableRulesStatement;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class DropShardingBroadcastTableRuleStatementUpdaterTest {
    
    private final DropShardingBroadcastTableRuleStatementUpdater updater = new DropShardingBroadcastTableRuleStatementUpdater();
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement(), null, mock(ShardingSphereResource.class));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutExistBroadcastTableRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement(), new ShardingRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration("foo", createSQLStatement(), currentRuleConfig);
        assertTrue(currentRuleConfig.getBroadcastTables().isEmpty());
    }
    
    private DropShardingBroadcastTableRulesStatement createSQLStatement() {
        return new DropShardingBroadcastTableRulesStatement();
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order"));
        result.getBroadcastTables().add("t_order");
        return result;
    }
}

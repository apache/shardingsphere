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

import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.exception.DuplicateBindingTablesException;
import org.apache.shardingsphere.sharding.distsql.handler.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingBindingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBindingTableRulesStatement;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

public final class CreateShardingBindingTableRuleStatementUpdaterTest {
    
    private final CreateShardingBindingTableRuleStatementUpdater updater = new CreateShardingBindingTableRuleStatementUpdater();
    
    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutCurrentTableRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement(), new ShardingRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = DuplicateBindingTablesException.class)
    public void assertCheckSQLStatementWithDuplicateTables() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createDuplicatedSQLStatement(), getCurrentRuleConfig(), mock(ShardingSphereResource.class));
    }
    
    private CreateShardingBindingTableRulesStatement createSQLStatement() {
        return new CreateShardingBindingTableRulesStatement(Arrays.asList(new BindingTableRuleSegment("t_order,t_order_item"), new BindingTableRuleSegment("t_1,t_2")));
    }
    
    private CreateShardingBindingTableRulesStatement createDuplicatedSQLStatement() {
        return new CreateShardingBindingTableRulesStatement(Arrays.asList(new BindingTableRuleSegment("t_order,t_order_item"), new BindingTableRuleSegment("t_order,t_order_item")));
    }
    
    private ShardingRuleConfiguration getCurrentRuleConfig() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_1"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_2"));
        return result;
    }
}

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

import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingBindingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBindingTableRulesStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingBindingTableRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final CreateShardingBindingTableRuleStatementUpdater updater = new CreateShardingBindingTableRuleStatementUpdater();
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentTableRule() {
        updater.checkSQLStatement(database, createSQLStatement(), new ShardingRuleConfiguration());
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicateTables() {
        List<BindingTableRuleSegment> segments = Arrays.asList(new BindingTableRuleSegment("t_order,t_order_item"), new BindingTableRuleSegment("t_order,t_order_item"));
        CreateShardingBindingTableRulesStatement statement = new CreateShardingBindingTableRulesStatement(segments);
        updater.checkSQLStatement(database, statement, getCurrentRuleConfig());
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDifferentCaseDuplicateTables() {
        List<BindingTableRuleSegment> segments = Arrays.asList(new BindingTableRuleSegment("T_ORDER,T_ORDER_ITEM"), new BindingTableRuleSegment("t_order,t_order_item"));
        CreateShardingBindingTableRulesStatement statement = new CreateShardingBindingTableRulesStatement(segments);
        updater.checkSQLStatement(database, statement, getCurrentRuleConfig());
    }
    
    private CreateShardingBindingTableRulesStatement createSQLStatement() {
        return new CreateShardingBindingTableRulesStatement(Arrays.asList(new BindingTableRuleSegment("t_order,t_order_item"), new BindingTableRuleSegment("t_1,t_2")));
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

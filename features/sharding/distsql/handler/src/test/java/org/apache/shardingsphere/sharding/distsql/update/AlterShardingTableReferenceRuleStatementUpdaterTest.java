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
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterShardingTableReferenceRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableReferenceRuleStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShardingTableReferenceRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final AlterShardingTableReferenceRuleStatementUpdater updater = new AlterShardingTableReferenceRuleStatementUpdater();
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(database, createSQLStatement(Collections.singletonList(new TableReferenceRuleSegment("t_order,t_order_item"))), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithNotExistedTables() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(database, createSQLStatement(Collections.singletonList(new TableReferenceRuleSegment("t_3,t_4"))), createCurrentRuleConfiguration());
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithOneTable() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(database, createSQLStatement(Collections.singletonList(new TableReferenceRuleSegment("t_order"))), createCurrentRuleConfiguration());
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicateTables() throws RuleDefinitionViolationException {
        List<TableReferenceRuleSegment> segments = Arrays.asList(new TableReferenceRuleSegment("t_order,t_order_item"), new TableReferenceRuleSegment("t_order,t_order_item"));
        updater.checkSQLStatement(database, createSQLStatement(segments), createCurrentRuleConfiguration());
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDifferentCaseDuplicateTables() throws RuleDefinitionViolationException {
        List<TableReferenceRuleSegment> segments = Arrays.asList(new TableReferenceRuleSegment("T_ORDER,T_ORDER_ITEM"), new TableReferenceRuleSegment("t_order,t_order_item"));
        updater.checkSQLStatement(database, createSQLStatement(segments), createCurrentRuleConfiguration());
    }
    
    private AlterShardingTableReferenceRuleStatement createSQLStatement(final List<TableReferenceRuleSegment> segments) {
        return new AlterShardingTableReferenceRuleStatement(segments);
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_1"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_2"));
        result.getBindingTableGroups().addAll(Collections.singletonList("t_order,t_order_item"));
        return result;
    }
}

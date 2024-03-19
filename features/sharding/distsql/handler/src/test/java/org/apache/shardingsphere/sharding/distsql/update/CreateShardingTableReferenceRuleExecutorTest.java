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

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingTableReferenceRuleExecutor;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateShardingTableReferenceRuleExecutorTest {
    
    private final CreateShardingTableReferenceRuleExecutor executor = new CreateShardingTableReferenceRuleExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckSQLStatementWithoutCurrentTableRule() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement(false, "foo", "t_order,t_order_item")));
    }
    
    private CreateShardingTableReferenceRuleStatement createSQLStatement(final boolean ifNotExists, final String name, final String reference) {
        Collection<TableReferenceRuleSegment> segments = new LinkedList<>();
        segments.add(new TableReferenceRuleSegment(name, reference));
        return new CreateShardingTableReferenceRuleStatement(ifNotExists, segments);
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateTables() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(getCurrentRuleConfig());
        executor.setRule(rule);
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement(false, "foo", "t_order,t_order_item")));
    }
    
    @Test
    void assertUpdateWithIfNotExists() {
        CreateShardingTableReferenceRuleStatement sqlStatement = createSQLStatement(true, "foo", "t_order,t_order_item");
        ShardingRuleConfiguration currentRuleConfig = getCurrentRuleConfig();
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        Collection<ShardingTableReferenceRuleConfiguration> referenceRuleConfigs = actual.getBindingTableGroups();
        assertThat(referenceRuleConfigs.size(), is(0));
    }
    
    private ShardingRuleConfiguration getCurrentRuleConfig() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0..2}"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item", "ds.t_order_item_${0..2}"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_1", "ds.t_1_${0..2}"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_2", "ds.t_2_${0..2}"));
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", "t_1,t_2"));
        return result;
    }
}

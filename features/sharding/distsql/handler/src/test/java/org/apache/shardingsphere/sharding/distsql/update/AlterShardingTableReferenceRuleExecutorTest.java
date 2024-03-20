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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterShardingTableReferenceRuleExecutor;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterShardingTableReferenceRuleExecutorTest {
    
    private final AlterShardingTableReferenceRuleExecutor executor = new AlterShardingTableReferenceRuleExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckBeforeUpdateWithNotExistedRule() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement("notExisted", "t_1,t_2")));
    }
    
    @Test
    void assertCheckWithNotExistedTables() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement("reference_0", "t_3,t_4")));
    }
    
    @Test
    void assertBuildToBeAlteredRuleConfiguration() {
        ShardingRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(createSQLStatement("reference_0", "t_3,t_4"));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getBindingTableGroups().iterator().next().getName(), is("reference_0"));
        assertThat(actual.getBindingTableGroups().iterator().next().getReference(), is("t_3,t_4"));
    }
    
    private AlterShardingTableReferenceRuleStatement createSQLStatement(final String name, final String reference) {
        return new AlterShardingTableReferenceRuleStatement(Collections.singleton(new TableReferenceRuleSegment(name, reference)));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order", null));
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item", null));
        result.getTables().add(new ShardingTableRuleConfiguration("t_1", null));
        result.getTables().add(new ShardingTableRuleConfiguration("t_2", null));
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_0", "t_order,t_order_item"));
        return result;
    }
    
    private ShardingRuleConfiguration createToBeAlteredRuleConfig() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_0", "t_order,t_order_item,t_1,t_2"));
        return result;
    }
}

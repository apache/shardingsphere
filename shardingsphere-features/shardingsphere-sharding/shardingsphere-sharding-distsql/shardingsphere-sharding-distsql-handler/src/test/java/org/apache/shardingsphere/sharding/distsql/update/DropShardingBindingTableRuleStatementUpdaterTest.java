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

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingBindingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBindingTableRulesStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingBindingTableRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final DropShardingBindingTableRuleStatementUpdater updater = new DropShardingBindingTableRuleStatementUpdater();
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutExistedBindingTableRule() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(), new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertCheckSQLStatementWithIfExists() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(true, "t_1,t_2"), null);
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.setBindingTableGroups(Collections.singletonList("t_3,t_4"));
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(true, "t_1,t_2"), shardingRuleConfiguration);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementIsNotTheSame() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("t"), currentRuleConfig);
    }
    
    @Test
    public void assertDropAllCurrentRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement(), currentRuleConfig);
        assertTrue(currentRuleConfig.getBindingTableGroups().isEmpty());
    }
    
    @Test
    public void assertHasAnyOneToBeDropped() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        DropShardingBindingTableRulesStatement sqlStatement = createSQLStatement(true, "t_1,t_2", "t_order,t_order_item");
        assertTrue(updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig));
    }
    
    @Test
    public void assertHasNotAnyOneToBeDropped() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        DropShardingBindingTableRulesStatement sqlStatement = createSQLStatement(true, "t_1,t_2");
        assertFalse(updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig));
    }
    
    @Test
    public void assertDropSpecifiedCurrentRuleConfiguration() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        currentRuleConfig.getBindingTableGroups().add("t_1,t_2");
        DropShardingBindingTableRulesStatement sqlStatement = createSQLStatement("t_1,t_2");
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertThat(currentRuleConfig.getBindingTableGroups().size(), is(1));
        assertTrue(currentRuleConfig.getBindingTableGroups().contains("t_order,t_order_item"));
    }
    
    @Test
    public void assertDropWrongOrderRulesCurrentRuleConfiguration() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        currentRuleConfig.getBindingTableGroups().add("t_1,t_2,t_3");
        DropShardingBindingTableRulesStatement sqlStatement = createSQLStatement("t_3,t_2,t_1");
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertThat(currentRuleConfig.getBindingTableGroups().size(), is(1));
        assertTrue(currentRuleConfig.getBindingTableGroups().contains("t_order,t_order_item"));
        assertFalse(currentRuleConfig.getBindingTableGroups().contains("t_1,t_2,t_3"));
    }
    
    private DropShardingBindingTableRulesStatement createSQLStatement(final String... group) {
        LinkedList<BindingTableRuleSegment> segments = Arrays.stream(group).map(BindingTableRuleSegment::new).collect(Collectors.toCollection(LinkedList::new));
        return new DropShardingBindingTableRulesStatement(segments);
    }
    
    private DropShardingBindingTableRulesStatement createSQLStatement(final boolean containsExistClause, final String... group) {
        LinkedList<BindingTableRuleSegment> segments = Arrays.stream(group).map(BindingTableRuleSegment::new).collect(Collectors.toCollection(LinkedList::new));
        return new DropShardingBindingTableRulesStatement(containsExistClause, segments);
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order"));
        result.getBindingTableGroups().add("t_order,t_order_item");
        return result;
    }
}

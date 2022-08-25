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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingBindingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBindingTableRulesStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingBindingTableRuleStatementUpdaterTest {
    
    private final DropShardingBindingTableRuleStatementUpdater updater = new DropShardingBindingTableRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement(database, createSQLStatement(), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutExistedBindingTableRule() throws DistSQLException {
        updater.checkSQLStatement(database, createSQLStatement(), new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertCheckSQLStatementWithIfExists() throws DistSQLException {
        updater.checkSQLStatement(database, createSQLStatement(true, "t_1,t_2"), null);
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setBindingTableGroups(Collections.singletonList("t_3,t_4"));
        updater.checkSQLStatement(database, createSQLStatement(true, "t_1,t_2"), shardingRuleConfig);
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
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertThat(currentRuleConfig.getBindingTableGroups().size(), is(1));
        assertTrue(currentRuleConfig.getBindingTableGroups().contains("t_order,t_order_item"));
    }
    
    @Test
    public void assertDropRulesCurrentRuleConfigurationWithNoOrder() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        currentRuleConfig.getBindingTableGroups().add("t_1,t_2,t_3");
        DropShardingBindingTableRulesStatement sqlStatement = createSQLStatement("t_3,t_2,t_1");
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertThat(currentRuleConfig.getBindingTableGroups().size(), is(1));
        assertTrue(currentRuleConfig.getBindingTableGroups().contains("t_order,t_order_item"));
        assertFalse(currentRuleConfig.getBindingTableGroups().contains("t_1,t_2,t_3"));
    }
    
    @Test
    public void assertDropRulesCurrentRuleConfigurationWithDifferentCase() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        currentRuleConfig.getBindingTableGroups().add("t_1,t_2,t_3");
        DropShardingBindingTableRulesStatement sqlStatement = createSQLStatement("T_3,T_2,T_1");
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertThat(currentRuleConfig.getBindingTableGroups().size(), is(1));
        assertTrue(currentRuleConfig.getBindingTableGroups().contains("t_order,t_order_item"));
        assertFalse(currentRuleConfig.getBindingTableGroups().contains("t_1,t_2,t_3"));
    }
    
    private DropShardingBindingTableRulesStatement createSQLStatement(final String... group) {
        Collection<BindingTableRuleSegment> segments = Arrays.stream(group).map(BindingTableRuleSegment::new).collect(Collectors.toList());
        return new DropShardingBindingTableRulesStatement(false, segments);
    }
    
    private DropShardingBindingTableRulesStatement createSQLStatement(final boolean ifExists, final String... group) {
        Collection<BindingTableRuleSegment> segments = Arrays.stream(group).map(BindingTableRuleSegment::new).collect(Collectors.toList());
        return new DropShardingBindingTableRulesStatement(ifExists, segments);
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order", null));
        result.getBindingTableGroups().add("t_order,t_order_item");
        return result;
    }
}

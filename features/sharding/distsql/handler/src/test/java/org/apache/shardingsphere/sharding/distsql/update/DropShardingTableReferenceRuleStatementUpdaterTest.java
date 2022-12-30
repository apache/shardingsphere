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
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingTableReferenceRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableReferenceRuleStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingTableReferenceRuleStatementUpdaterTest {
    
    private final DropShardingTableReferenceRuleStatementUpdater updater = new DropShardingTableReferenceRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckWithoutCurrentRule() {
        updater.checkSQLStatement(database, createSQLStatement("notExisted"), null);
    }
    
    private DropShardingTableReferenceRuleStatement createSQLStatement(final String ruleName) {
        return createSQLStatement(false, Collections.singletonList(ruleName));
    }
    
    private DropShardingTableReferenceRuleStatement createSQLStatement(final boolean ifExists, final Collection<String> ruleNames) {
        return new DropShardingTableReferenceRuleStatement(ifExists, ruleNames);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckWithNotExistedShardingTableReferenceRule() {
        updater.checkSQLStatement(database, createSQLStatement("notExisted"), new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertCheckWithIfExists() {
        DropShardingTableReferenceRuleStatement statement = createSQLStatement(true, Collections.singletonList("notExisted"));
        updater.checkSQLStatement(database, statement, null);
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setBindingTableGroups(Collections.singletonList(new ShardingTableReferenceRuleConfiguration("foo", "t_3,t_4")));
        updater.checkSQLStatement(database, statement, shardingRuleConfig);
    }
    
    @Test
    public void assertHasAnyOneToBeDropped() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        DropShardingTableReferenceRuleStatement sqlStatement = createSQLStatement(true, Arrays.asList("reference_0", "reference_1"));
        assertTrue(updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order", null));
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_0", "t_order,t_order_item"));
        return result;
    }
    
    @Test
    public void assertHasNotAnyOneToBeDropped() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        DropShardingTableReferenceRuleStatement sqlStatement = createSQLStatement("foo");
        assertFalse(updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig));
    }
    
    @Test
    public void assertDropSpecifiedReferenceRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        currentRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_1", "t_1,t_2"));
        DropShardingTableReferenceRuleStatement sqlStatement = createSQLStatement("reference_1");
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertThat(currentRuleConfig.getBindingTableGroups().size(), is(1));
        assertThat(currentRuleConfig.getBindingTableGroups().iterator().next().getReference(), is("t_order,t_order_item"));
    }
    
    @Test
    public void assertDropMultipleReferenceRules() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        currentRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_1", "t_1,t_2,t_3"));
        DropShardingTableReferenceRuleStatement sqlStatement = createSQLStatement(false, Arrays.asList("reference_0", "reference_1"));
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertTrue(currentRuleConfig.getBindingTableGroups().isEmpty());
    }
}

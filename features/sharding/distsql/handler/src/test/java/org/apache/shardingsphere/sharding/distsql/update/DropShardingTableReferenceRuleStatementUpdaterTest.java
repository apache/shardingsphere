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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DropShardingTableReferenceRuleStatementUpdaterTest {
    
    private final DropShardingTableReferenceRuleStatementUpdater updater = new DropShardingTableReferenceRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckWithoutCurrentRule() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, new DropShardingTableReferenceRuleStatement(false, Collections.singleton("notExisted")), null));
    }
    
    @Test
    void assertCheckWithNotExistedShardingTableReferenceRule() {
        assertThrows(MissingRequiredRuleException.class,
                () -> updater.checkSQLStatement(database, new DropShardingTableReferenceRuleStatement(false, Collections.singleton("notExisted")), new ShardingRuleConfiguration()));
    }
    
    @Test
    void assertCheckWithIfExists() {
        DropShardingTableReferenceRuleStatement statement = new DropShardingTableReferenceRuleStatement(true, Collections.singleton("notExisted"));
        updater.checkSQLStatement(database, statement, null);
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setBindingTableGroups(Collections.singleton(new ShardingTableReferenceRuleConfiguration("foo", "t_3,t_4")));
        updater.checkSQLStatement(database, statement, shardingRuleConfig);
    }
    
    @Test
    void assertHasAnyOneToBeDropped() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        DropShardingTableReferenceRuleStatement sqlStatement = new DropShardingTableReferenceRuleStatement(true, Arrays.asList("reference_0", "reference_1"));
        assertTrue(updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item", null));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order", null));
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_0", "t_order,t_order_item"));
        return result;
    }
    
    @Test
    void assertHasNotAnyOneToBeDropped() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        DropShardingTableReferenceRuleStatement sqlStatement = new DropShardingTableReferenceRuleStatement(false, Collections.singleton("foo"));
        assertFalse(updater.hasAnyOneToBeDropped(sqlStatement, currentRuleConfig));
    }
    
    @Test
    void assertDropSpecifiedReferenceRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        currentRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_1", "t_1,t_2"));
        DropShardingTableReferenceRuleStatement sqlStatement = new DropShardingTableReferenceRuleStatement(false, Collections.singleton("reference_1"));
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertThat(currentRuleConfig.getBindingTableGroups().size(), is(1));
        assertThat(currentRuleConfig.getBindingTableGroups().iterator().next().getReference(), is("t_order,t_order_item"));
    }
    
    @Test
    void assertDropMultipleReferenceRules() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        currentRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_1", "t_1,t_2,t_3"));
        DropShardingTableReferenceRuleStatement sqlStatement = new DropShardingTableReferenceRuleStatement(false, Arrays.asList("reference_0", "reference_1"));
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertTrue(currentRuleConfig.getBindingTableGroups().isEmpty());
    }
}

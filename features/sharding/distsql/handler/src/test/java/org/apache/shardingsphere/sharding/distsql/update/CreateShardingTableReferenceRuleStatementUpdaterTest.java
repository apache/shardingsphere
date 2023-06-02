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
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingTableReferenceRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableReferenceRuleStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CreateShardingTableReferenceRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final CreateShardingTableReferenceRuleStatementUpdater updater = new CreateShardingTableReferenceRuleStatementUpdater();
    
    @Test
    void assertCheckSQLStatementWithoutCurrentTableRule() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, "foo", "t_order,t_order_item"), new ShardingRuleConfiguration()));
    }
    
    private CreateShardingTableReferenceRuleStatement createSQLStatement(final boolean ifNotExists, final String name, final String reference) {
        Collection<TableReferenceRuleSegment> segments = new LinkedList<>();
        segments.add(new TableReferenceRuleSegment(name, reference));
        return new CreateShardingTableReferenceRuleStatement(ifNotExists, segments);
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateTables() {
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, "foo", "t_order,t_order_item"), getCurrentRuleConfig()));
    }
    
    @Test
    void assertUpdateWithIfNotExists() {
        CreateShardingTableReferenceRuleStatement sqlStatement = createSQLStatement(true, "foo", "t_order,t_order_item");
        ShardingRuleConfiguration currentRuleConfig = getCurrentRuleConfig();
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, sqlStatement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        Collection<ShardingTableReferenceRuleConfiguration> referenceRuleConfigurations = currentRuleConfig.getBindingTableGroups();
        assertThat(referenceRuleConfigurations.size(), is(1));
        Iterator<ShardingTableReferenceRuleConfiguration> iterator = referenceRuleConfigurations.iterator();
        assertThat(iterator.next().getReference(), is("t_1,t_2"));
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

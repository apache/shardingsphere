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

import com.google.common.base.Splitter;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.sharding.distsql.handler.exception.ShardingTableRulesInUsedException;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class DropShardingTableRuleStatementUpdaterTest {
    
    private final DropShardingTableRuleStatementUpdater updater = new DropShardingTableRuleStatementUpdater();
    
    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", new DropShardingTableRuleStatement(Collections.emptyList()), null, mock(ShardingSphereResource.class));
    }
    
    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutExistedTableRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement("t_order"), new ShardingRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = ShardingTableRulesInUsedException.class)
    public void assertCheckSQLStatementWithBindingTableRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement("t_order_item"), createCurrentRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration("foo", createSQLStatement("t_order"), currentRuleConfig);
        assertFalse(getShardingTables(currentRuleConfig).contains("t_order"));
        assertTrue(getBindingTables(currentRuleConfig).contains("t_order_item"));
    }
    
    private DropShardingTableRuleStatement createSQLStatement(final String tableName) {
        return new DropShardingTableRuleStatement(Collections.singleton(new TableNameSegment(0, 3, new IdentifierValue(tableName))));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order"));
        result.setBindingTableGroups(Collections.singleton("t_order_item"));
        return result;
    }
    
    private Collection<String> getShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private Collection<String> getBindingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        currentRuleConfig.getBindingTableGroups().forEach(each -> result.addAll(Splitter.on(",").splitToList(each)));
        return result;
    }
}

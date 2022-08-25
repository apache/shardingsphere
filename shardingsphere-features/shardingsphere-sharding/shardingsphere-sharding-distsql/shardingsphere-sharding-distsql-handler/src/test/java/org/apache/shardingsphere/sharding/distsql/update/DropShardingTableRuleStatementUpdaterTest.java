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
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingTableRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final DropShardingTableRuleStatementUpdater updater = new DropShardingTableRuleStatementUpdater();
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(database, new DropShardingTableRuleStatement(Collections.emptyList()), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutExistedTableRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(database, createSQLStatement("t_order"), new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertCheckSQLStatementWithIfExists() throws RuleDefinitionViolationException {
        DropShardingTableRuleStatement statement = new DropShardingTableRuleStatement(true, Collections.singleton(new TableNameSegment(0, 3, new IdentifierValue("t_order_if_exists"))));
        updater.checkSQLStatement(database, statement, null);
        updater.checkSQLStatement(database, statement, new ShardingRuleConfiguration());
    }
    
    @Test(expected = RuleInUsedException.class)
    public void assertCheckSQLStatementWithBindingTableRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(database, createSQLStatement("t_order_item"), createCurrentRuleConfiguration());
    }
    
    @Test
    public void assertUpdate() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement("t_order"), currentRuleConfig);
        assertFalse(getShardingTables(currentRuleConfig).contains("t_order"));
        assertTrue(getBindingTables(currentRuleConfig).contains("t_order_item"));
    }
    
    @Test
    public void assertUpdateWithDifferentCase() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement("T_ORDER"), currentRuleConfig);
        assertFalse(getShardingTables(currentRuleConfig).contains("t_order"));
        assertTrue(getBindingTables(currentRuleConfig).contains("t_order_item"));
    }
    
    @Test
    public void assertDropRuleAndUnusedAlgorithm() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        DropShardingTableRuleStatement sqlStatement = createSQLStatement("t_order");
        sqlStatement.setDropUnusedAlgorithms(true);
        updater.updateCurrentRuleConfiguration(sqlStatement, currentRuleConfig);
        assertFalse(getShardingTables(currentRuleConfig).contains("t_order"));
        assertTrue(getBindingTables(currentRuleConfig).contains("t_order_item"));
        assertThat(currentRuleConfig.getShardingAlgorithms().size(), is(2));
    }
    
    private DropShardingTableRuleStatement createSQLStatement(final String tableName) {
        return new DropShardingTableRuleStatement(Collections.singleton(new TableNameSegment(0, 3, new IdentifierValue(tableName))));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("t_order_item");
        tableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_item_algorithm"));
        result.getTables().add(tableRuleConfig);
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order", null));
        result.setBindingTableGroups(Collections.singleton("t_order_item"));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "default_table_strategy"));
        result.getShardingAlgorithms().put("unused_algorithm", null);
        result.getShardingAlgorithms().put("t_order_item_algorithm", null);
        result.getShardingAlgorithms().put("default_table_strategy", null);
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

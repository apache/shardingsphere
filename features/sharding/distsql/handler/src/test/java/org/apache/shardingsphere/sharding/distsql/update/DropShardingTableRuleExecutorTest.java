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
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingTableRuleExecutor;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropShardingTableRuleExecutorTest {
    
    private final DropShardingTableRuleExecutor executor = new DropShardingTableRuleExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, Answers.RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedTableRule() throws RuleDefinitionViolationException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement("t_order")));
    }
    
    @Test
    void assertCheckSQLStatementIfExists() throws RuleDefinitionViolationException {
        DropShardingTableRuleStatement statement = new DropShardingTableRuleStatement(true, Collections.singleton(new TableNameSegment(0, 3, new IdentifierValue("t_order_if_exists"))));
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(new ShardingRuleConfiguration());
        executor.setRule(rule);
        executor.checkBeforeUpdate(statement);
    }
    
    @Test
    void assertCheckSQLStatementWithBindingTableRule() throws RuleDefinitionViolationException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(RuleInUsedException.class, () -> executor.checkBeforeUpdate(createSQLStatement("t_order_item")));
    }
    
    @Test
    void assertUpdate() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        ShardingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(createSQLStatement("t_order"));
        assertTrue(getShardingTables(actual).contains("t_order"));
        assertFalse(getBindingTables(actual).contains("t_order_item"));
    }
    
    @Test
    void assertUpdateWithDifferentCase() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        ShardingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(createSQLStatement("T_ORDER"));
        assertTrue(getShardingTables(actual).contains("t_order"));
        assertFalse(getBindingTables(actual).contains("t_order_item"));
    }
    
    @Test
    void assertDropRuleAndUnusedAlgorithm() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        DropShardingTableRuleStatement sqlStatement = createSQLStatement("t_order");
        ShardingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertTrue(getShardingTables(actual).contains("t_order"));
        assertFalse(getBindingTables(actual).contains("t_order_item"));
        assertThat(actual.getShardingAlgorithms().size(), is(2));
        assertThat(actual.getKeyGenerators().size(), is(1));
        assertThat(actual.getAuditors().size(), is(1));
    }
    
    private DropShardingTableRuleStatement createSQLStatement(final String tableName) {
        return new DropShardingTableRuleStatement(false, Collections.singleton(new TableNameSegment(0, 3, new IdentifierValue(tableName))));
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("t_order_item", null);
        tableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_item_algorithm"));
        tableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "in_used_key_generator"));
        tableRuleConfig.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("in_used_auditor"), false));
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(tableRuleConfig);
        ShardingAutoTableRuleConfiguration autoTableRuleConfig = new ShardingAutoTableRuleConfiguration("t_order", null);
        autoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_algorithm"));
        result.getAutoTables().add(autoTableRuleConfig);
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_0", "t_order_item"));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "default_table_strategy"));
        result.getShardingAlgorithms().put("unused_algorithm", null);
        result.getShardingAlgorithms().put("t_order_item_algorithm", null);
        result.getShardingAlgorithms().put("t_order_algorithm", null);
        result.getShardingAlgorithms().put("default_table_strategy", null);
        result.getKeyGenerators().put("in_used_key_generator", null);
        result.getKeyGenerators().put("unused_key_generator", null);
        result.getAuditors().put("in_used_auditor", null);
        result.getAuditors().put("unused_auditor", null);
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
        currentRuleConfig.getBindingTableGroups().forEach(each -> result.addAll(Splitter.on(",").splitToList(each.getReference())));
        return result;
    }
}

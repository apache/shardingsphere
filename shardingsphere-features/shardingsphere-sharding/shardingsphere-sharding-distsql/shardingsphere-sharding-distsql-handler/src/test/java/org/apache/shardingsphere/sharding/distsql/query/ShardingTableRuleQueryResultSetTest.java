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

package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingTableRuleQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingTableRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        DatabaseDistSQLResultSet resultSet = new ShardingTableRuleQueryResultSet();
        resultSet.init(mockDatabase(), mock(ShowShardingTableRulesStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(14));
        assertThat(actual.get(0), is("t_order"));
        assertThat(actual.get(1), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(actual.get(2), is(""));
        assertThat(actual.get(3), is("STANDARD"));
        assertThat(actual.get(4), is("user_id"));
        assertThat(actual.get(5), is("INLINE"));
        assertThat(actual.get(6), is("algorithm-expression=ds_${user_id % 2}"));
        assertThat(actual.get(7), is("STANDARD"));
        assertThat(actual.get(8), is("order_id"));
        assertThat(actual.get(9), is("INLINE"));
        assertThat(actual.get(10), is("algorithm-expression=t_order_${order_id % 2}"));
        assertThat(actual.get(11), is("order_id"));
        assertThat(actual.get(12), is("SNOWFLAKE"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(result.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createShardingTableRuleConfiguration());
        result.getBindingTableGroups().add("t_order,t_order_item");
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "database_inline"));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.getShardingAlgorithms().put("database_inline", createShardingInlineAlgorithmConfiguration("ds_${user_id % 2}"));
        result.getShardingAlgorithms().put("t_order_inline", createShardingInlineAlgorithmConfiguration("t_order_${order_id % 2}"));
        result.getKeyGenerators().put("snowflake", createKeyGeneratorConfiguration());
        return result;
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_inline"));
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return result;
    }
    
    private AlgorithmConfiguration createShardingInlineAlgorithmConfiguration(final String algorithmExpression) {
        Properties props = new Properties();
        props.put("algorithm-expression", algorithmExpression);
        return new AlgorithmConfiguration("INLINE", props);
    }
    
    private AlgorithmConfiguration createKeyGeneratorConfiguration() {
        return new AlgorithmConfiguration("SNOWFLAKE", new Properties());
    }
}

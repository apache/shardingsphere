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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.DefaultShardingStrategyQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAlgorithmsStatement;
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

public final class ShardingDefaultShardingStrategyQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule1 = mock(ShardingRule.class);
        when(rule1.getConfiguration()).thenReturn(createRuleConfiguration1());
        when(database.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.of(rule1));
        DefaultShardingStrategyQueryResultSet resultSet = new DefaultShardingStrategyQueryResultSet();
        resultSet.init(database, mock(ShowShardingAlgorithmsStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(6));
        assertThat(actual.get(0), is("TABLE"));
        assertThat(actual.get(1), is("NONE"));
        assertThat(actual.get(2), is(""));
        assertThat(actual.get(3), is(""));
        assertThat(actual.get(4), is(""));
        assertThat(actual.get(5), is(""));
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(6));
        assertThat(actual.get(0), is("DATABASE"));
        assertThat(actual.get(1), is("COMPLEX"));
        assertThat(actual.get(2), is("use_id, order_id"));
        assertThat(actual.get(3), is("database_inline"));
        assertThat(actual.get(4), is("INLINE"));
        assertThat(actual.get(5).toString(), is("{algorithm-expression=ds_${user_id % 2}}"));
        ShardingRule rule2 = mock(ShardingRule.class);
        when(rule2.getConfiguration()).thenReturn(createRuleConfiguration2());
        when(database.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.of(rule2));
        resultSet = new DefaultShardingStrategyQueryResultSet();
        resultSet.init(database, mock(ShowShardingAlgorithmsStatement.class));
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(6));
        assertThat(actual.get(0), is("TABLE"));
        assertThat(actual.get(1), is("STANDARD"));
        assertThat(actual.get(2), is("use_id"));
        assertThat(actual.get(3), is("database_inline"));
        assertThat(actual.get(4), is("INLINE"));
        assertThat(actual.get(5).toString(), is("{algorithm-expression=ds_${user_id % 2}}"));
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(6));
        assertThat(actual.get(0), is("DATABASE"));
        assertThat(actual.get(1), is("HINT"));
        assertThat(actual.get(2), is(""));
        assertThat(actual.get(3), is("database_inline"));
        assertThat(actual.get(4), is("INLINE"));
        assertThat(actual.get(5).toString(), is("{algorithm-expression=ds_${user_id % 2}}"));
    }
    
    private RuleConfiguration createRuleConfiguration1() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Properties props = new Properties();
        props.put("algorithm-expression", "ds_${user_id % 2}");
        result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", props));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.setDefaultDatabaseShardingStrategy(new ComplexShardingStrategyConfiguration("use_id, order_id", "database_inline"));
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration2() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Properties props = new Properties();
        props.put("algorithm-expression", "ds_${user_id % 2}");
        result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", props));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("use_id", "database_inline"));
        result.setDefaultDatabaseShardingStrategy(new HintShardingStrategyConfiguration("database_inline"));
        return result;
    }
}

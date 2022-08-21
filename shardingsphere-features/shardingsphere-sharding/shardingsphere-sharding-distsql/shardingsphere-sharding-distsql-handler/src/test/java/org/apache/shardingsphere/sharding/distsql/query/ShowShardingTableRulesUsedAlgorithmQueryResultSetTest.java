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
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingTableRulesUsedAlgorithmQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAlgorithmStatement;
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

public final class ShowShardingTableRulesUsedAlgorithmQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(database.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.of(rule));
        DatabaseDistSQLResultSet resultSet = new ShardingTableRulesUsedAlgorithmQueryResultSet();
        ShowShardingTableRulesUsedAlgorithmStatement statement = mock(ShowShardingTableRulesUsedAlgorithmStatement.class);
        when(statement.getShardingAlgorithmName()).thenReturn(Optional.of("t_order_inline"));
        resultSet.init(database, statement);
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("table"));
        assertThat(actual.get(1), is("t_order"));
        when(statement.getShardingAlgorithmName()).thenReturn(Optional.of("auto_mod"));
        resultSet.init(database, statement);
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("auto_table"));
        assertThat(actual.get(1), is("t_order_auto"));
    }
    
    private ShardingRuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createShardingTableRuleConfiguration());
        result.getAutoTables().add(createShardingAutoTableRuleConfiguration());
        result.getBindingTableGroups().add("t_order,t_order_item");
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "database_inline"));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.getShardingAlgorithms().put("database_inline", createShardingInlineAlgorithmConfiguration("ds_${user_id % 2}"));
        result.getShardingAlgorithms().put("t_order_inline", createShardingInlineAlgorithmConfiguration("t_order_${order_id % 2}"));
        result.getShardingAlgorithms().put("auto_mod", createShardingAutoModAlgorithmConfiguration());
        result.getKeyGenerators().put("snowflake", createKeyGeneratorConfiguration());
        return result;
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("t_order_auto", "ds_0, ds_1");
        result.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "auto_mod"));
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
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
    
    private AlgorithmConfiguration createShardingAutoModAlgorithmConfiguration() {
        Properties props = new Properties();
        props.put("sharding-count", 4);
        return new AlgorithmConfiguration("MOD", props);
    }
    
    private AlgorithmConfiguration createKeyGeneratorConfiguration() {
        return new AlgorithmConfiguration("SNOWFLAKE", new Properties());
    }
}

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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingTableRulesUsedAlgorithmQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAlgorithmStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
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
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().findRuleConfiguration(ShardingRuleConfiguration.class)).thenReturn(Collections.singleton(createRuleConfiguration()));
        when(metaData.getName()).thenReturn("sharding_db");
        DistSQLResultSet resultSet = new ShardingTableRulesUsedAlgorithmQueryResultSet();
        ShowShardingTableRulesUsedAlgorithmStatement statement = mock(ShowShardingTableRulesUsedAlgorithmStatement.class);
        when(statement.getAlgorithmName()).thenReturn(Optional.of("t_order_inline"));
        resultSet.init(metaData, statement);
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("sharding_db"));
        assertThat(actual.get(1), is("table"));
        assertThat(actual.get(2), is("t_order"));
        when(statement.getAlgorithmName()).thenReturn(Optional.of("auto_mod"));
        resultSet.init(metaData, statement);
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("sharding_db"));
        assertThat(actual.get(1), is("auto_table"));
        assertThat(actual.get(2), is("t_order_auto"));
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

    private ShardingSphereAlgorithmConfiguration createShardingInlineAlgorithmConfiguration(final String algorithmExpression) {
        Properties props = new Properties();
        props.put("algorithm-expression", algorithmExpression);
        return new ShardingSphereAlgorithmConfiguration("INLINE", props);
    }

    private ShardingSphereAlgorithmConfiguration createShardingAutoModAlgorithmConfiguration() {
        Properties props = new Properties();
        props.put("sharding-count", 4);
        return new ShardingSphereAlgorithmConfiguration("MOD", props);
    }

    private ShardingSphereAlgorithmConfiguration createKeyGeneratorConfiguration() {
        Properties props = new Properties();
        props.put("worker-id", "123");
        return new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", props);
    }
}

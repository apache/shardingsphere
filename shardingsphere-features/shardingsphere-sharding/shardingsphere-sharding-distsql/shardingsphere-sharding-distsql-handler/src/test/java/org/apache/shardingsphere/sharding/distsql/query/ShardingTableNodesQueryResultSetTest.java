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
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingTableNodesQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
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

public final class ShardingTableNodesQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        DatabaseDistSQLResultSet resultSet = new ShardingTableNodesQueryResultSet();
        resultSet.init(mockDatabase(), mock(ShowShardingTableNodesStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("t_order"));
        assertThat(actual.get(1), is("ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1"));
        resultSet.next();
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.get(0), is("t_product"));
        assertThat(actual.get(1), is("ds_2.t_product_0, ds_3.t_product_1"));
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.get(0), is("t_user"));
        assertThat(actual.get(1), is("ds_2.t_user_0, ds_3.t_user_1, ds_2.t_user_2, ds_3.t_user_3"));
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
        result.getAutoTables().add(createProductAutoTableConfiguration());
        result.getAutoTables().add(createUserAutoTableConfiguration());
        result.getShardingAlgorithms().put("t_product_algorithm", new AlgorithmConfiguration("FOO.DISTSQL.FIXTURE", newProperties("sharding-count", 2)));
        result.getShardingAlgorithms().put("t_user_algorithm", new AlgorithmConfiguration("BAR.DISTSQL.FIXTURE", newProperties("sharding-ranges", "10,20,30")));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "t_product_algorithm"));
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "t_product_algorithm"));
        return result;
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration() {
        return new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
    }
    
    private ShardingAutoTableRuleConfiguration createProductAutoTableConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("t_product", "ds_2,ds_3");
        result.setShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "t_product_algorithm"));
        return result;
    }
    
    private ShardingAutoTableRuleConfiguration createUserAutoTableConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("t_user", "ds_2,ds_3");
        result.setShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "t_user_algorithm"));
        return result;
    }
    
    private Properties newProperties(final String key, final Object value) {
        Properties result = new Properties();
        result.put(key, value);
        return result;
    }
}
